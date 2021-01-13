package de.tu_darmstadt.cbs.emailsmpc;

/**
 * Enum for the app state
 * @author Tobias Kussel
 */
public enum AppState {
    
    /** The none. */
    NONE, 
 /** The starting. */
 STARTING, 
 /** The participating. */
 PARTICIPATING, 
 /** The entering values. */
 ENTERING_VALUES, 
 /** The initial sending. */
 INITIAL_SENDING, 
 /** The sending share. */
 SENDING_SHARE, 
 /** The recieving share. */
 RECIEVING_SHARE, 
 /** The sending result. */
 SENDING_RESULT, 
 /** The recieving result. */
 RECIEVING_RESULT, 
 /** The finished. */
 FINISHED
}
