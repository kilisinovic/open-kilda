package org.flint.si.kilda.models.messages;

import java.io.Serializable;

import java.util.Map;

public class NetconfSpeakerResponse implements Serializable {
    String response;
    int transactionId;
    Map<String, String> mapGetResponses;
    private static final long serialVersionUID = 100L;

    public NetconfSpeakerResponse(String response, int transactionId) {
        this.response = response;
        this.transactionId = transactionId;
    }

    public NetconfSpeakerResponse(Map<String, String> mapGetResponses, int transactionId) {
        this.mapGetResponses = mapGetResponses;
        this.transactionId = transactionId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public Map<String, String> getGetResponses() {
        return mapGetResponses;
    }

    public void setGetResponses(Map<String,String> mapGetResponses) {
        this.mapGetResponses = mapGetResponses;

    }

}