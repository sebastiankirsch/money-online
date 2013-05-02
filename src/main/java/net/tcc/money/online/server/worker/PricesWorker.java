package net.tcc.money.online.server.worker;

import com.google.appengine.repackaged.com.google.common.base.StringUtil;
import com.google.appengine.repackaged.com.google.common.collect.Iterables;
import net.tcc.gae.ServerTools.PersistenceTemplate;
import net.tcc.money.online.server.domain.PersistentPrice;
import net.tcc.money.online.server.domain.PersistentPurchase;
import net.tcc.money.online.server.domain.PersistentPurchasing;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.Lists.newArrayList;
import static net.tcc.gae.ServerTools.executeWithoutTransaction;
import static net.tcc.gae.ServerTools.startTransaction;

public class PricesWorker extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(PricesWorker.class.getName());

    public static final String PURCHASE_ID = "purchaseId";

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String purchaseIdParam = request.getParameter(PURCHASE_ID);
        if (StringUtil.isEmptyOrWhitespace(purchaseIdParam)) {
            String msg = "The parameter [" + PURCHASE_ID + "] is missing!";
            LOG.warning(msg);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
            return;
        }
        final long purchaseId;
        try {
            purchaseId = Long.parseLong(purchaseIdParam);
        } catch (NumberFormatException nFE) {
            String msg = "The value [" + purchaseIdParam + "] for parameter [" + PURCHASE_ID + "] is no valid number!";
            LOG.warning(msg);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
            return;
        }
        calculateAndStorePricesForPurchase(purchaseId);
    }

    void calculateAndStorePricesForPurchase(final long purchaseId) {
        executeWithoutTransaction(new PersistenceTemplate<Collection<PersistentPrice>>() {

            @Override
            public Collection<PersistentPrice> doWithPersistenceManager(PersistenceManager persistenceManager) {
                PersistentPurchase purchase = fetchPurchase(persistenceManager);
                ArrayList<PersistentPrice> prices = calculatePricesOf(purchase);

                Query query = persistenceManager.newQuery(PersistentPrice.class, "shop == pShop && article == pArticle");
                query.declareParameters("net.tcc.money.online.server.domain.PersistentShop pShop, net.tcc.money.online.server.domain.PersistentArticle pArticle");
                for (PersistentPrice price : prices) {
                    Transaction tx = startTransaction(persistenceManager);
                    @SuppressWarnings("unchecked")
                    List<PersistentPrice> existingPrices = (List<PersistentPrice>) query.execute(price.getShop(), price.getArticle());
                    if (existingPrices.isEmpty()) {
                        persistenceManager.makePersistent(price);
                        tx.commit();
                        continue;
                    }
                    PersistentPrice existingPrice = Iterables.getOnlyElement(existingPrices);
                    boolean priceHasNotChanged = existingPrice.getPrice().equals(price.getPrice());
                    boolean existingPriceIsNewerThanNewPrice = existingPrice.getSince().compareTo(price.getSince()) > 0;
                    if (priceHasNotChanged || existingPriceIsNewerThanNewPrice) {
                        continue;
                    }
                    existingPrice.setSince(new Date(price.getSince().getTime()));
                    existingPrice.setPrice(price.getPrice());
                    persistenceManager.makePersistent(existingPrice);

                    tx.commit();
                }

                return null;
            }

            private PersistentPurchase fetchPurchase(PersistenceManager persistenceManager) {
                String fetchGroupName = "priceCalculation";
                persistenceManager.getFetchGroup(PersistentPurchase.class, fetchGroupName).addMember("shop").addMember("purchasings");
                persistenceManager.getFetchGroup(PersistentPurchasing.class, fetchGroupName).addMember("article");
                persistenceManager.getFetchPlan().addGroup(fetchGroupName);
                final PersistentPurchase purchase;
                try {
                    purchase = persistenceManager.getObjectById(PersistentPurchase.class, purchaseId);
                } catch (JDOObjectNotFoundException oNFE) {
                    LOG.warning("Purchase #" + purchaseId + " wasn't found. Maybe data is not yet consistent.");
                    throw oNFE;
                }
                return purchase;
            }

            private ArrayList<PersistentPrice> calculatePricesOf(PersistentPurchase purchase) {
                ArrayList<PersistentPrice> prices = newArrayList();
                for (PersistentPurchasing purchasing : purchase) {
                    BigDecimal quantity = purchasing.getQuantity();
                    if (quantity == null) {
                        continue;
                    }
                    PersistentPrice price = new PersistentPrice(purchase.getShop(),
                            purchasing.getArticle(), purchase.getPurchaseDate(), purchasing.getPrice().divide(quantity));
                    prices.add(price);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Storing " + price);
                    }
                }
                return prices;
            }

        });
    }

}
