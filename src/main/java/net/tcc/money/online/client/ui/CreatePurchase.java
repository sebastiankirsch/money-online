package net.tcc.money.online.client.ui;

import static com.google.gwt.user.client.ui.HasHorizontalAlignment.ALIGN_LOCALE_END;
import static com.google.gwt.user.client.ui.HasHorizontalAlignment.ALIGN_LOCALE_START;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.*;
import net.tcc.money.online.client.ShoppingServiceAsync;
import net.tcc.money.online.shared.dto.Purchase;
import net.tcc.money.online.shared.dto.Shop;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class CreatePurchase extends Composite {

    interface MyUiBinder extends UiBinder<VerticalPanel, CreatePurchase> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private final ShoppingServiceAsync shoppingService;
    @SuppressWarnings("Convert2Diamond")
    private final List<Shop> shops = new ArrayList<Shop>();

    private final DialogBox newShopDialog = new DialogBox(false, true);
    @UiField
    ListBox selectedShop;
    @UiField
    Button newShop;
    @UiField
    DateBox purchaseDate;
    @UiField
    Button ok;
    @UiField
    Label errors;

    public CreatePurchase(final Callback<Purchase, Void> callback, ShoppingServiceAsync shoppingService,
                          Iterable<Shop> result) {
        this.shoppingService = shoppingService;
        initWidget(uiBinder.createAndBindUi(this));
        for (Shop shop : result) {
            shops.add(shop);
            selectedShop.addItem(shop.getName(), String.valueOf(shop.getKey()));
        }
        this.purchaseDate.setValue(new Date());
        this.purchaseDate.setFormat(new DateBox.DefaultFormat(DateTimeFormat
                .getFormat(DateTimeFormat.PredefinedFormat.DATE_LONG)));

        final TextBox shopName = setUpNewShopDialog();
        newShop.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                newShopDialog.center();
                shopName.setFocus(true);
                newShopDialog.show();
            }
        });
        ok.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                callback.onSuccess(new Purchase(getSelectedShop(), purchaseDate.getValue()));
            }
        });
    }

    private Shop getSelectedShop() {
        return shops.get(selectedShop.getSelectedIndex());
    }

    private TextBox setUpNewShopDialog() {
        newShopDialog.setText("Neuen Laden anlegen");
        newShopDialog.setAnimationEnabled(true);
        final Button closeButton = new Button("Abbrechen");
        // We can set the id of a widget by accessing its Element
        closeButton.getElement().setId("closeButton");
        VerticalPanel dialogVPanel = new VerticalPanel();
        dialogVPanel.addStyleName("dialogVPanel");
        dialogVPanel.add(new HTML("Bitte gib den Namen des Ladens ein"));
        final TextBox shopName = new TextBox();
        shopName.setWidth("auto");
        dialogVPanel.add(shopName);

        HorizontalPanel dialogHPanel = new HorizontalPanel();
        dialogHPanel.setHorizontalAlignment(ALIGN_LOCALE_START);
        dialogHPanel.add(closeButton);
        final Button createShop = new Button("Anlegen");
        dialogHPanel.setHorizontalAlignment(ALIGN_LOCALE_START);
        dialogHPanel.add(createShop);
        dialogVPanel.setHorizontalAlignment(ALIGN_LOCALE_END);
        dialogVPanel.add(dialogHPanel);
        newShopDialog.setWidget(dialogVPanel);

        shopName.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
                    saveShop(createShop, closeButton, shopName);
                }
            }
        });

        createShop.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                saveShop(createShop, closeButton, shopName);
            }
        });

        closeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                selectedShop.setFocus(true);
                newShopDialog.hide();
            }
        });
        return shopName;
    }

    void saveShop(final Button createShop, final Button closeButton, final TextBox shopName) {
        createShop.setEnabled(false);
        closeButton.setEnabled(false);

        shoppingService.createShop(shopName.getText(), new AsyncCallback<Shop>() {
            @Override
            public void onFailure(Throwable caught) {
                newShopDialog.hide();
                createShop.setEnabled(true);
                closeButton.setEnabled(true);
                errors.setText("Laden konnte nicht angelegt werden!\n" + caught);
            }

            @Override
            public void onSuccess(Shop result) {
                int index = shops.size();
                shops.add(result);
                selectedShop.addItem(result.getName(), String.valueOf(result.getKey()));
                selectedShop.setSelectedIndex(index);
                purchaseDate.setFocus(true);
                newShopDialog.hide();
                createShop.setEnabled(true);
                closeButton.setEnabled(true);
                shopName.setText("");
            }
        });
    }

}
