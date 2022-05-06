package org.bihealth.mi.easysmpc.components;

import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easysmpc.resources.Resources;

public class ComponentRadioEntry extends JPanel {

    /** SVUID */
    private static final long serialVersionUID = -3142722039116254922L;
    
    /** Radio button group */
    private  ButtonGroup group;
    /** Upper option */
    private JRadioButton upperOption;
    /** Lower option */
    private JRadioButton lowerOption;
    /** Radio panel */
    private JPanel radioPanel;
    
    /**
     * Create a new instance with y-axis oriented radios
     * 
     * @param title
     * @param upperOptionText
     * @param lowerOptionText
     * @param listener
     */
    ComponentRadioEntry(String title,
                        String upperOptionText,
                        String lowerOptionText,
                        ChangeListener listener) {
        this(title, upperOptionText, lowerOptionText, listener, true);
    }
    
    /**
     * Create a new instance
     * 
     * @param title
     * @param upperOptionText
     * @param lowerOptionText
     * @param listener
     * @param orientationRadiosYAxis  
     */
    ComponentRadioEntry(String title,
                        String upperOptionText,
                        String lowerOptionText,
                        ChangeListener listener,
                        boolean orientationRadiosYAxis) {
        // Init
        JPanel titlePanel = null;
        
        // Create Button groups with options
        group = new ButtonGroup();
        upperOption = new JRadioButton(upperOptionText);
        upperOption.addChangeListener(listener);
        upperOption.setSelected(true);
        lowerOption = new JRadioButton(lowerOptionText);
        lowerOption.addChangeListener(listener);
        group.add(upperOption);
        group.add(lowerOption);
        
        // Create radio panel
        radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, orientationRadiosYAxis ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
        radioPanel.add(upperOption);
        radioPanel.add(lowerOption);
        
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
     * Is upper option selected?
     * 
     * @return
     */
    public boolean isFirstOptionSelected() {
        return upperOption.isSelected();
    }
    
    /**
     * Set or unset upper option and the respective inverse for the lower option
     * 
     * @param selected
     */
    public void setFirstOptionSelected(boolean selected) {
        this.upperOption.setSelected(selected);
        this.lowerOption.setSelected(!selected);
    }
}