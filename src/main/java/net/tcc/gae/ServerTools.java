package net.tcc.gae;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

public final class ServerTools {

    private static final PersistenceManagerFactory pmfInstance = JDOHelper.getPersistenceManagerFactory("default");

    public static PersistenceManager getPersistenceManager() {
        return pmfInstance.getPersistenceManager();
    }

    public static void close(PersistenceManager persistenceManager) {
        Transaction tx = persistenceManager.currentTransaction();
        try {
            if (tx.isActive()) {
                tx.rollback();
            }
        } finally {
            persistenceManager.close();
        }
    }

    public static Transaction startTransaction(PersistenceManager pm) {
        Transaction tx = pm.currentTransaction();
        tx.begin();
        return tx;
    }

    public static void rollback(PersistenceManager pm) {
        Transaction tx = pm.currentTransaction();
        if (tx.isActive()) {
            tx.rollback();
        }
    }

    private static <T> T execute(PersistenceTemplate<T> template, boolean withTransaction) {
        PersistenceManager persistenceManager = getPersistenceManager();
        try {
            Transaction transaction = withTransaction ?
                    startTransaction(persistenceManager) : null;
            T result = template.doWithPersistenceManager(persistenceManager);
            if (withTransaction) transaction.commit();
            return result;
        } finally {
            close(persistenceManager);
        }
    }

    public static <T> T executeWithoutTransaction(PersistenceTemplate<T> template) {
        return execute(template, false);
    }

    public static <T> T executeWithTransaction(PersistenceTemplate<T> template) {
        return execute(template, true);
    }

    private ServerTools() {
        super();
    }

    public static interface PersistenceTemplate<T> {
        public T doWithPersistenceManager(PersistenceManager persistenceManager);
    }

}
