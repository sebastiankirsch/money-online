package net.tcc.money.online.server.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jdo.annotations.*;

import com.google.appengine.repackaged.com.google.common.base.Function;
import net.tcc.gae.AbstractEntity;
import net.tcc.money.online.shared.Constants;
import net.tcc.money.online.shared.dto.Category;
import net.tcc.money.online.shared.dto.Purchasing;

import com.google.appengine.datanucleus.annotations.Unowned;

@PersistenceCapable
public class PersistentPurchasing extends AbstractEntity<String> implements Serializable {

	private static final long serialVersionUID = Constants.SERIAL_VERSION;

	public static final Function<PersistentPurchasing, Purchasing> toPurchasing = new Function<PersistentPurchasing, Purchasing>() {
		@Nullable
        @Override
		public Purchasing apply(@Nullable PersistentPurchasing purchasing) {
			return purchasing == null ? null : purchasing.toPurchasing();
		}
	};

    @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@PrimaryKey
    @SuppressWarnings("unused") // written by JDO
	private String key;

	@Nonnull
	@Persistent(nullValue = NullValue.EXCEPTION)
	@Unowned
	private PersistentArticle article;

	@Nullable
	@Persistent
	private BigDecimal quantity;

	@Nonnull
	@Persistent(nullValue = NullValue.EXCEPTION)
	private BigDecimal price;

	@Nullable
	@Persistent
	@Unowned
	private PersistentCategory category;

    @Deprecated
	@SuppressWarnings("unused")
	private PersistentPurchasing() {
        // for JDO
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

    @Override
    protected String getKey() {
        return this.key;
    }

}
