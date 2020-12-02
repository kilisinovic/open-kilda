// this is not used for now, but will later be used
// for Edit config, once the logic of chosing
// the right datastore is created

package com.flint.si.client;

import com.flint.si.client.NetconfTargetType;

import java.io.Serializable;

public class NetconfTarget implements Serializable {
    private NetconfTargetType type;
    private String xml;
    private static final long serialVersionUID = 100L;

    public NetconfTarget(NetconfTargetType type, String xml) {
        this.type = type;
        this.xml = xml;
    }

    public NetconfTargetType getType() {
        return type;
    }

    public void setType(NetconfTargetType type) {
        this.type = type;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
}
