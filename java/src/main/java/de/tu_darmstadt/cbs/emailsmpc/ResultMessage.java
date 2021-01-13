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
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * Message for results
 * @author Tobias Kussel
 */
public class ResultMessage implements Serializable {
    
    /**  SVUID. */
    private static final long serialVersionUID = -4200808171593709179L;
    
    /**
     * Decode and verify.
     *
     * @param msg the msg
     * @param sender the sender
     * @param model the model
     * @return the result message
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public static ResultMessage decodeAndVerify(String msg, Participant sender, AppModel model)
            throws IOException, ClassNotFoundException {
        ResultMessage rm = decodeMessage(msg);
        if (verify(rm, sender, model))
            return rm;
        else
            throw new IllegalArgumentException("Message invalid");
    }
    
    /**
     * Decode message.
     *
     * @param msg the msg
     * @return the result message
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws ClassNotFoundException the class not found exception
     */
    public static ResultMessage decodeMessage(String msg)
            throws IOException, IllegalArgumentException, ClassNotFoundException {
        Decoder decoder = Base64.getDecoder();
        ByteArrayInputStream stream = new ByteArrayInputStream(decoder.decode(msg));
        ObjectInputStream ois = new ObjectInputStream(stream);
        Object o = ois.readObject();
        if (!(o instanceof ResultMessage))
            throw new IllegalArgumentException("Message invalid");
        return (ResultMessage) o;
    }

    /**
     * Verify.
     *
     * @param msg the msg
     * @param sender the sender
     * @param model the model
     * @return true, if successful
     */
    public static boolean verify(ResultMessage msg, Participant sender, AppModel model) {
        return msg.sender.equals(sender) && msg.bins.length == model.bins.length;
    }

    /** The bins. */
    public MessageBin[] bins;

    /** The sender. */
    public Participant sender;

    /**
     * Instantiates a new result message.
     *
     * @param model the model
     */
    public ResultMessage(AppModel model) {
        sender = model.participants[model.ownId];
        bins = new MessageBin[model.bins.length];
        for (int i = 0; i < model.bins.length; i++) {
            bins[i] = new MessageBin(model.bins[i].name, model.bins[i].getSumShare());
        }
    }

    /**
     * Equals.
     *
     * @param o the o
     * @return true, if successful
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ResultMessage))
            return false;
        ResultMessage msg = (ResultMessage) o;
        if (bins.length != msg.bins.length)
            return false;
        boolean equal = this.sender.equals(msg.sender);
        for (int i = 0; i < bins.length; i++) {
            equal = equal && bins[i].equals(msg.bins[i]);
            if (equal == false) // Short circuit comparisons if false
                return false;
        }
        return equal;
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

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = sender.hashCode();
        for (MessageBin b : bins) {
            result = 31 * result + b.hashCode();
        }
        return result;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        String result = "Sender: " + sender + "\n";
        for (MessageBin b : bins) {
            result = result + b.toString() + "\n";
        }
        return result;
    }
}
