package net.tcc.money.online.server.domain;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.datanucleus.annotations.Unowned;
import com.google.appengine.repackaged.com.google.common.base.Predicate;
import net.tcc.gae.AbstractEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jdo.annotations.*;
import java.util.ArrayList;
import java.util.List;

import static com.google.appengine.repackaged.com.google.common.collect.Iterables.filter;
import static com.google.appengine.repackaged.com.google.common.collect.Iterables.getOnlyElement;

@PersistenceCapable
public class PersistentPrices extends AbstractEntity<Key> {

    @Nullable
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

    @Nullable
    @Override
    protected Key getKey() {
        return this.key;
    }

    public PersistentPrice getPriceFor(@Nonnull PersistentArticle article) {
        return getOnlyElement(filter(this.prices, withArticle(article)), null);
    }

    private Predicate<? super PersistentPrice> withArticle(@Nonnull final PersistentArticle article) {
        return new Predicate<PersistentPrice>() {
            @Override
            public boolean apply(@Nullable PersistentPrice persistentPrice) {
                return persistentPrice != null && article.equals(persistentPrice.getArticle());
            }
        };
    }

    public void add(PersistentPrice price) {
        price.setKey(KeyFactory.createKey(getKey(), price.getClass().getSimpleName(), price.getArticle().getKeyOrThrow()));
        this.prices.add(price);
    }

}
