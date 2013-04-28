package net.tcc.money.online.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Article implements IsSerializable {

	private Long key;

	private String name;

	private String brand;

	private boolean vegan;

	private String lotSize;
	
	private Category category;

	@SuppressWarnings("unused") // for GWT
	private Article() {
		this.key = null;
	}

	public Article(Long key, String name, String brand, boolean vegan, String lotSize) {
		this.key = key;
		this.name = name;
		this.brand = brand;
		this.vegan = vegan;
		this.lotSize = lotSize;
	}

	public Article(String name, String brand, boolean vegan, String lotSize) {
		this(null, name, brand, vegan, lotSize);
	}

	public String toString() {
		return "Article [" + this.name + "/" + this.brand + "]";
	}

	public String getBrand() {
		return this.brand;
	}
	
	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Long getKey(){
		return this.key;
	}

	public String getLotSize() {
		return lotSize;
	}

	public String getName() {
		return this.name;
	}

	public boolean isVegan() {
		return this.vegan;
	}

}
