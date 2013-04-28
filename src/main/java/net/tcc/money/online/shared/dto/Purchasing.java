package net.tcc.money.online.shared.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Purchasing implements IsSerializable {

	private Article article;

	private BigDecimal quantity;

	private BigDecimal price;

	private Category category;
	
	@SuppressWarnings("unused")
	private Purchasing(){
		// for GWT
	}

	public Purchasing(Article article, BigDecimal quantity, BigDecimal price, Category category) {
		this.article = article;
		this.quantity = quantity.setScale(3, RoundingMode.HALF_UP);
		this.price = price.setScale(2, RoundingMode.HALF_UP);
		this.category = category;
	}

	public Article getArticle() {
		return article;
	}

	public Category getCategory() {
		return category;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setArticle(Article article) {
		this.article = article;
	}

}
