package net.tcc.money.online.shared.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Purchase implements IsSerializable, Iterable<Purchasing> {

    private Shop shop;

    private Date purchaseDate;

    private List<Purchasing> purchasings = new ArrayList<>();

    @Deprecated
    @SuppressWarnings("unused")
    private Purchase() {
        // for GWT
    }

    public Purchase(Shop shop, Date purchaseDate) {
        this.shop = shop;
        this.purchaseDate = new Date(purchaseDate.getTime());
    }

    @Override
    public String toString() {
        return "Purchase @" + shop.getName();
    }

    @Override
    public Iterator<Purchasing> iterator() {
        return this.purchasings.iterator();
    }

    public void add(Purchasing purchasing) {
        this.purchasings.add(purchasing);
    }

    public void addAll(Iterable<Purchasing> purchasings) {
        for (Purchasing purchasing : purchasings)
            add(purchasing);
    }

    public Shop getShop() {
        return shop;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public BigDecimal getTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Purchasing purchasing : this) {
            sum = sum.add(purchasing.getPrice());
        }
        return sum;
    }

}
