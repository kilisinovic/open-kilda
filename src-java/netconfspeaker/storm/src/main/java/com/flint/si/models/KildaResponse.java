package com.flint.si.models;

public class KildaResponse {
    private String trackingId;
    private String responseData;

	public KildaResponse(KildaRequest kildaRequest, String response) {
        this.trackingId = kildaRequest.getTrackingId();
        this.responseData = response; // TODO extract only important part with some util method and check that final xml is valid
    }
    
    @Override
    public String toString() {
        return "<response trackingId=\"" + trackingId + "\">" +
                 "<data>"+
                 responseData +
                 "</data>"+
                "</response>";
    }
    
}
