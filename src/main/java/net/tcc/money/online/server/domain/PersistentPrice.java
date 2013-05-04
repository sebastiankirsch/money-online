package net.tcc.money.online.server.domain;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.datanucleus.annotations.Unowned;
import net.tcc.gae.AbstractEntity;
import net.tcc.money.online.shared.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Nullable
    @PrimaryKey
    @Persistent
    @SuppressWarnings("unused") // written by JDO
    private Key key;

    @Nonnull
    @Persistent(nullValue = NullValue.EXCEPTION)
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @Unowned
    private PersistentArticle article;

    @Nonnull
    @Persistent(nullValue = NullValue.EXCEPTION)
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private Date since;

    @Nonnull
    @Persistent(nullValue = NullValue.EXCEPTION)
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private BigDecimal price;

    @Deprecated
    @SuppressWarnings("unused")
    private PersistentPrice() {
        // for JDO
    }

    public PersistentPrice(@Nonnull PersistentArticle article, @Nonnull Date since, @Nonnull BigDecimal price) {
        this.article = article;
        this.since = since;
        this.price = price;
    }

    @Nullable
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

    public void setKey(@Nonnull Key key) {
        this.key = key;
    }
}
