package de.tu_darmstadt.cbs.emailsmpc;

import java.math.BigInteger;
import java.io.Serializable;
import java.util.Base64.*;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import de.tu_darmstadt.cbs.secretshare.*;

public class ResultMessage implements Serializable {
    public MessageBin[] bins;
    public Participant sender;

    @Override
    public String toString() {
        String result = "Sender: " + sender + "\n";
        for (MessageBin b : bins) {
            result = result + b.toString() + "\n";
        }
        return result;
    }
    public ResultMessage(AppModel model) {
        this.sender = model.participants[model.ownId];
        this.bins = new MessageBin[model.bins.length];
        for (int i = 0; i < model.bins.length; i++) {
            this.bins[i] = new MessageBin(model.bins[i].name, model.bins[i].getSumShare());
        }
    }

    public String getMessage() throws IOException {
        Encoder encoder = Base64.getEncoder();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(this);
        return encoder.encodeToString(stream.toByteArray());
    }

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

    public static ResultMessage decodeAndVerify(String msg, Participant sender, AppModel model)
            throws IOException, ClassNotFoundException {
        ResultMessage sm = decodeMessage(msg);
        if (verify(sm, sender, model))
            return sm;
        else
            throw new IllegalArgumentException("Message invalid");
    }

    public static boolean verify(ResultMessage msg, Participant sender, AppModel model) {
        return msg.sender.equals(sender) && msg.bins.length == model.bins.length;
    }

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

    @Override
    public int hashCode() {
        int result = sender.hashCode();
        for (MessageBin b : bins) {
            result = 31 * result + b.hashCode();
        }
        return result;
    }
}
