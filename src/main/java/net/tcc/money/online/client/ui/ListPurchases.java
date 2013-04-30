package net.tcc.money.online.client.ui;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import net.tcc.money.online.client.ExceptionHandler;
import net.tcc.money.online.client.ShoppingServiceAsync;
import net.tcc.money.online.shared.dto.Purchase;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class ListPurchases extends Composite {

    interface MyUiBinder extends UiBinder<VerticalPanel, ListPurchases> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField
    CellTable<Purchase> purchases;

    @UiField
    SimplePager pager;

    public ListPurchases(ShoppingServiceAsync shoppingService, ExceptionHandler exceptionHandler) {
        initWidget(uiBinder.createAndBindUi(this));

        pager.setDisplay(this.purchases);
        initColumns();
        setUpDataProvider(shoppingService, exceptionHandler);
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
        dateColumn.setDataStoreName("purchaseDate");
        this.purchases.addColumn(dateColumn, "Datum");
        this.purchases.addColumn(new TextColumn<Purchase>() {
            @Override
            public String getValue(Purchase purchase) {
                return purchase.getShop().getName();
            }
        }, "Laden");
        this.purchases.addColumn(new Column<Purchase, Number>(new NumberCell()) {
            @Override
            public BigDecimal getValue(Purchase purchase) {
                return purchase.getTotal();
            }
        }, "Summe");

        ColumnSortEvent.AsyncHandler columnSortHandler = new ColumnSortEvent.AsyncHandler(this.purchases);
        this.purchases.addColumnSortHandler(columnSortHandler);
        this.purchases.getColumnSortList().push(dateColumn);
    }

    private void setUpDataProvider(final ShoppingServiceAsync shoppingService, final ExceptionHandler exceptionHandler) {
        AsyncDataProvider<Purchase> dataProvider = new AsyncDataProvider<Purchase>() {

            @Override
            protected void onRangeChanged(HasData<Purchase> display) {
                final Range range = display.getVisibleRange();
                ColumnSortList.ColumnSortInfo columnSortInfo = ListPurchases.this.purchases.getColumnSortList().get(0);

                shoppingService.loadPurchases(range, columnSortInfo.getColumn().getDataStoreName(), columnSortInfo.isAscending(), new AsyncCallback<List<Purchase>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        exceptionHandler.handleException("Konnte die Einkäufe nicht laden!", caught);
                    }

                    @Override
                    public void onSuccess(List<Purchase> result) {
                        ListPurchases.this.purchases.setRowData(range.getStart(), result);
                        adaptRowCountIfApplicable(result);
                    }

                    private void adaptRowCountIfApplicable(List<Purchase> result) {
                        boolean endOfDataReached = result.size() < range.getLength();
                        int purchaseCount = range.getStart() + result.size();
                        if (endOfDataReached || purchaseCount > ListPurchases.this.purchases.getRowCount()) {
                            ListPurchases.this.purchases.setRowCount(purchaseCount, endOfDataReached);
                        }
                    }

                });

            }
        };
        dataProvider.addDataDisplay(this.purchases);
    }

}
