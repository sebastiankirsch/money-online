package net.tcc.money.online.client.ui;

import java.util.HashMap;
import java.util.Map;

import net.tcc.money.online.client.ExceptionHandler;
import net.tcc.money.online.client.ShoppingServiceAsync;
import net.tcc.money.online.shared.dto.Article;
import net.tcc.money.online.shared.dto.Category;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ManageCategories extends Composite {

	interface MyUiBinder extends UiBinder<VerticalPanel, ManageCategories> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	private final ExceptionHandler exceptionHandler;
	private final ShoppingServiceAsync shoppingService;
	private final HashMap<Long, TreeItem> categories = new HashMap<Long, TreeItem>();
	private final HashMap<Long, TreeItem> articles = new HashMap<Long, TreeItem>();

	@UiField
	Tree categoryTree;

	@UiField
	Button newCategory;

	public ManageCategories(ExceptionHandler exceptionHandler, ShoppingServiceAsync shoppingService,
			Iterable<Article> articles, Iterable<Category> categories) {
		this.exceptionHandler = exceptionHandler;
		this.shoppingService = shoppingService;
		initWidget(uiBinder.createAndBindUi(this));

		TreeItem root = initRootNode();
		initCategories(root, categories);
		initArticles(articles);
		root.setState(true);

		setUpSelectionHandlerForCategoryTree();
		setUpClickHandlerForNewCategory();
	}

	private TreeItem initRootNode() {
		InlineLabel label = new InlineLabel("Kategorien");
		label.addStyleName("bold");
		label.addStyleName("italic");
		TreeItem root = categoryTree.addItem(label);
		addDropHandling(root);
		this.categories.put(null, root);
		return root;
	}

	private void initCategories(TreeItem root, Iterable<Category> categories) {
		Map<Category, TreeItem> categoriesToTreeItems = new HashMap<Category, TreeItem>();
		for (Category category : categories) {
			TreeItem treeItem = getTreeItem(root, categoriesToTreeItems, category);
			this.categories.put(category.getKey(), treeItem);
		}
	}

	private TreeItem getTreeItem(TreeItem root, Map<Category, TreeItem> categoriesToTreeItems, final Category category) {
		if (category == null)
			return root;
		TreeItem treeItem = categoriesToTreeItems.get(category);
		if (treeItem != null)
			return treeItem;
		TreeItem parentItem = getTreeItem(root, categoriesToTreeItems, category.getParent());

		TreeItem categoryItem = createTreeItemFor(category);
		categoriesToTreeItems.put(category, categoryItem);
		parentItem.addItem(categoryItem);
		return categoryItem;
	}

	private TreeItem createTreeItemFor(final Category category) {
		final Label label = new InlineLabel(category.getName());
		label.addStyleName("bold");
		final TreeItem categoryItem = new TreeItem(label);
		categoryItem.setUserObject(category);
		label.getElement().setDraggable(Element.DRAGGABLE_TRUE);
		label.addDragStartHandler(new DragStartHandler() {
			@Override
			public void onDragStart(DragStartEvent event) {
				event.setData("application/category.key", String.valueOf(category.getKey()));
				event.getDataTransfer().setDragImage(label.getElement(), 10, 10);
			}
		});
		addDropHandling(categoryItem);

		return categoryItem;
	}

	private void addDropHandling(final TreeItem treeItem) {
		Label label = (Label) treeItem.getWidget();
		label.addDragOverHandler(new DragOverHandler() {
			@Override
			public void onDragOver(DragOverEvent event) {
			}
		});

		label.addDropHandler(new DropHandler() {
			@Override
			public void onDrop(DropEvent event) {
				event.preventDefault();
				String data = event.getData("application/category.key");
				if (data != null && data.length() >0){
				TreeItem droppedCategory = categories.get(Long.parseLong(data));
				handleCategoryDrop(droppedCategory, treeItem); return;}
				data = event.getData("application/article.key"); 
				if (data != null && data.length() >0){
				TreeItem droppedArticle = articles.get(Long.parseLong(data));
				handleArticleDrop(droppedArticle, treeItem); return;}
			}
		});
	}

	void handleCategoryDrop(final TreeItem sourceItem, final TreeItem targetItem) {
		Category source = (Category) sourceItem.getUserObject();
		Category target = (Category) targetItem.getUserObject();
		if (source.equals(target) || target != null && target.isChildOf(source))
			return;
		final Category formerParent = source.getParent();
		shoppingService.setParentFor(source, target, new AsyncCallback<Category>() {
			@Override
			public void onSuccess(Category result) {
				sourceItem.setUserObject(result);
				Long formerParentKey = formerParent == null ? null : formerParent.getKey();
				categories.get(formerParentKey).removeItem(sourceItem);
				targetItem.addItem(sourceItem);
			}

			@Override
			public void onFailure(Throwable caught) {
				exceptionHandler.handleException("Kategorie konnte nicht verschoben werden!", caught);
			}
		});
	}

	void handleArticleDrop(final TreeItem sourceItem, final TreeItem targetItem) {
		Article source = (Article) sourceItem.getUserObject();
		Category target = (Category) targetItem.getUserObject();
		final Category formerParent = source.getCategory();
		shoppingService.setCategoryFor(source, target, new AsyncCallback<Article>() {
			@Override
			public void onSuccess(Article result) {
				sourceItem.setUserObject(result);
				Long formerParentKey = formerParent == null ? null : formerParent.getKey();
				categories.get(formerParentKey).removeItem(sourceItem);
				targetItem.addItem(sourceItem);
			}

			@Override
			public void onFailure(Throwable caught) {
				exceptionHandler.handleException("Artikel konnte nicht verschoben werden!", caught);
			}
		});
	}

	private void initArticles(Iterable<Article> articles) {
		for (Article article : articles) {
			TreeItem articleItem = createTreeItemFor(article);
			this.articles.put(article.getKey(), articleItem);
			Long categoryKey = article.getCategory() == null ? null : article.getCategory().getKey();
			this.categories.get(categoryKey).addItem(articleItem);
		}
	}

	private TreeItem createTreeItemFor(final Article article) {
		final Label label = new InlineLabel(article.getName());
		final TreeItem categoryItem = new TreeItem(label);
		categoryItem.setUserObject(article);
		label.getElement().setDraggable(Element.DRAGGABLE_TRUE);
		label.addDragStartHandler(new DragStartHandler() {
			@Override
			public void onDragStart(DragStartEvent event) {
				event.setData("application/article.key", String.valueOf(article.getKey()));
				event.getDataTransfer().setDragImage(label.getElement(), 10, 10);
			}
		});
		return categoryItem;
	}

	private void setUpSelectionHandlerForCategoryTree() {
		this.categoryTree.addSelectionHandler(new SelectionHandler<TreeItem>() {

			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				newCategory.setEnabled(!(event.getSelectedItem().getUserObject() instanceof Article));
			}
		});
	}

	private void setUpClickHandlerForNewCategory() {
		this.newCategory.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final TreeItem parentItem = categoryTree.getSelectedItem();
				Category selectedCategory = (Category) parentItem.getUserObject();

				String name = Window.prompt("Name für die neue Kategorie", null);
				if (name == null)
					return;
				shoppingService.createCategory(name, selectedCategory, new AsyncCallback<Category>() {
					@Override
					public void onSuccess(Category result) {
						parentItem.addItem(createTreeItemFor(result));
						parentItem.setState(true);
					}

					@Override
					public void onFailure(Throwable caught) {
						exceptionHandler.handleException("Kategorie konnte nicht angelegt werden!", caught);
					}
				});
			}
		});
	}

}
