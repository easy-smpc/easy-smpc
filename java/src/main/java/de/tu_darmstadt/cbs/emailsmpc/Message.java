package de.tu_darmstadt.cbs.emailsmpc;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;

public class Message implements Serializable {
    public final String       recipientName;
    public final String       recipientEmailAddress;
    public final String       data;
    private static final long serialVersionUID = -3994038144373807054L;

    public Message(Participant recipient, String data) {
        this.recipientName = recipient.name;
        this.recipientEmailAddress = recipient.emailAddress;
        this.data = getHashedData(data);
    }

    public Message(String recipientName, String recipientEmailAddress, String data) {
        this.recipientName = recipientName;
        this.recipientEmailAddress = recipientEmailAddress;
        this.data = getHashedData(data);
    }

    // FW: Kontsuktor temporär eingefügt bis geklärt
    public Message(String recipientName, String recipientEmailAddress, String data, boolean temp) {
        this.recipientName = recipientName;
        this.recipientEmailAddress = recipientEmailAddress;
        this.data = data;
    }

    // Disallow default constructor to avoid illegal states
    private Message() {
        recipientName = null;
        recipientEmailAddress = null;
        data = null;
    }

    private String getHashedData(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((this.recipientName + this.recipientEmailAddress +
                                       data).getBytes());
            Encoder be = Base64.getEncoder();
            return data + "@" + be.encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean validateData(Participant recipient, String message) {
        if (!(message.contains("@"))) return false;
        String[] parts = message.split("@");
        if (parts.length != 2) return false;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((recipient.name + recipient.emailAddress +
                                       parts[0]).getBytes());
            Encoder be = Base64.getEncoder();
            return parts[1].equals(be.encodeToString(digest));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getMessageData(Message msg) throws IllegalArgumentException {
        return getMessageData(msg.data);
    }

    public static String getMessageData(String msg) throws IllegalArgumentException {
        if (!(msg.contains("@"))) throw new IllegalArgumentException("Message invalid");
        String[] parts = msg.split("@");
        if (parts.length != 2) throw new IllegalArgumentException("Message invalid");
        return parts[0];
    }

    @Override
    public String toString() {
        return recipientName + "<" + recipientEmailAddress + ">:\n" + data;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Message)) return false;
        Message m = (Message) o;
        return m.recipientName.equals(recipientName) &&
               m.recipientEmailAddress.equals(recipientEmailAddress) && m.data.equals(data);
    }

    @Override
    public int hashCode() {
        int result = recipientName.hashCode();
        result = 31 * result + recipientEmailAddress.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }
}
