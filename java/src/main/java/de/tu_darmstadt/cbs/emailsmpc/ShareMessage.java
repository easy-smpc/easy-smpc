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

public class ShareMessage implements Serializable {
    public MessageBin[] bins;
    public Participant recipient;
    public Participant sender;

    public ShareMessage(AppModel model, int recipientId) {
        this.recipient = model.participants[recipientId];
        this.sender = model.participants[model.ownId];
        this.bins = new MessageBin[model.bins.length];
        for (int i = 0; i < model.bins.length; i++) {
            bins[i] = new MessageBin(model.bins[i], recipientId);
        }
    }

    public String getMessage() throws IOException {
        Encoder encoder = Base64.getEncoder();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(this);
        return encoder.encodeToString(stream.toByteArray());
    }

    public static ShareMessage decodeMessage(String msg)
            throws IOException, IllegalArgumentException, ClassNotFoundException {
        Decoder decoder = Base64.getDecoder();
        ByteArrayInputStream stream = new ByteArrayInputStream(decoder.decode(msg));
        ObjectInputStream ois = new ObjectInputStream(stream);
        Object o = ois.readObject();
        if (!(o instanceof ShareMessage))
            throw new IllegalArgumentException("Message not of type ShareMessage");
        return (ShareMessage) o;
    }

    public static ShareMessage decodeAndVerify(String msg, Participant sender, AppModel model)
            throws IOException, ClassNotFoundException {
        ShareMessage sm = decodeMessage(msg);
        if (verify(sm, sender, model))
            return sm;
        else
            throw new IllegalArgumentException("Message invalid");
    }

    public static boolean verify(ShareMessage msg, Participant sender, AppModel model) {
        return msg.sender.equals(sender) && msg.recipient == model.participants[model.ownId]
                && msg.bins.length == model.bins.length;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ShareMessage))
            return false;
        ShareMessage msg = (ShareMessage) o;
        if (bins.length != msg.bins.length)
            return false;
        boolean equal = this.sender.equals(msg.sender);
        equal = equal && this.recipient.equals(msg.recipient);
        for (int i = 0; i < bins.length; i++) {
            equal = equal && bins[i].equals(msg.bins[i]);
            if (equal == false) // Short circuit comparisons if false
                return false;
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int result = recipient.hashCode();
        result = 31 * result + sender.hashCode();
        for (MessageBin b : bins) {
            result = 31 * result + b.hashCode();
        }
        return result;
    }
}
