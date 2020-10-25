/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tu_darmstadt.cbs.app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import de.tu_darmstadt.cbs.app.components.ComponentProgress;
import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * Main UI of the app
 * 
 * @author Fabian Prasser
 */
public class App extends JFrame {

    /** SVUID */
    private static final long serialVersionUID = 8047583915796168387L;

    /**
     * Main entry point
     * 
     * @throws UnsupportedLookAndFeelException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static void main(String[] args) throws ClassNotFoundException,
                                           InstantiationException,
                                           IllegalAccessException,
                                           UnsupportedLookAndFeelException,
                                           IOException {

        // Configure look and feel
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        // Start App
        new App();
    }

    /** Cards */
    private JPanel            cards;
    /** Menu */
    private JMenu             actionMenu;
    /** List of perspectives */
    private List<Perspective> perspectives = new ArrayList<Perspective>();

    /**
     * Creates a new instance
     * 
     * @throws IOException
     */
    public App() throws IOException {

        // Title
        super(Resources.getString("App.0")); //$NON-NLS-1$

        // Settings
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setIconImage(Resources.getIcon());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        
        // -----------------
        // Progress
        // -----------------
        this.add(new ComponentProgress(0), BorderLayout.NORTH);
        
        // -----------------
        // Panels
        // -----------------
        this.cards = new JPanel(new CardLayout());
        this.add(this.cards, BorderLayout.CENTER);

        // -----------------
        // Menu
        // ------------------
        JMenuBar jmb = new JMenuBar();
        this.setJMenuBar(jmb);

        // Action menu
        actionMenu = new JMenu(Resources.getString("App.1")); //$NON-NLS-1$
        jmb.add(actionMenu);

        // Exit menu
        JMenuItem jmiExit = new JMenuItem(Resources.getString("App.4"), Resources.getMenuItem()); //$NON-NLS-1$
        actionMenu.add(jmiExit);
        jmb.add(actionMenu);
        jmiExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showExitDialog();
            }
        });
        // Help menu
        JMenu jmHelp = new JMenu(Resources.getString("App.2")); //$NON-NLS-1$

        // About menu
        JMenuItem jmiAbout = new JMenuItem(Resources.getString("App.3"), Resources.getMenuItem()); //$NON-NLS-1$
        jmHelp.add(jmiAbout);
        jmb.add(jmHelp);
        jmiAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAboutDialog();
            }
        });

        // Add perspectives
        addPerspective(new PerspectiveFinalize(this));
        addPerspective(new PerspectiveReceive(this));
        addPerspective(new PerspectiveSend(this));
        addPerspective(new PerspectiveParticipate(this));
        addPerspective(new PerspectiveCreate(this));
        addPerspective(new PerspectiveStart(this));
        
        // Show the first perspective
        showPerspective(0);

        // Finally, make the frame visible
        this.setVisible(true);
    }

    /**
     * Adds a new perspective
     * 
     * @param perspective
     * @throws IOException
     */
    private void addPerspective(Perspective perspective) throws IOException {

        perspectives.add(0, perspective);
        cards.add(perspective.getPanel(), perspective.getTitle());

        JMenuItem jmiPerspective = new JMenuItem(perspective.getTitle(), Resources.getMenuItem());
        actionMenu.add(jmiPerspective, 0);
        jmiPerspective.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPerspective(perspective);
            }
        });
    }

    /**
     * Shows a certain perspective
     * 
     * @param perspective
     */
    protected void showPerspective(Perspective perspective) {
        CardLayout cl = (CardLayout) (cards.getLayout());
        cl.show(cards, perspective.getTitle());
    }

    /**
     * Shows the perspective with the given index
     * 
     * @param index
     */
    protected void showPerspective(int index) {
        showPerspective(perspectives.get(index));
    }

    /**
     * Shows the about dialog
     */
    private void showAboutDialog() {
        // TODO Implement
    }

    /**
     * Shows the exit dialog
     */
    private void showExitDialog() {
        // TODO Implement
    }

    /**
     * Shows a perspective
     * 
     * @param clazz
     */
    protected void showPerspective(Class<?> clazz) {
        int index = 0;
        for (Perspective p : perspectives) {
            if (p.getClass().equals(clazz)) {
                showPerspective(index);
            }
            index++;
        }
    }

    /**
     * Returns a perspective
     * 
     * @param clazz
     * @return Found perspective - null if not found!
     */
    protected Perspective getPerspective(Class<?> clazz) {
        Perspective returnPerspective = null;
        for (Perspective p : perspectives) {
            if (p.getClass().equals(clazz)) {
                returnPerspective = p;
                break;
            }
        }
        return returnPerspective;
    }
}