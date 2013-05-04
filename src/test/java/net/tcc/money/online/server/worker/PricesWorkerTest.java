package net.tcc.money.online.server.worker;

import com.google.appengine.api.datastore.KeyFactory;
import net.tcc.gae.ServerTools.PersistenceTemplate;
import net.tcc.money.online.server.ServerSideTest;
import net.tcc.money.online.server.domain.*;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import java.math.BigDecimal;
import java.util.Date;

import static com.google.appengine.repackaged.com.google.common.collect.Iterables.getOnlyElement;
import static java.lang.System.currentTimeMillis;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static net.tcc.gae.ServerTools.executeWithoutTransaction;
import static net.tcc.gae.ServerTools.startTransaction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings({"ConstantConditions", "unchecked"})
public final class PricesWorkerTest extends ServerSideTest {

    public static final Date YESTERDAY = new Date(currentTimeMillis() - 1000 * 60 * 60 * 24);
    public static final Date TOMORROW = new Date(currentTimeMillis() + 1000 * 60 * 60 * 24);
    public static final Date TODAY = new Date();
    private final PricesWorker objectUnderTest = new PricesWorker();

    private static Matcher<? super PersistentPrice> since(final Date since) {
        return new TypeSafeMatcher<PersistentPrice>() {
            @Override
            protected boolean matchesSafely(PersistentPrice item) {
                return since.equals(item.getSince());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a PersistentPrice in place since [" + since + "]");
            }

            @Override
            public void describeMismatchSafely(PersistentPrice item, Description mismatchDescription) {
                mismatchDescription.appendText("since was ").appendValue(item.getSince());
            }

        };
    }

    private static Matcher<? super PersistentPrice> withPrice(final BigDecimal price) {
        return new TypeSafeMatcher<PersistentPrice>() {
            @Override
            protected boolean matchesSafely(PersistentPrice item) {
                return price.equals(item.getPrice());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a PersistentPrice with price [" + price + "]");
            }

            @Override
            public void describeMismatchSafely(PersistentPrice item, Description mismatchDescription) {
                mismatchDescription.appendText("price was ").appendValue(item.getPrice());
            }

        };
    }

    public PricesWorkerTest() {
        super(false);
    }

    @Test
    public final void shouldStorePriceForPurchase() {
        final Long purchaseId = givenAPurchase(TEN).getKey();

        objectUnderTest.calculateAndStorePricesForPurchase(purchaseId);

        Iterable<PersistentPrice> prices = fetchPricesOfShopRelatedToPurchase(purchaseId);
        assertThat(prices, Matchers.<PersistentPrice>iterableWithSize(1));
    }

    @Test
    public final void shouldOverrideExistingPriceForNewPurchase() {
        final PersistentPurchase purchase = givenAPurchase(TEN);
        givenAnExistingPriceSince(purchase, ONE, YESTERDAY);

        objectUnderTest.calculateAndStorePricesForPurchase(purchase.getKey());

        Iterable<PersistentPrice> prices = fetchPricesOfShopRelatedToPurchase(purchase.getKey());
        assertThat(prices, Matchers.<PersistentPrice>iterableWithSize(1));
        assertThat(prices, hasItem(both(withPrice(TEN)).and(since(TODAY))));
    }

    @Test
    public final void shouldKeepExistingPriceForOlderPurchase() {
        final PersistentPurchase purchase = givenAPurchase(TEN);
        givenAnExistingPriceSince(purchase, ONE, TOMORROW);

        objectUnderTest.calculateAndStorePricesForPurchase(purchase.getKey());

        Iterable<PersistentPrice> prices = fetchPricesOfShopRelatedToPurchase(purchase.getKey());
        assertThat(prices, Matchers.<PersistentPrice>iterableWithSize(1));
        assertThat(prices, hasItem(both(withPrice(ONE)).and(since(TOMORROW))));
    }

    @Test
    public final void shouldKeepExistingPriceForSamePrice() {
        final PersistentPurchase purchase = givenAPurchase(TEN);
        givenAnExistingPriceSince(purchase, TEN, YESTERDAY);

        objectUnderTest.calculateAndStorePricesForPurchase(purchase.getKey());

        Iterable<PersistentPrice> prices = fetchPricesOfShopRelatedToPurchase(purchase.getKey());
        assertThat(prices, Matchers.<PersistentPrice>iterableWithSize(1));
        assertThat(prices, hasItem(both(withPrice(TEN)).and(since(YESTERDAY))));
    }

