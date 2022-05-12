package org.bihealth.mi.easysmpc.components;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easysmpc.resources.Resources;

public class EntryEMailDetailsAdvanced extends EntryEMailDetails {
    
    /** SVUID */
    private static final long       serialVersionUID = -7005095447195643542L;
    /** E-mail entry */
    private final ComponentEntryOne emailEntry;
    /** Password entry */
    private ComponentEntryOne       passwordEntry;
    /** user name entry */
    private ComponentEntryOne       userNameEntry;
    /** Auth mechanism entry */
    private ComponentEntryOne       authMechEntry;

    EntryEMailDetailsAdvanced(String title, int standardPort) {
        // Super
        super(title, standardPort);
        
        // Reset
        this.removeAll();
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                        title,
                                                        TitledBorder.LEFT,
                                                        TitledBorder.DEFAULT_POSITION));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // E-mail address entry
        emailEntry = new ComponentEntryOne(Resources.getString("EmailConfig.1"),
        "",
        true,
        new ComponentTextFieldValidator() {
              @Override
              public boolean validate(String text) {
                  return Participant.isEmailValid(text);
              }
          }, false, false);
        this.add(emailEntry);
        
        // Password entry
        passwordEntry = new ComponentEntryOne(Resources.getString("EmailConfig.2"),
                                              null,
                                              true,
                                              new ComponentTextFieldValidator() {

                                                  @Override
                                                  public boolean validate(String text) {
                                                      return text != null && !text.isBlank();
                                                  }
                                              },
                                              true,
                                              false);

        // User name entry
        userNameEntry = new ComponentEntryOne(Resources.getString("EmailConfig.28"),
                                              null,
                                              true,
                                              new ComponentTextFieldValidator() {

                                                  @Override
                                                  public boolean validate(String text) {
                                                      return true;
                                                  }
                                              },
                                              false,
                                              false);
        userNameEntry.setFieldEnabled(false);
        
        // Checkbox user name and panel
        JCheckBox userNameCheckBox = new JCheckBox();
        userNameCheckBox.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if(userNameCheckBox.isSelected()) {
                    userNameEntry.setFieldEnabled(true);
                } else {
                    userNameEntry.setFieldEnabled(false);
                    userNameEntry.setValue(null);
                }
            }
        });        
        JPanel userNamePanel = new JPanel();
        userNamePanel.setLayout(new BorderLayout());
        
        // Auth mechanism entry
        authMechEntry = new ComponentEntryOne(Resources.getString("EmailConfig.30"),
                                              null,
                                              true,
                                              new ComponentTextFieldValidator() {

                                                  @Override
                                                  public boolean validate(String text) {
                                                      // TODO Validate auth mechanism
                                                      return true;
                                                  }
                                              },
                                              false,
                                              false);
        authMechEntry.setFieldEnabled(false);
        
        // Checkbox auth mechanism and panel
        JCheckBox authMechcheckBox = new JCheckBox();
        authMechcheckBox.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if(authMechcheckBox.isSelected()) {
                    authMechEntry.setFieldEnabled(true);
                } else {
                    authMechEntry.setFieldEnabled(false);
                    authMechEntry.setValue(null);
                }
            }
        });
        JPanel authMechanismPanel = new JPanel();
        authMechanismPanel.setLayout(new BorderLayout());
        
        // Add
        this.add(emailEntry);
        userNamePanel.add(userNameCheckBox, BorderLayout.WEST);
        userNamePanel.add(userNameEntry, BorderLayout.CENTER);
        this.add(userNamePanel);
        this.add(passwordEntry);
        this.add(getServerEntry());
        this.add(getPortEntry());
        authMechanismPanel.add(authMechcheckBox, BorderLayout.WEST);
        authMechanismPanel.add(authMechEntry, BorderLayout.CENTER);
        this.add(authMechanismPanel);
        this.add(getRadioEncryptionType());                
    }
    
    /**
     *  Returns whether the settings are valid
     * 
     * @return
     */
    @Override
    public boolean areValuesValid() {
        return super.areValuesValid() 
                && emailEntry.isValueValid()
                && userNameEntry.isValueValid()
                && passwordEntry.isValueValid()
                && authMechEntry.isValueValid();
    }
    
    /**
     * Sets a change listener
     * @param listener
     */
    @Override
    public void setChangeListener(ChangeListener listener) {
        super.setChangeListener(listener);
        this.emailEntry.setChangeListener(listener);
        this.userNameEntry.setChangeListener(listener);
        this.passwordEntry.setChangeListener(listener);
        this.authMechEntry.setChangeListener(listener);
    }
}
