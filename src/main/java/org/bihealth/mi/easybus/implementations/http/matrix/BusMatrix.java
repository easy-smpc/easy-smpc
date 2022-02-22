package org.bihealth.mi.easybus.implementations.http.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Message;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easybus.implementations.http.ExecutHTTPRequest;
import org.bihealth.mi.easybus.implementations.http.matrix.model.RoomEvent;
import org.bihealth.mi.easybus.implementations.http.matrix.model.sync.Invitation;
import org.bihealth.mi.easysmpc.resources.Resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.client.Invocation.Builder;

/**
 *  Bus implementation by the matrix protocol (see matrix.org)
 * 
 * @author Felix Wirth
 *
 */
public class BusMatrix extends Bus{

    /** Logger */
    private static final Logger     LOGGER                 = LogManager.getLogger(BusMatrix.class);
    /** Path to create a room */
    private final static String     PATH_CREATE_ROOM       = "";
    /** Path to sync */
    private static final String     PATH_SYNC              = "_matrix/client/r0/sync";
    /** Path pattern to join a room */
    private static final String     PATH_JOIN_ROOM_PATTERN = "_matrix/client/v3/join/%s";
    /** Connection details */
    private final ConnectionMatrix         connection;
    /** Thread */
    private final Thread                   thread;
    /** Stop flag */
    boolean                                stop                   = false;
    /** Subscribed users */
    private final Map<String, Participant> subscribedParticipants = new HashMap<>();
    /** Last time synchronized */
    private String                         lastSynchronized       = null;
    /** Jackson object mapper */
    private ObjectMapper                   mapper                 = new ObjectMapper();

