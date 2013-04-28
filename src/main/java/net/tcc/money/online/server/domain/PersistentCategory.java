package net.tcc.money.online.server.domain;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.repackaged.com.google.common.base.Function;
import net.tcc.money.online.shared.Constants;
import net.tcc.money.online.shared.dto.Category;

import com.google.appengine.datanucleus.annotations.Unowned;

@PersistenceCapable
public class PersistentCategory extends DataSetBoundEntity implements Serializable {

	private static final long serialVersionUID = Constants.SERIAL_VERSION;

	public static final Function<PersistentCategory, Category> toCategory = new Function<PersistentCategory, Category>() {
		@Nonnull
		@Override
		public Category apply(@Nonnull PersistentCategory category) {
			return category.toCategory();
		}
	};

	@Nonnull
	@Persistent
	private String name;

	@Nullable
	@Persistent
	@Unowned
	private PersistentCategory parent;

	@Deprecated
	@SuppressWarnings("unused")
	private PersistentCategory() {// for JDO
		super();
	}

	public PersistentCategory(@Nonnull String dataSetId, @Nonnull String name, @Nullable PersistentCategory parent) {
		super(dataSetId);
		this.name = name;
		this.parent = parent;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getKey() + " [" + this.name + "]";
	}

	@Nonnull
	public Category toCategory() {
		return new Category(getKey(), this.name, this.parent == null ? null : this.parent.toCategory());
	}

	public void setParent(@Nullable PersistentCategory parent) {
		if (parent != null) {
			if (equals(parent) || parent.isChildOf(this))
				throw new IllegalArgumentException("Cannot set a child of this category as a parent!");
		}
		this.parent = parent;
	}

	private boolean isChildOf(@Nullable PersistentCategory category) {
		if (parent == null)
			return false;
		if (parent.equals(category))
			return true;
		return parent.isChildOf(category);
	}

}
