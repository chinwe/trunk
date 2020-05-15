package org.interpreter;

import java.util.HashMap;

public class Calculator {

    AbstractExpression expression;

    public Calculator() {

        this.expression = new AddExpression(
                new SubExpression(
                    new AddExpression(
                            new VarExpression("a"),
                            new VarExpression("b")),
                    new VarExpression("c")
                ),
                new VarExpression("d")
        );
    }

    public int run(HashMap<String, Integer> var) {
        return this.expression.interpreter(var);
    }
}
