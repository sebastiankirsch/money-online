package net.tcc.money.online.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Shop extends DtoWithLongKey implements IsSerializable {

	private String name;

    @Deprecated
	@SuppressWarnings({"deprecation", "unused"})
	private Shop() {
       super();
	}

	public Shop(long key, String name) {
		super(key);
		this.name = name;
	}

    @Override
    public String toString(){
        return super.toString() + " [" + getName() + "]";
    }

	public String getName() {
		return this.name;
	}

}
