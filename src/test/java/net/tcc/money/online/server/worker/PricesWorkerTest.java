package net.tcc.money.online.server.worker;

import net.tcc.gae.ServerTools.PersistenceTemplate;
import net.tcc.money.online.server.ServerSideTest;
import net.tcc.money.online.server.domain.*;
import org.junit.Test;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.google.appengine.repackaged.com.google.common.collect.Iterables.getOnlyElement;
import static java.lang.System.currentTimeMillis;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static net.tcc.gae.ServerTools.executeWithoutTransaction;
import static net.tcc.gae.ServerTools.startTransaction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("ConstantConditions")
public final class PricesWorkerTest extends ServerSideTest {

    public static final Date YESTERDAY = new Date(currentTimeMillis() - 1000 * 60 * 60 * 24);
    public static final Date TOMORROW = new Date(currentTimeMillis() + 1000 * 60 * 60 * 24);
    private final PricesWorker objectUnderTest = new PricesWorker();

    public PricesWorkerTest() {
        super(false);
    }

    @Test
    public final void shouldStorePriceForPurchase() {
        final Long purchaseId = givenAPurchase(TEN).getKey();

        objectUnderTest.calculateAndStorePricesForPurchase(purchaseId);

        List<PersistentPrice> prices = fetchPricesOfShopRelatedToPurchase(purchaseId);

        assertThat(prices, hasSize(1));
    }

    @Test
    public final void shouldOverrideExistingPriceForNewPurchase() {
        final PersistentPurchase purchase = givenAPurchase(TEN);
        givenAnExistingPriceSince(purchase, ONE, YESTERDAY);

        objectUnderTest.calculateAndStorePricesForPurchase(purchase.getKey());

        List<PersistentPrice> prices = fetchPricesOfShopRelatedToPurchase(purchase.getKey());
        assertThat(prices, hasSize(1));
        assertThat(getOnlyElement(prices).getPrice(), is(TEN));
    }

    @Test
    public final void shouldKeepExistingPriceForOlderPurchase() {
        final PersistentPurchase purchase = givenAPurchase(TEN);
        givenAnExistingPriceSince(purchase, ONE, TOMORROW);

        objectUnderTest.calculateAndStorePricesForPurchase(purchase.getKey());

        List<PersistentPrice> prices = fetchPricesOfShopRelatedToPurchase(purchase.getKey());
        assertThat(prices, hasSize(1));
        assertThat(getOnlyElement(prices).getPrice(), is(ONE));
    }

    @Test
    public final void shouldKeepExistingPriceForSamePrice() {
        final PersistentPurchase purchase = givenAPurchase(TEN);
        givenAnExistingPriceSince(purchase, TEN, YESTERDAY);

        objectUnderTest.calculateAndStorePricesForPurchase(purchase.getKey());

        List<PersistentPrice> prices = fetchPricesOfShopRelatedToPurchase(purchase.getKey());
        assertThat(prices, hasSize(1));
        assertThat(getOnlyElement(prices).getPrice(), is(TEN));
        assertThat(getOnlyElement(prices).getSince(), is(YESTERDAY));
    }

    @Test
    public final void shouldDeleteDuplicatePrice() {
        Date twoDaysAgo = new Date(currentTimeMillis() - 1000 * 60 * 60 * 24 * 2);
        Date threeDaysAgo = new Date(currentTimeMillis() - 1000 * 60 * 60 * 24 * 3);
        final PersistentPurchase purchase = givenAPurchase(TEN);
        givenAnExistingPriceSince(purchase, TEN, YESTERDAY);
        givenAnExistingPriceSince(purchase, ONE, twoDaysAgo);
        givenAnExistingPriceSince(purchase, TEN, threeDaysAgo);

        objectUnderTest.calculateAndStorePricesForPurchase(purchase.getKey());

        List<PersistentPrice> prices = fetchPricesOfShopRelatedToPurchase(purchase.getKey());
        assertThat(prices, hasSize(1));
        assertThat(getOnlyElement(prices).getPrice(), is(TEN));
        assertThat(getOnlyElement(prices).getSince(), is(YESTERDAY));
    }

    private void givenAnExistingPriceSince(final PersistentPurchase purchase, final BigDecimal price, final Date since) {
        executeWithoutTransaction(new PersistenceTemplate<Object>() {
            @Override
            public Object doWithPersistenceManager(PersistenceManager persistenceManager) {
                PersistentShop shop = persistenceManager.getObjectById(PersistentShop.class, purchase.getShop().getKey());
                PersistentArticle article = persistenceManager.getObjectById(PersistentArticle.class, getOnlyElement(purchase).getArticle().getKey());
                Transaction tx = startTransaction(persistenceManager);
                PersistentPrice pPrice = persistenceManager.makePersistent(new PersistentPrice(shop, article, since, price));
                tx.commit();
                return pPrice;
            }
        });
    }

    private List<PersistentPrice> fetchPricesOfShopRelatedToPurchase(final Long purchaseId) {
        return executeWithoutTransaction(new PersistenceTemplate<List<PersistentPrice>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<PersistentPrice> doWithPersistenceManager(PersistenceManager persistenceManager) {
                PersistentPurchase purchase = persistenceManager.getObjectById(PersistentPurchase.class, purchaseId);
                Query query = persistenceManager.newQuery(PersistentPrice.class, "shop == pShop");
                query.declareParameters("net.tcc.money.online.server.domain.PersistentShop pShop");

                return (List<PersistentPrice>) query.execute(purchase.getShop());
            }
        });
    }

    private PersistentPurchase givenAPurchase(final BigDecimal price) {
        return executeWithoutTransaction(new PersistenceTemplate<PersistentPurchase>() {
            @Override
            public PersistentPurchase doWithPersistenceManager(PersistenceManager persistenceManager) {
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
                PersistentPurchase purchase = new PersistentPurchase(dataSetId, shop, new Date(), asList(new PersistentPurchasing(article, ONE, price, null)));
                purchase = persistenceManager.makePersistent(purchase);
                tx.commit();

                return purchase;
            }
        });
    }

}
