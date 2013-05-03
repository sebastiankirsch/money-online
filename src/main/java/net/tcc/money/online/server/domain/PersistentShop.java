package net.tcc.money.online.server.domain;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.repackaged.com.google.common.base.Function;
import net.tcc.money.online.shared.Constants;
import net.tcc.money.online.shared.dto.Shop;

@PersistenceCapable
public class PersistentShop extends DataSetBoundEntity implements Serializable {

    private static final long serialVersionUID = Constants.SERIAL_VERSION;

    public static final Function<PersistentShop, Shop> toShop = new Function<PersistentShop, Shop>() {
        @Nullable
        @Override
        public Shop apply(@Nullable PersistentShop shop) {
            return shop == null ? null : shop.toShop();
        }
    };

    @Persistent(nullValue = NullValue.EXCEPTION)
    private String name;

    @Deprecated
    @SuppressWarnings({"deprecation", "unused"})
    private PersistentShop() { // for JDO
        super();
    }

    public PersistentShop(@Nonnull String dataSetId, @Nonnull String name) {
        super(dataSetId);
        this.name = name;
    }

    @Override
    public String toString() {
        return super.toString() + " [" + name + "]";
    }

    @Nonnull
    public Shop toShop() {
        Long key = getKey();
        if (key == null) {
            throw new IllegalStateException("Entity is not yet persisted!");
        }
        return new Shop(key, name);
    }

}
