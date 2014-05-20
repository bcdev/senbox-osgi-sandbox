package org.teo.core;

/**
 * Created by Norman on 20.02.14.
 */
public abstract class OperatorSpi {
    private final String name;

    public OperatorSpi(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract Operator createOperator();
}
