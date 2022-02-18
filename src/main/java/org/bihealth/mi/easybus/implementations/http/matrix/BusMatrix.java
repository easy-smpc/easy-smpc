package org.bihealth.mi.easybus.implementations.http.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Message;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easybus.implementations.http.ExecutHTTPRequest;
import org.bihealth.mi.easybus.implementations.http.matrix.model.Sync;
import org.bihealth.mi.easysmpc.resources.Resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.Response;

/**
 *  Bus implementation by the matrix protocol (see matrix.org)
 * 
 * @author Felix Wirth
 *
 */
public class BusMatrix extends Bus{

    /** Logger */
    private static final Logger LOGGER = LogManager.getLogger(BusMatrix.class);    
    /** Path to create a room */
    private final static String     PATH_CREATE_ROOM      = "";
    /** Path to sync */
    private static final String PATH_SYNC = "_matrix/client/r0/sync";
    /** Connection details */
    private final ConnectionMatrix  connection;
    /** Thread */
    private final Thread            thread;
    /** Stop flag */
    boolean                         stop                  = false;
    /** Subscribed users */
    private final List<Participant> subscribedParticipant = new ArrayList<>();
    /** Last time synchronized */
    private String lastSynchronized = null;

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
                                 this.connection.getSelf().getEmailAddress(),
                                 participant.getName(),
                                 participant.getEmailAddress());
        }
        
        return String.format("EasySMPC%s%s.%sto%s.%s",
                             participant.getName(),
                             participant.getEmailAddress(),
                             this.connection.getSelf().getName(),
                             this.connection.getSelf().getEmailAddress());
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
    
    /**
     * Receives message from rooms
     * @throws BusException, InterruptedException 
     */
    private void receive() throws BusException, InterruptedException {

        // Create URL path and parameter
        jakarta.ws.rs.client.Invocation.Builder request;
        
        if (this.lastSynchronized == null) {
            request = this.connection.getBuilder(PATH_SYNC);
        } else {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("since", this.lastSynchronized);
            request = this.connection.getBuilder(PATH_SYNC, parameters);
        }
    
        // Create task
        FutureTask<Sync> future = new ExecutHTTPRequest<Sync>(request,
                                                              ExecutHTTPRequest.REST_TYPE.GET,
                                                              () -> getExecutor(),
                                                              null,
                                                              new Function<Response, Sync>() {

                                                                  @Override
                                                                  public Sync
                                                                         apply(Response reponse) {
                                                                      try {
                                                                          return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                                                                                   .readValue(reponse.readEntity(String.class), Sync.class);
                                                                      } catch (JsonProcessingException e) {
                                                                          throw new IllegalStateException("Unable to understand response body!", e);
                                                                      }
                                                                  }
                                                              },
                                                              ConnectionMatrix.DEFAULT_ERROR_HANDLER,
                                                              this.connection).execute();
        
        // Wait for task end or exception
        try {
            Sync sync = future.get(Resources.TIMEOUT_MATRIX_ACTIVITY, TimeUnit.MILLISECONDS);           
            this.lastSynchronized = sync.getNextBatch();
            System.out.println(this.lastSynchronized);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BusException("Error in http connection!", e);
        }
    }

    /**
     * Receives messages from a room
     * 
     * @param roomName
     */
    private void receiveFromRoom(String roomName) {
        // TODO Auto-generated method stub        
    }

    /**
     * Lists all rooms which are necessary but not subscribed/joined so far
     * 
     * @return
     */
    private List<String> getNotSubscribedRooms() {
        
        // Get all necessary rooms
        List<String> necessaryRooms = new ArrayList<>();
        for(Participant participant : subscribedParticipant) {
            necessaryRooms.add(generateRoomName(participant, false));
        }        
        
        // Remove subscribed
        necessaryRooms.removeAll(getSubscribedRoomNames());
        
        // Return
        return necessaryRooms;
    }

    /**
     * Check for invitations for rooms and accept if invited
     * 
     * @param rooms
     * @return
     */
    private List<String> checkAcceptInvites(List<String> rooms) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected synchronized void receivePostActivities(Participant participant) {
        this.subscribedParticipant.add(participant);        
    }
}
