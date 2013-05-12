package net.tcc.money.online.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Navigation extends Composite {

    interface MyUiBinder extends UiBinder<VerticalPanel, Navigation> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField
    Anchor newPurchase;

    @UiField
    Anchor manageCategories;

    @UiField
    Anchor listPurchases;

    @UiField
    Anchor totalExpenses;

    public Navigation(Runnable newPurchaseAction, Runnable manageCategoriesAction, Runnable listPurchasesAction, Runnable totalExpensesAction) {
        initWidget(uiBinder.createAndBindUi(this));

        executeOnClick(this.newPurchase, newPurchaseAction);
        executeOnClick(this.manageCategories, manageCategoriesAction);
        executeOnClick(this.listPurchases, listPurchasesAction);
        executeOnClick(this.totalExpenses, totalExpensesAction);
    }

    private static void executeOnClick(Anchor anchor, final Runnable action) {
        anchor.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                action.run();
            }
        });
    }

}
