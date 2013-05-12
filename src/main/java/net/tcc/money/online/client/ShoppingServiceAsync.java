package net.tcc.money.online.client;

import com.google.gwt.view.client.Range;
import net.tcc.money.online.shared.dto.Article;
import net.tcc.money.online.shared.dto.Category;
import net.tcc.money.online.shared.dto.Purchase;
import net.tcc.money.online.shared.dto.Shop;

import com.google.gwt.user.client.rpc.AsyncCallback;
import net.tcc.money.online.shared.dto.diagram.MonthlyExpensesData;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ShoppingServiceAsync {

    /** Call to {@link ShoppingService#loadShops()}. */
	void loadShops(AsyncCallback<Iterable<Shop>> callback);

    /** Call to {@link ShoppingService#createShop(String)}. */
	void createShop(String name, AsyncCallback<Shop> callback);

    /** Call to {@link ShoppingService#loadCategories()}. */
	void loadCategories(AsyncCallback<Iterable<Category>> callback);

    /** Call to {@link ShoppingService#createPurchase(net.tcc.money.online.shared.dto.Purchase)}. */
	void createPurchase(Purchase purchase, AsyncCallback<Void> callback);

    /** Call to {@link ShoppingService#loadArticles()}. */
	void loadArticles(AsyncCallback<Iterable<Article>> asyncCallback);

    /** Call to {@link ShoppingService#createCategory(String, net.tcc.money.online.shared.dto.Category)}. */
	void createCategory(String name, Category selectedCategory, AsyncCallback<Category> callback);

    /** Call to {@link ShoppingService#setParentFor(net.tcc.money.online.shared.dto.Category, net.tcc.money.online.shared.dto.Category)}. */
	void setParentFor(Category category, Category parent, AsyncCallback<Category> asyncCallback);

    /** Call to {@link ShoppingService#setCategoryFor(net.tcc.money.online.shared.dto.Article, net.tcc.money.online.shared.dto.Category)}. */
	void setCategoryFor(Article article, Category category, AsyncCallback<Article> asyncCallback);

    /** Call to {@link ShoppingService#loadPurchases(com.google.gwt.view.client.Range, String, boolean)}. */
    void loadPurchases(Range range, String columnName, boolean ascending, AsyncCallback<List<Purchase>> asyncCallback);

    /** Call to {@link ShoppingService#loadCategorySpendings()}. */
    void loadCategorySpendings(AsyncCallback<Map<Category, BigDecimal>> asyncCallback);

    /** Call to {@link ShoppingService#loadMonthlyCategorySpendings()}. */
    void loadMonthlyCategorySpendings(AsyncCallback<MonthlyExpensesData> asyncCallback);
}
