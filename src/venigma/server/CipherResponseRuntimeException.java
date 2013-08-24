/*
 * Apache Software License 2.0, (c) Copyright 2012, Evan Summers
 * 
 */
package venigma.server;

/**
 *
 * @author evan
 */
public class CipherResponseRuntimeException extends RuntimeException {
 
    CipherResponse response;

    public CipherResponseRuntimeException(CipherResponse response) {
        super(getMessage(response));
        this.response = response;
    }

    public CipherResponse getResponse() {
        return response;
    }
        
    public static String getMessage(CipherResponse response) {
        return response.getResponseType().name();
    }
    
}