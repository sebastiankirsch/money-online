package net.tcc.money.online.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.PieChart;
import net.tcc.money.online.shared.dto.Category;

import java.math.BigDecimal;
import java.util.Map;

import static com.google.gwt.visualization.client.AbstractDataTable.ColumnType.NUMBER;
import static com.google.gwt.visualization.client.AbstractDataTable.ColumnType.STRING;

public class Diagram extends Composite {

    private final HTMLPanel panel;

    static interface MyUiBinder extends UiBinder<HTMLPanel, Diagram> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    public Diagram(Map<Category, BigDecimal> data) {
        panel = uiBinder.createAndBindUi(this);
        initWidget(panel);
        VisualizationUtils.loadVisualizationApi(createPieChart(data), PieChart.PACKAGE);
    }

    private Runnable createPieChart(final Map<Category, BigDecimal> data) {
        return new Runnable() {
            @Override
            public void run() {
                PieChart chart = new PieChart(createData(data), createOptions());
                chart.setWidth("100%");
                chart.setHeight("100%");
                panel.add(chart);
            }

            private AbstractDataTable createData(Map<Category, BigDecimal> data) {
                DataTable dataTable = DataTable.create();
                dataTable.addColumn(STRING, "Kategorie");
                dataTable.addColumn(NUMBER, "Ausgaben [€]");
                dataTable.addRows(data.size());
                int row = 0;
                for (Map.Entry<Category, BigDecimal> entry : data.entrySet()) {
                    dataTable.setValue(row, 0, entry.getKey() == null ? "Nicht zugeordnet" : entry.getKey().getName());
                    dataTable.setValue(row++, 1, entry.getValue().doubleValue());
                }
                return dataTable;
            }

            private PieChart.Options createOptions() {
                PieChart.Options options = PieChart.Options.create();
                options.setTitle("Gesamtausgaben nach Kategorie");
                options.setWidth(800);
                options.setHeight(600);
                return options;
            }
        };
    }

}
