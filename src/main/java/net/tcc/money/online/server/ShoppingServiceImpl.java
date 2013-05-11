package net.tcc.money.online.server;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.common.base.Function;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.view.client.Range;
import net.tcc.gae.ServerTools;
import net.tcc.money.online.client.ShoppingService;
import net.tcc.money.online.server.domain.*;
import net.tcc.money.online.shared.Constants;
import net.tcc.money.online.shared.dto.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
import static com.google.appengine.api.taskqueue.TaskOptions.Method.POST;
import static com.google.appengine.repackaged.com.google.common.collect.Iterables.getOnlyElement;
import static com.google.appengine.repackaged.com.google.common.collect.Iterables.transform;
import static com.google.appengine.repackaged.com.google.common.collect.Lists.newArrayList;
import static java.lang.System.currentTimeMillis;
import static net.tcc.gae.ServerTools.*;
import static net.tcc.money.online.server.domain.PersistentArticle.toArticle;
import static net.tcc.money.online.server.domain.PersistentCategory.toCategory;
import static net.tcc.money.online.server.domain.PersistentPurchase.toPurchase;
import static net.tcc.money.online.server.domain.PersistentShop.toShop;
import static net.tcc.money.online.server.worker.PricesWorker.PURCHASE_ID;

public class ShoppingServiceImpl extends RemoteServiceServlet implements ShoppingService {

    private static final long serialVersionUID = Constants.SERIAL_VERSION;

    private static final Logger LOG = Logger.getLogger(ShoppingServiceImpl.class.getName());

    private final UserService userService = UserServiceFactory.getUserService();

    @Nonnull
    private String getDataSetId() {
        return userService.getCurrentUser().getUserId();
    }

    @Nonnull
    @Override
    public Iterable<Article> loadArticles() {
        return loadAll(PersistentArticle.class, toArticle);
    }

    @Nonnull
    @Override
    public Iterable<Category> loadCategories() {
        return loadAll(PersistentCategory.class, toCategory);
    }

    @Nonnull
    @Override
    public List<Purchase> loadPurchases(Range range, String columnName, boolean ascending) {
        return loadAll(PersistentPurchase.class, toPurchase, columnName + (ascending ? " ASC" : " DESC"), range);
    }

    @Nonnull
    @Override
    public Iterable<Shop> loadShops() {
        return loadAll(PersistentShop.class, toShop, "name ASC");
    }

