package org.bihealth.mi.easysmpc.components;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A component to display and enter data which can be represented as a list
 * 
 * @author Felix Wirth
 *
 */
public abstract class ComponentListData<T> extends JPanel implements ChangeListener {

    /** SVUID */
    private static final long               serialVersionUID = -3441370491038684638L;
    /** Input data */
    private final List<T>                   inputData;
    /** Change listener */
    private final ChangeListener            listener;

    public ComponentListData(List<T> inputData, ChangeListener listener) {
        // Super
        super();

        // Store
        this.inputData = inputData;
        this.listener = listener;
    }

    /**
     * Returns whether all values are valid
     */
    public abstract boolean areValuesValid();

    /**
     * Returns the data
     * 
     * @return
     */
    public List<T> getOutputData() {

        if (!areValuesValid()) { return null; }

        return collectOutputData();
    }

    /**
     * Collects and returns the output data
     * 
     * @return
     */
    protected abstract List<T> collectOutputData();
    
    /**
     * Returns the number of the filled rows, thus the numbers of rows a list in getOutputtData would have
     * 
     * @return
     */
    public abstract int getLength();

    /**
     * @return the listener
     */
    public ChangeListener getListener() {
        return listener;
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        listener.stateChanged(e);
    }
    
    /**
     * Removes empty lines in the data
     */
    public abstract void removeEmptyLines();

    /**
     * Removes all data
     */
    public abstract void reset();
    // TODO remove?
    
    /**
     * 
     * @return the inputData
     */
    protected List<T> getInputData() {
        return inputData;
    }
}
