package net.tcc.money.online.server.domain;

import static com.google.appengine.repackaged.com.google.common.collect.Iterables.transform;
import static com.google.appengine.repackaged.com.google.common.collect.Lists.newArrayList;
import static net.tcc.money.online.server.domain.PersistentPurchasing.toPurchasing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.repackaged.com.google.common.base.Function;
import net.tcc.money.online.shared.Constants;
import net.tcc.money.online.shared.dto.Purchase;

import com.google.appengine.datanucleus.annotations.Unowned;

@PersistenceCapable
public class PersistentPurchase extends DataSetBoundEntity implements Serializable, Iterable<PersistentPurchasing> {

	private static final long serialVersionUID = Constants.SERIAL_VERSION;

	public static final Function<PersistentPurchase, Purchase> toPurchase = new Function<PersistentPurchase, Purchase>() {
		@Nullable
		@Override
		public Purchase apply(@Nullable PersistentPurchase purchase) {
			return purchase == null ? null : purchase.toPurchase();
		}
	};

	@Nonnull
	@Persistent(nullValue = NullValue.EXCEPTION)
	@Unowned
	private PersistentShop shop;

	@Nonnull
	@Persistent(nullValue = NullValue.EXCEPTION)
	private Date purchaseDate;

	@Nonnull
	@Persistent(nullValue = NullValue.EXCEPTION)
	private List<PersistentPurchasing> purchasings = new ArrayList<>();

	@Deprecated
	@SuppressWarnings({"deprecation", "unused"})
	private PersistentPurchase() { // for JDO
		super();
	}

	public PersistentPurchase(@Nonnull String dataSetId, @Nonnull PersistentShop shop, @Nonnull Date purchaseDate,
			@Nonnull Iterable<PersistentPurchasing> purchasings) {
		super(dataSetId);
		this.shop = shop;
		this.purchaseDate = purchaseDate;
		this.purchasings = newArrayList(purchasings);
	}

	@Override
	public Iterator<PersistentPurchasing> iterator() {
		return this.purchasings.iterator();
	}

	@Nonnull
	public Purchase toPurchase() {
		Purchase purchase = new Purchase(shop.toShop(), purchaseDate);
		purchase.addAll(transform(this, toPurchasing));
		return purchase;
	}

    @Nonnull
    public PersistentShop getShop() {
        return this.shop;
    }

    @Nonnull
    public Date getPurchaseDate() {
        return purchaseDate;
    }

}
