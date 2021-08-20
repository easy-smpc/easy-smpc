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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Unit tests for the API
 * @author Tobias Kussel
 */
public class StudyTest {
    
    /**
     * Rigorous Test :-).
     *
     * @param numParticipants the num participants
     * @param numBins the num bins
     * @return the initialized model
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    static private Study getInitializedModel(int numParticipants, int numBins) throws IllegalStateException, IOException {
        Study testmodel = new Study();
        Participant[] part = new Participant[numParticipants];
        Bin[] bins = new Bin[numBins];
        for (int i = 0; i < part.length; i++) {
            part[i] = new Participant("Participant " + i, "part" + i + "@test.com");
        }
        for (int i = 0; i < bins.length; i++) {
            bins[i] = new Bin("Bin " + i);
            bins[i].initialize(part.length);
            bins[i].shareValue(BigInteger.valueOf(3 * i + 7));
        }
        testmodel.toStarting();
        testmodel.toInitialSending("Teststudy", part, bins, null);
        return testmodel;
    }

    /**
     * Gets the initialized model.
     *
     * @param numParticipants the num participants
     * @param numBins the num bins
     * @param array the array
     * @return the initialized model
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    static private Study getInitializedModel(int numParticipants, int numBins, BigInteger[] array) throws IllegalStateException, IOException {
        Study testmodel = new Study();
        Participant[] part = new Participant[numParticipants];
        Bin[] bins = new Bin[numBins];
        for (int i = 0; i < part.length; i++) {
            part[i] = new Participant("Participant " + i, "part" + i + "@test.com");
        }
        for (int i = 0; i < bins.length; i++) {
            bins[i] = new Bin("Bin " + i);
            bins[i].initialize(part.length);
            bins[i].shareValue(array[i]);
        }
        testmodel.toStarting();
        testmodel.toInitialSending("Teststudy", part, bins, null);
        return testmodel;
    }
    
    /**
     * Adding bins.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void AddingBins() throws IllegalStateException, IOException {
        Study testmodel = StudyTest.getInitializedModel(3, 4);
        assertTrue(testmodel.getBins().length == 4);
    }

    /**
     * Adding participants.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void AddingParticipants() throws IllegalStateException, IOException {
        Study testmodel = StudyTest.getInitializedModel(3, 4);
        assertTrue(testmodel.getParticipants().length == 3);
    }

    /**
     * Clone test.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void CloneTest() throws IllegalStateException, IOException {
        BigInteger[] secrets0 = { BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3),
                BigInteger.valueOf(4) };
        Study model0 = StudyTest.getInitializedModel(3, 4, secrets0);
        Study copy = (Study) model0.clone();
        assertTrue((copy != model0));
        assertTrue(copy.equals(model0));
        assertTrue(copy.getStudyUID().equals(model0.getStudyUID()));
    }
    
    /**
     * Non colliding UID.
     */
    @Test
    public void NonCollidingUID(){
      Set<String> ids = new HashSet<String>();
      for (int i = 0; i < 1000; i++ ) {
        Study model = new Study();
        assertTrue(!ids.contains(model.getStudyUID()));
        ids.add(model.getStudyUID());
      }
    }

    /**
     * Save load.
     *
     * @throws ClassNotFoundException the class not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void SaveLoad() throws ClassNotFoundException, IOException {
        Study testmodel = StudyTest.getInitializedModel(3, 4);
            File fn = new File("testing.dat");
            testmodel.setFilename(fn);
            testmodel.saveProgram();
            Study load = Study.loadModel(fn);
            assertTrue(load.equals(testmodel));
    }

    /**
     * Test with three.
     *
     * @throws ClassNotFoundException the class not found exception
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    @Test
    public void TestWithThree() throws ClassNotFoundException, IllegalStateException, IOException, IllegalArgumentException, NoSuchAlgorithmException {
        BigInteger[] secrets0 = { BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3),
                BigInteger.valueOf(4) };
        Study model0 = StudyTest.getInitializedModel(3, 4, secrets0);
        Study model1 = new Study();
        Study model2 = new Study();
        model1.toParticipating();
        model2.toParticipating();
        Message initialMessage1 = model0.getUnsentMessageFor(1);
        Message initialMessage2 = model0.getUnsentMessageFor(2);
        model0.markMessageSent(1);
        model0.markMessageSent(2);
        model1.toEnteringValues(initialMessage1.data);
        model2.toEnteringValues(initialMessage2.data);
        BigInteger[] secrets1 = { BigInteger.valueOf(7 * 1), BigInteger.valueOf(7 * 2), BigInteger.valueOf(7 * 3),
                BigInteger.valueOf(7 * 4) };
        BigInteger[] secrets2 = { BigInteger.valueOf(11 * 1), BigInteger.valueOf(11 * 2), BigInteger.valueOf(11 * 3),
                BigInteger.valueOf(0) };
        model1.toSendingShares(secrets1);
        model2.toSendingShares(secrets2);
        Message share10 = model1.getUnsentMessageFor(0);
        Message share12 = model1.getUnsentMessageFor(2);
        Message share20 = model2.getUnsentMessageFor(0);
        Message share21 = model2.getUnsentMessageFor(1);
        model1.markMessageSent(0);
        model1.markMessageSent(2);
        model2.markMessageSent(0);
        model2.markMessageSent(1);
        model0.toRecievingShares();
        model1.toRecievingShares();
        model2.toRecievingShares();
        model0.setShareFromMessage(share10);
        model0.setShareFromMessage(share20);
        model1.setShareFromMessage(share21);
        model2.setShareFromMessage(share12);
        model0.toSendingResult();
        model1.toSendingResult();
        model2.toSendingResult();
        Message result01 = model0.getUnsentMessageFor(1);
        Message result02 = model0.getUnsentMessageFor(2);
        Message result10 = model1.getUnsentMessageFor(0);
        Message result12 = model1.getUnsentMessageFor(2);
        Message result20 = model2.getUnsentMessageFor(0);
        Message result21 = model2.getUnsentMessageFor(1);
        model0.markMessageSent(1);
        model0.markMessageSent(2);
        model1.markMessageSent(0);
        model1.markMessageSent(2);
        model2.markMessageSent(0);
        model2.markMessageSent(1);
        model0.toRecievingResult();
        model1.toRecievingResult();
        model2.toRecievingResult();
        model0.setShareFromMessage(result10);
        model0.setShareFromMessage(result20);
        model1.setShareFromMessage(result01);
        model1.setShareFromMessage(result21);
        model2.setShareFromMessage(result02);
        model2.setShareFromMessage(result12);
        model0.toFinished();
        model1.toFinished();
        model2.toFinished();
        BinResult[] sum0 = model0.getAllResults();
        BinResult[] sum1 = model1.getAllResults();
        BinResult[] sum2 = model2.getAllResults();
        assertTrue(sum0.length == sum1.length);
        assertTrue(sum0.length == sum2.length);
        BigInteger[] sum = { BigInteger.valueOf(1 + 7 * 1 + 11 * 1), BigInteger.valueOf(2 + 7 * 2 + 11 * 2),
                BigInteger.valueOf(3 + 7 * 3 + 11 * 3), BigInteger.valueOf(4 + 7 * 4 + 0), };
        for (int i = 0; i < sum0.length; i++) {
            assertTrue(sum0[i].equals(sum1[i]));
            assertTrue(sum0[i].equals(sum2[i]));
            assertTrue(sum0[i].value.equals(sum[i]));
        }
    }
}