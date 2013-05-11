package net.tcc.money.online.client.common;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.Parser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

public class BigDecimalParser implements Parser<BigDecimal> {

    public static final BigDecimalParser INSTANCE = new BigDecimalParser();

    private BigDecimalParser() {
    }

    public BigDecimal parse(CharSequence object) throws ParseException {
        String text = object.toString();
        if ("".equals(text)) {
            return null;
        }

        try {
            BigDecimal bigDecimal = new BigDecimal(NumberFormat.getDecimalFormat().parse(text));
            bigDecimal = bigDecimal.setScale(guessScale(text), RoundingMode.HALF_UP);
            return bigDecimal;
        } catch (NumberFormatException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    private static int guessScale(String text) {
        for (int i = text.length(); i-- > 0; ) {
            if (!Character.isDigit(text.charAt(i))) {
                return (text.length() - 1) - i;
            }
        }
        return 0;
    }

}