    /**
     * Creates a new instance
     * 
     * @param sizeThreadpool
     * @param millis
     * @param connection
     */
    public BusMatrix(int sizeThreadpool, int millis, ConnectionMatrix connection) {
        // Super
        super(sizeThreadpool);
        
        // Store
        this.connection = connection;
        
        // Create thread
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!stop) {
                        try {
                            receive();
                        } catch (BusException e) {
                            // Log exception
                            LOGGER.error("Error receiving messages", e);
                        }
                        Thread.sleep(millis);
                    }
                } catch (InterruptedException e) {
                    // Die silently
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public boolean isAlive() {
        return this.thread != null && this.thread.isAlive();
    }

    @Override
    protected Void sendInternal(Message message, Scope scope, Participant participant) throws Exception {
        
        // Check room and create if necessary
        if(!getSubscribedRoomNames().contains(generateRoomName(participant, true))) {
            createRoom(generateRoomName(participant, true));
        }
        
        // Send message
        putMessageToRoom(message, scope, participant);        
        return null;
    }

    private void putMessageToRoom(Message message, Scope scope, Participant participant) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Creates a new room
     * 
     * @param generateRoomName
     * @throws BusException 
     */
    private void createRoom(String generateRoomName) throws BusException {
        // Check
        if(generateRoomName == null) {
            return;
        }
        // TODO Implement
    }

    /**
     * Generates a room name
     * 
     * @param participant
     * @param isSender
     * @return
     */
    private String generateRoomName(Participant participant, boolean isSender) {
        // Check
        if(participant == null) {
            throw new IllegalArgumentException("Participant can not be null");
        }
        
        // Return if sender
        if (isSender) {
            return String.format("EasySMPC%s.%sto%s.%s",
                                 this.connection.getSelf().getName(),
                                 this.connection.getSelf().getIdentifier(),
                                 participant.getName(),
                                 participant.getIdentifier());
        }
        
        return String.format("EasySMPC%s.%sto%s.%s",
                             participant.getName(),
                             participant.getIdentifier(),
                             this.connection.getSelf().getName(),
                             this.connection.getSelf().getIdentifier());
    }

    /**
     * List all subscribed/joined room names
     * 
     * @return
     */
    private List<String> getSubscribedRoomNames() { 
        // TODO Add a caching mechanism?
        
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * Receives message from rooms
     * @throws BusException, InterruptedException 
     */
    private void receive() throws BusException, InterruptedException {
        // Prepare
        String syncString;
        
        // Create URL path and parameter
        Builder request;
        
        if (this.lastSynchronized == null) {
            request = this.connection.getBuilder(PATH_SYNC);
        } else {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("since", this.lastSynchronized);
            request = this.connection.getBuilder(PATH_SYNC, parameters);
        }
    
        // Create task to get sync
        FutureTask<String> future = new ExecutHTTPRequest<String>(request,
                                                              ExecutHTTPRequest.REST_TYPE.GET,
                                                              () -> getExecutor(),
                                                              null,
                                                              (response) -> response.readEntity(String.class),
                                                              ConnectionMatrix.DEFAULT_ERROR_HANDLER,
                                                              this.connection).execute();
        
        // Wait for task end or exception
        try {
            syncString = future.get(Resources.TIMEOUT_MATRIX_ACTIVITY, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BusException("Error while executing HTTP request!", e);
        }
        
        // Understand sync
        JsonNode sync;
        try {
            sync = mapper.readTree(syncString);
        } catch (JsonProcessingException e) {
            throw new BusException("Error deserializing sync string!", e);
        }
        this.lastSynchronized = sync.get("next_batch").asText();
        Map<String, Invitation> invitations = mapper.convertValue(sync.get("rooms").get("invite"), new TypeReference<Map<String, Invitation>>(){});
        
        // Accept invitations for relevant rooms
        ExecutHTTPRequest.executeRequestPackage(joinRooms(checkAcceptInvites(invitations)),
                                                Resources.TIMEOUT_MATRIX_ACTIVITY,
                                                (e) -> LOGGER.error("Unable to join room", e));
    }

    /**
     * Check for invitations for rooms returns the invitations to accept
     * 
     * @param invitations
     * @return invitations to accept
     */
    private List<String> checkAcceptInvites(Map<String, Invitation> invitations) {
        // Prepare
        List<String> accept = new ArrayList<>();
        List<String> participantNames = new ArrayList<>();
        this.subscribedParticipants.forEach((k, v) -> participantNames.add(k));
        
        // Loop over invitations
        for (Entry<String, Invitation> invitation : invitations.entrySet()) {
            String expectedRoomName = null;

            // Check inviter is relevant
            for(RoomEvent event : invitation.getValue().getInviteState().getEvents()) {
                if(event.getType().equals("m.room.create") && participantNames.contains(event.getContent().getCreator())) {
                    expectedRoomName = generateRoomName(this.subscribedParticipants.get(event.getContent().getCreator()), false);
                    break;
                }               
            }

            // If inviter not relevant proceed to next invitation
            if(expectedRoomName == null) {
                continue;
            }

            // Check room name
            for(RoomEvent event : invitation.getValue().getInviteState().getEvents()) {
                if(event.getType().equals("m.room.name") && event.getContent().getName().equals(expectedRoomName)) {
                    accept.add(invitation.getKey());
                }
            }
        }
        
        // Return
        return accept;
    }

    /**
     * Joins rooms
     * 
     * @param ids
     * @return 
     */
    private List<ExecutHTTPRequest<?>> joinRooms(List<String> ids) {
        
        // Prepare
        List<ExecutHTTPRequest<?>> requests = new ArrayList<>();

        for(String id: ids) {
            // Create URL path and parameter
            final Builder request = this.connection.getBuilder(String.format(PATH_JOIN_ROOM_PATTERN, id));

            // Create task and add to list
            requests.add(new ExecutHTTPRequest<Void>(request,
                    ExecutHTTPRequest.REST_TYPE.POST,
                    () -> getExecutor(),
                    null,
                    (response) -> null,
                    ConnectionMatrix.DEFAULT_ERROR_HANDLER,
                    this.connection));
        }
        
        // Return
        return requests;
    }
    
    @Override
    protected synchronized void receivePostActivities(Participant participant) {
        this.subscribedParticipants.put(participant.getIdentifier(), participant);
    }
    
    @Override
    public void stop() {
        // Set stop flag
        this.stop = true;

        // Shutdown executor
        getExecutor().shutdown();

        // If on the same thread, just return
        if (this.thread == null || Thread.currentThread().equals(this.thread)) {
            return;

        // If on another thread, interrupt and wait for thread to die
        } else {

            // Stop thread
            this.thread.interrupt();

            // Wait for thread to stop
            while (thread != null && thread.isAlive()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }
    }
}
