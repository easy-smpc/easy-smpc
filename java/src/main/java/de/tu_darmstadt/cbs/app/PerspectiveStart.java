package de.tu_darmstadt.cbs.app;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * A perspective
 * @author Fabian Prasser
 */
public class PerspectiveStart extends Perspective {

    protected PerspectiveStart(App app) {
        super(app, Messages.getString("PerspectiveStart.0")); //$NON-NLS-1$
    }

    @Override
    protected void createContents(JPanel panel) {
        
        // Buttons panel
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(3,1));
        
        // Action 1
        JButton button1 = new JButton(Messages.getString("PerspectiveStart.1")); //$NON-NLS-1$
        buttons.add(button1);
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getApp().showPerspective(PerspectiveCreate.class);
            }
        });
        
        // Action 2
        JButton button2 = new JButton(Messages.getString("PerspectiveStart.2")); //$NON-NLS-1$
        buttons.add(button2);
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getApp().showPerspective(PerspectiveContinue.class);
            }
        });
        
        // Action 3
        JButton button3 = new JButton(Messages.getString("PerspectiveStart.3")); //$NON-NLS-1$
        buttons.add(button3);
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getApp().showPerspective(PerspectiveFinalize.class);
            }
        });
        
        // Add
        panel.setLayout(new GridBagLayout());
        panel.add(buttons, new GridBagConstraints());
    }
}
