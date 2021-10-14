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
import java.util.Objects;

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
        model.studyUID = msg.studyUID;
        model.name = msg.name;
        model.participants = msg.participants;
        model.numParticipants = msg.participants.length;
        model.ownId = msg.recipientId;
        model.automatedMode = msg.automatedMode;
        model.state = StudyState.PARTICIPATING;
        model.bins = new Bin[msg.bins.length];
        for (int i = 0; i < msg.bins.length; i++) {
            model.bins[i] = MessageBin.getBin(msg.bins[i], model.numParticipants);
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
    
    /** Are e-mails processed manually or automatically */
    public boolean automatedMode;
    
    /** The study UID. */
    public String studyUID;

    /**
     * Instantiates a new initial message.
     *
     * @param model the model
     * @param recipientId the recipient id
     */
    public MessageInitial(Study model, int recipientId) {
        this.studyUID = model.studyUID;
        this.name = model.name;
        this.participants = model.participants;
        this.recipientId = recipientId;
        this.automatedMode = model.automatedMode;
        this.bins = new MessageBin[model.bins.length];
        for (int i = 0; i < model.bins.length; i++) {
            bins[i] = new MessageBin(model.bins[i], recipientId);
        }
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
        result = prime * result + Arrays.hashCode(participants);
        result = prime * result + Objects.hash(automatedMode, name, recipientId, studyUID);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MessageInitial other = (MessageInitial) obj;
        return automatedMode == other.automatedMode && Arrays.equals(bins, other.bins) &&
               Objects.equals(name, other.name) &&
               Arrays.equals(participants, other.participants) &&
               recipientId == other.recipientId && Objects.equals(studyUID, other.studyUID);
    }
}
