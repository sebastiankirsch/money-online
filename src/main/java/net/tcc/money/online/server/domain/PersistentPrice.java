package net.tcc.money.online.server.domain;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.datanucleus.annotations.Unowned;
import net.tcc.gae.AbstractEntity;
import net.tcc.money.online.shared.Constants;

import javax.annotation.Nonnull;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@PersistenceCapable
public class PersistentPrice extends AbstractEntity<Key> implements Serializable {

    private static final long serialVersionUID = Constants.SERIAL_VERSION;

    @Nonnull
    @PrimaryKey
    @Persistent
    private Key key;

    @Nonnull
    @Persistent(nullValue = NullValue.EXCEPTION)
    @Unowned
    private PersistentArticle article;

    @Nonnull
    @Persistent(nullValue = NullValue.EXCEPTION)
    private Date since;

    @Nonnull
    @Persistent(nullValue = NullValue.EXCEPTION)
    private BigDecimal price;

    @Deprecated
    @SuppressWarnings("unused")
    private PersistentPrice() {
        // for JDO
    }

    PersistentPrice(@Nonnull Key parentKey, @Nonnull PersistentArticle article, @Nonnull Date since, @Nonnull BigDecimal price) {
        this.key = KeyFactory.createKey(parentKey, getClass().getSimpleName(), article.getKeyOrThrow());
        this.article = article;
        this.since = since;
        this.price = price;
    }

    @Nonnull
    @Override
    protected Key getKey() {
        return this.key;
    }

    @Nonnull
    public PersistentArticle getArticle() {
        return article;
    }

    @Nonnull
    public Date getSince() {
        return since;
    }

    public void setSince(@Nonnull Date since) {
        this.since = since;
    }

    @Nonnull
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(@Nonnull BigDecimal price) {
        this.price = price;
    }

}
