package com.flint.si.client;

import java.io.Serializable;

public enum NetconfOperationType implements Serializable {
    EDIT_CONFIG("edit-config"), GET_CONFIG("get-config");
    String operation;
    private static final long serialVersionUID = 100L;
    
    NetconfOperationType(String operation){
        this.operation = operation;
    }

    @Override
    public String toString() {
        return operation;
    }
}
