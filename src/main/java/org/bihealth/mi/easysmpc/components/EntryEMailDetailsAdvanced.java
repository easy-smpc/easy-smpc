package org.bihealth.mi.easysmpc.components;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easysmpc.resources.Resources;

public class EntryEMailDetailsAdvanced extends EntryEMailDetails {
    
    /** SVUID */
    private static final long serialVersionUID = -7005095447195643542L;
    /** E-mail entry */
    private final ComponentEntryOne emailEntry;
    /** Password entry */
    private ComponentEntryOne passwordEntry;
    /** user name entry */
    private ComponentEntryOne userNameEntry;
    /** Auth mechanism entry */
    private ComponentEntryOne authMechEntry;

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
          }, true, false);
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
        
        // Add
        this.add(emailEntry);
        this.add(userNameEntry);
        this.add(passwordEntry);
        this.add(getServerEntry());
        this.add(getPortEntry());
        this.add(authMechEntry);
        this.add(getRadioEncryptionType());                
    }
}
