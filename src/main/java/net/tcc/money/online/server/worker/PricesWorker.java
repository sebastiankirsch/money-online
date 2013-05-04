package net.tcc.money.online.server.worker;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.common.base.StringUtil;
import net.tcc.gae.ServerTools;
import net.tcc.gae.ServerTools.PersistenceTemplate;
import net.tcc.money.online.server.domain.*;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                PersistentShop shop = purchase.getShop();
                PersistentPrices persistentPrices = fetchPrices(persistenceManager, shop);

                Date purchaseDate = purchase.getPurchaseDate();
                Transaction tx = startTransaction(persistenceManager);
                for (PersistentPurchasing purchasing : purchase) {
                    if (purchasing.getQuantity() == null) {
                        continue;
                    }
                    PersistentArticle article = purchasing.getArticle();
                    BigDecimal price = purchasing.getPrice().divide(purchasing.getQuantity());
                    PersistentPrice existingPrice = persistentPrices.getPriceFor(article);
                    if (existingPrice == null) {
                        persistentPrices.addPriceFor(article, purchaseDate, price);
                        if (LOG.isLoggable(Level.INFO)) {
                            LOG.info("Storing new price for " + shop + "/" + article);
                        }
                        continue;
                    }
                    boolean priceHasNotChanged = existingPrice.getPrice().equals(price);
                    boolean existingPriceIsNewerThanNewPrice = existingPrice.getSince().compareTo(purchaseDate) > 0;
                    if (priceHasNotChanged || existingPriceIsNewerThanNewPrice) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Keeping existing price for " + shop + "/" + article);
                        }
                        continue;
                    }
                    existingPrice.setSince((Date) purchaseDate.clone());
                    existingPrice.setPrice(price);
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("Updating existing price for " + shop + "/" + article);
                    }
                }
                persistenceManager.makePersistent(persistentPrices);
                tx.commit();

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
                } finally {
                    persistenceManager.getFetchPlan().removeGroup(fetchGroupName);
                }

                return purchase;
            }

            @SuppressWarnings("unchecked")
            private PersistentPrices fetchPrices(PersistenceManager persistenceManager, PersistentShop shop) {
                Key key = KeyFactory.createKey(PersistentPrices.class.getSimpleName(), shop.getKeyOrThrow());
                String fetchGroupName = "priceWithFields";
                persistenceManager.getFetchGroup(PersistentPrices.class, fetchGroupName).addMember("shop").addMember("prices");
                persistenceManager.getFetchGroup(PersistentPrice.class, fetchGroupName).addMember("article");
                persistenceManager.getFetchPlan().addGroup(fetchGroupName);
                PersistentPrices persistentPrices;
                Transaction tx = startTransaction(persistenceManager);
                try {
                    persistentPrices = persistenceManager.getObjectById(PersistentPrices.class, key);
                } catch (JDOObjectNotFoundException e) {
                    persistentPrices = new PersistentPrices(shop);
                    persistenceManager.makePersistent(persistentPrices);
                    tx.commit();
                } finally {
                    persistenceManager.getFetchPlan().removeGroup(fetchGroupName);
                    ServerTools.rollback(persistenceManager);
                }

                return persistentPrices;
            }

        });
    }

}
