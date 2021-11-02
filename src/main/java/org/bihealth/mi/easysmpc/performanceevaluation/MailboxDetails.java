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
package org.bihealth.mi.easysmpc.performanceevaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;

/**
 * Stores the mailbox connections details
 * 
 * @author Felix Wirth
 *
 */
public class MailboxDetails {
    /** Replace this by index */
    public static final String INDEX_REPLACE = "REPLACE";
    /** Use several or exactly one mail box for the bus */
    private boolean isSharedMailbox;
    /** Template to create ConnectionIMAPSettings */
    private final ConnectionIMAPSettings connectionIMAPTemplate;
    /** Max participants*/
    private Integer maxParticipants;
    /** Tracker*/
    private transient PerformanceTracker tracker;

    /**
     * Creates a new instance
     * 
     * @param isSharedMailbox
     * @param connectionIMAPTemplate
     * @param participants
     * @param tracker
     */
    public MailboxDetails(boolean isSharedMailbox, ConnectionIMAPSettings connectionIMAPTemplate, List<Integer> participants, PerformanceTracker tracker) {
        
        // Check index replace index is provided        
        if(!isSharedMailbox && !connectionIMAPTemplate.getEmailAddress().contains(INDEX_REPLACE )) {
            throw new IllegalStateException(String.format("A non-shared mailbox must contain the indication %s", INDEX_REPLACE));
        }
        
        // Store
        this.isSharedMailbox = isSharedMailbox;        
        this.connectionIMAPTemplate = connectionIMAPTemplate;
        this.maxParticipants = Collections.max(participants);
        this.tracker = tracker;
    }
    
    
    /**
     * Get all distinct connections
     * 
     * @return
     */
    public List<ConnectionIMAPSettings> getAllConnections() {
        // Init
        Set<ConnectionIMAPSettings> connections = new HashSet<>();

        // Get all connections with no duplicates
        for (int index = 0; index < this.maxParticipants; index++) {
            connections.add(getConnection(index));
        }

        // Return
        return new ArrayList<>(connections);
    }
    
    /**
     * Gets the connection for participant with given index
     * 
     * @param index
     * @return
     */
    public ConnectionIMAPSettings getConnection(int index) {
        // Return same connection for all indexes if shared mailbox
        if(isSharedMailbox) {
            return new ConnectionIMAPSettings(connectionIMAPTemplate.getEmailAddress()).setPassword(connectionIMAPTemplate.getPassword())
                                                                                       .setIMAPPort(connectionIMAPTemplate.getIMAPPort())
                                                                                       .setIMAPServer(connectionIMAPTemplate.getIMAPServer())
                                                                                       .setSMTPServer(connectionIMAPTemplate.getSMTPServer())
                                                                                       .setSMTPPort(connectionIMAPTemplate.getSMTPPort())
                                                                                       .setAcceptSelfSignedCertificates(true)
                                                                                       .setSearchForProxy(false)
                                                                                       .setPerformanceListener(tracker);
        }
        
        // Replace        
        String newEmail = connectionIMAPTemplate.getEmailAddress().replaceFirst(INDEX_REPLACE, String.valueOf(index));
        
        // Return object with new mail address
        return new ConnectionIMAPSettings(newEmail).setPassword(connectionIMAPTemplate.getPassword())
                                                   .setIMAPPort(connectionIMAPTemplate.getIMAPPort())
                                                   .setIMAPServer(connectionIMAPTemplate.getIMAPServer())
                                                   .setSMTPServer(connectionIMAPTemplate.getSMTPServer())
                                                   .setSMTPPort(connectionIMAPTemplate.getSMTPPort())
                                                   .setAcceptSelfSignedCertificates(true)
                                                   .setSearchForProxy(false)
                                                   .setPerformanceListener(tracker);
    }
    
    /**
     * Returns the tracker
     * @return
     */
    public PerformanceTracker getTracker() {
    	return this.tracker;
    }
    
    /**
     * Is shared mailbox used?
     * 
     * @return
     */
    public boolean isSharedMailbox() {
        return this.isSharedMailbox;
    }
}