package net.tcc.money.online.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.view.client.Range;
import net.tcc.money.online.shared.dto.Article;
import net.tcc.money.online.shared.dto.Category;
import net.tcc.money.online.shared.dto.Purchase;
import net.tcc.money.online.shared.dto.Shop;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RemoteServiceRelativePath("shoppingService")
public interface ShoppingService extends RemoteService {

    Iterable<Shop> loadShops();

    Shop createShop(String name);

    Iterable<Category> loadCategories();

    void createPurchase(Purchase purchase);

    Iterable<Article> loadArticles();

    Category createCategory(String name, Category category);

    Category setParentFor(Category category, Category parent);

    Article setCategoryFor(Article article, Category category);

    List<Purchase> loadPurchases(Range range, String columnName, boolean ascending);

    Map<Category, BigDecimal> loadCategorySpendings();

}
