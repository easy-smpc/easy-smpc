package de.tu_darmstadt.cbs.emailsmpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;

/**
 * A message to be sent between instances
 * @author Tobias Kussel
 */
public class Message implements Serializable, Cloneable {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -3994038144373807054L;
    
    /**
     * Deserialize message.
     *
     * @param msg the msg
     * @return the message
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public static Message deserializeMessage(String msg) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(msg);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Message message = (Message) ois.readObject();
        ois.close();
        return message;
    }
    
    /**
     * Gets the message data.
     *
     * @param msg the msg
     * @return the message data
     * @throws IllegalArgumentException the illegal argument exception
     */
    public static String getMessageData(Message msg) throws IllegalArgumentException {
        return getMessageData(msg.data);
    }
    
    /**
     * Gets the message data.
     *
     * @param msg the msg
     * @return the message data
     * @throws IllegalArgumentException the illegal argument exception
     */
    public static String getMessageData(String msg) throws IllegalArgumentException {
        if (!(msg.contains("@")))
            throw new IllegalArgumentException("Message invalid");
        String[] parts = msg.split("@");
        if (parts.length != 2)
            throw new IllegalArgumentException("Message invalid");
        return parts[0];
    }
    
    /**
     * Serialize message.
     *
     * @param msg the msg
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String serializeMessage(Message msg) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        oos.close();
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    /**
     * Validate data.
     *
     * @param senderID the sender ID
     * @param recipient the recipient
     * @param message the message
     * @return true, if successful
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
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

    /** The recipient name. */
    public final String recipientName;

    /** The recipient email address. */
    public final String recipientEmailAddress;

    /** The data. */
    public final String data;

    /** The sender ID. */
    public final int senderID;

    /**
     * Instantiates a new message.
     *
     * @param senderID the sender ID
     * @param recipient the recipient
     * @param data the data
     */
    public Message(int senderID, Participant recipient, String data) {
        this.senderID = senderID;
        this.recipientName = recipient.name;
        this.recipientEmailAddress = recipient.emailAddress;
        this.data = getHashedData(data);
    }

    /**
     * Instantiates a new message.
     *
     * @param senderID the sender ID
     * @param recipientName the recipient name
     * @param recipientEmailAddress the recipient email address
     * @param data the data
     */
    public Message(int senderID, String recipientName, String recipientEmailAddress, String data) {
        this.senderID = senderID;
        this.recipientName = recipientName;
        this.recipientEmailAddress = recipientEmailAddress;
        this.data = getHashedData(data);
    }

    /**
     * Disallow default constructor to avoid illegal states
     */
    @SuppressWarnings("unused")
    private Message() {
        recipientName = null;
        recipientEmailAddress = null;
        data = null;
        senderID = -1;
    }

    /**
     * Clone.
     *
     * @return the object
     */
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
        if (!(o instanceof Message))
            return false;
        Message m = (Message) o;
        return (m.senderID == senderID) && m.recipientName.equals(recipientName)
          && m.recipientEmailAddress.equals(recipientEmailAddress) && m.data.equals(data);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = recipientName.hashCode();
        result = 31 * result + senderID;
        result = 31 * result + recipientEmailAddress.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "From "+ String.valueOf(senderID) + " to " +recipientName + "<" + recipientEmailAddress + ">:\n" + data;
    }

    /**
     * Gets the hashed data.
     *
     * @param data the data
     * @return the hashed data
     */
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
}
