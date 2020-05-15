package org.interpreter;

import java.util.HashMap;

public class VarExpression extends AbstractExpression {

    private String key;

    public VarExpression(String key) {
        this.key = key;
    }

    @Override
    int interpreter(HashMap<String, Integer> var) {
        return var.get(key);
    }
}
