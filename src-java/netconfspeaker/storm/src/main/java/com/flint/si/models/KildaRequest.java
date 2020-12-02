package com.flint.si.models;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.flint.si.utils.XMLUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * example of kilda request: Example 1: <request> <operation> <get-config/>
 * </operation> <target> <devices> <device> <name>d1</name> </device> </devices>
 * </target> </request>
 * 
 * Example2: <request> <operation> <edit-config> <dry-run/> </edit-config>
 * </operation> <!--target> <devices/> </target--> <data> <devices> <device>
 * <name>d1</name> <address>192.1.1.1</address> <port>22</port> </device>
 * </devices> </data> </request>
 * 
 */
public class KildaRequest {
    String originalRequest;
    Document document;
    String trackingId = null;
    Node data = null, operation = null;

    public KildaRequest(String request) {
        originalRequest = request;

        try {
            document = XMLUtils.toXmlDocument(originalRequest);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            // TODO report error
        }

    }

    public String getOperation() {
        return getOperationNode().getNodeName();
    }

    //TODO add general version to utils
    private boolean doesNodeExist(String xpathExpression){
        Node node = getNode(xpathExpression);
        return node != null;
    }

    public Node getDataNode(){
        if(data == null){
            data = getNode("/request/data");    
        }
        return data;
    }

    public String getTrackingId(){
        if(trackingId == null){
            Node request = getNode("/request");
            trackingId = request.getAttributes().getNamedItem("trackingId").getNodeValue();
        }
        return trackingId;
    }

	public Node getOperationNode() {
        if(operation == null){
            operation = getNode("/request/operation");    
        }
        return operation.getFirstChild();
    }
    
    private Node getNode(String xpathExpression, int index){
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        Node node = null;
        try{
            XPathExpression exp = xpath.compile(xpathExpression);
            NodeList nodes = (NodeList) exp.evaluate(document, XPathConstants.NODESET);

            if(nodes != null && nodes.getLength()>index){
                node = nodes.item(index);
            }
        } catch(XPathExpressionException e){
            // TODO probably nothing
        }
        return node;
    }

    private Node getNode(String xpathExpression){
        return getNode(xpathExpression, 0);
    }

    private NodeList getNodeList(String xpathExpression){
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        try{
            XPathExpression exp = xpath.compile(xpathExpression);
            return (NodeList) exp.evaluate(document, XPathConstants.NODESET);
            
        } catch(XPathExpressionException e){
            // TODO probably nothing
        }
        return null;
        
    }

	public String[] getDeviceNames() {
        NodeList deviceNames = getNodeList("/request/data/devices/device/name");
        if(deviceNames != null && deviceNames.getLength()>0){
            String[] names = new String[deviceNames.getLength()];
            for(int i = deviceNames.getLength()-1 ; i>=0; i--){
                names[i] = deviceNames.item(i).getNodeValue();
            }
            return names;
        }
        return null;
	}

}