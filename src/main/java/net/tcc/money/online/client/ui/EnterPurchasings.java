package net.tcc.money.online.client.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.tcc.money.online.client.common.BigDecimalBox;
import net.tcc.money.online.client.ui.ArticleSuggestOracle.ArticleSuggestion;
import net.tcc.money.online.shared.dto.Article;
import net.tcc.money.online.shared.dto.Category;
import net.tcc.money.online.shared.dto.Purchase;
import net.tcc.money.online.shared.dto.Purchasing;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class EnterPurchasings extends Composite {

	interface MyUiBinder extends UiBinder<VerticalPanel, EnterPurchasings> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	private final List<Category> categories = new ArrayList<Category>();
	private Purchase purchase;

	@UiField
	Grid purchasings;
	@UiField
	Button cancel;
	@UiField
	Button ok;
	@UiField
	Label errors;

	public EnterPurchasings(final Callback<Purchase, Void> callback, Purchase purchase, Iterable<Article> articles,
			Iterable<Category> categories) {
		this.purchase = purchase;
		initWidget(uiBinder.createAndBindUi(this));

		ArticleSuggestOracle articleNameOracle = new ArticleSuggestOracle();
		MultiWordSuggestOracle brandOracle = new MultiWordSuggestOracle();

		ColumnFormatter columnFormatter = purchasings.getColumnFormatter();
		String[] widths = new String[] { "30%", "20%", "5%", "10%", "10%", "10%", "15%" };
		for (int i = widths.length; i-- > 0;) {
			columnFormatter.setWidth(i, widths[i]);
		}
		purchasings.resizeRows(1 + 10);
		for (int row = purchasings.getRowCount(); --row > 0;) {
			SuggestBox articleSuggestBox = new SuggestBox(articleNameOracle);
			articleSuggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

				private int row;

				@Override
				public void onSelection(SelectionEvent<Suggestion> event) {
					ArticleSuggestion suggestion = (ArticleSuggestion) event.getSelectedItem();
					Article suggestedArticle = suggestion.getArticle();
					((SuggestBox) purchasings.getWidget(row, 1)).setValue(suggestedArticle.getBrand());
					((CheckBox) purchasings.getWidget(row, 2)).setValue(suggestedArticle.isVegan());
					((TextBox) purchasings.getWidget(row, 3)).setValue(suggestedArticle.getLotSize());
					((FocusWidget) purchasings.getWidget(row, 3)).setFocus(true);
					int i = EnterPurchasings.this.categories.indexOf(suggestedArticle.getCategory());
					if (i >= 0) {
						((ListBox) purchasings.getWidget(row, 6)).setSelectedIndex(i + 1);
					}
				}

				public SelectionHandler<SuggestOracle.Suggestion> setRow(int row) {
					this.row = row;
					return this;
				}
			}.setRow(row));
			purchasings.setWidget(row, 0, articleSuggestBox);
			purchasings.setWidget(row, 1, new SuggestBox(brandOracle));
			purchasings.setWidget(row, 2, new CheckBox());
			TextBox lotSize = new TextBox();
			lotSize.setStyleName("purchasing-lotSize");
			purchasings.setWidget(row, 3, lotSize);
			BigDecimalBox quantity = new BigDecimalBox();
			quantity.setStyleName("purchasing-quantity");
			purchasings.setWidget(row, 4, quantity);
			BigDecimalBox price = new BigDecimalBox();
			price.setStyleName("purchasing-price");
			purchasings.setWidget(row, 5, price);
			purchasings.setWidget(row, 6, new ListBox());
		}
		initCategorySelectBoxes(categories);
		initSuggestOracles(articleNameOracle, brandOracle, articles);

		cancel.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				callback.onFailure(null);
			}
		});
		ok.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				callback.onSuccess(fillPurchase());
			}
		});
	}

	private void initCategorySelectBoxes(Iterable<Category> result) {
		for (int row = purchasings.getRowCount(); --row > 0;) {
			((ListBox) purchasings.getWidget(row, 6)).addItem("");
		}
		for (Category category : result) {
			categories.add(category);
			for (int row = purchasings.getRowCount(); --row > 0;) {
				((ListBox) purchasings.getWidget(row, 6))
						.addItem(category.getName(), String.valueOf(category.getKey()));
			}
		}
	}

	private void initSuggestOracles(ArticleSuggestOracle articleNameOracle, MultiWordSuggestOracle brandOracle,
			Iterable<Article> articles) {
		for (Article article : articles) {
			articleNameOracle.add(article);
			String brand = article.getBrand();
			if (brand != null) {
				brandOracle.add(brand);
			}
		}
	}

	private Purchase fillPurchase() {
		for (int row = purchasings.getRowCount(); --row > 0;) {
			String articleName = ((SuggestBox) purchasings.getWidget(row, 0)).getValue();
			if ("".equals(articleName))
				continue;
			Article article = new Article(articleName, ((SuggestBox) purchasings.getWidget(row, 1)).getValue(),
					((CheckBox) purchasings.getWidget(row, 2)).getValue(),
					((TextBox) purchasings.getWidget(row, 3)).getValue());
			BigDecimal price = ((BigDecimalBox) purchasings.getWidget(row, 5)).getValue();
			if (price == null)
				continue;
			Purchasing purchasing = new Purchasing(article, ((BigDecimalBox) purchasings.getWidget(row, 4)).getValue(),
					price, getSelectedCategory(((ListBox) purchasings.getWidget(row, 6))));
			this.purchase.add(purchasing);
		}
		return this.purchase;
	}

	private Category getSelectedCategory(ListBox listBox) {
		int selectedIndex = listBox.getSelectedIndex() - 1;
		return selectedIndex < 0 ? null : this.categories.get(selectedIndex);
	}

}