    @Test
    public final void shouldKeepExistingPriceForOtherArticle() {
        final PersistentPurchase purchase = givenAPurchase(TEN);
        givenAnExistingPriceSinceForAnotherArticle(purchase, ONE, YESTERDAY);

        objectUnderTest.calculateAndStorePricesForPurchase(purchase.getKey());

        Iterable<PersistentPrice> prices = fetchPricesOfShopRelatedToPurchase(purchase.getKey());
        assertThat(prices, Matchers.<PersistentPrice>iterableWithSize(2));
        assertThat(prices, hasItems(
                both(withPrice(ONE)).and(since(YESTERDAY)),
                both(withPrice(TEN)).and(since(TODAY))));
    }

    private void givenAnExistingPriceSince(final PersistentPurchase purchase, final BigDecimal price, final Date since) {
        executeWithoutTransaction(new PersistenceTemplate<Object>() {
            @Override
            public Object doWithPersistenceManager(PersistenceManager persistenceManager) {
                PersistentShop shop = persistenceManager.getObjectById(PersistentShop.class, purchase.getShop().getKey());
                PersistentArticle article = persistenceManager.getObjectById(PersistentArticle.class, getOnlyElement(purchase).getArticle().getKey());

                PersistentPrices prices = new PersistentPrices(shop);
                Transaction tx = startTransaction(persistenceManager);
                prices = persistenceManager.makePersistent(prices);
                tx.commit();
                tx = startTransaction(persistenceManager);
                prices.add(new PersistentPrice(article, since, price));
                persistenceManager.makePersistent(prices);
                tx.commit();

                return prices;
            }
        });
    }

    private void givenAnExistingPriceSinceForAnotherArticle(final PersistentPurchase purchase, final BigDecimal price, final Date since) {
        executeWithoutTransaction(new PersistenceTemplate<Object>() {
            @Override
            public Object doWithPersistenceManager(PersistenceManager persistenceManager) {
                Transaction tx = startTransaction(persistenceManager);
                PersistentArticle article = persistenceManager.makePersistent(new PersistentArticle(DATA_SET_ID, "OtherArticle", null, false, null));
                tx.commit();

                PersistentShop shop = persistenceManager.getObjectById(PersistentShop.class, purchase.getShop().getKey());

                PersistentPrices prices = new PersistentPrices(shop);
                tx = startTransaction(persistenceManager);
                prices = persistenceManager.makePersistent(prices);
                tx.commit();
                tx = startTransaction(persistenceManager);
                prices.add(new PersistentPrice(article, since, price));
                persistenceManager.makePersistent(prices);
                tx.commit();

                return prices;
            }
        });
    }

    private Iterable<PersistentPrice> fetchPricesOfShopRelatedToPurchase(final Long purchaseId) {
        return executeWithoutTransaction(new PersistenceTemplate<Iterable<PersistentPrice>>() {
            @Override
            public Iterable<PersistentPrice> doWithPersistenceManager(PersistenceManager persistenceManager) {
                PersistentPurchase purchase = persistenceManager.getObjectById(PersistentPurchase.class, purchaseId);
                PersistentPrices prices = persistenceManager.getObjectById(PersistentPrices.class,
                        KeyFactory.createKey(PersistentPrices.class.getSimpleName(), purchase.getShop().getKeyOrThrow()));
                prices.iterator(); // initialize
                return prices;
            }
        });
    }

    private PersistentPurchase givenAPurchase(final BigDecimal price) {
        return executeWithoutTransaction(new PersistenceTemplate<PersistentPurchase>() {
            @Override
            public PersistentPurchase doWithPersistenceManager(PersistenceManager persistenceManager) {
                Transaction tx = startTransaction(persistenceManager);
                PersistentShop shop = new PersistentShop(DATA_SET_ID, "Shop");
                persistenceManager.makePersistent(shop);
                tx.commit();
                tx = startTransaction(persistenceManager);
                PersistentArticle article = new PersistentArticle(DATA_SET_ID, "Article", null, false, null);
                persistenceManager.makePersistent(article);
                tx.commit();
                tx = startTransaction(persistenceManager);
                PersistentPurchase purchase = new PersistentPurchase(DATA_SET_ID, shop, TODAY, asList(new PersistentPurchasing(article, ONE, price, null)));
                purchase = persistenceManager.makePersistent(purchase);
                tx.commit();

                return purchase;
            }
        });
    }


}
