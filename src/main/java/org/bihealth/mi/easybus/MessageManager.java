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
 * A message manager for the bus implementations.
 * Can be used to split larger messages into a series
 * of smaller message fragments.
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class MessageManager {

    /** Maximal size of a single message in byte */
    private int                                     maxMessageSize;
    /** Message fragments */
    private final Map<String, BusMessageFragment[]> messagesFragments;

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
     * Merges message fragments into a message 
     * 
     * @param message
     * @return A message or null if the parameter was a fragment and the message is not complete, yet.
     * @throws BusException 
     */
    public BusMessage mergeMessage(BusMessage message) throws BusException {
        
        // Check if fragment
        if (!(message instanceof BusMessageFragment)) {
            message.delete();
            message.expunge();
            return message;
        }
        
        // Convert to fragment
        BusMessageFragment messageFragment = (BusMessageFragment)message;
        
        // Get or create fragments array
        BusMessageFragment[] messageFragments = this.messagesFragments.computeIfAbsent(messageFragment.getMessageID(), new Function<String, BusMessageFragment[]>() {

            @Override
            public BusMessageFragment[] apply(String key) {
                return new BusMessageFragment[messageFragment.getNumberOfFragments()];
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
     * Splits a message into one or several MessageFragments
     * 
     * @param message
     * @return
     * @throws IOException
     */
    public BusMessage[] splitMessage(BusMessage message) throws IOException {

        // Create fragments of the serialized message
        List<String> fragments = splitStringByByteLength(message.getMessage(),
                                                         this.maxMessageSize);
        
        // Create objects from fragments
        int index = 0;
        String id = UIDGenerator.generateShortUID(10);
        BusMessage[] result = new BusMessage[fragments.size()];

        for (String fragment : fragments) {
            result[index] = new BusMessageFragment(message.getReceiver(),
                                                   message.getScope(),
                                                   fragment,
                                                   id, 
                                                   index++,
                                                   fragments.size());
        }

        // Return
        return result;
    }
    
    /**
     * Builds a message object from all fragments
     * 
     * @param messageId
     * @return
     * @throws BusException 
     */
    private BusMessage buildMessage(String messageId) throws BusException {
        
        // Init
        BusMessageFragment[] messageFragments = this.messagesFragments.get(messageId);
        String messageContent = "";

        // Loop over fragments to re-assemble string
        for (int index = 0; index < messageFragments.length; index++) {
            messageContent = messageContent + messageFragments[index].getMessage();
            messageFragments[index].delete();
        }
        
        // Finish
        messageFragments[0].expunge();
        
        // Return
        return new BusMessage(messageFragments[0].getReceiver(),
                              messageFragments[0].getScope(),
                              messageContent);
    }
    
    /**
     * Is a message complete?
     * 
     * @param fragments
     * @return
     */
    private boolean messageComplete(BusMessageFragment[] fragments) {
        
        // Loop over array
        for(int index = 0; index < fragments.length; index++) {
            if(fragments[index] == null) {
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
    private List<String> splitStringByByteLength(String src, int maxsize) {
        
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