package org.bihealth.mi.easybus.implementations.http;

import jakarta.ws.rs.core.Response;

public class HTTPUtil {

    
    public static void raiseException(Response response) throws HTTPException {
        String body = "Body not readable";
        try {
            body = response.readEntity(String.class);
        } catch (Exception e) {
            // Ignore
        }
        throw new HTTPException(String.format("Error executing HTTP request with return code %s and body %s",
                                response.getStatus(), body));
    }
}
