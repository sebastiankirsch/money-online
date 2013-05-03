package net.tcc.money.online.server.domain;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.datanucleus.annotations.Unowned;
import net.tcc.gae.AbstractEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.ArrayList;
import java.util.List;

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
        key= KeyFactory.createKey(getClass().getSimpleName(), shop.getKeyOrThrow());
        this.shop = shop;
    }

    @Nullable
    @Override
    protected Key getKey() {
        return this.key;
    }

}
