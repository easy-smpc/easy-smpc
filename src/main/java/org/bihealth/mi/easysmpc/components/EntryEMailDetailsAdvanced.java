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
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.resources.Resources;

public class EntryEMailDetailsAdvanced extends EntryEMailDetails {
    
    /** SVUID */
    private static final long serialVersionUID = -7005095447195643542L;
    /** E-mail entry */
    private ComponentEntryOne emailEntry;
    /** Password entry */
    private ComponentEntryOne passwordEntry;
    /** user name entry */
    private ComponentEntryOne userNameEntry;
    /** Auth mechanism entry */
    private ComponentEntryOne authMechEntry;

    /**
     * Creates a new instance
     * 
     * @param title
     * @param standardPort
     */
    EntryEMailDetailsAdvanced(String title, int standardPort) {
        // Super
        super(title, standardPort);
        
        // Create elements
        createAdditionalgElements(title);
    }
    
    /**
     * Creates a new instance. If settings is not null fields are pre-filled from it
     * 
     * @param title
     * @param standardPort
     * @param settings
     * @param isIMAP 
     */
    EntryEMailDetailsAdvanced(String title, int standardPort, ConnectionIMAPSettings settings, boolean isIMAP) {
        // Super
        super(title, standardPort, settings);
        
        // Create element
        createAdditionalgElements(title);
        
        // Check
        if(settings == null) {
            return;
        }
        
        // Set values
        if (isIMAP) {

            // Set values from IMAP
            this.emailEntry.setValue(settings.getIMAPEmailAddress());
            this.passwordEntry.setValue(settings.getIMAPPassword());
            this.userNameEntry.setValue(settings.getIMAPUserName());
            this.authMechEntry.setValue(settings.getIMAPAuthMechanisms());
        }
        {
            // Set values from SMTP
            this.emailEntry.setValue(settings.getSMTPEmailAddress());
            this.passwordEntry.setValue(settings.getSMTPPassword());
            this.userNameEntry.setValue(settings.getSMTPUserName());
            this.authMechEntry.setValue(settings.getSMTPAuthMechanisms());
        }
    }
    
    /**
     * Creates a new instance. If oldDetails is not null fields are pre-filled from it
     * 
     * @param title
     * @param standardPort
     * @param oldDetails
     */
    EntryEMailDetailsAdvanced(String title, int standardPort, EntryEMailDetails oldDetails) {
        // Super
        super(title, standardPort, oldDetails);
        
        // Create element
        createAdditionalgElements(title);
        
        // Check
        if(oldDetails == null) {
            return;
        }
        
        this.emailEntry.setValue(oldDetails.getEmailaddress());
        this.passwordEntry.setValue(oldDetails.getPassword());
        this.userNameEntry.setValue(oldDetails.getUserName());
        this.authMechEntry.setValue(oldDetails.getAuthMechanisms());
        
    }
    
    /**
     * Creates a new instance. If oldDetails is not null fields are pre-filled from it. Finally the parameters emailAddress and password overwrite the values from oldDetails again
     * 
     * @param title
     * @param standardPort
     * @param oldDetails
     * @param eMailAddress
     * @param password
    EntryEMailDetailsAdvanced(String title, int standardPort, EntryEMailDetails oldDetails, String emailAddress, String password) {
        this(title, standardPort, oldDetails);
        
        // Overwrite values
        emailEntry.setValue(emailAddress);
        passwordEntry.setValue(password);
    }

    /**
     * Creates the additional elements
     * 
     * @param title
     */
    private void createAdditionalgElements(String title) {
        
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
    
    /**
     * Returns e-mail address
     * 
     * @return
     */
    @Override
    public String getEmailaddress() {
        return emailEntry.getValue();
    }

    /**
     * Return password
     * 
     * @return
     */
    @Override
    public String getPassword() {
        return passwordEntry.getValue();
    }    
    
    /**
     * Get user name
     * 
     * @return
     */
    @Override
    public String getUserName() {
        return userNameEntry.getValue();
    }

    /**
     * Get auth mechansism
     * 
     * @return
     */
    @Override
    public String getAuthMechanisms() {
        return authMechEntry.getValue();
    }
}
