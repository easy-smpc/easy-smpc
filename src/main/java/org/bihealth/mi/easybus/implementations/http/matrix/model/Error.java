package org.bihealth.mi.easybus.implementations.http.matrix.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A model for errors in matrix
 * 
 * @author Felix Wirth
 *
 */

public class Error {
    
    /** Error code */
    private final String errcode;
    /** Error message */
    private final String error;
    
    /**
    *
    * @param mIdentityServer
    * @param orgExampleCustomProperty
    * @param mHomeserver
    */
   @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
   public Error(@JsonProperty("errcode") String errcode,
                    @JsonProperty("error") String error) {
       super();
       this.errcode = errcode;
       this.error = error;
   }
    
    /**
     * @return
     */
    public String getErrcode() {
        return errcode;
    }

    /**
     * @return
     */
    public String getError() {
        return error;
    }
}
