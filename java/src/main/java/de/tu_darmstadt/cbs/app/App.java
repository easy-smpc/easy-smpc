package de.tu_darmstadt.cbs.app;

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

/**
 * Main UI of the app
 * @author Fabian Prasser
 */
public class App extends JFrame {

    /** SVUID*/
    private static final long serialVersionUID = 8047583915796168387L;

    /** Main entry point
     * @throws UnsupportedLookAndFeelException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     * @throws IOException */
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException {
        
        // Configure look and feel
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        
        // Start App
        new App();
    }
    /** Cards*/
    private JPanel cards;
    /** Menu*/
    private JMenu actionMenu;
    /** List of perspectives*/
    private List<Perspective> perspectives = new ArrayList<Perspective>();
    
    /**
     * Creates a new instance
     * @throws IOException 
     */
    public App() throws IOException {
        
        // Title
        super(Messages.getString("App.0")); //$NON-NLS-1$
        
        // Settings
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setIconImage(Messages.getIcon());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 

        // -----------------
        // Panels
        // -----------------
        this.cards = new JPanel(new CardLayout());
        this.add(this.cards);
        
        // -----------------
        // Menu
        // ------------------
        JMenuBar jmb = new JMenuBar();
        this.setJMenuBar(jmb);
        
        // Action menu
        actionMenu = new JMenu(Messages.getString("App.1")); //$NON-NLS-1$
        jmb.add(actionMenu);

        // Exit menu
        JMenuItem jmiExit = new JMenuItem(Messages.getString("App.4"), Messages.getMenuItem()); //$NON-NLS-1$
        actionMenu.add(jmiExit);
        jmb.add(actionMenu);
        jmiExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showExitDialog();
            }
        });
        // Help menu
        JMenu jmHelp = new JMenu(Messages.getString("App.2")); //$NON-NLS-1$
        
        // About menu
        JMenuItem jmiAbout = new JMenuItem(Messages.getString("App.3"), Messages.getMenuItem()); //$NON-NLS-1$
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
        addPerspective(new PerspectiveContinue(this));
        addPerspective(new PerspectiveCreate(this));
        addPerspective(new PerspectiveStart(this));
        
        // Show the first perspective
        showPerspective(0);
        
        // Finally, make the frame visible
        this.setVisible(true);
    }

    /**
     * Adds a new perspective
     * @param perspective
     * @throws IOException 
     */
    private void addPerspective(Perspective perspective) throws IOException {
        
        perspectives.add(0, perspective);
        cards.add(perspective.getPanel(), perspective.getTitle());
        
        JMenuItem jmiPerspective = new JMenuItem(perspective.getTitle(), Messages.getMenuItem());
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
     * @param perspective
     */
    protected void showPerspective(Perspective perspective) {
        CardLayout cl = (CardLayout)(cards.getLayout());
        cl.show(cards, perspective.getTitle());
    }
    
    /**
     * Shows the perspective with the given index
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
}