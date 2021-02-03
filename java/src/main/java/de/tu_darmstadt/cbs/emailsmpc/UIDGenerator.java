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
package de.tu_darmstadt.cbs.emailsmpc;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

/**
 * This class implements a UID Generator.
 *
 * @author Tobias Kussel
 */
public class UIDGenerator {

    /** The Constant HEX_ALPHABET. */
    private static final char[] HEX_ALPHABET = "0123456789ABCDEF".toCharArray();

    /**
     * Converts a byte array to an hex string.
     *
     * @param bytes
     *            input byte array
     * @return return string
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ALPHABET[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ALPHABET[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Generates a short random UID by generating a uuid and xoring the bytes
     * until a given size of hex chars is reached. This increases collision
     * probability so use a reasonable size.
     *
     * @param size
     *            Size of resulting UID in hex chars. Must be power of 2.
     * @return UID String
     * @throws IllegalStateException
     *             the illegal state exception
     */
    public static String generateShortUID(int size) throws IllegalStateException {
        if ((size % 2) != 0) throw new IllegalArgumentException("Invalid size");
        int numSplits = (int) (6 - Math.log(size) / Math.log(2)); // bisecting 265bit hash leads to full byte hex chars
        try {
            MessageDigest salt = MessageDigest.getInstance("SHA-256");
            salt.update(UUID.randomUUID().toString().getBytes("UTF-8"));
            byte[] uuidArray = salt.digest();
            for (int i = 0; i < numSplits; i++) {
                int newLength = uuidArray.length / 2;
                byte[] splitA = new byte[newLength];
                byte[] splitB = new byte[newLength];
                splitA = Arrays.copyOfRange(uuidArray, 0, newLength); // copy of range excludes endpoint
                splitB = Arrays.copyOfRange(uuidArray, newLength, uuidArray.length);
                for (int j = 0; j < splitA.length; j++) {
                    splitA[j] = (byte) (splitA[j] ^ splitB[j]);
                }
                uuidArray = splitA;
            }
            return bytesToHex(uuidArray);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Can not generate UID");
        }
    }
}
