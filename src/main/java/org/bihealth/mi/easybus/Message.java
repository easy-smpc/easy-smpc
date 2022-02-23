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
package org.bihealth.mi.easybus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The message a party is sending
 * 
 * @author Felix Wirth
 *
 */
public class Message implements Serializable {
    
    /** SVUID */
    private static final long serialVersionUID = 1557145500504512577L;    
    /** The message content */
    private Object messageContent;
    /** Participant to respond to on this message */
    private Participant respondTo;
    /** A response will be sent with this reply id */
    private String replyID;
    
    /**
     * Creates a new instance
     */
    public Message(Object messageContent){
       this(messageContent, null, null);
    }
    
    public Message(Object messageContent, Participant respondTo, String replyID) {
        this.messageContent = messageContent;
        this.respondTo = respondTo;
        this.replyID = replyID;
    }
    
    /**
     * Returns the message
     */
    public Object getMessage(){
        return this.messageContent;
    }
    
    /**
     * Returns the respond to participant
     */
    public Participant getRespondTo(){
        return this.respondTo;
    }
    
    /**
     * Returns the reply id
     */
    public String getReplyID(){
        return this.replyID;
    }
    
    /**
     * Serializes this messages to string
     * 
     * @return
     * @throws IOException
     */
    public String serialize() throws IOException {
        return serializeMessage(this);
    }
    
    /**
     * Serialize message.
     *
     * @param msg the msg
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String serializeMessage(Message msg) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(bos));
        oos.writeObject(msg);
        oos.close();
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }
    
    /**
     * Deserialize message.
     *
     * @param msg the msg
     * @return the message
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public static Message deserializeMessage(String msg) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(msg);
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(data)));
        Message message = (Message) ois.readObject();
        ois.close();
        return message;
    }
}