    @Nonnull
    private <Dto, Persistence> List<Dto> loadAll(@Nonnull Class<Persistence> persistenceClass, @Nonnull Function<Persistence, Dto> function, @Nullable String order, @Nullable Range range) {
        String dataSetId = getDataSetId();
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(persistenceClass, "dataSetId == pDataSetId");
            query.declareParameters("java.lang.String pDataSetId");
            query.setOrdering(order);
            if (range != null) {
                query.setRange(range.getStart(), range.getStart() + range.getLength());
            }
            @SuppressWarnings("unchecked")
            Collection<Persistence> result = (Collection<Persistence>) query.execute(dataSetId);
            return newArrayList(transform(result, function));
        } finally {
            pm.close();
        }
    }

    @Nonnull
    private <Dto, Persistence> List<Dto> loadAll(@Nonnull Class<Persistence> persistenceClass, @Nonnull Function<Persistence, Dto> function, @Nullable String order) {
        return loadAll(persistenceClass, function, order, null);
    }

    private <Dto, Persistence> Iterable<Dto> loadAll(Class<Persistence> persistenceClass,
                                                     Function<Persistence, Dto> function) {
        return loadAll(persistenceClass, function, null);
    }

    @Override
    public Map<Category, BigDecimal> loadCategorySpendings() {
        final String dataSetId = getDataSetId();
        return executeWithoutTransaction(new PersistenceTemplate<Map<Category, BigDecimal>>() {
            @Override
            public Map<Category, BigDecimal> doWithPersistenceManager(PersistenceManager persistenceManager) {
                String fetchGroup = "calculateCategorySpendings";
                persistenceManager.getFetchGroup(PersistentPurchase.class, fetchGroup).addMember("purchasings");
                persistenceManager.getFetchGroup(PersistentPurchasing.class, fetchGroup).addMember("article").addMember("category");
                persistenceManager.getFetchGroup(PersistentArticle.class, fetchGroup).addMember("category");
                persistenceManager.getFetchPlan().addGroup(fetchGroup);
                Query query = persistenceManager.newQuery(PersistentPurchase.class, "dataSetId == pDataSetId");
                query.declareParameters("java.lang.String pDataSetId");
                @SuppressWarnings("unchecked")
                Collection<PersistentPurchase> purchases = (Collection<PersistentPurchase>) query.execute(dataSetId);

                Map<PersistentCategory, BigDecimal> persistentData = new HashMap<>();
                for (PersistentPurchase purchase : purchases) {
                    for (PersistentPurchasing purchasing : purchase) {
                        PersistentCategory category = purchasing.getCategory();
                        if (category == null) {
                            category = purchasing.getArticle().getCategory();
                        }
                        BigDecimal sum = persistentData.get(category);
                        if (sum == null) {
                            sum = BigDecimal.ZERO;
                        }
                        persistentData.put(category, sum.add(purchasing.getPrice()));
                    }
                }

                HashMap<Category, BigDecimal> data = new HashMap<>(persistentData.size(), 1f);
                for (Map.Entry<PersistentCategory, BigDecimal> dataEntry : persistentData.entrySet()) {
                    data.put(toCategory.apply(dataEntry.getKey()), dataEntry.getValue());
                }
                return data;
            }
        });
    }

    @Nonnull
    @Override
    public Shop createShop(@Nonnull final String name) {
        long start = currentTimeMillis();
        final String dataSetId = getDataSetId();
        Shop shop = executeWithTransaction(new PersistenceTemplate<PersistentShop>() {
            @Override
            public PersistentShop doWithPersistenceManager(PersistenceManager pm) {
                return pm.makePersistent(new PersistentShop(dataSetId, name));
            }
        }).toShop();
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info((currentTimeMillis() - start) + "ms to persist " + shop);
        }
        return shop;
    }

    @Nonnull
    @Override
    public Category createCategory(@Nonnull final String name, @Nullable final Category parent) {
        long start = currentTimeMillis();
        final String dataSetId = getDataSetId();
        Category category = ServerTools.executeWithoutTransaction(new PersistenceTemplate<PersistentCategory>() {
            @Override
            public PersistentCategory doWithPersistenceManager(PersistenceManager pm) {
                PersistentCategory parentCategory = parent == null ? null : pm.getObjectById(PersistentCategory.class,
                        parent.getKey());
                Transaction tx = startTransaction(pm);
                try {
                    return pm.makePersistent(new PersistentCategory(dataSetId, name, parentCategory));
                } finally {
                    tx.commit();
                }
            }
        }).toCategory();
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info((currentTimeMillis() - start) + "ms to persist " + category);
        }
        return category;
    }

    @Nonnull
    @Override
    public Category setParentFor(@Nonnull final Category plainCategory, @Nullable final Category plainParent) {
        long start = currentTimeMillis();
        Category category = executeWithoutTransaction(new PersistenceTemplate<Category>() {
            @Override
            public Category doWithPersistenceManager(PersistenceManager pm) {
                PersistentCategory category = pm.getObjectById(PersistentCategory.class, plainCategory.getKey());
                PersistentCategory parent;
                if (plainParent == null) {
                    parent = null;
                } else {
                    parent = pm.getObjectById(PersistentCategory.class, plainParent.getKey());
                }
                Transaction tx = startTransaction(pm);
                try {
                    category.setParent(parent);
                    return pm.makePersistent(category).toCategory();
                } finally {
                    tx.commit();
                }
            }
        });
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info((currentTimeMillis() - start) + "ms to update " + category);
        }
        return category;
    }

    @Nonnull
    @Override
    public Article setCategoryFor(@Nonnull final Article plainArticle, @Nullable final Category plainCategory) {
        long start = currentTimeMillis();
        Article article = executeWithoutTransaction(new PersistenceTemplate<Article>() {
            @Override
            public Article doWithPersistenceManager(PersistenceManager pm) {
                PersistentArticle article = pm.getObjectById(PersistentArticle.class, plainArticle.getKey());
                PersistentCategory category;
                if (plainCategory == null) {
                    category = null;
                } else {
                    category = pm.getObjectById(PersistentCategory.class, plainCategory.getKey());
                }
                Transaction tx = startTransaction(pm);
                try {
                    article.setCategory(category);
                    return pm.makePersistent(article).toArticle();
                } finally {
                    tx.commit();
                }
            }
        });
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info((currentTimeMillis() - start) + "ms to update " + article);
        }
        return article;
    }

    @Override
    public void createPurchase(@Nonnull Purchase purchase) {
        long start = currentTimeMillis();
        PersistenceManager pm = getPersistenceManager();
        PersistentPurchase persistentPurchase;
        try {
            persistentPurchase = toPersistentPurchase(purchase, pm);
            Transaction tx = startTransaction(pm);
            persistentPurchase = pm.makePersistent(persistentPurchase);
            pm.flush();
            QueueFactory.getQueue("prices").add(withUrl("/worker/prices").method(POST).param(PURCHASE_ID, String.valueOf(persistentPurchase.getKey())));
            tx.commit();
        } finally {
            close(pm);
        }
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info((currentTimeMillis() - start) + "ms to persist " + persistentPurchase.toPurchase());
        }
    }

    @Nonnull
    private PersistentPurchase toPersistentPurchase(@Nonnull Purchase purchase, PersistenceManager pm) {
        String dataSetId = getDataSetId();
        return new PersistentPurchase(dataSetId, loadShop(pm, purchase.getShop()), purchase.getPurchaseDate(),
                transform(purchase, toPersistentPurchasing(pm, dataSetId)));
    }

    @Nonnull
    private PersistentShop loadShop(PersistenceManager pm, @Nonnull Shop shop) {
        Transaction tx = startTransaction(pm);
        try {
            return pm.getObjectById(PersistentShop.class, shop.getKey());
        } finally {
            tx.rollback();
        }
    }

    @Nonnull
    private Function<Purchasing, PersistentPurchasing> toPersistentPurchasing(final PersistenceManager pm,
                                                                              @Nonnull final String dataSetId) {
        return new Function<Purchasing, PersistentPurchasing>() {

            @Nullable
            @Override
            public PersistentPurchasing apply(@Nullable Purchasing purchasing) {
                assert purchasing != null;
                return new PersistentPurchasing(loadOrCreateArticle(pm, dataSetId, purchasing.getArticle()),
                        purchasing.getQuantity(), purchasing.getPrice(), loadCategory(pm, purchasing.getCategory()));
            }
        };
    }

    @Nonnull
    private PersistentArticle loadOrCreateArticle(PersistenceManager pm, @Nonnull String dataSetId,
                                                  @Nonnull Article article) {
        Transaction tx = startTransaction(pm);
        try {
            Query query = pm
                    .newQuery(PersistentArticle.class,
                            "dataSetId == pDataSetId && name == pName && brand == pBrand && vegan == pVegan && lotSize == pLotSize");
            query.declareParameters("java.lang.String pDataSetId, java.lang.String pName, java.lang.String pBrand, boolean pVegan, java.lang.String pLotSize");
            @SuppressWarnings("unchecked")
            Collection<PersistentArticle> articles = (Collection<PersistentArticle>) query.executeWithArray(dataSetId,
                    article.getName(), article.getBrand(), article.isVegan(), article.getLotSize());
            PersistentArticle persistedArticle = getOnlyElement(articles, null);
            if (persistedArticle == null) {
                persistedArticle = new PersistentArticle(dataSetId, article.getName(), article.getBrand(),
                        article.isVegan(), article.getLotSize());
                persistedArticle = pm.makePersistent(persistedArticle);
                tx.commit();
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Persisted " + persistedArticle);
                }
            } else {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Loaded " + persistedArticle);
                }
            }
            return persistedArticle;
        } finally {
            ServerTools.rollback(pm);
        }
    }

    @Nullable
    private PersistentCategory loadCategory(PersistenceManager pm, @Nullable Category category) {
        if (category == null) {
            return null;
        }
        Transaction tx = startTransaction(pm);
        try {
            PersistentCategory persistentCategory = pm.getObjectById(PersistentCategory.class, category.getKey());
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Loaded " + persistentCategory);
            }
            return persistentCategory;
        } finally {
            tx.rollback();
        }
    }

}
