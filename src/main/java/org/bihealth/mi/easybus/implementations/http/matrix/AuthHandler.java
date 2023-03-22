package org.bihealth.mi.easybus.implementations.http.matrix;

import org.bihealth.mi.easybus.BusException;

import jakarta.ws.rs.client.Invocation.Builder;

/**
 * An interface to perform authentification
 * 
 * @author Felix Wirth
 *
 */
public interface AuthHandler {
    
    /**
     * Tries to (re-)authenticate and returns a builder with new authorization bearer
     * 
     * @param builder
     * @return
     * @throws BusException
     */
    Builder authenticate(Builder builder) throws BusException;

}