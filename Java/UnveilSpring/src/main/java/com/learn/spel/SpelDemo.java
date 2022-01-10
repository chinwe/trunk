package com.learn.spel;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * SpelDemo
 * @author chinwe
 * 2022/1/9
 */
public class SpelDemo {
    public static void main(String[] args) {
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression("new String('hello world').toUpperCase()");
        String message = exp.getValue(String.class);
        System.out.println(message);

        Class dateClass = parser.parseExpression("T(java.util.Date)").getValue(Class.class);


    }
}
