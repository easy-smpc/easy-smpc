package org.bihealth.mi.easybus.implementations.http.matrix;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.BusMessage;
import org.bihealth.mi.easybus.BusMessageFragment;
import org.bihealth.mi.easybus.MessageFilter;
import org.bihealth.mi.easybus.MessageManager;
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
import org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined.Messages;
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
    private static final Logger           LOGGER                              = LogManager.getLogger(BusMatrix.class);
    /** Path to create a room */
    private final static String           PATH_CREATE_ROOM                    = "_matrix/client/v3/createRoom";
    /** Path to sync */
    private static final String           PATH_SYNC                           = "_matrix/client/r0/sync";
    /** Path pattern to join a room */
    private static final String           PATH_JOIN_ROOM_PATTERN              = "_matrix/client/v3/join/%s";
    /** Path pattern to send a message */
    private static final String           PATH_SEND_MESSAGE_PATTERN           = "_matrix/client/v3/rooms/%s/send/m.room.message/%s";
    /** Path pattern to redact a message */
    private static final String           PATH_REDACT_MESSAGE_PATTERN_PATTERN = "_matrix/client/v3/rooms/%s/redact/%s/%s";
    /** Path pattern to leave a room */
    private static final String           PATH_LEAVE_ROOM_PATTERN             = "_matrix/client/v3/rooms/%s/leave";
    /** Path pattern to manage account data */
    private static final String           PATH_TAG_PATTERN                    = "_matrix/client/r0/user/%s/account_data/%s";
    /** Path pattern to forget a room */
    private static final String           PATH_FORGET_ROOM_PATTERN            = "_matrix/client/v3/rooms/%s/forget";
    /** Path pattern to forget a room */
    private static final String           PATH_GET_MESSAGES_FROM_ROOM_PATTERN = "_matrix/client/v3/rooms/%s/messages";
    /** String indicating start of scope */
    public static final String            SCOPE_NAME_START_TAG                = "BEGIN_NAME_SCOPE";
    /** String indicating end of scope */
    public static final String            SCOPE_NAME_END_TAG                  = "END_NAME_SCOPE";
    /** String indicating start of the content */
    public static final String            CONTENT_START_TAG                   = "BEGIN_CONTENT";
    /** String indicating end of the content */
    public static final String            CONTENT_END_TAG                     = "END_CONTENT";
    /** Name of custom account data field to manage room ids per user */
    public static final String            ROOM_IDS_USER_TAG                   = "org.bihealth.mi.roomids";
    /** Connection details */
    private final ConnectionMatrix        connection;
    /** Thread */
    private final Thread                  thread;
    /** Stop flag */
    boolean                               stop                                = false;
    /** Last time synchronized */
    private String                        lastSynchronized                    = null;
    /** Jackson object mapper */
    private ObjectMapper                  mapper                              = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    /** Join rooms */
    private final Map<String, JoinedRoom> joinedRooms                         = new ConcurrentHashMap<>();
    /** Maps participant names to room ids */
    private final Map<String, String>     ids                                 = new ConcurrentHashMap<>();
    /** Message manager */
    private MessageManager messageManager;

    /**
     * Creates a new instance
     * 
     * @param sizeThreadpool
     * @param millis
     * @param connection
     */
    public BusMatrix(int sizeThreadpool, int millis, ConnectionMatrix connection) {
        this(sizeThreadpool, millis, connection, Resources.MATRIX_DEFAULT_MESSAGE_SIZE);
    }
    
    /**
     * Creates a new instance
     * 
     * @param sizeThreadpool
     * @param millis
     * @param connection
     * @param maxMessageSize
     */
    public BusMatrix(int sizeThreadpool, int millis, ConnectionMatrix connection,  int maxMessageSize) {
        // Super
        super(sizeThreadpool);
        
        // Store
        this.connection = connection;
        messageManager = new MessageManager(maxMessageSize);
        
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
    protected Void sendInternal(BusMessage message) throws Exception {

        // Prepare room id list, only first entry is relevant
        String roomId = null;
        
        // Check if room exists        
        roomId = this.ids.get(message.getReceiver().getIdentifier());
        
        // If not found update and search again
        if(roomId == null) {
            this.ids.putAll(getParticipantRoomIds());
            roomId = this.ids.get(message.getReceiver().getIdentifier());
        }        
        
        // If necessary create room and update joined rooms 
        if (roomId == null) {
            roomId = createRoom(new CreateRoom().setName(generateRoomName(message.getReceiver(), true)).addInvite(message.getReceiver().getIdentifier()));
            this.ids.put(message.getReceiver().getIdentifier(), roomId);
            setParticipantRoomIds(this.ids);
        }
        
        // Send message and return
        try {
            for (BusMessage m : messageManager.splitMessage(message)) {
                send(roomId, message.getScope(), m);
            }
        } catch (IOException | BusException e) {
            throw new BusException("Unable to send message", e);
        }
        
        
        // Return
        return null;
    }

    /**
     * Search id of a room by room name pattern
     * 
     * @param room pattern
     * @param breakAfterFirstFinding
     * @return roomId
     */
    private List<String> searchForRoomName(String pattern, boolean breakAfterFirstFinding) {
        
        // Prepare
        List<String> roomIds = new ArrayList<>();
            
        // Loop over rooms
        for (Entry<String, JoinedRoom> joinedRoom : joinedRooms.entrySet()) {            
            
            // Aggregate all events
            List<EventJoined> events = new ArrayList<>();
            events.addAll(joinedRoom.getValue().getState().getEvents());
            events.addAll(joinedRoom.getValue().getTimeline().getEvents());            

            // Check room name
            for (EventJoined event : events) {
                if (event.getType().equals("m.room.name") &&
                    event.getContent().getName().matches(pattern)) {
                    roomIds.add(joinedRoom.getKey());
                    break;
                }
            }

            // Stop loop if found
            if (breakAfterFirstFinding && roomIds.size() > 0) {
                break;
            }
        }
            
        // Return
        return roomIds;
    }

    /**
     * Send a message to a matrix chat
     * 
     * @param roomId
     * @param scope
     * @param message
     * @throws BusException 
     */
    private void send(String roomId, Scope scope, Object message) throws BusException {
        
        // Prepare payload as string
        String payload;        
        try {
            CreateMessage createMessage = new CreateMessage().setBody(serializeObject(message))
                                                             .setScope(scope.getName());
            payload = mapper.writer().writeValueAsString(createMessage);
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
                                                              this.connection,
                                                              Resources.RETRY_MATRIX_ACTIVITY_NUMBER,
                                                              Resources.RETRY_MATRIX_ACTIVITY_WAIT).execute();
        
        // Wait for task end or exception
        try {
            future.get(Resources.TIMEOUT_MATRIX_ACTIVITY, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // TODO If fails due to non-existing roomId remove from participants/room mapping
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
            createRoomSerialized = mapper.writer().writeValueAsString(createRoom);
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
                                                                        responseTree = mapper.reader().readTree(responseString);
                                                                    } catch (JsonProcessingException e) {
                                                                        throw new IllegalStateException("Unable to understand response!", e);
                                                                    }
                                                                    if(responseTree == null || responseTree.get("room_id").isMissingNode()) {
                                                                        throw new IllegalStateException(String.format("Room id not in answer: %s", responseString));
                                                                    }
                                                                    
                                                                    // Return
                                                                    return responseTree.get("room_id").textValue();
                                                                }
                                                            },
                                                              ConnectionMatrix.DEFAULT_ERROR_HANDLER,
                                                              this.connection,
                                                              Resources.RETRY_MATRIX_ACTIVITY_NUMBER,
                                                              Resources.RETRY_MATRIX_ACTIVITY_WAIT).execute();
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
        if(isSender && participant == null) {
            throw new IllegalArgumentException("Participant can not be null if sender is set");
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
                             "*",
                             "*",
                             this.connection.getSelf().getName(),
                             this.connection.getSelf().getIdentifier());
    }
    
    /**
     * Receives message from rooms
     * 
     * @throws BusException, InterruptedException 
     */
    private void receive() throws BusException, InterruptedException {
        // TODO Make this method synchronized?
        
        // Log
        LOGGER.debug("Started receiving");
        
        // Get participant/rooms mapping
        this.ids.putAll(getParticipantRoomIds());
        
        // Get sync data
        JsonNode sync = getSyncTree(null);
        
        // Update joined rooms
        updateJoinedRooms(sync);
        
        // Get and accept invitations for relevant rooms
        processInvitations(sync);
        
        // Check joined rooms for new messages
        checkNewMessages();
        
        // Update since if necessary
        if (!sync.path("next_batch").isMissingNode()) {
            synchronized (this.connection) {
                this.lastSynchronized = sync.path("next_batch").asText();
            }
        }
    }
    
    /**
     * Leaves and forgets all EasySMPC relevant rooms
     */
    @Override
    public void purge(MessageFilter filter) throws BusException {
        // TODO Use filter
        
    	// Init
    	List<String> ids = new ArrayList<>();
    	
        // Get sync data
        JsonNode sync = getSyncTree(null);
        
        // Update joined rooms
        updateJoinedRooms(sync);
        
        // Loop over joined rooms
        ids.addAll(searchForRoomName("EasySMPC.{0,}", false));
        
        // Loop over invitations if existing
        if (sync.path("rooms").path("invite") != null && !sync.path("rooms").path("invite").isMissingNode()) {
            // Read invitations
            Map<String, Invitation> invitations = mapper.convertValue(sync.path("rooms")
                                                                          .path("invite"),
                                                                      new TypeReference<Map<String, Invitation>>() {
                                                                      });
            for (Entry<String, Invitation> invitation : invitations.entrySet()) {

                // Check room name
                for(EventInvited event : invitation.getValue().getInviteState().getEvents()) {
                    if(event.getType().equals("m.room.name") && event.getContent().getName().startsWith("EasySMPC")) {
                    	ids.add(invitation.getKey());
                    }
                }
            }
        }
        
        // Leave rooms
        ExecutHTTPRequest.executeRequestPackage(leaveRooms(ids),
                                                Resources.TIMEOUT_MATRIX_ACTIVITY,
                                                (e) -> LOGGER.error("Unable to leave room", e));
        
        // Forget rooms
        ExecutHTTPRequest.executeRequestPackage(forgetRooms(ids),
                                                Resources.TIMEOUT_MATRIX_ACTIVITY,
                                                (e) -> LOGGER.error("Unable to forget room", e));
        
        // TODO Remove rooms from tag list automatically when left
        setParticipantRoomIds(new HashMap<String, String>());
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
    private void updateJoinedRooms(JsonNode sync) {

        // Set if content is available
        if (sync.path("rooms").path("path") != null && !sync.path("rooms").path("join").isMissingNode()) {
            this.joinedRooms.putAll(mapper.convertValue(sync.path("rooms").path("join"), new TypeReference<HashMap<String, JoinedRoom>>() {}));
        }
    }

    /**
     * Check for new messages in joined rooms from the sync API
     * If the sync result is indicated as limited for one or more rooms, the messages API will be called for the respective rooms
     */
    private void checkNewMessages() {
        
        // Check
        if(this.joinedRooms == null) {
            return;
        }
        
        // Init
        Map<String, String> roomIdPrevBatchMap = new HashMap<>();
        
        // Loop over room
        for(Entry<String, JoinedRoom> room : this.joinedRooms.entrySet()) {
            // Init
            List<EventJoined> successfulReadEvents = new ArrayList<>();
            
            // If result is limited, further messages must be read with the messages api later on
            if(room.getValue().getTimeline().getLimited()) {
                roomIdPrevBatchMap.put(room.getKey(), room.getValue().getTimeline().getPrevBatch());
            }
            
            
            // Loop over events
            for (EventJoined event : room.getValue().getTimeline().getEvents()) {
                // If element can be processed and is relevant
                if (event.getType() != null && event.getType().equals("m.room.message") &&
                    event.getContent().getMsgType() != null &&
                    event.getContent().getMsgType().equals("m.text") && event.getSender() != null &&
                    event.getContent().getScope() != null &&
                    isParticipantScopeRegistered(new Scope(event.getContent().getScope()),
                                                 this.connection.getSelf())) {

                    // Process message
                    try {
                        
                        // Process with message manager
                        BusMessage messageComplete = messageManager.mergeMessage(createMessageFragmentFromEvent(event, room.getKey()));
                        
                        // Send to scope and participant
                        if (messageComplete != null) {
                            receiveInternal(messageComplete);
                        }
                        successfulReadEvents.add(event);
                    } catch (ClassNotFoundException | InterruptedException | IOException e) {
                        LOGGER.error(String.format("Unable to understand message with id %s in room %s", event.getEventId(), room.getKey()), e);
                        continue;
                    } catch (BusException e) {
                        LOGGER.error(String.format("Unable to receive message internally", event.getEventId(), room.getKey()), e);
                        continue;
                    }
                }
            }
            // Remove successful read events
            room.getValue().getTimeline().getEvents().removeAll(successfulReadEvents);
        }
        
        // Read messages for rooms with limited entries
        checkNewMessagesFromMessagesAPI(roomIdPrevBatchMap);
    }

    /**
     * Check new messages with the messages API
     * 
     * @param roomIdPrevBatchMap - Contains a mapping from the room id to the prevBatch id to start reading from 
     */
    private void checkNewMessagesFromMessagesAPI(Map<String, String> roomIdPrevBatchMap) {
        // Loop over rooms
        for(Entry<String, String> entry : roomIdPrevBatchMap.entrySet()) {
            String fromBatch = entry.getValue();
            while (fromBatch != null) {                
                try {
                    // Get messages object
                    Messages messages = getMessagesFromRoom(entry.getKey(), fromBatch, this.lastSynchronized);
                    
                    // Loop over events
                    for(EventJoined event : messages.getChunk()) {
                        // If element can be processed and is relevant
                        if (event.getType() != null && event.getType().equals("m.room.message") &&
                                event.getContent().getMsgType() != null &&
                                event.getContent().getMsgType().equals("m.text") && event.getSender() != null &&
                                event.getContent().getScope() != null &&
                                isParticipantScopeRegistered(new Scope(event.getContent().getScope()),
                                                             getParticipantsMap().get(event.getSender()))) {

                            // Try to receive message internally
                            try {
                                
                                // Process with message manager
                                BusMessage messageComplete = messageManager.mergeMessage(createMessageFragmentFromEvent(event, entry.getKey()));
                                
                                // Send to scope and participant
                                if (messageComplete != null) {
                                    receiveInternal(messageComplete);
                                }
                                
                            }  catch (ClassNotFoundException | InterruptedException | IOException e) {
                                LOGGER.error(String.format("Unable to understand message with id %s in room %s", event.getEventId(), entry.getKey()), e);
                            } catch (BusException e) {
                                LOGGER.error(String.format("Unable to receive message internally", event.getEventId(), entry.getKey()), e);
                            }
                        }
                    }
                    
                    // End of messages in this room reached?
                    fromBatch = messages.getEnd() == null ||
                                messages.getEnd().equals(messages.getStart()) ? null
                                        : messages.getEnd();
                } catch (BusException e) {
                    LOGGER.error(String.format("Unable to read messages for room %s with from value %s", entry.getKey(), fromBatch), e);
                }
            }            
        }        
    }

    /**
     * Creates a message fragment out of an joined room event
     * 
     * @param event
     * @param roomKey
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private BusMessage createMessageFragmentFromEvent(EventJoined event, String roomKey) throws IOException, ClassNotFoundException {
        
        // Create fragment and return
        Object o = deserializeMessage(event.getContent().getBody());
        
        
        
        if (o instanceof BusMessageFragment) {
            return new BusMessageFragment((BusMessageFragment) o) {
                
                /** SVUID */
                private static final long serialVersionUID = -2294134512332533758L;

                @Override
                public void delete() throws BusException {
                    try {
                        redactMessage(roomKey, event.getEventId(), CreateRedacted.DEFAULT);
                    } catch (BusException e) {
                        LOGGER.error(String.format("Unable to redact message with id %s in room %s", event.getEventId(), roomKey), e);
                    }
                }
                @Override
                public void expunge() throws BusException {
                    // Empty
                }
            };
        } else {
            return new BusMessage((BusMessage) o) {

                /** SVUID */
                private static final long serialVersionUID = 2247134512332533758L;

                @Override
                public void delete() throws BusException {
                    try {
                        redactMessage(roomKey, event.getEventId(), CreateRedacted.DEFAULT);
                    } catch (BusException e) {
                        LOGGER.error(String.format("Unable to redact message with id %s in room %s", event.getEventId(), roomKey), e);
                    }
                }
                @Override
                public void expunge() throws BusException {
                    // Empty
                }
            };
        }
    }

    /**
     * Redacts respectively "deletes" a message (see matrix documentation for explanation of redact)
     * 
     * @param roomId
     * @param eventId
     * @throws BusException 
     */
    protected void redactMessage(String roomId, String eventId, CreateRedacted createRedacted ) throws BusException {
        
        // Prepare request
        Builder request = this.connection.getBuilder(String.format(PATH_REDACT_MESSAGE_PATTERN_PATTERN, roomId, eventId, UIDGenerator.generateShortUID(10)));
        
        // Serialize redact message
        String createRedactedSerialized;
        
        try {
            createRedactedSerialized = mapper.writer().writeValueAsString(createRedacted);
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
                                                              this.connection,
                                                              Resources.RETRY_MATRIX_ACTIVITY_NUMBER,
                                                              Resources.RETRY_MATRIX_ACTIVITY_WAIT).execute();
        
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
     * @param Filter - Id of filter
     * @return
     * @throws BusException
     */
    private JsonNode getSyncTree(String filter) throws BusException {
        
        // Prepare
        String syncString;       
        Builder request;
        Map<String, String> parameters = new HashMap<String, String>();
        
        // Set since parameter to only get delta
        if (this.lastSynchronized != null) {
            parameters.put("since", this.lastSynchronized);
        }

        // Set filter and create request
        parameters.put("filter", filter != null ? filter : "0");
        request = this.connection.getBuilder(PATH_SYNC, parameters);
    
        // Create task to get sync
        FutureTask<String> future = new ExecutHTTPRequest<String>(request,
                                                              ExecutHTTPRequest.REST_TYPE.GET,
                                                              () -> getExecutor(),
                                                              null,
                                                              (response) -> response.readEntity(String.class),
                                                              ConnectionMatrix.DEFAULT_ERROR_HANDLER,
                                                              this.connection,
                                                              Resources.RETRY_MATRIX_ACTIVITY_NUMBER,
                                                              Resources.RETRY_MATRIX_ACTIVITY_WAIT).execute();
        
        // Wait for task end or exception
        try {
            syncString = future.get(Resources.TIMEOUT_MATRIX_ACTIVITY, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BusException("Error while executing HTTP request!", e);
        }
        
        // Understand sync
        JsonNode sync;
        try {
            sync = mapper.reader().readTree(syncString);
        } catch (JsonProcessingException e) {
            throw new BusException("Error deserializing sync string!", e);
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
            
            // Check room name
            for(EventInvited event : invitation.getValue().getInviteState().getEvents()) {
                if(event.getType().equals("m.room.name") 
                        && event.getContent().getName().matches(generateRoomName(null, false))) {
                    accept.add(invitation.getKey());
                    continue;
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
                    this.connection,
                    Resources.RETRY_MATRIX_ACTIVITY_NUMBER,
                    Resources.RETRY_MATRIX_ACTIVITY_WAIT));
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
    
    /**
     * Leave rooms
     * 
     * @param room ids
     * @return prepared, but not started requests
     */
    private List<ExecutHTTPRequest<?>> leaveRooms(List<String> ids) {
        
        // Prepare
        List<ExecutHTTPRequest<?>> requests = new ArrayList<>();

        for(String id: ids) {
            // Create URL path and parameter
            final Builder request = this.connection.getBuilder(String.format(PATH_LEAVE_ROOM_PATTERN, id));

            // Create task and add to list
            requests.add(new ExecutHTTPRequest<Void>(request,
                    ExecutHTTPRequest.REST_TYPE.POST,
                    () -> getExecutor(),
                    null,
                    (response) -> null,
                    ConnectionMatrix.DEFAULT_ERROR_HANDLER,
                    this.connection,
                    Resources.RETRY_MATRIX_ACTIVITY_NUMBER,
                    Resources.RETRY_MATRIX_ACTIVITY_WAIT));
        }
        
        // Return
        return requests;    
    }
    
    /**
     * Forget rooms
     * 
     * @param room ids
     * @return prepared, but not started requests
     */
    private List<ExecutHTTPRequest<?>> forgetRooms(List<String> ids) {
        
        // Prepare
        List<ExecutHTTPRequest<?>> requests = new ArrayList<>();

        for(String id: ids) {
            // Create URL path and parameter
            final Builder request = this.connection.getBuilder(String.format(PATH_FORGET_ROOM_PATTERN, id));

            // Create task and add to list
            requests.add(new ExecutHTTPRequest<Void>(request,
                    ExecutHTTPRequest.REST_TYPE.POST,
                    () -> getExecutor(),
                    null,
                    (response) -> null,
                    ConnectionMatrix.DEFAULT_ERROR_HANDLER,
                    this.connection,
                    Resources.RETRY_MATRIX_ACTIVITY_NUMBER,
                    Resources.RETRY_MATRIX_ACTIVITY_WAIT));
        }
        
        // Return
        return requests;    
    }
    
    /**
     * Get participant/room ids from the account data tag
     * 
     * @return
     * @throws BusException
     */
    private Map<String, String> getParticipantRoomIds() throws BusException {

        // Prepare request
        String encodedUserName;
        try {
            encodedUserName = URLEncoder.encode(this.connection.getSelf().getIdentifier(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BusException("Unable to encode user name", e);
        }
        Builder request = this.connection.getBuilder(String.format(PATH_TAG_PATTERN, encodedUserName, ROOM_IDS_USER_TAG));        
        
        // Create task to get sync
        FutureTask<Map<String, String>> future = new ExecutHTTPRequest<Map<String, String>>(request,
                ExecutHTTPRequest.REST_TYPE.GET,
                () -> getExecutor(),
                null,
                new Function<Response, Map<String, String>>() {

                    @Override
                    public Map<String, String>
                    apply(Response response) {
                        try {
                            return mapper.readValue(response.readEntity(String.class),
                                                    new TypeReference<Map<String, String>>() {});
                        } catch (JsonProcessingException e) {
                            throw new IllegalStateException("Unable to understand response!", e);
                        }
                    }

                },
                ConnectionMatrix.DEFAULT_ERROR_HANDLER,
                this.connection,
                Resources.RETRY_MATRIX_ACTIVITY_NUMBER,
                Resources.RETRY_MATRIX_ACTIVITY_WAIT).execute();

        // Wait for task end or exception
        try {
            return future.get(Resources.TIMEOUT_MATRIX_ACTIVITY, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // TODO improve error handling
            // Ignore
            return new HashMap<>();
        }        
    }
    
    /**
     * Set participant/room ids from the account data tag
     * 
     * @param ids
     * @throws BusException
     */
    private void setParticipantRoomIds(Map<String, String> ids) throws BusException {

        // Prepare request
        String encodedUserName;
        try {
            encodedUserName = URLEncoder.encode(this.connection.getSelf().getIdentifier(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BusException("Unable to encode user name", e);
        }
        Builder request = this.connection.getBuilder(String.format(PATH_TAG_PATTERN, encodedUserName, ROOM_IDS_USER_TAG));
        
        // Serialize ids
        String idsSerialized;
        
        try {
            idsSerialized = mapper.writer().writeValueAsString(ids);
        } catch (JsonProcessingException e) {
            throw new BusException("Unable to serialize ids", e);
        }
        
        // Create task to get sync
        FutureTask<Void> future = new ExecutHTTPRequest<Void>(request,
                ExecutHTTPRequest.REST_TYPE.PUT,
                () -> getExecutor(),
                idsSerialized,
                (response) -> null,
                ConnectionMatrix.DEFAULT_ERROR_HANDLER,
                this.connection,
                Resources.RETRY_MATRIX_ACTIVITY_NUMBER,
                Resources.RETRY_MATRIX_ACTIVITY_WAIT).execute();

        // Wait for task end or exception
        try {
            future.get(Resources.TIMEOUT_MATRIX_ACTIVITY, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BusException("Error while executing HTTP request!", e);
        }        
    }
    
    /**
     * Gets the messages for a room. Use from and to parameters to paginate. Direction is always backwards.
     * 
     * @param roomId
     * @param from
     * @param to
     * @throws BusException 
     */
    private Messages getMessagesFromRoom(String roomId, String from, String to) throws BusException {
        
        // Init
        Map<String, String> parameters = new HashMap<String, String>();
        Builder request;
        
        // Prepare request including parameters
        if (from != null) {
            parameters.put("from", from);
        }
        if (to != null) {
            parameters.put("to", to);
        }
        parameters.put("dir", "b");
        request = this.connection.getBuilder(String.format(PATH_GET_MESSAGES_FROM_ROOM_PATTERN, roomId), parameters);

        // Execute request
        FutureTask<Messages> future = new ExecutHTTPRequest<Messages>(request,
                ExecutHTTPRequest.REST_TYPE.GET,
                () -> getExecutor(),
                null,
                new Function<Response, Messages>() {

                    @Override
                    public Messages apply(Response response) {                        
                        try {
                            return  mapper.reader().readValue(response.readEntity(String.class), Messages.class);
                        } catch (IOException e) {
                            throw new IllegalStateException("Unable to understand response", e);
                        }
                    }
                    
                },
                ConnectionMatrix.DEFAULT_ERROR_HANDLER,
                this.connection,
                Resources.RETRY_MATRIX_ACTIVITY_NUMBER,
                Resources.RETRY_MATRIX_ACTIVITY_WAIT).execute();
        
        // Wait for task end or exception
        try {
            return future.get(Resources.TIMEOUT_MATRIX_ACTIVITY, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BusException("Error while executing HTTP request!", e);
        }
    }
    
    /**
     * Serialize an object.
     *
     * @param object
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private String serializeObject(Object o) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(bos));
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }
    
    /**
     * Deserialize an object
     *
     * @param serialued object
     * @return the object
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    private Object deserializeMessage(String msg) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(msg);
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(data)));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    @Override
    public FutureTask<Void>
           sendPlain(String recipient, String subject, String body) throws BusException {
        // TODO Check if necessary
        throw new UnsupportedOperationException();
    }
}