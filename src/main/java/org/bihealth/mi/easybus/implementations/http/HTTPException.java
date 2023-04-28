package org.bihealth.mi.easybus.implementations.http;

public class HTTPException extends RuntimeException {
    
    /** SVUID*/
    private static final long serialVersionUID = 2548768954338424491L;
    
    /** Status code*/
    private int statusCode = 0;
    
    /**
     * Creates a new instance
     * @param message
     */
    public HTTPException(String message) {
        super(message);
    }

    /**
     * Creates a new instance
     * @param message
     * @param statusCode;
     */
    public HTTPException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    /**
     * Creates a new instance
     * @param message
     * @param wrappedException
     */
    public HTTPException(String message, Exception wrappedException) {
        super(message, wrappedException);
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }
}
