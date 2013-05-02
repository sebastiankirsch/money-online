package net.tcc.money.online.server;

import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import net.tcc.gae.ServerTools;
import net.tcc.money.online.server.domain.PersistentShop;
import net.tcc.money.online.shared.dto.*;
import org.junit.Test;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import java.util.Date;

import static com.google.appengine.repackaged.com.google.common.collect.Iterables.getOnlyElement;
import static java.math.BigDecimal.ONE;
import static net.tcc.gae.ServerTools.startTransaction;
import static net.tcc.money.online.server.worker.PricesWorker.PURCHASE_ID;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShoppingServiceImplTest extends ServerSideTest {

    private final ShoppingServiceImpl objectUnderTest = new ShoppingServiceImpl();

    public ShoppingServiceImplTest() {
        super(true);
    }

    @Test
    public final void canCreateCategory() {
        String name = "ACME";
        Category category = objectUnderTest.createCategory(name, null);

        assertThat("Key is NOT set!", category.getKey(), is(notNullValue()));
        assertThat("Name should be stored!", category.getName(), is(name));
        assertThat("A parent is set!", category.getParent(), is(nullValue()));
    }

    @Test
    public final void addsTaskToPricesQueueIfPurchaseIsCreated() {
        Shop shop = ServerTools.executeWithoutTransaction(new ServerTools.PersistenceTemplate<Shop>() {
            @Override
            public Shop doWithPersistenceManager(PersistenceManager persistenceManager) {
                Transaction tx = startTransaction(persistenceManager);
                PersistentShop shop = new PersistentShop(DATA_SET_ID, "Shop");
                shop = persistenceManager.makePersistent(shop);
                tx.commit();
                return shop.toShop();
            }
        });

        Purchase purchase = new Purchase(shop, new Date());
        purchase.add(new Purchasing(new Article("Article", "ACME", false, null), null, ONE, null));
        objectUnderTest.createPurchase(purchase);

        QueueStateInfo queueStateInfo = LocalTaskQueueTestConfig.getLocalTaskQueue().getQueueStateInfo().get("prices");
        assertThat(queueStateInfo.getCountTasks(), is(1));
        String body = getOnlyElement(queueStateInfo.getTaskInfo()).getBody();
        assertThat(body, startsWith(PURCHASE_ID));
        assertThat(Long.valueOf(body.substring((PURCHASE_ID + "=").length())), is(notNullValue()));
    }

}
