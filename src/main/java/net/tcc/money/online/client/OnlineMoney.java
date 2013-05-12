package net.tcc.money.online.client;

import net.tcc.money.online.client.ui.*;
import net.tcc.money.online.client.ui.diagram.TotalExpenses;
import net.tcc.money.online.shared.dto.Article;
import net.tcc.money.online.shared.dto.Category;
import net.tcc.money.online.shared.dto.Purchase;
import net.tcc.money.online.shared.dto.Shop;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import java.math.BigDecimal;
import java.util.Map;

public class OnlineMoney implements EntryPoint, ExceptionHandler {

    private final ShoppingServiceAsync shoppingService = GWT.create(ShoppingService.class);

    public void onModuleLoad() {
        setContent(new Label("Anwendung wird geladen..."));
        initNavigation();
        startPurchase();
    }

    private void initNavigation() {
        RootPanel navigationPanel = RootPanel.get("navigationPanel");
        navigationPanel.add(new Navigation(new Runnable() {
            @Override
            public void run() {
                startPurchase();
            }
        }, new Runnable() {
            @Override
            public void run() {
                manageCategories();
            }
        }, new Runnable() {
            @Override
            public void run() {
                listPurchases();
            }
        }, new Runnable() {
            @Override
            public void run() {
                displayTotalExpenses();
            }
        }
        ));
    }

    void startPurchase() {
        setContent(new Label("Läden werden geladen..."));
        shoppingService.loadShops(new AsyncCallback<Iterable<Shop>>() {
            @Override
            public void onSuccess(Iterable<Shop> result) {
                setContent(new CreatePurchase(new Callback<Purchase, Void>() {
                    @Override
                    public void onSuccess(Purchase result) {
                        startEnteringPurchasings(result);
                    }

                    @Override
                    public void onFailure(Void reason) {
                        throw new RuntimeException("Who calls me? I said: who is calling me?!?");
                    }
                }, shoppingService, result));
            }

            @Override
            public void onFailure(Throwable caught) {
                handleException("Konnte die Läden nicht laden!", caught);
            }
        });
    }

    void startEnteringPurchasings(final Purchase purchase) {
        setContent(new Label("Einkauf wird initialisiert..."));
        new ArticlesAndCategoriesLoader(this.shoppingService) {
            @Override
            protected void done(Iterable<Article> articles, Iterable<Category> categories) {
                setContent(new EnterPurchasings(new Callback<Purchase, Void>() {
                    @Override
                    public void onSuccess(Purchase result) {
                        setContent(new Label("Einkauf wird gespeichert..."));
                        shoppingService.createPurchase(result, new AsyncCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                setContent(new Label("Einkauf wurde gespeichert!"));
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                handleException("Konnte Einkauf nicht speichern!", caught);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Void reason) {
                        startPurchase();
                    }
                }, purchase, articles, categories));
            }
        }.load();
    }

    void manageCategories() {
        setContent(new Label("Artikel & Kategorien werden geladen..."));
        new ArticlesAndCategoriesLoader(this.shoppingService) {

            @Override
            protected void done(Iterable<Article> articles, Iterable<Category> categories) {
                setContent(new ManageCategories(OnlineMoney.this, shoppingService, articles, categories));
            }
        }.load();
    }

    void listPurchases() {
        setContent(new ListPurchases(this.shoppingService, this));
    }

    void displayTotalExpenses() {
        setContent(new Label("Daten werden geladen..."));
        this.shoppingService.loadCategorySpendings(new AsyncCallback<Map<Category, BigDecimal>>() {

            @Override
            public void onFailure(Throwable caught) {
                handleException("Konnte Daten nicht laden!", caught);
            }

            @Override
            public void onSuccess(Map<Category, BigDecimal> data) {
                setContent(new TotalExpenses(data));
            }

        });
    }

    void setContent(IsWidget widget) {
        RootPanel rootPanel = RootPanel.get("contentPanel");
        rootPanel.clear();
        rootPanel.add(widget);
    }

    @Override
    public void handleException(String message, Throwable caught) {
        StringBuilder buffy = new StringBuilder(message);
        buffy.append("<br/>");
        buffy.append(caught);
        for (StackTraceElement ste : caught.getStackTrace()) {
            buffy.append("<br/>");
            buffy.append(ste.toString());
        }
        setContent(new Label(buffy.toString()));
    }

    private abstract class ArticlesAndCategoriesLoader {
        private final ShoppingServiceAsync shoppingService;
        private Iterable<Article> articles;
        private Iterable<Category> categories;

        protected ArticlesAndCategoriesLoader(ShoppingServiceAsync shoppingService) {
            this.shoppingService = shoppingService;
        }

        private synchronized void executeDoneIfEverythingIsLoaded() {
            if (this.articles == null || this.categories == null)
                return;
            done(articles, categories);
        }

        void setArticles(Iterable<Article> articles) {
            this.articles = articles;
            executeDoneIfEverythingIsLoaded();
        }

        void setCategories(Iterable<Category> categories) {
            this.categories = categories;
            executeDoneIfEverythingIsLoaded();
        }

        protected abstract void done(Iterable<Article> articles, Iterable<Category> categories);

        public void load() {
            this.shoppingService.loadArticles(new AsyncCallback<Iterable<Article>>() {

                @Override
                public void onFailure(Throwable caught) {
                    handleException("Konnte Artikel nicht laden!", caught);
                }

                @Override
                public void onSuccess(Iterable<Article> articles) {
                    setArticles(articles);
                }

            });
            shoppingService.loadCategories(new AsyncCallback<Iterable<Category>>() {

                @Override
                public void onFailure(Throwable caught) {
                    handleException("Konnte Kategorien nicht laden!", caught);
                }

                @Override
                public void onSuccess(Iterable<Category> categories) {
                    setCategories(categories);
                }

            });
        }
    }

}
