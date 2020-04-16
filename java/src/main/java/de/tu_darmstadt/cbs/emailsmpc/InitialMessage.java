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

class InitialMessageBin {
    public final String name;
    public final ArithmeticShare share;

    public InitialMessageBin(Bin bin, int recipientId) {
        this.name = bin.name;
        this.share = bin.getOutShare(recipientId);
    }

    public static Bin getBin(InitialMessageBin imb, int numParticipants) {
        Bin bin = new Bin(imb.name, numParticipants);
        bin.setInShare(imb.share, 0);
        return bin;
    }
}

public class InitialMessage implements Serializable {
    public String name;
    public Participant[] participants;
    public InitialMessageBin[] bins;
    public int recipientId;

    public InitialMessage(AppModel model, int recipientId) {
        this.name = model.name;
        this.participants = model.participants;
        this.recipientId = recipientId;
        this.bins = new InitialMessageBin[model.bins.length];
        for (int i = 0; i < model.bins.length; i++) {
            bins[i] = new InitialMessageBin(model.bins[i], recipientId);
        }
    }

    public static AppModel getAppModel(InitialMessage msg) {
        AppModel model = new AppModel();
        model.name = msg.name;
        model.participants = msg.participants;
        model.numParticipants = msg.participants.length;
        model.ownId = msg.recipientId;
        model.state = AppState.PARTICIPATING;
        model.bins = new Bin[msg.bins.length];
        for (int i = 0; i < msg.bins.length; i++) {
            model.bins[i] = InitialMessageBin.getBin(msg.bins[i], model.numParticipants);
        }
        return model;
    }

    public String getMessage() throws IOException {
        Encoder encoder = Base64.getEncoder();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(this);
        return encoder.encodeToString(stream.toByteArray());
    }

    public static InitialMessage decodeMessage(String msg)
            throws IOException, IllegalArgumentException, ClassNotFoundException {
        Decoder decoder = Base64.getDecoder();
        ByteArrayInputStream stream = new ByteArrayInputStream(decoder.decode(msg));
        ObjectInputStream ois = new ObjectInputStream(stream);
        Object o = ois.readObject();
        if (!(o instanceof InitialMessage))
            throw new IllegalArgumentException("Message not of type InitialMessage");
        return (InitialMessage) o;
    }

}
