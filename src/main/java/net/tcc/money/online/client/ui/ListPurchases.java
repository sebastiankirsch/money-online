package net.tcc.money.online.client.ui;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import net.tcc.money.online.shared.dto.Purchase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

public class ListPurchases extends Composite {

	interface MyUiBinder extends UiBinder<VerticalPanel, ListPurchases> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField
    CellTable<Purchase> purchases;

    @UiField
    SimplePager pager;

	public ListPurchases(Iterable<Purchase> purchases) {
        initWidget(uiBinder.createAndBindUi(this));

        pager.setDisplay(this.purchases);

        initColumns();

        final ArrayList<Purchase> values = new ArrayList<Purchase>();
        for(Purchase p:purchases)
        values.add(p);

        setUpDataProvider(values);
    }

    private void initColumns() {
        Column<Purchase, Date> dateColumn = new Column<Purchase, Date>(new DateCell()) {
            @Override
            public Date getValue(Purchase purchase) {
                return purchase.getPurchaseDate();
            }
        };
        dateColumn.setSortable(true);
        dateColumn.setDefaultSortAscending(false);
        this.purchases.addColumn(dateColumn, "Datum");
        TextColumn<Purchase> shopColumn = new TextColumn<Purchase>() {
            @Override
            public String getValue(Purchase purchase) {
                return purchase.getShop().getName();
            }
        };
        shopColumn.setSortable(true);
        this.purchases.addColumn(shopColumn, "Laden");
        this.purchases.addColumn(new Column<Purchase, Number>(new NumberCell()) {
            @Override
            public BigDecimal getValue(Purchase purchase) {
                return purchase.getTotal();
            }
        }, "Summe");

        ColumnSortEvent.AsyncHandler columnSortHandler = new ColumnSortEvent.AsyncHandler(this.purchases);
        this.purchases.addColumnSortHandler(columnSortHandler);
    }

    private void setUpDataProvider(final ArrayList<Purchase> values) {
        AsyncDataProvider<Purchase> dataProvider = new AsyncDataProvider<Purchase>() {

            @Override
            protected void onRangeChanged(HasData<Purchase> display) {
                final Range range = display.getVisibleRange();
                int start = range.getStart();
                int end = start + range.getLength();

                final ColumnSortList sortList = ListPurchases.this.purchases.getColumnSortList();

                ArrayList<Purchase> data = new ArrayList<Purchase>(end - start);
                for (int i = start; i < end && values.size()>i;i++){
                    data.add(values.get(i));
                }


                ListPurchases.this.purchases.setRowData(start, data);
                ListPurchases.this.purchases.setRowCount(values.size());
            }
        };
        dataProvider.addDataDisplay(this.purchases);
    }

}
