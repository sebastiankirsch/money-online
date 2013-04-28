package net.tcc.money.online.server.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.repackaged.com.google.common.base.Function;
import net.tcc.money.online.shared.Constants;
import net.tcc.money.online.shared.dto.Category;
import net.tcc.money.online.shared.dto.Purchasing;

import com.google.appengine.datanucleus.annotations.Unowned;

@PersistenceCapable
public class PersistentPurchasing implements Serializable {

	private static final long serialVersionUID = Constants.SERIAL_VERSION;

	public static final Function<PersistentPurchasing, Purchasing> toPurchasing = new Function<PersistentPurchasing, Purchasing>() {
		@Override
		@Nonnull
		public Purchasing apply(@Nonnull PersistentPurchasing purchasing) {
			return purchasing == null ? null : purchasing.toPurchasing();
		}
	};

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
	private String key;

	@Nonnull
	@Persistent
	@Unowned
	private PersistentArticle article;

	@Nullable
	@Persistent
	private BigDecimal quantity;

	@Nonnull
	@Persistent
	private BigDecimal price;

	@Nullable
	@Persistent
	@Unowned
	private PersistentCategory category;

	@SuppressWarnings("unused")
	private PersistentPurchasing() {// for JDO
		super();
	}

	public PersistentPurchasing(@Nonnull PersistentArticle article, @Nullable BigDecimal quantity,
			@Nonnull BigDecimal price, @Nullable PersistentCategory category) {
		this.article = article;
		this.quantity = quantity;
		this.price = price;
		this.category = category;
	}

	@Nonnull
	Purchasing toPurchasing() {
		Category category = this.category == null ? null : this.category.toCategory();
		return new Purchasing(article.toArticle(), quantity, price, category);
	}

}
