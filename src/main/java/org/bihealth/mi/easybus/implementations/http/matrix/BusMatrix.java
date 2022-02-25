package org.bihealth.mi.easybus.implementations.http.matrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
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
import org.bihealth.mi.easybus.implementations.http.matrix.model.CreateRoom;
import org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.invited.EventInvited;
import org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.invited.Invitation;
import org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined.CreateMessage;
import org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined.CreateRedacted;
import org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined.EventJoined;
import org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined.JoinedRoom;
import org.bihealth.mi.easysmpc.resources.Resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.tu_darmstadt.cbs.emailsmpc.UIDGenerator;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.Response;

/**
 *  Bus implementation by the matrix protocol (see matrix.org)
 * 
 * @author Felix Wirth
 *
 */
public class BusMatrix extends Bus{

    /** Logger */
    private static final Logger            LOGGER                      = LogManager.getLogger(BusMatrix.class);
    /** Path to create a room */
    private final static String            PATH_CREATE_ROOM            = "_matrix/client/v3/createRoom";
    /** Path to sync */
    private static final String            PATH_SYNC                   = "_matrix/client/r0/sync";
    /** Path pattern to join a room */
    private static final String            PATH_JOIN_ROOM_PATTERN      = "_matrix/client/v3/join/%s";
    /** Path pattern to send a message */
    private static final String            PATH_SEND_MESSAGE_PATTERN   = "_matrix/client/v3/rooms/%s/send/m.room.message/%s";
    /** Path pattern to redact a message */
    private static final String            PATH_REDACT_MESSAGE_PATTERN = "_matrix/client/v3/rooms/%s/redact/%s/%s";
    /** String indicating start of scope */
    public static final String             SCOPE_NAME_START_TAG        = "BEGIN_NAME_SCOPE";
    /** String indicating end of scope */
    public static final String             SCOPE_NAME_END_TAG          = "END_NAME_SCOPE";
    /** String indicating start of the content */
    public static final String             CONTENT_START_TAG           = "BEGIN_CONTENT";
    /** String indicating end of the content */
    public static final String             CONTENT_END_TAG             = "END_CONTENT";
    /** Connection details */
    private final ConnectionMatrix         connection;
    /** Thread */
    private final Thread                   thread;
    /** Stop flag */
    boolean                                stop                        = false;
    /** Last time synchronized */
    private String                         lastSynchronized            = null;
    // TODO ObjectMapper is thread safe. However, will a single instance impact performance?
    /** Jackson object mapper */
    private ObjectMapper                   mapper                      = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    /** Join rooms */
    private Map<String, JoinedRoom>        joinedRooms;

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
        
        // Start thread
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public boolean isAlive() {
        return this.thread != null && this.thread.isAlive();
    }

    @Override
    protected Void sendInternal(Message message, Scope scope, Participant participant) throws Exception {

        // Prepare
        String roomId = null;
        
        // Check if room exists
        roomId = searchForRoomName(participant);
        
        // If not found update room and search again
        if(roomId == null) {
            updateJoinedRooms(getSyncTree(false));
            roomId = searchForRoomName(participant);
        }        
        
        // If necessary create room and update joined rooms 
        if(roomId == null) {
            roomId = createRoom(new CreateRoom().setName(generateRoomName(participant, true)).addInvite(participant.getIdentifier()));
            updateJoinedRooms(getSyncTree(false));
        }
        
        // Send message and return
        sendMessageToMatrixChat(roomId, scope, message);
        
        // Return
        return null;
    }

    /**
     * Search id of a room by the generated room name for the remote participant
     * 
     * @param participant
     * @return roomId
     */
    private String searchForRoomName(Participant participant) {
        
        // Check
        if(this.joinedRooms == null) {
            return null;
        }
        
        // Prepare
        String roomId = null;
            
        // Loop over rooms
        for (Entry<String, JoinedRoom> joinedRoom : joinedRooms.entrySet()) {

            // Check room name
            for (EventJoined event : joinedRoom.getValue().getTimeline().getEvents()) {
                if (event.getType().equals("m.room.name") &&
                    event.getContent().getName().equals(generateRoomName(participant, true))) {
                    roomId = joinedRoom.getKey();
                    break;
                }
            }

            // Stop loop if found
            if (roomId != null) {
                break;
            }
        }
            
        // Return
        return roomId;
    }

