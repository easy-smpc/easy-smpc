package org.bihealth.mi.easysmpc.components;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

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
     * Create a new instance
     * 
     * @param title
     * @param upperOptionText
     * @param lowerOptionText
     */
    ComponentRadioEntry(String title, String upperOptionText, String lowerOptionText) {
        // Create Button groups with options
        group = new ButtonGroup();
        upperOption = new JRadioButton(upperOptionText);
        lowerOption = new JRadioButton(lowerOptionText);
        group.add(upperOption);
        group.add(lowerOption);
        
        // Create radio panel
        radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        radioPanel.add(upperOption);
        radioPanel.add(lowerOption);
        
        // Add to layout
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        JTextArea titleArea = new JTextArea(title);
        titleArea.setEditable(false);
        this.add(titleArea);
        this.add(radioPanel);
    }
    
    /**
     * Is upper option selected?
     * 
     * @return
     */
    public boolean isUpperOptionSelected() {
        return upperOption.isSelected();
    }
}