package net.tcc.money.online.server;

import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import net.tcc.money.online.server.domain.PersistentArticle;
import net.tcc.money.online.server.domain.PersistentCategory;
import net.tcc.money.online.server.domain.PersistentShop;
import net.tcc.money.online.shared.dto.*;
import org.junit.Test;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import java.util.Date;

import static com.google.appengine.repackaged.com.google.common.collect.Iterables.getOnlyElement;
import static java.math.BigDecimal.ONE;
import static net.tcc.gae.ServerTools.*;
import static net.tcc.money.online.server.worker.PricesWorker.PURCHASE_ID;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShoppingServiceImplTest extends ServerSideTest {

    private final ShoppingServiceImpl objectUnderTest = new ShoppingServiceImpl();

    public ShoppingServiceImplTest() {
        super(true);
    }

    @Test
    public void canCreateCategory() {
        String name = "ACME";
        Category category = objectUnderTest.createCategory(name, null);

        assertThat("Key is NOT set!", category.getKey(), is(notNullValue()));
        assertThat("Name should be stored!", category.getName(), is(name));
        assertThat("A parent is set!", category.getParent(), is(nullValue()));
    }

    @Test
    public void addsTaskToPricesQueueIfPurchaseIsCreated() {
        Shop shop = givenAShop();

        Purchase purchase = new Purchase(shop, new Date());
        purchase.add(new Purchasing(new Article("Article", "ACME", false, null), null, ONE, null));
        objectUnderTest.createPurchase(purchase);

        QueueStateInfo queueStateInfo = LocalTaskQueueTestConfig.getLocalTaskQueue().getQueueStateInfo().get("prices");
        assertThat(queueStateInfo.getCountTasks(), is(1));
        String body = getOnlyElement(queueStateInfo.getTaskInfo()).getBody();
        assertThat(body, startsWith(PURCHASE_ID));
        assertThat(Long.valueOf(body.substring((PURCHASE_ID + "=").length())), is(notNullValue()));
    }

    @Test
    public void createdArticleIsAssignedToSelectedCategory() {
        Shop shop = givenAShop();
        Category category = givenACategory();
        Purchase purchase = new Purchase(shop, new Date());
        purchase.add(new Purchasing(new Article("Article", null, false, null), ONE, ONE, category));

        objectUnderTest.createPurchase(purchase);

        PersistentArticle persistentArticle = executeWithoutTransaction(new PersistenceTemplate<PersistentArticle>() {
            @Override
            public PersistentArticle doWithPersistenceManager(PersistenceManager persistenceManager) {
                String fetchGroup = "fetchGroup";
                persistenceManager.getFetchGroup(PersistentArticle.class, fetchGroup).addMember("category");
                persistenceManager.getFetchPlan().addGroup(fetchGroup);
                Extent<PersistentArticle> articles = persistenceManager.getExtent(PersistentArticle.class);
                return getOnlyElement(articles);
            }
        });

        assertThat(persistentArticle, is(notNullValue()));
        PersistentCategory persistentCategory = persistentArticle.getCategory();
        assertThat("Article is not assigned to any Category!", persistentCategory, is(notNullValue()));
        //noinspection ConstantConditions
        assertThat("Article is assigned to wrong Category!", persistentCategory.getKey(), is(equalTo(category.getKey())));
    }

    private Shop givenAShop() {
        Shop shop = executeWithTransaction(new PersistenceTemplate<Shop>() {
            @Override
            public Shop doWithPersistenceManager(PersistenceManager persistenceManager) {
                PersistentShop shop = new PersistentShop(DATA_SET_ID, "Shop");
                shop = persistenceManager.makePersistent(shop);
                persistenceManager.flush();
                return shop.toShop();
            }
        });
        assert "Shop".equals(shop.getName());
        return shop;
    }

    private Category givenACategory() {
        Category category = executeWithTransaction(new PersistenceTemplate<Category>() {
            @Override
            public Category doWithPersistenceManager(PersistenceManager persistenceManager) {
                PersistentCategory category = new PersistentCategory(DATA_SET_ID, "Category", null);
                category = persistenceManager.makePersistent(category);
                persistenceManager.flush();
                return category.toCategory();
            }
        });
        assert "Category".equals(category.getName());
        return category;
    }

}