    /**
     * Send a message to a matrix chat
     * 
     * @param roomId
     * @param scope
     * @param message
     * @throws BusException 
     */
    private void sendMessageToMatrixChat(String roomId, Scope scope, Message message) throws BusException {
        
        // Prepare payload as string
        String payload;
        try {
            CreateMessage createMessage = new CreateMessage().setBody(message.serialize())
                                                             .setScope(scope.getName());
            payload = mapper.writeValueAsString(createMessage);
        } catch (IOException e) {
            throw new BusException("Unable to send serialize message!", e);
        }

        // Create request
        Builder request = this.connection.getBuilder(String.format(PATH_SEND_MESSAGE_PATTERN, roomId, UIDGenerator.generateShortUID(10)));
        
        // Create task to get sync
        FutureTask<String> future = new ExecutHTTPRequest<String>(request,
                                                              ExecutHTTPRequest.REST_TYPE.PUT,
                                                              () -> getExecutor(),
                                                              payload,
                                                              null,
                                                              ConnectionMatrix.DEFAULT_ERROR_HANDLER,
                                                              this.connection).execute();
        
        // Wait for task end or exception
        try {
            future.get(Resources.TIMEOUT_MATRIX_ACTIVITY, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BusException("Error while executing HTTP request!", e);
        }        
    }

    /**
     * Creates a new room to send messages to the given participant
     * 
     * @param createRoom
     * @return id of created room
     * @throws BusException 
     */
    private String createRoom(CreateRoom createRoom) throws BusException {
       
        // Check
        if(createRoom == null) {
            throw new BusException("createRoom cann not be null");
        }
        
        // Init        
        String createRoomSerialized;
        String roomId = null;
        
        // Serialize room to create
        try {
            createRoomSerialized = mapper.writeValueAsString(createRoom);
        } catch (JsonProcessingException e) {
            throw new BusException("Unable to serialize createRoom object", e);
        }
        
        // Create task
        FutureTask<String> future = new ExecutHTTPRequest<String>(this.connection.getBuilder(PATH_CREATE_ROOM),
                                                              ExecutHTTPRequest.REST_TYPE.POST,
                                                              () -> getExecutor(),
                                                              createRoomSerialized,
                                                              new Function<Response, String>() {

                                                                @Override
                                                                public String apply(Response response) {
                                                                    // Init
                                                                    String responseString = response.readEntity(String.class);
                                                                    JsonNode responseTree = null;
                                                                    
                                                                    // Understand response
                                                                    try {
                                                                        responseTree = new ObjectMapper().readTree(responseString);
                                                                    } catch (JsonProcessingException e) {
                                                                        throw new IllegalStateException("Unable to understand response!", e);
                                                                    }
                                                                    if(responseTree == null || responseTree.get("room_id").isMissingNode()) {
                                                                        throw new IllegalStateException(String.format("Room id not in answer: %s", responseString));
                                                                    }
                                                                    
                                                                    // Return
                                                                    return responseString;
                                                                }
                                                            },
                                                              ConnectionMatrix.DEFAULT_ERROR_HANDLER,
                                                              this.connection).execute();
        // Wait for task end or exception
        try {
            roomId = future.get(Resources.TIMEOUT_MATRIX_ACTIVITY, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BusException("Error while executing or understanding HTTP request!", e);
        }
        
        // Return
        return roomId;
    }

    /**
     * Generates a room name with the remote participant and the "self" participant
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
            return String.format("EasySMPC%s.%sTO%s.%s",
                                 this.connection.getSelf().getName(),
                                 this.connection.getSelf().getIdentifier(),
                                 participant.getName(),
                                 participant.getIdentifier());
        }
        
        return String.format("EasySMPC%s.%sTO%s.%s",
                             participant.getName(),
                             participant.getIdentifier(),
                             this.connection.getSelf().getName(),
                             this.connection.getSelf().getIdentifier());
    }
    
    /**
     * Receives message from rooms
     * 
     * @throws BusException, InterruptedException 
     */
    private void receive() throws BusException, InterruptedException {
        
        // Log
        LOGGER.debug("Started receiving");
        
        // Get sync data
        JsonNode sync = getSyncTree(true);
        
        // Update joined rooms
        updateJoinedRooms(sync);
        
        // Get and accept invitations for relevant rooms
        processInvitations(sync);
        
        // Check joined rooms for new messages
        checkNewMessages();        
    }

    /**
     *  Read matrix room invitations and accept relevant ones 
     * 
     * @param sync
     */
    private void processInvitations(JsonNode sync) {
        if (sync.path("rooms").path("invite") != null && !sync.path("rooms").path("invite").isMissingNode()) {
            // Read invitations
            Map<String, Invitation> invitations = mapper.convertValue(sync.path("rooms")
                                                                          .path("invite"),
                                                                      new TypeReference<Map<String, Invitation>>() {
                                                                      });
            // Check invitations and join rooms
            ExecutHTTPRequest.executeRequestPackage(joinRooms(checkAcceptInvites(invitations)),
                                                    Resources.TIMEOUT_MATRIX_ACTIVITY,
                                                    (e) -> LOGGER.error("Unable to join room", e));
        }
    }

    /**
     * Reads the joined rooms from a sync node and stores it
     * 
     * @param sync
     */
    private synchronized void updateJoinedRooms(JsonNode sync) {

        // TODO Since the joinedRoom can be set in parallel it is a ConcurrentHashMap AND this method is synchronized. Better use remove and addAll instead?
        // Set if content is available
        if (sync.path("rooms").path("path") != null && !sync.path("rooms").path("join").isMissingNode()) {
            this.joinedRooms = mapper.convertValue(sync.path("rooms").path("join"), new TypeReference<ConcurrentHashMap<String, JoinedRoom>>() {});
        }
    }

    /**
     * Check for new messages in joined rooms
     */
    private void checkNewMessages() {
        
        // Check
        if(this.joinedRooms == null) {
            return;
        }
        
        // Loop over room
        for(Entry<String, JoinedRoom> room : this.joinedRooms.entrySet()) {

            // Loop over events
            for (EventJoined event : room.getValue().getTimeline().getEvents()) {
                // If element can be processed and is relevant
                if (event.getType() != null && event.getType().equals("m.room.message") &&
                    event.getContent().getMsgType() != null &&
                    event.getContent().getMsgType().equals("m.text") && event.getSender() != null &&
                    event.getContent().getScope() != null &&
                    isParticipantScopeRegistered(new Scope(event.getContent().getScope()),
                                                 getParticipantsMap().get(event.getSender()))) {

                    // Try to receive message internal
                    try {
                        this.receiveInternal(Message.deserializeMessage(event.getContent().getBody()),
                                             new Scope(event.getContent().getScope()),
                                             getParticipantsMap().get(event.getSender()));
                    } catch (ClassNotFoundException | InterruptedException | IOException e) {
                        LOGGER.error(String.format("Unable to understand message with id %s in room %s", event.getEventId(), room.getKey()), e);
                        continue;
                    }
                    
                    // Redact message
                    try {
                        redactMessage(room.getKey(), event.getEventId(), new CreateRedacted(Resources.REASON_REDACTED_READ));
                    } catch (BusException e) {
                        LOGGER.error(String.format("Unable to redact message with id %s in room %s", event.getEventId(), room.getKey()), e);
                    }
                    // TODO: Create a package of redact message to redact in paralell
                }
            }
        }
    }

    /**
     * Redacts respectively "deletes" a message (see matrix documentation for explanation of redact 
     * 
     * @param roomId
     * @param eventId
     * @throws BusException 
     */
    private void redactMessage(String roomId, String eventId, CreateRedacted createRedacted ) throws BusException {
        
        // Prepare request
        Builder request = this.connection.getBuilder(String.format(PATH_REDACT_MESSAGE_PATTERN, roomId, eventId, UIDGenerator.generateShortUID(10)));
        
        // Serialize redact message
        String createRedactedSerialized;
        
        try {
            createRedactedSerialized = mapper.writeValueAsString(createRedacted);
        } catch (JsonProcessingException e) {
            throw new BusException("Unable to serialize createRedactedSerialized", e);
        }
        
        // Create task to get sync
        FutureTask<String> future = new ExecutHTTPRequest<String>(request,
                                                              ExecutHTTPRequest.REST_TYPE.PUT,
                                                              () -> getExecutor(),
                                                              createRedactedSerialized,
                                                              (response) -> response.readEntity(String.class),
                                                              ConnectionMatrix.DEFAULT_ERROR_HANDLER,
                                                              this.connection).execute();
        
        // Wait for task end or exception
        try {
            future.get(Resources.TIMEOUT_MATRIX_ACTIVITY, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BusException("Error while executing HTTP request!", e);
        }        
    }

    /**
     * Gets the sync tree
     * 
     * @param UpdateSince - should the since variable be updated. Only set true if all messages in the tree are processed
     * @return
     * @throws BusException
     */
    private JsonNode getSyncTree(boolean UpdateSince) throws BusException {
        
        // Prepare
        String syncString;       
        Builder request;
        
        // Create URL path and parameter 
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
        
        // Update since if necessary
        if (UpdateSince && !sync.path("next_batch").isMissingNode()) {
            synchronized (this.connection) {
                this.lastSynchronized = sync.path("next_batch").asText();
            }
        }
        
        // Return
        return sync;
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
        List<String> participantIdentifier = new ArrayList<>();
        getAllParticipants().forEach((e) -> participantIdentifier.add(e.getIdentifier()));
        
        // Loop over invitations
        for (Entry<String, Invitation> invitation : invitations.entrySet()) {
            String expectedRoomName = null;

            // Check inviter is relevant
            for(EventInvited event : invitation.getValue().getInviteState().getEvents()) {
                if(event.getType().equals("m.room.create") && participantIdentifier.contains(event.getContent().getCreator())) {
                    expectedRoomName = generateRoomName(getParticipantsMap().get(event.getContent().getCreator()), false);
                    break;
                }               
            }

            // If inviter not relevant proceed to next invitation
            if(expectedRoomName == null) {
                continue;
            }

            // Check room name
            for(EventInvited event : invitation.getValue().getInviteState().getEvents()) {
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
     * @param room ids
     * @return prepared, but not started requests
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
     * Gets all participants in form of a map with the identifier as a key
     * 
     * @return
     */
    private Map<String, Participant> getParticipantsMap() {
        // Prepare
        Map<String, Participant> result = new HashMap<>();
        
        // Create map
        for( Participant participant : getAllParticipants()) {
            result.put(participant.getIdentifier(), participant);
        }
        
        // Return
        return result;
    }
}
