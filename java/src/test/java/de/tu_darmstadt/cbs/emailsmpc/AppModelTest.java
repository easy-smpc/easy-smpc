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
    @Test
    public void AddingBins() {
        AppModel testmodel = new AppModel();
        Participant[] part = new Participant[3];
        Bin[] bins = new Bin[4];
        for (int i = 0; i < part.length; i++) {
            part[i] = new Participant("Participant " + i, "part" + i + "@test.com");
        }
        for (int i = 0; i < bins.length; i++) {
            bins[i] = new Bin("Bin " + i);
            bins[i].initialize(part.length);
            bins[i].shareValue(BigInteger.valueOf(3 * i + 7));
        }
        testmodel.advanceState(AppState.STARTING);
        testmodel.initializeStudy("Teststudy", part, bins);
        assertTrue(testmodel.bins.length == 4);
    }

    @Test
    public void AddingParticipants() {
        AppModel testmodel = new AppModel();
        Participant[] part = new Participant[3];
        Bin[] bins = new Bin[4];
        for (int i = 0; i < part.length; i++) {
            part[i] = new Participant("Participant " + i, "part" + i + "@test.com");
        }
        for (int i = 0; i < bins.length; i++) {
            bins[i] = new Bin("Bin " + i);
            bins[i].initialize(part.length);
            bins[i].shareValue(BigInteger.valueOf(3 * i + 7));
        }
        testmodel.advanceState(AppState.STARTING);
        testmodel.initializeStudy("Teststudy", part, bins);
        assertTrue(testmodel.participants.length == 3);
    }

    @Test
    public void SaveLoad() throws IOException, ClassNotFoundException {
        AppModel testmodel = new AppModel();
        Participant[] part = new Participant[3];
        Bin[] bins = new Bin[4];
        for (int i = 0; i < part.length; i++) {
            part[i] = new Participant("Participant " + i, "part" + i + "@test.com");
        }
        for (int i = 0; i < bins.length; i++) {
            bins[i] = new Bin("Bin " + i);
            bins[i].initialize(part.length);
            bins[i].shareValue(BigInteger.valueOf(3 * i + 7));
        }
        testmodel.advanceState(AppState.STARTING);
        testmodel.initializeStudy("Teststudy", part, bins);
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
