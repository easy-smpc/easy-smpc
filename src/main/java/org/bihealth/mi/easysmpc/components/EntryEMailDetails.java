package org.bihealth.mi.easysmpc.components;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.resources.Resources;

public class EntryEMailDetails extends JPanel {

    /** SVUID */
    private static final long serialVersionUID = 3375406542374186905L;
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
          }, false, false);
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
                                                      Resources.getString("EmailConfig.22"));
        //radioEncryptionType.setChangeListener(this);
        this.add(radioEncryptionType);
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
        this.portEntry.setChangeListener(listener);
        this.serverEntry.setChangeListener(listener);
        this.radioEncryptionType.setChangeListener(listener);
    }
        
    /**
     *  Returns whether the settings are valid
     * 
     * @return
     */
    public boolean areValuesValid() {
        return serverEntry.isValueValid() && portEntry.isValueValid();          
    }
}
