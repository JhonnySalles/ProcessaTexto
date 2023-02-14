package org.jisho.textosJapones.components;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import javafx.util.converter.NumberStringConverter;

public class NumberToStringConverter extends NumberStringConverter {
    public NumberToStringConverter() {
        super();
    }

    public NumberToStringConverter(Locale locale, String pattern) {
        super(locale, pattern);
    }

    public NumberToStringConverter(Locale locale) {
        super(locale);
    }

    public NumberToStringConverter(NumberFormat numberFormat) {
        super(numberFormat);
    }

    public NumberToStringConverter(String pattern) {
        super(pattern);
    }

    @Override
    public Number fromString(String value) {
        //to transform the double, given by the textfield, just multiply by 100 and round if any left
        Number rValue = Math.round(super.fromString(value).doubleValue() * 100);
        return rValue.longValue();
    }

    @Override
    public String toString(Number value) {
        if(value == null) {
            return "";
        }
        //Check for too big long value
        //If the long is too big, it could result in a strange double value.
        if(value.longValue() > 1000000000000l || value.longValue() < -1000000000000l ) {
            return "";
        }
        BigDecimal myBigDecimal = new BigDecimal(value.longValue());
        //to convert the long to a double (currency with fractional digits)
        myBigDecimal = myBigDecimal.movePointLeft(2);
        double asDouble = myBigDecimal.doubleValue();
        if(asDouble == Double.NEGATIVE_INFINITY || asDouble == Double.POSITIVE_INFINITY) {
            return "";
        }
        return super.toString(asDouble);
    }
}
