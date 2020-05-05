package de.tu_darmstadt.cbs.emailsmpc;

import static org.junit.Assert.assertTrue;
import java.math.BigInteger;
import java.io.File;
import java.io.IOException;

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
}
