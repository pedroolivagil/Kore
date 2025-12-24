package com.olivadevelop.kore.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {
    /**
     * Redondea un número decimal a la alza según el número de decimales especificado.
     *
     * @param value        Valor a redondear.
     * @param decimalCount Número de decimales a mantener.
     * @return Valor redondeado a la alza.
     */
    public static double roundUp(double value, int decimalCount) {
        if (decimalCount < 0) { throw new IllegalArgumentException("decimalCount debe ser >= 0"); }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(decimalCount, RoundingMode.CEILING);
        return bd.doubleValue();
    }
    public static double roundUp(double value) { return roundUp(value, 2); }
}
