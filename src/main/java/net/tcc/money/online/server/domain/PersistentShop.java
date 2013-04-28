package net.tcc.money.online.server.domain;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.repackaged.com.google.common.base.Function;
import net.tcc.money.online.shared.Constants;
import net.tcc.money.online.shared.dto.Shop;

@PersistenceCapable
public class PersistentShop extends DataSetBoundEntity implements Serializable {

	private static final long serialVersionUID = Constants.SERIAL_VERSION;

	public static final Function<PersistentShop, Shop> toShop = new Function<PersistentShop, Shop>() {
		@Override
		@Nonnull
		public Shop apply(@Nonnull PersistentShop shop) {
			return shop.toShop();
		}
	};

	@Persistent
	private String name;

	@Deprecated
	@SuppressWarnings("unused")
	private PersistentShop() { // for JDO
		super();
	}

	public PersistentShop(@Nonnull String dataSetId, @Nonnull String name) {
		super(dataSetId);
		this.name = name;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getKey() + " [" + getKey() + "]";
	}

	@Nonnull
	public Shop toShop() {
		return new Shop(getKey(), name);
	}

}
