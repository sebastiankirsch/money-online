package net.tcc.money.online.server.domain;

import com.google.appengine.datanucleus.annotations.Unowned;
import net.tcc.gae.AbstractEntity;
import net.tcc.money.online.shared.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jdo.annotations.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@PersistenceCapable
public class PersistentPrice extends AbstractEntity<Long> implements Serializable {

    private static final long serialVersionUID = Constants.SERIAL_VERSION;

    @Nullable
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @SuppressWarnings("unused") // written by JDO
    private Long key;

    @Nonnull
    @Persistent(nullValue = NullValue.EXCEPTION)
    @Unowned
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private PersistentShop shop;

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

    public PersistentPrice(@Nonnull PersistentShop shop, @Nonnull PersistentArticle article, @Nonnull Date since, @Nonnull BigDecimal price) {
        this.shop = shop;
        this.article = article;
        this.since = since;
        this.price = price;
    }

    @Nullable
    @Override
    protected Long getKey() {
        return this.key;
    }

}
