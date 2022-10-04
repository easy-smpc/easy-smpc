/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bihealth.mi.easybus.implementations.http;

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
     * Tries to (re-)authenticate and returns a rest builder with new authorization bearer if builder not null
     * 
     * @param builder
     * @return
     * @throws BusException
     */
    Builder authenticate(Builder builder) throws BusException;

}