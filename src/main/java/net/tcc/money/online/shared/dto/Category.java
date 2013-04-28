package net.tcc.money.online.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Category implements IsSerializable {

	private Long key;

	private String name;

	private Category parent;

	@SuppressWarnings("unused")
	private Category() {
		// for GWT
	}

	public Category(Long key, String name, Category parent) {
		this.key = key;
		this.name = name;
		this.parent = parent;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("Category [").append(this.name).append("]");
		if (this.parent != null) {
			buf.append(", child of ").append(this.parent);
		}
		return buf.toString();
	}

	@Override
	public int hashCode() {
		return key.intValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Category other = (Category) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	public String getName() {
		return this.name;
	}

	public Category getParent() {
		return this.parent;
	}

	public void setParent(Category parent) {
		this.parent = parent;
	}

	public Long getKey() {
		return this.key;
	}

	public boolean isChildOf(Category category) {
		if (parent == null)
			return false;
		if (parent.equals(category))
			return true;
		return parent.isChildOf(category);
	}

}
