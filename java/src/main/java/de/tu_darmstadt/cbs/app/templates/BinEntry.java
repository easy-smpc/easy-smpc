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

import lombok.Getter;

/**
 * @author Felix Wirth
 *
 */
public class BinEntry extends JPanel {
    @Getter
    private JTextField binNameTextField;
    @Getter
    private JTextField binValueField;

    public BinEntry() {
        this.setLayout(new GridLayout(0, 5, 0, 0));
        this.setMaximumSize(new Dimension(950, 30));
        JLabel binNameLabel = new JLabel("Name of bin");
        this.add(binNameLabel);
        binNameTextField = new JTextField();
        this.add(binNameTextField);
        binNameTextField.setColumns(10);

        JPanel emptyPanel = new JPanel();
        this.add(emptyPanel);

        JLabel binValueLabel = new JLabel("Your frequency");
        this.add(binValueLabel);

        this.binValueField = new JTextField();
        binValueField.setColumns(10);
        this.add(binValueField);
    }

    public BinEntry(String name, boolean enabled) {
        this();
        this.binNameTextField.setText(name);
        this.binNameTextField.setEnabled(enabled);
    }
}
