package com.flint.si.client;

import com.flint.si.client.NetconfOperationType;

import java.io.Serializable;

public class NetconfOperation implements Serializable {
    NetconfOperationType type;
    String xml;
    private static final long serialVersionUID = 100L;

    public NetconfOperation(NetconfOperationType type, String xml) {
        this.type = type;
        this.xml = xml;
    }

    public NetconfOperationType getType() {
        return type;
    }

    public void setType(NetconfOperationType type) {
        this.type = type;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
}
