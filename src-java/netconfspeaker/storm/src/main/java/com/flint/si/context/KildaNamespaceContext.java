package com.flint.si.context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class KildaNamespaceContext implements NamespaceContext {
    Map<String, List<String>> prefixes;

    public KildaNamespaceContext() {
        prefixes = new HashMap<>();
        prefixes.put("http://example.org/yang/device-tree", Arrays.asList("dev3"));
        prefixes.put("urn:ietf:params:xml:ns:yang:ietf-inet-types", Arrays.asList("inet"));
        prefixes.put("urn:ietf:params:xml:ns:yang:ietf-yang-types", Arrays.asList("yang"));
        prefixes.put("http://example.org/yang/router-config", Arrays.asList("router"));
        prefixes.put("http://example.org/yang/request", Arrays.asList("request"));
        prefixes.put("http://example.org/yang/service-tree", Arrays.asList("svc3"));
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if ("dev3".equals(prefix)) {
            return "http://example.org/yang/device-tree";
        } else if ("inet".equals(prefix)) {
            return "urn:ietf:params:xml:ns:yang:ietf-inet-types";
        } else if ("yang".equals(prefix)) {
            return "urn:ietf:params:xml:ns:yang:ietf-yang-types";
        } else if ("router".equals(prefix)) {
            return "http://example.org/yang/router-config";
        } else if ("request".equals(prefix)) {
            return "http://example.org/yang/request";
        } else if ("svc3".equals(prefix)) {
            return "http://example.org/yang/service-tree";
        }
        return null;
    }

    @Override
    public String getPrefix(String uri) {
        if ("http://example.org/yang/device-tree".equals(uri)) {
            return "dev3";
        } else if ("urn:ietf:params:xml:ns:yang:ietf-inet-types".equals(uri)) {
            return "inet";
        } else if ("urn:ietf:params:xml:ns:yang:ietf-yang-types".equals(uri)) {
            return "yang";
        } else if ("http://example.org/yang/router-config".equals(uri)) {
            return "router";
        } else if ("http://example.org/yang/request".equals(uri)) {
            return "request";
        } else if ("http://example.org/yang/service-tree".equals(uri)) {
            return "svc3";
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(String uri) {
        return prefixes.get(uri).iterator();
    }

}
