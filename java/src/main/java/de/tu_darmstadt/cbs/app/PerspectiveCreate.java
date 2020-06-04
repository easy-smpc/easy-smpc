package de.tu_darmstadt.cbs.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * A perspective
 * @author Fabian Prasser
 */
public class PerspectiveCreate extends Perspective {

    protected PerspectiveCreate(App app) {
        super(app, Messages.getString("PerspectiveCreate.0")); //$NON-NLS-1$
    }

    @Override
    protected void createContents(JPanel panel) {
        
        // Layout
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Name
        Box box1 = Box.createHorizontalBox();
        box1.add(new JLabel("Name of study:"));
        box1.add(Box.createHorizontalGlue());
        box1.add(new JTextField());
        panel.add(box1);

        // Participants
        String[] pColumnNames = { "Name", "Email" };
        Object[][] pData = getData();
        JTable pTable = new JTable(pData, pColumnNames);
        JScrollPane pScrollPane = new JScrollPane(pTable);
        pTable.setFillsViewportHeight(true);
        Box box2 = Box.createVerticalBox();
        box2.add(new JLabel("Participants:"));
        box2.add(pScrollPane);
        panel.add(box2);

        // Bins
        String[] bColumnNames = { "Name", "Initial value" };
        Object[][] bData = getData();
        JTable bTable = new JTable(bData, bColumnNames);
        JScrollPane bScrollPane = new JScrollPane(bTable);
        bTable.setFillsViewportHeight(true);
        Box box3 = Box.createVerticalBox();
        box3.add(new JLabel("Bins:"));
        box3.add(bScrollPane);
        panel.add(box3);
        
        // Buttons panel
        Box box4 = Box.createHorizontalBox();
        JButton button1 = new JButton("Save");
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        });
        box4.add(button1);
        panel.add(box4);
    }

    /**
     * Returns 100 empty rows
     * @return
     */
    private Object[][] getData() {
        Object[][] data = new Object[100][];
        for (int i=0; i<100; i++) {
            data[i] = new Object[] {"", ""};
        }
        return data;
    }
}
