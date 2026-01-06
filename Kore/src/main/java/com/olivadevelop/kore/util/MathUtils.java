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
        return BigDecimal.valueOf(value).setScale(decimalCount, RoundingMode.CEILING).doubleValue();
    }
    public static double roundUp(double value) { return roundUp(value, 2); }
    public static int clamp(int value, int min, int max) {
        if (min > max) { throw new IllegalArgumentException(min + " > " + max); }
        return Math.min(max, Math.max(value, min));
    }
    public static long clamp(long value, long min, long max) {
        if (min > max) { throw new IllegalArgumentException(min + " > " + max); }
        return Math.min(max, Math.max(value, min));
    }
    public static float clamp(float value, float min, float max) {
        if (min > max) { throw new IllegalArgumentException(min + " > " + max); }
        return Math.min(max, Math.max(value, min));
    }
    public static double clamp(double value, double min, double max) {
        if (min > max) { throw new IllegalArgumentException(min + " > " + max); }
        return Math.min(max, Math.max(value, min));
    }
}
