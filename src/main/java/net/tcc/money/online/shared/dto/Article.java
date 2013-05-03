package net.tcc.money.online.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Article extends DtoWithLongKey implements IsSerializable {

    private String name;

    private String brand;

    private boolean vegan;

    private String lotSize;

    private Category category;

    @Deprecated
    @SuppressWarnings({"deprecation", "unused"}) // for GWT
    private Article() {
        super();
    }

    public Article(Long key, String name, String brand, boolean vegan, String lotSize) {
        super(key);
        this.name = name;
        this.brand = brand;
        this.vegan = vegan;
        this.lotSize = lotSize;
    }

    public Article(String name, String brand, boolean vegan, String lotSize) {
        this(null, name, brand, vegan, lotSize);
    }

    public String toString() {
        return super.toString() + " [" + this.name + "/" + this.brand + "]";
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
