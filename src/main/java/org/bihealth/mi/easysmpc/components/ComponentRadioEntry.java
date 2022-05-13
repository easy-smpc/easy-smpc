package org.bihealth.mi.easysmpc.components;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easysmpc.resources.Resources;

public class ComponentRadioEntry extends JPanel implements ItemListener {

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
    ComponentRadioEntry(String title,
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
     * @param orientationRadiosYAxis  
     */
    ComponentRadioEntry(String title,
                        String firstOptionText,
                        String secondOptionText,
                        boolean orientationRadiosYAxis) {
        // Init
        JPanel titlePanel = null;
        
        // Create Button groups with options
        group = new ButtonGroup();
        firstOption = new JRadioButton(firstOptionText);
        firstOption.addChangeListener(listener);
        firstOption.addItemListener(this);
        firstOption.setSelected(true);
        secondOption = new JRadioButton(secondOptionText);
        secondOption.addChangeListener(listener);
        group.add(firstOption);
        group.add(secondOption);
        
        // Create radio panel
        radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, orientationRadiosYAxis ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
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
        return firstOption.isSelected();
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

    @Override
    public void itemStateChanged(ItemEvent e) {
       if(this.listener != null) {
           this.listener.stateChanged(new ChangeEvent(this));
       }        
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
}