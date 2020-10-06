/**
 * 
 */
//TODO: GPL licence
package de.tu_darmstadt.cbs.app.templates;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.tu_darmstadt.cbs.app.Resources;
import lombok.Getter;

/**
 * @author Felix Wirth
 *
 */
public class BinEntry extends JPanel {
    /** SVUID */
    private static final long serialVersionUID = 7347360570010162977L;
    
    @Getter
    private JTextField binNameTextField;
    @Getter
    private JTextField binValueField;

    /**
     * Creates a new instance
     */
    public BinEntry() {
        
        this.setLayout(new GridLayout(0, 3, 0, 0));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, Resources.ROW_HEIGHT));
        JLabel binNameLabel = new JLabel(Resources.getString("BinEntry.0")); //$NON-NLS-1$
        this.add(binNameLabel);
        binNameTextField = new JTextField();
        this.add(binNameTextField);
        binNameTextField.setColumns(10);

        JPanel emptyPanel = new JPanel();
        this.add(emptyPanel);

        JLabel binValueLabel = new JLabel(Resources.getString("BinEntry.1")); //$NON-NLS-1$
        this.add(binValueLabel);

        this.binValueField = new JTextField();
        binValueField.setColumns(10);
        this.add(binValueField);
    }

    /**
     * Creates a new instance
     * @param name
     * @param enabled
     */
    public BinEntry(String name, boolean enabled) {
        this();
        this.binNameTextField.setText(name);
        this.binNameTextField.setEnabled(enabled);
    }
}
