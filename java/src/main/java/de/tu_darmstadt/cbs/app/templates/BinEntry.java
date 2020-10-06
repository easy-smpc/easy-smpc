/**
 * 
 */
//TODO: GPL licence
package de.tu_darmstadt.cbs.app.templates;

import java.awt.BorderLayout;
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
        
        this.setLayout(new GridLayout(0, 2, Resources.ROW_GAP, Resources.ROW_GAP));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, Resources.ROW_HEIGHT));
        
        // Left
        JPanel left = new JPanel();
        left.setLayout(new BorderLayout());
        this.add(left);
        
        JLabel binNameLabel = new JLabel(Resources.getString("BinEntry.0")); //$NON-NLS-1$
        binNameTextField = new JTextField();
        binNameTextField.setColumns(10);
        
        left.add(binNameLabel, BorderLayout.WEST);
        left.add(binNameTextField, BorderLayout.CENTER);

        // Right
        JPanel right = new JPanel();
        right.setLayout(new BorderLayout());
        this.add(right);
        
        JLabel binValueLabel = new JLabel(Resources.getString("BinEntry.1")); //$NON-NLS-1$
        this.binValueField = new JTextField();
        binValueField.setColumns(10);
        
        right.add(binValueLabel, BorderLayout.WEST);
        right.add(binValueField, BorderLayout.CENTER);
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
