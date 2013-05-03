package net.tcc.money.online.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Category extends DtoWithLongKey implements IsSerializable {

	private String name;

	private Category parent;

    @Deprecated
	@SuppressWarnings({"deprecation", "unused"})
	private Category() {
		super();
	}

	public Category(Long key, String name, Category parent) {
		super(key);
		this.name = name;
		this.parent = parent;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(" [").append(this.name).append("]");
		if (this.parent != null) {
			buf.append(", child of ").append(this.parent);
		}
		return super.toString() + buf.toString();
	}

	public String getName() {
		return this.name;
	}

	public Category getParent() {
		return this.parent;
	}

    @SuppressWarnings("SimplifiableIfStatement")
    public boolean isChildOf(Category category) {
        if (parent == null)
			return false;
        return parent.equals(category) || parent.isChildOf(category);
    }

}
