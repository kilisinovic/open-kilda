package com.flint.si.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;

public class KildaRequestTest {
    private final String request = "<request trackingId=\"101\">" + "<operation>" + "<edit-config>" + "<dry-run/>"
            + "</edit-config>" + "</operation>" + "<data>" + "<devices xmlns=\"http://example.org/yang/device-tree\">"
            + "<device>" + "<name/>" + "<config>" + "<router>" + "<router-id/>" + "<bgp>" + "<as-number/>"
            + "<neighbors>" + "<id/>" + "<remote-as/>" + "</neighbors>" + "</bgp>" + "</router>" + "</config>"
            + "</device>" + "</devices>" + "</data>" + "</request>";

    private KildaRequest kildaRequest;

    @Before
    public void initEach(){
        kildaRequest = new KildaRequest(request);
    }

    @Test
    public void testGetOperation() {
        assertEquals("edit-config", kildaRequest.getOperation());
    }

    @Test
    public void testGetOperationNode() {
        Node edit = kildaRequest.getOperationNode();
        assertNotNull(edit);
        assertTrue(edit instanceof Node);
        assertEquals("edit-config", edit.getNodeName());
    }

    
    @Test
    public void testGetTrackingId(){
        assertEquals("101", kildaRequest.getTrackingId());
    }

    @Test
    public void testGetDataNode(){
        Node data = kildaRequest.getDataNode();
        assertNotNull(data);
        assertTrue(data instanceof Node);
        assertEquals("data", data.getNodeName());
    }


}