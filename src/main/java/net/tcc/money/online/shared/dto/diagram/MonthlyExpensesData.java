package net.tcc.money.online.shared.dto.diagram;

import com.google.gwt.user.client.rpc.IsSerializable;
import net.tcc.money.online.shared.dto.Category;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthlyExpensesData implements IsSerializable {

    private List<Date> months;

    @SuppressWarnings("Convert2Diamond")
    private Map<Category, List<BigDecimal>> categories = new HashMap<Category, List<BigDecimal>>();

    public Iterable<Category> getCategories() {
        return categories.keySet();
    }

    public int getNumberOfRows() {
        return this.months.size();
    }

    public Iterable<BigDecimal> getDataFor(Category category) {
        return categories.get(category);
    }

    public Iterable<Date> getMonths() {
        return months;
    }

    public void addMonths(List<Date> dates) {
        months = dates;
    }

    public void addCategory(Category category, List<BigDecimal> sums) {
        if (getNumberOfRows() != sums.size())
            throw new IllegalArgumentException("The number of sums must be " + getNumberOfRows() + "!");
        this.categories.put(category, sums);
    }

}
