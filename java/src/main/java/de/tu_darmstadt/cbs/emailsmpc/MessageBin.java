package de.tu_darmstadt.cbs.emailsmpc;

import java.io.Serializable;
import de.tu_darmstadt.cbs.secretshare.*;

public class MessageBin implements Serializable {
    public final String name;
    public final ArithmeticShare share;

    public MessageBin(Bin bin, int recipientId) {
        this.name = bin.name;
        this.share = bin.getOutShare(recipientId);
    }

    public MessageBin(String name, ArithmeticShare share) {
        this.name = name;
        this.share = share;
    }

    public static Bin getBin(MessageBin mb, int numParticipants) {
        Bin bin = new Bin(mb.name, numParticipants);
        bin.setInShare(mb.share, 0);
        return bin;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof MessageBin))
            return false;
        MessageBin bin = (MessageBin) o;
        return this.name.equals(bin.name) && this.share.equals(bin.share);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        return 31 * result + share.hashCode();
    }
}
