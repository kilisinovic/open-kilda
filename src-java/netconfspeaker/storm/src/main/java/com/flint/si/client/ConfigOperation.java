package com.flint.si.client;

import java.io.Serializable;

public enum ConfigOperation implements Serializable {
    CREATE("create"), DELETE("delete"), MERGE("merge"), REMOVE("remove"), REPLACE("replace");
    String operation;
    private static final long serialVersionUID = 100L;

    ConfigOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return operation;
    }
}
