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
package de.tu_darmstadt.cbs.emailsmpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;

import de.tu_darmstadt.cbs.emailsmpc.Study.StudyState;

/**
 * Initial message
 * @author Tobias Kussel
 */
public class MessageInitial implements Serializable {
    
    /** SVUID */
    private static final long serialVersionUID = 1631395617989735129L;
    
    /**
     * Decode message.
     *
     * @param msg the msg
     * @return the initial message
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws ClassNotFoundException the class not found exception
     */
    public static MessageInitial decodeMessage(String msg)
            throws IOException, IllegalArgumentException, ClassNotFoundException {
        Decoder decoder = Base64.getDecoder();
        ByteArrayInputStream stream = new ByteArrayInputStream(decoder.decode(msg));
        ObjectInputStream ois = new ObjectInputStream(stream);
        Object o = ois.readObject();
        if (!(o instanceof MessageInitial))
            throw new IllegalArgumentException("Message not of type InitialMessage");
        return (MessageInitial) o;
    }
    
    /**
     * Gets the app model.
     *
     * @param msg the msg
     * @return the app model
     */
    public static Study getAppModel(MessageInitial msg) {
        Study model = new Study();
        model.setStudyUID(msg.studyUID);
        model.setName(msg.name);
        model.setParticipants(msg.participants);
        model.setNumParticipants(msg.participants.length);
        model.setOwnId(msg.recipientId);
        model.setConnectionIMAPSettings(msg.connectionIMAPSettings);
        model.setState(StudyState.PARTICIPATING);
        model.setBins(new Bin[msg.bins.length]);
        for (int i = 0; i < msg.bins.length; i++) {
            model.getBins()[i] = MessageBin.getBin(msg.bins[i], model.getNumParticipants());
        }
        return model;
    }
    
    /** The name. */
    public String name;
    
    /** The participants. */
    public Participant[] participants;

    /** The bins. */
    public MessageBin[] bins;

    /** The recipient id. */
    public int recipientId;
    
    /** Connections settings for automatic mail processing. */
    public ConnectionIMAPSettings connectionIMAPSettings;
    
    /** The study UID. */
    public String studyUID;

    /**
     * Instantiates a new initial message.
     *
     * @param model the model
     * @param recipientId the recipient id
     */
    public MessageInitial(Study model, int recipientId) {
        this.studyUID = model.getStudyUID();
        this.name = model.getName();
        this.participants = model.getParticipants();
        this.recipientId = recipientId;
        this.connectionIMAPSettings = model.getConnectionIMAPSettings(); 
        this.bins = new MessageBin[model.getBins().length];
        for (int i = 0; i < model.getBins().length; i++) {
            bins[i] = new MessageBin(model.getBins()[i], recipientId);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MessageInitial other = (MessageInitial) obj;
        if (!Arrays.equals(bins, other.bins)) return false;
        if (connectionIMAPSettings == null) {
            if (other.connectionIMAPSettings != null) return false;
        } else if (!connectionIMAPSettings.equals(other.connectionIMAPSettings)) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        if (!Arrays.equals(participants, other.participants)) return false;
        if (recipientId != other.recipientId) return false;
        return true;
    }

    /**
     * Gets the message.
     *
     * @return the message
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String getMessage() throws IOException {
        Encoder encoder = Base64.getEncoder();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(this);
        return encoder.encodeToString(stream.toByteArray());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(bins);
        result = prime * result +
                 ((connectionIMAPSettings == null) ? 0 : connectionIMAPSettings.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + Arrays.hashCode(participants);
        result = prime * result + recipientId;
        return result;
    }
}
