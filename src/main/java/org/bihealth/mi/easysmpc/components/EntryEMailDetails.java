package org.bihealth.mi.easysmpc.components;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.resources.Resources;

public class EntryEMailDetails extends JPanel implements ChangeListener {

    /** SVUID */
    private static final long serialVersionUID = 3375406542374186905L;
    /** Listener */
    private ChangeListener listener;
    /** Server entry */
    private final ComponentEntryOne serverEntry;
    /** Port entry */
    private final ComponentEntryOne portEntry;
    /** Encryption type radio */
    private final ComponentRadioEntry radioEncryptionType;
    
    EntryEMailDetails(String title, int standardPort) {
        
        // General
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                        title,
                                                        TitledBorder.LEFT,
                                                        TitledBorder.DEFAULT_POSITION));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Server entry
        serverEntry = new ComponentEntryOne(Resources.getString("EmailConfig.39"),
        "",
        true,
        new ComponentTextFieldValidator() {
              @Override
              public boolean validate(String text) {
                  return text != null && !text.isBlank();
              }
          }, true, false);
        this.add(serverEntry);
        
        // Port entry
        portEntry = new ComponentEntryOne(Resources.getString("EmailConfig.40"),
                                                            String.valueOf(standardPort),
                                                              true,
                                                              new ComponentTextFieldValidator() {
                                                                    @Override
                                                                    public boolean validate(String text) {
                                                                        try {
                                                                            ConnectionIMAPSettings.checkPort(Integer.parseInt(text));
                                                                            return true;
                                                                        }
                                                                        catch (Exception e) {
                                                                            return false;
                                                                        }
                                                                    }
                                                                }, false, false);
        this.add(portEntry);
        
        // Encryption entry
        radioEncryptionType = new ComponentRadioEntry(Resources.getString("EmailConfig.20"),
                                                      Resources.getString("EmailConfig.21"),
                                                      Resources.getString("EmailConfig.22"),
                                                      this);
        this.add(radioEncryptionType);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (this.listener != null) {
            this.listener.stateChanged(e);
        }
    }

    /**
     * @return the serverEntry
     */
    protected ComponentEntryOne getServerEntry() {
        return serverEntry;
    }

    /**
     * @return the portEntry
     */
    protected ComponentEntryOne getPortEntry() {
        return portEntry;
    }

    /**
     * @return the radioEncryptionIMAP
     */
    protected ComponentRadioEntry getRadioEncryptionType() {
        return radioEncryptionType;
    }
    
    /**
     * Sets a change listener
     * @param listener
     */
    public void setChangeListener(ChangeListener listener) {
        this.listener = listener;
    }
}
