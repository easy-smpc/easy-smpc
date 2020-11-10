package de.tu_darmstadt.cbs.emailsmpc;

import static org.junit.Assert.assertTrue;
import java.math.BigInteger;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppModelTest {
    /**
     * Rigorous Test :-)
     */
    static private AppModel getInitializedModel(int numParticipants, int numBins) {
        AppModel testmodel = new AppModel();
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
        testmodel.toInitialSending("Teststudy", part, bins);
        return testmodel;
    }

    static private AppModel getInitializedModel(int numParticipants, int numBins, BigInteger[] array) {
        AppModel testmodel = new AppModel();
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
        testmodel.toInitialSending("Teststudy", part, bins);
        return testmodel;
    }
    @Test
    public void CloneTest() {
        BigInteger[] secrets0 = { BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3),
                BigInteger.valueOf(4) };
        AppModel model0 = AppModelTest.getInitializedModel(3, 4, secrets0);
        AppModel copy = (AppModel) model0.clone();
        assertTrue((copy != model0));
        assertTrue(copy.equals(model0));
        assertTrue(copy.studyUID.equals(model0.studyUID));
    }

    @Test
    public void AddingBins() {
        AppModel testmodel = AppModelTest.getInitializedModel(3, 4);
        assertTrue(testmodel.bins.length == 4);
    }

    @Test
    public void AddingParticipants() {
        AppModel testmodel = AppModelTest.getInitializedModel(3, 4);
        assertTrue(testmodel.participants.length == 3);
    }
    @Test
    public void NonCollidingUID(){
      Set<String> ids = new HashSet<String>();
      for (int i = 0; i < 1000; i++ ) {
        AppModel model = new AppModel();
        assertTrue(!ids.contains(model.studyUID));
        ids.add(model.studyUID);
      }
    }

    @Test
    public void SaveLoad() throws IOException, ClassNotFoundException {
        AppModel testmodel = AppModelTest.getInitializedModel(3, 4);
        try {
            File fn = new File("testing.dat");
            testmodel.filename = fn;
            testmodel.saveProgram();
            AppModel load = AppModel.loadModel(fn);
            assertTrue(load.equals(testmodel));
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw e;
        }
    }

    @Test
    public void TestWithThree() throws IOException, ClassNotFoundException {
        BigInteger[] secrets0 = { BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3),
                BigInteger.valueOf(4) };
        AppModel model0 = AppModelTest.getInitializedModel(3, 4, secrets0);
        AppModel model1 = new AppModel();
        AppModel model2 = new AppModel();
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
        model0.setShareFromMessage(share10, model0.getParticipantFromId(1));
        model0.setShareFromMessage(share20, model0.getParticipantFromId(2));
        model1.setShareFromMessage(share21, model1.getParticipantFromId(2));
        model2.setShareFromMessage(share12, model2.getParticipantFromId(1));
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
        model0.setShareFromMessage(result10, model0.getParticipantFromId(1));
        model0.setShareFromMessage(result20, model0.getParticipantFromId(2));
        model1.setShareFromMessage(result01, model1.getParticipantFromId(0));
        model1.setShareFromMessage(result21, model1.getParticipantFromId(2));
        model2.setShareFromMessage(result02, model2.getParticipantFromId(0));
        model2.setShareFromMessage(result12, model2.getParticipantFromId(1));
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
