package net.tcc.money.online.client;

import com.google.gwt.view.client.Range;
import net.tcc.money.online.shared.dto.Article;
import net.tcc.money.online.shared.dto.Category;
import net.tcc.money.online.shared.dto.Purchase;
import net.tcc.money.online.shared.dto.Shop;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ShoppingServiceAsync {

	void loadShops(AsyncCallback<Iterable<Shop>> callback);

	void createShop(String name, AsyncCallback<Shop> callback);

	void loadCategories(AsyncCallback<Iterable<Category>> callback);

	void createPurchase(Purchase purchase, AsyncCallback<Void> callback);

	void loadArticles(AsyncCallback<Iterable<Article>> asyncCallback);

	void createCategory(String name, Category selectedCategory, AsyncCallback<Category> callback);

	void setParentFor(Category category, Category parent, AsyncCallback<Category> asyncCallback);

	void setCategoryFor(Article article, Category category, AsyncCallback<Article> asyncCallback);

    void loadPurchases(Range range, String columnName, boolean ascending, AsyncCallback<List<Purchase>> asyncCallback);

    void loadCategorySpendings(AsyncCallback<Map<Category, BigDecimal>> asyncCallback);
}
