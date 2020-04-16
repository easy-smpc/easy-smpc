package de.tu_darmstadt.cbs.emailsmpc;

public class AppModel {
    public int numParticipants;
    public AppState state;
    private Bin[] bins;
    public Participant[] participants;
    public String name;
    public String[] unsentMessages;

    public AppModel() {
        name = null;
        numParticipants = 0;
        state = AppState.NONE;
        bins = null;
        participants = null;
        unsentMessages = null;
    }

    public void initializeStudy(String name, Participant[] participants, Bin[] bins) throws IllegalStateException {
        if (state != AppState.NONE || state != AppState.STARTING)
            throw new IllegalStateException("Unable to initialize study at state" + state);
        this.name = name;
        numParticipants = participants.length;
        for (Bin bin : bins) {
            if (!(bin.isInitialized()))
                throw new IllegalStateException("Unable to initialize bin " + bin.name);
            bin.initialize(numParticipants);
        }
        this.bins = bins;
        this.participants = participants;
        if (state == AppState.NONE)
            state = AppState.STARTING;
    }
    /**
     * Legal State Transitions:
     * +-----------------+     +------------------+
     * |    Starting     | <-- |       None       |
     * +-----------------+     +------------------+
     *   |                       |
     *   |                       |
     *   v                       v
     * +-----------------+     +------------------+
     * | Initial_Sending |     |  Participating   |
     * +-----------------+     +------------------+
     *   |                       |
     *   |                       |
     *   |                       v
     *   |                     +------------------+
     *   |                     |  Sending_Share   |
     *   |                     +------------------+
     *   |                       |
     *   |                       |
     *   |                       v
     *   |                     +------------------+
     *   +-------------------> | Recieving_Share  |
     *                         +------------------+
     *                           |
     *                           |
     *                           v
     *                         +------------------+
     *                         |  Sending_Result  |
     *                         +------------------+
     *                           |
     *                           |
     *                           v
     *                         +------------------+
     *                         | Recieving_Result |
     *                         +------------------+
     *                           |
     *                           |
     *                           v
     *                         +------------------+
     *                         |     Finished     |
     *                         +------------------+
     */

}
