package com.olivadevelop.kore.util;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public interface FormulaEvaluator {
    static Double evaluate(String formula) {
        try {
            Expression expression = new ExpressionBuilder(formula).build();
            return expression.evaluate();
        } catch (IllegalArgumentException | ArithmeticException e) {
            return null;
        }
    }
}