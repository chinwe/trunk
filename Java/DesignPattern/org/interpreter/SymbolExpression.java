package org.interpreter;

import java.util.HashMap;

public class SymbolExpression extends AbstractExpression {

    AbstractExpression left;
    AbstractExpression right;

    public SymbolExpression(AbstractExpression left, AbstractExpression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    int interpreter(HashMap<String, Integer> var) {
        return 0;
    }
}
