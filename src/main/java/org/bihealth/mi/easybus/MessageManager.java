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
package org.bihealth.mi.easybus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import de.tu_darmstadt.cbs.emailsmpc.UIDGenerator;

/**
 * A message manager for the bus implementations
 * 
 * @author Felix Wirth
 *
 */
public class MessageManager {

    /** Maximal size of a single message in byte */
    private int                                  maxMessageSize;
    /** Message fragments */
    private final Map<String, MessageFragment[]> messagesFragments;

    /**
     * Creates a new instance
     * 
     * @param maxMessageSize in bytes
     */
    public MessageManager(int maxMessageSize) {
        
        // Store and init
        this.maxMessageSize = maxMessageSize;
        this.messagesFragments = new ConcurrentHashMap<>();       
    }
    
    /**
     * Splits a message into one or several MessageFragments
     * 
     * @param message
     * @return
     * @throws IOException
     */
    public MessageFragment[] splitMessage(Message message) throws IOException {

        // Create fragments of the serialized message
        List<String> fragmentList = SplitStringByByteLength(message.serialize(),
                                                            this.maxMessageSize);

        // Create objects from fragments
        int index = 0;
        String id = UIDGenerator.generateShortUID(10);
        MessageFragment[] result = new MessageFragment[fragmentList.size()];

        for (String fragment : fragmentList) {
            result[index] = new MessageFragment(id, index, fragmentList.size(), fragment);
            index++;
        }

        // Return
        return result;
    }    
    
    /**
     * Merges message fragments into a message 
     * 
     * @param message - null if not complete message or complete message
     * @return
     * @throws BusException 
     */
    public Message mergeMessage(MessageFragment messageFragment) throws BusException {
        
        // Get or create fragments array
        MessageFragment[] messageFragments = this.messagesFragments.computeIfAbsent(messageFragment.getMessageID(), new Function<String, MessageFragment[]>() {

            @Override
            public MessageFragment[] apply(String key) {
                return new MessageFragment[messageFragment.getNumberOfFragments()];
            }            
        });               
        
        // Check
        if (messageFragment.getNumberOfFragments() > messageFragments.length) {
            throw new BusException(String.format("Index for number of messages %d for new fragment does not suit to total number of messages %d for message %s",
                                                 messageFragment.getFragmentNumber(),
                                                 messageFragments.length,
                                                 messageFragment.getMessageID()));
        }

        // Add to list
        messageFragments[messageFragment.getFragmentNumber()] = messageFragment;
        
        // If message complete return or return null
        return messageComplete(messageFragments) ? buildMessage(messageFragment.getMessageID()) : null;              
    }
    
    /**
     * Builds a message object from a string
     * 
     * @param messageFragement
     * @return
     * @throws BusException 
     */
    private Message buildMessage(String messageId) throws BusException {
        
        // Init
        MessageFragment[] messageFragments = this.messagesFragments.get(messageId);
        String messageSerialized = "";
        Message message;

        // Loop over fragments to re-assemble string
        for (int index = 0; index < messageFragments.length; index++) {
            messageSerialized = messageSerialized + (String) messageFragments[index].getMessage();
            messageFragments[index].delete();
        }

        // Recreate message
        try {
            message =  Message.deserializeMessage(messageSerialized);            
        } catch (ClassNotFoundException | IOException e) {
            throw new BusException("Unable to deserialize message", e);
        }
        
        // Finish
        messageFragments[0].finalize();
        
        // Return
        return message;
    }
    
    /**
     * Is a message complete?
     * 
     * @param messageFragement
     * @return
     */
    private boolean messageComplete(MessageFragment[] messageFragement) {
        
        // Loop over array
        for(int index = 0; index < messageFragement.length; index++) {
            if(messageFragement[index] == null) {
                return false;
            }
        }  
        
        // Finished
        return true;
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

}