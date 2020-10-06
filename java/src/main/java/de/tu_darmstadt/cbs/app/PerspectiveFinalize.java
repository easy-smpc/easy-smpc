package de.tu_darmstadt.cbs.app;

import javax.swing.JLabel;
import javax.swing.JPanel;

import lombok.Getter;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 */
public class PerspectiveFinalize extends Perspective {

    @Getter
    private JLabel myResult;

    protected PerspectiveFinalize(App app) {
        super(app, Resources.getString("PerspectiveFinalize.0")); //$NON-NLS-1$
    }

    @Override
    protected void createContents(JPanel panel) {
        // TODO Auto-generated method stub
        this.myResult = new JLabel("Placeholder");
        panel.add(myResult);

    }
}
