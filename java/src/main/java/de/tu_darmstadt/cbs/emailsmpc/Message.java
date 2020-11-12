package de.tu_darmstadt.cbs.emailsmpc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64.*;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable, Cloneable {
    public final String recipientName;
    public final String recipientEmailAddress;
    public final String data;
    public final int senderID;
    private static final long serialVersionUID = -3994038144373807054L;

    public Message(int senderID, Participant recipient, String data) {
        this.senderID = senderID;
        this.recipientName = recipient.name;
        this.recipientEmailAddress = recipient.emailAddress;
        this.data = getHashedData(data);
    }

    public Message(int senderID, String recipientName, String recipientEmailAddress, String data) {
        this.senderID = senderID;
        this.recipientName = recipientName;
        this.recipientEmailAddress = recipientEmailAddress;
        this.data = getHashedData(data);
    }

    // Disallow default constructor to avoid illegal states
    private Message() {
        recipientName = null;
        recipientEmailAddress = null;
        data = null;
        senderID = -1;
    }

    private String getHashedData(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((String.valueOf(this.senderID) + this.recipientName + this.recipientEmailAddress + data).getBytes());
            Encoder be = Base64.getEncoder();
            return data + "@" + be.encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean validateData(int senderID, Participant recipient, String message) throws NoSuchAlgorithmException {
        if (!(message.contains("@")))
            return false;
        String[] parts = message.split("@");
        if (parts.length != 2)
            return false;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest((String.valueOf(senderID) + recipient.name + recipient.emailAddress + parts[0]).getBytes());
        Encoder be = Base64.getEncoder();
        return parts[1].equals(be.encodeToString(digest));
    }

    public static String getMessageData(Message msg) throws IllegalArgumentException {
        return getMessageData(msg.data);
    }

    public static String getMessageData(String msg) throws IllegalArgumentException {
        if (!(msg.contains("@")))
            throw new IllegalArgumentException("Message invalid");
        String[] parts = msg.split("@");
        if (parts.length != 2)
            throw new IllegalArgumentException("Message invalid");
        return parts[0];
    }

    public static String serializeMessage(Message msg) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        oos.close();
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    public static Message deserializeMessage(String msg) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(msg);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Message message = (Message) ois.readObject();
        ois.close();
        return message;
    }

    @Override
    public String toString() {
        return "From "+ String.valueOf(senderID) + " to " +recipientName + "<" + recipientEmailAddress + ">:\n" + data;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Message))
            return false;
        Message m = (Message) o;
        return (m.senderID == senderID) && m.recipientName.equals(recipientName)
          && m.recipientEmailAddress.equals(recipientEmailAddress) && m.data.equals(data);
    }

    @Override
    public int hashCode() {
        int result = recipientName.hashCode();
        result = 31 * result + senderID;
        result = 31 * result + recipientEmailAddress.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }

    @Override
    public Object clone() {
      try {
        return (Message) super.clone();
      } catch (CloneNotSupportedException e) {
        try {
          return Message.deserializeMessage(Message.serializeMessage(this));
        } catch (Exception er) {
          throw new RuntimeException("Cloning a Message seriously went wrong!");
        }
      }
    }
}
