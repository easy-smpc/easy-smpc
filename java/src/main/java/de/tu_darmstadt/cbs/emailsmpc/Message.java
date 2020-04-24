package de.tu_darmstadt.cbs.emailsmpc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64.*;
import java.util.Base64;

public class Message {
    public final String recipientName;
    public final String recipientEmailAddress;
    public final String data;

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

    // Disallow default constructor to avoid illegal states
    private Message() {
        recipientName = null;
        recipientEmailAddress = null;
        data = null;
    }

    private String getHashedData(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA256");
            byte[] digest = md.digest((this.recipientName + this.recipientEmailAddress + data).getBytes());
            Encoder be = Base64.getEncoder();
            return data + "@" + be.encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean validateData(Participant recipient, String message) {
        if (!(message.contains("@")))
            return false;
        String[] parts = message.split("@");
        if (parts.length != 2)
            return false;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA256");
            byte[] digest = md.digest((recipient.name + recipient.emailAddress + parts[0]).getBytes());
            Encoder be = Base64.getEncoder();
            return parts[1] == be.encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getMessageData(Message msg) throws IllegalArgumentException {
        if (!(msg.data.contains("@")))
            throw new IllegalArgumentException("Message invalid");
        String[] parts = msg.data.split("@");
        if (parts.length != 2)
            throw new IllegalArgumentException("Message invalid");
        return parts[0];
    }
}
