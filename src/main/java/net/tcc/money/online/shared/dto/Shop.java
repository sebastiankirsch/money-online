package net.tcc.money.online.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Shop implements IsSerializable {

	private long key;

	private String name;

	@SuppressWarnings("unused")
	private Shop() { // for GWT
		super();
	}

	public Shop(long key, String name) {
		this.key = key;
		this.name = name;
	}

	public long getKey() {
		return key;
	}

	public String getName() {
		return this.name;
	}

}
