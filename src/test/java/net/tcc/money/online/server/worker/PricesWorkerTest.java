package net.tcc.money.online.server.worker;

import net.tcc.gae.ServerTools.PersistenceTemplate;
import net.tcc.money.online.server.ServerSideTest;
import net.tcc.money.online.server.domain.*;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static net.tcc.gae.ServerTools.executeWithoutTransaction;
import static net.tcc.gae.ServerTools.startTransaction;
import static org.hamcrest.Matchers.hasSize;

public final class PricesWorkerTest extends ServerSideTest {

    private final PricesWorker objectUnderTest = new PricesWorker();

    public PricesWorkerTest() {
        super(false);
    }

    @Test
    public final void shouldStorePricesForPurchase() {
        final Long purchaseId = executeWithoutTransaction(new PersistenceTemplate<Long>() {
            @Override
            public Long doWithPersistenceManager(PersistenceManager persistenceManager) {
                String dataSetId = "JUnit";
                Transaction tx = startTransaction(persistenceManager);
                PersistentShop shop = new PersistentShop(dataSetId, "Shop");
                persistenceManager.makePersistent(shop);
                tx.commit();
                tx = startTransaction(persistenceManager);
                PersistentArticle article = new PersistentArticle(dataSetId, "Article", null, false, null);
                persistenceManager.makePersistent(article);
                tx.commit();
                tx = startTransaction(persistenceManager);
                PersistentPurchase purchase = new PersistentPurchase(dataSetId, shop, new Date(), asList(new PersistentPurchasing(article, BigDecimal.ONE, BigDecimal.TEN, null)));
                persistenceManager.makePersistent(purchase);
                tx.commit();

                return purchase.getKey();
            }
        });

        objectUnderTest.calculateAndStorePricesForPurchase(purchaseId);

        List<PersistentPrice> prices = executeWithoutTransaction(new PersistenceTemplate<List<PersistentPrice>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<PersistentPrice> doWithPersistenceManager(PersistenceManager persistenceManager) {
                PersistentPurchase purchase = persistenceManager.getObjectById(PersistentPurchase.class, purchaseId);
                Query query = persistenceManager.newQuery(PersistentPrice.class, "shop == pShop");
                query.declareParameters("net.tcc.money.online.server.domain.PersistentShop pShop");

                return (List<PersistentPrice>) query.execute(purchase.getShop());
            }
        });

        MatcherAssert.assertThat(prices, hasSize(1));
    }

}
