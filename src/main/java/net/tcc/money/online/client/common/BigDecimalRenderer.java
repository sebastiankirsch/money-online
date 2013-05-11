package net.tcc.money.online.client.common;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.AbstractRenderer;

import java.math.BigDecimal;

public class BigDecimalRenderer extends AbstractRenderer<BigDecimal> {

    public static final BigDecimalRenderer INSTANCE = new BigDecimalRenderer();

    private BigDecimalRenderer() {
    }

    public String render(BigDecimal object) {
        if (null == object) {
            return "";
        }

        return NumberFormat.getDecimalFormat().format(object);
    }

}
