package org.flint.si.kilda.netconfspeaker.exception;


public class NetconfClientException extends Exception { 
    public NetconfClientException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}