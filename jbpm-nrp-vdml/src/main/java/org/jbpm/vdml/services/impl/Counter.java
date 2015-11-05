package org.jbpm.vdml.services.impl;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Counter {
    private String expression;

    public Counter(String expression) {
        this.expression = expression;
    }

    public double getValue(double[] d) {
        try {
            int count = 0;
            ScriptEngine se = new ScriptEngineManager().getEngineByName("javascript");
            ScriptContext ctx = se.getContext();
            for (double v : d) {
                ctx.setAttribute("actualValue", v, ScriptContext.ENGINE_SCOPE);
                if (Boolean.TRUE.equals(se.eval(expression))) {
                    count++;
                }
            }
            return count;
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    public double getValue(Enum<?>[] d) {
        int count = 0;
        int firstIndex = expression.indexOf('\'');
        String name = expression.substring(firstIndex, expression.indexOf('\'', firstIndex + 1));
        for (Enum<?> anEnum : d) {
            if (anEnum.name().equalsIgnoreCase(name)) {
                count++;
            }
        }
        return count;
    }
}
