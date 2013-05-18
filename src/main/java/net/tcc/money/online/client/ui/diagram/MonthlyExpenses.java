package net.tcc.money.online.client.ui.diagram;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AreaChart;
import net.tcc.money.online.shared.dto.Category;
import net.tcc.money.online.shared.dto.diagram.MonthlyExpensesData;

import java.math.BigDecimal;
import java.util.Date;

import static com.google.gwt.visualization.client.AbstractDataTable.ColumnType.NUMBER;
import static com.google.gwt.visualization.client.AbstractDataTable.ColumnType.STRING;
import static com.google.gwt.visualization.client.visualizations.AreaChart.Options;
import static com.google.gwt.visualization.client.visualizations.AreaChart.PACKAGE;

public class MonthlyExpenses extends VerticalPanel {

    public MonthlyExpenses(MonthlyExpensesData data) {
        super();
        VisualizationUtils.loadVisualizationApi(createChart(data), PACKAGE);
    }

    private Runnable createChart(final MonthlyExpensesData data) {
        return new Runnable() {

            @Override
            public void run() {
                AreaChart chart = new AreaChart(createData(data), createOptions());
                chart.setWidth("100%");
                chart.setHeight("100%");
                add(chart);
            }

            private AbstractDataTable createData(MonthlyExpensesData data) {
                DateTimeFormat monthFormat = DateTimeFormat.getFormat("y/M");
                DataTable dataTable = DataTable.create();
                dataTable.addRows(data.getNumberOfRows());
                dataTable.addColumn(STRING, "Zeitraum");
                for (Category category : data.getCategories()) {
                    dataTable.addColumn(NUMBER, category == null ? "Nicht zugeordnet" : category.getName());
                }
                int row = 0;
                for (Date month : data.getMonths()) {
                    dataTable.setValue(row++, 0, monthFormat.format(month));
                }
                int column = 0;
                for (Category category : data.getCategories()) {
                    column++;
                    row = 0;
                    for (BigDecimal sum : data.getDataFor(category)) {
                        dataTable.setValue(row++, column, sum.doubleValue());
                    }
                }

                return dataTable;
            }

            private Options createOptions() {
                Options options = Options.create();
                options.setTitle("Monatliche Ausgaben nach Kategorie");
                options.setTitleX("Monat");
                options.setTitleY("Ausgaben");
                options.setWidth(800);
                options.setHeight(600);
                options.setStacked(true);
                options.setMin(0);
                return options;
            }
        };
    }

}
