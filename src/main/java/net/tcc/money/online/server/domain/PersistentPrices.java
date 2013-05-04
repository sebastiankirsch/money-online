package net.tcc.money.online.server.domain;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.datanucleus.annotations.Unowned;
import com.google.appengine.repackaged.com.google.common.base.Predicate;
import net.tcc.gae.AbstractEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jdo.annotations.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.google.appengine.repackaged.com.google.common.collect.Iterables.filter;
import static com.google.appengine.repackaged.com.google.common.collect.Iterables.getOnlyElement;

@PersistenceCapable
public class PersistentPrices extends AbstractEntity<Key> implements Iterable<PersistentPrice> {

    @Nonnull
    @Persistent
    @PrimaryKey
    private Key key;

    @Nonnull
    @Persistent(nullValue = NullValue.EXCEPTION)
    @Unowned
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private PersistentShop shop;

    @Nonnull
    @Persistent
    private List<PersistentPrice> prices = new ArrayList<>();

    @Deprecated
    @SuppressWarnings("unused")
    private PersistentPrices() {
        // for JDO
    }

    public PersistentPrices(@Nonnull PersistentShop shop) {
        key = KeyFactory.createKey(getClass().getSimpleName(), shop.getKeyOrThrow());
        this.shop = shop;
    }

    @Nonnull
    @Override
    protected Key getKey() {
        return this.key;
    }

    @Override
    public Iterator<PersistentPrice> iterator() {
        return this.prices.iterator();
    }

    @Nullable
    public PersistentPrice getPriceFor(@Nonnull PersistentArticle article) {
        return getOnlyElement(filter(this.prices, withArticle(article)), null);
    }

    @Nonnull
    private Predicate<? super PersistentPrice> withArticle(@Nonnull final PersistentArticle article) {
        return new Predicate<PersistentPrice>() {
            @Override
            public boolean apply(@Nullable PersistentPrice persistentPrice) {
                return persistentPrice != null && article.equals(persistentPrice.getArticle());
            }
        };
    }

    public PersistentPrice addPriceFor(PersistentArticle article, Date since, BigDecimal price) {
        PersistentPrice persistentPrice = new PersistentPrice(getKey(), article, since, price);
        this.prices.add(persistentPrice);
        return persistentPrice;
    }

}
