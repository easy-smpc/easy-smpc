package org.bihealth.mi.easysmpc.components;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Choose between two radio buttons
 * 
 * @author Felix Wirth
 *
 */
public class ComponentRadioEntry extends JPanel implements ActionListener {

    /** SVUID */
    private static final long serialVersionUID = -3142722039116254922L;   
    /** Radio button group */
    private  ButtonGroup group;
    /** First option */
    private JRadioButton firstOption;
    /** Second option */
    private JRadioButton secondOption;
    /** Radio panel */
    private JPanel radioPanel;
    /** Change listener */
    private ChangeListener listener;
    
    /**
     * Create a new instance with y-axis oriented radios
     * 
     * @param title
     * @param firstOptionText
     * @param secondOptionText
     */
    public ComponentRadioEntry(String title,
                        String firstOptionText,
                        String secondOptionText) {
        this(title, firstOptionText, secondOptionText, true);
    }
    
    /**
     * Create a new instance
     * 
     * @param title
     * @param firstOptionText
     * @param secondOptionText 
     * @param orientationRadios - true if y-oriented, else x-oriented
     */
    public ComponentRadioEntry(String title,
                        String firstOptionText,
                        String secondOptionText,
                        boolean orientationRadios) {
        // Init
        JPanel titlePanel = null;
        
        // Create Button groups with options
        group = new ButtonGroup();
        firstOption = new JRadioButton(firstOptionText);
        firstOption.addChangeListener(listener);
        firstOption.addActionListener(this);
        firstOption.setSelected(true);
        secondOption = new JRadioButton(secondOptionText);
        secondOption.addChangeListener(listener);
        secondOption.addActionListener(this);
        group.add(firstOption);
        group.add(secondOption);
        
        // Create radio panel
        radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, orientationRadios ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
        radioPanel.add(firstOption);
        radioPanel.add(secondOption);
        
        // Create title panel
        if (title != null) {
            titlePanel = new JPanel();
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
            titlePanel.add(new JLabel(title));
        }
        
        // Add to layout
        this.setBorder(new EmptyBorder(Resources.ROW_GAP, Resources.ROW_GAP, Resources.ROW_GAP, Resources.ROW_GAP));
        this.setLayout(new GridLayout(1, titlePanel == null ? 1 : 2, Resources.ROW_GAP, Resources.ROW_GAP));
        if (titlePanel != null) {
            this.add(titlePanel);
        }
        this.add(radioPanel);
    }
    
    /**
     * Is first option selected?
     * 
     * @return
     */
    public boolean isFirstOptionSelected() {
        return firstOption == null ? false : firstOption.isSelected();
    }
    
    /**
     * Set or unset first option and the respective inverse for the second option
     * 
     * @param selected
     */
    public void setFirstOptionSelected(boolean selected) {
        this.firstOption.setSelected(selected);
        this.secondOption.setSelected(!selected);
    }
    
    /**
     * Sets a change listener
     * @param listener
     */
    public void setChangeListener(ChangeListener listener) {
        this.firstOption.addChangeListener(listener);
        this.secondOption.addChangeListener(listener);
        this.listener = listener;
    }
    
    /**
     * Returns the change listener
     * 
     * @return
     */
    public ChangeListener getChangeListener() {
        return this.listener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // Empty
    }
}