package net.tcc.money.online.client.ui;

import net.tcc.money.online.shared.dto.Purchase;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ListPurchases extends Composite {

	interface MyUiBinder extends UiBinder<VerticalPanel, ListPurchases> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	public ListPurchases(Iterable<Purchase> purchases) {
		VerticalPanel panel = uiBinder.createAndBindUi(this);
		initWidget(panel);

		for (Purchase purchase : purchases) {
			panel.add(new Label(purchase.getShop().getName() + " am " + purchase.getPurchaseDate() + " für " + purchase.getTotal() +"€"));
		}

	}

}
