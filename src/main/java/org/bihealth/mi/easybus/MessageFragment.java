package org.bihealth.mi.easybus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.tu_darmstadt.cbs.emailsmpc.UIDGenerator;

/**
 * Represents a fragment/splitted part of a { @link org.bihealth.mi.easybus.message message}
 * 
 * @author Felix Wirth
 *
 */
public class MessageFragment implements Serializable {
    
    /** SVUID */
    private static final long serialVersionUID = -14070272349211270L;
    /** Number of this message fragment */
    private final int         splitNr;
    /** Total of fragments */
    private final int         splitTotal;
    /** Id of message (same over all fragments) */
    private final String      id;
    /** The fragment content */
    private final String      content;
    
    /**
     * Creates a new instance
     * 
     * @param id
     * @param splitNr
     * @param splitTotal
     * @param content
     */
    public MessageFragment(String id, int splitNr, int splitTotal, String content) {
        // Check
        if(id == null || content == null) {
            throw new IllegalArgumentException("Id and content can not be null!");
        }
        
        if(splitNr >= splitTotal || splitTotal < 1) {
            throw new IllegalArgumentException("Please provide a number >= 1 for splitTotal and a splitNr < splitTotal");
        }
        
        // Store
        this.id = id;
        this.splitNr = splitNr;
        this.splitTotal = splitTotal;
        this.content = content;
    }

    /**
     * @return the splitNr
     */
    public int getSplitNr() {
        return splitNr;
    }

    /**
     * @return the splitTotal
     */
    public int getSplitTotal() {
        return splitTotal;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }       

    /**
     * Creates the number of fragments necessary when splitting the message into the given size 
     * 
     * @param message
     * @param sizeFragement - Size of fragment
     * @return the fragments
     * @throws IOException 
     */
    public static List<MessageFragment> createInternalMessagesFromMessage(Message message, int sizeFragement) throws IOException {
        
        // Prepare
        List<MessageFragment> result = new ArrayList<>();
        
        // Create fragments of the serialized message
        List<String> fragmentList = SplitStringByByteLength(message.serialize(), sizeFragement);
        
        // Create objects from fragments
        int index = 0;
        String id = UIDGenerator.generateShortUID(10);
        for(String fragment : fragmentList) {
            result.add(new MessageFragment(id, index, fragmentList.size(), fragment));
            index++;
        }        
        
        // Return 
        return result;
    }
    
    /**
     * Splits a string into a list of strings suiting the size limit
     * Derived from: https://stackoverflow.com/questions/48868721/splitting-a-string-with-byte-length-limits-in-java
     * 
     * @param src
     * @param encoding
     * @param maxsize
     * @return
     */
    public static List<String> SplitStringByByteLength(String src, int maxsize) {
        
        // Prepare
        Charset cs = Charset.forName("UTF-8");
        CharsetEncoder coder = cs.newEncoder();
        ByteBuffer out = ByteBuffer.allocate(maxsize);
        CharBuffer in = CharBuffer.wrap(src);
        List<String> stringList = new ArrayList<>();
        int pos = 0;
        
        // Create result
        while (true) {
            // Encode a current chunk
            CoderResult cr = coder.encode(in, out, true); 
            int newpos = src.length() - in.length();
            String s = src.substring(pos, newpos);
            
            // Add
            stringList.add(s);
            
            // Set new start position and rewind buffer
            pos = newpos; 
            out.rewind();
            
            // Finished
            if (!cr.isOverflow()) {
                break; 
            }
        }
        
        // Return
        return stringList;
    }
    
    /**
     * Serializes this messages to string
     * 
     * @return
     * @throws IOException
     */
    public String serialize() throws IOException {
        return serializeMessage(this);
    }
    
    /**
     * Serialize message as base64 encoded string
     *
     * @param msg the msg
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String serializeMessage(MessageFragment msg) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(bos));
        oos.writeObject(msg);
        oos.close();
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }
    
    /**
     * Deserialize message from base64 encoded string
     *
     * @param msg the msg
     * @return the message
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public static MessageFragment deserializeMessage(String msg) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(msg);
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(data)));
        MessageFragment message = (MessageFragment) ois.readObject();
        ois.close();
        return message;
    }
}
