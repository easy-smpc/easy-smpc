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
package org.bihealth.mi.easysmpc.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.PasswordStore;
import org.bihealth.mi.easybus.implementations.email.ConnectionSettingsIMAP;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * A class to obtain email connection settings from CLI
 * 
 * @author Felix Wirth
 *
 */
public class ConnectionSettingsParserEmail extends ConnectionSettingsParser {
    
    /** Encryption ssl/tls */
    private static final String SSL_TLS                 = "SSLTLS";
    /** Encryption starttls */
    private static final String START_TLS               = "STARTTLS";
    
    /** Command line option */
    private static final Option OPTION_MAILADDRESS_RECEIVING = Option.builder("a")
                                                                     .desc("(receiving) E-mail address")
                                                                     .longOpt("email")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_PASSWORD_RECEIVING    = Option.builder("p")
                                                                     .desc("Password for (receiving) email box")
                                                                     .longOpt("password")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();

    /** Command line option */
    private static final Option OPTION_MAILADDRESS_SENDING   = Option.builder("v")
                                                                     .desc("Sending e-mail address")
                                                                     .longOpt("emailSending")
                                                                     .required(false)
                                                                     .hasArg(true)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_PASSWORD_SENDING      = Option.builder("m")
                                                                     .desc("Password for sending email box")
                                                                     .longOpt("passwordSending")
                                                                     .required(false)
                                                                     .hasArg(true)
                                                                     .build();

    /** Command line option */
    private static final Option OPTION_IMAP_SERVER           = Option.builder("i")
                                                                     .desc("IMAP server for email box")
                                                                     .longOpt("imap-server")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_IMAP_PORT             = Option.builder("x")
                                                                     .desc("IMAP port for email box")
                                                                     .longOpt("imap-port")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_IMAP_ENCRYPTION       = Option.builder("y")
                                                                     .desc("IMAP encryption for email box")
                                                                     .longOpt("imap-encryption")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_SMTP_SERVER           = Option.builder("s")
                                                                     .desc("SMTP server for email box")
                                                                     .longOpt("smtp-server")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_SMTP_PORT             = Option.builder("z")
                                                                     .desc("SMTP port for email box")
                                                                     .longOpt("smtp-port")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_SMTP_ENCRYPTION       = Option.builder("q")
                                                                     .desc("SMTP encryption for email box")
                                                                     .longOpt("smtp-encryption")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();
    
    /** Command line option */
    private static final Option OPTION_IMAP_USER_NAME        = Option.builder("n")
                                                                     .desc("IMAP user name (only if user name deviates from IMAP e-mail address)")
                                                                     .longOpt("impa-user-name")
                                                                     .required(false)
                                                                     .hasArg(true)
                                                                     .build();
    
    /** Command line option */
    private static final Option OPTION_SMTP_USER_NAME        = Option.builder("w")
                                                                     .desc("SMTP user name (only if user name deviates from IMAP e-mail address)")
                                                                     .longOpt("smtp-user-name")
                                                                     .required(false)
                                                                     .hasArg(true)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_IMAP_LOGIN_MECHANISMS = Option.builder("u")
                                                                     .desc("Set auth mechanisms for imap")
                                                                     .longOpt("impap-mechanisms")
                                                                     .required(false)
                                                                     .hasArg(true)
                                                                     .build();

    /** Command line option */
    private static final Option OPTION_SMTP_LOGIN_MECHANISMS = Option.builder("t")
                                                                     .desc("Set auth mechanisms for smtp")
                                                                     .longOpt("smtp-mechanisms")
                                                                     .required(false)
                                                                     .hasArg(true)
                                                                     .build();

    /** Command line option */
    private static final Option OPTION_CHECK_INTERVAL   = Option.builder("mbc")
                                                                     .desc("Mailbox check interval (sec)")
                                                                     .longOpt("mailbox-check-interval")
                                                                     .required(false)
                                                                     .hasArg(true)
                                                                     .build();

    /** Command line option */
    private static final Option OPTION_SEND_TIMEOUT          = Option.builder("tm")
                                                                     .desc("E-mail Send timeout sec)")
                                                                     .longOpt("send-time-out")
                                                                     .required(false)
                                                                     .hasArg(true)
                                                                     .build();

    /** Command line option */
    private static final Option OPTION_MAX_MESSAGE_SIZE      = Option.builder("ms")
                                                                     .desc("Message size (MB)")
                                                                     .longOpt("max-message-size")
                                                                     .required(false)
                                                                     .hasArg(true)
                                                                     .build();
    
    /** Command line option */
    private static final Option OPTION_PARTICIPANT_NAME      = Option.builder("o")
                                                                     .desc("Name of participant")
                                                                     .longOpt("participant-name")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();
    

    /**
     * Creates a new instance
     * 
     * @param args
     * @param options
     * @throws ParseException
     */
    public ConnectionSettingsParserEmail(String[] args, Options options) throws ParseException {
        super(args, options);
    }

    @Override
    protected void addOptions(Options options) {
        // Add options
        options.addOption(OPTION_MAILADDRESS_RECEIVING)
               .addOption(OPTION_PASSWORD_RECEIVING)
               .addOption(OPTION_IMAP_SERVER)
               .addOption(OPTION_IMAP_PORT)
               .addOption(OPTION_IMAP_ENCRYPTION)
               .addOption(OPTION_SMTP_SERVER)
               .addOption(OPTION_SMTP_PORT)
               .addOption(OPTION_SMTP_ENCRYPTION)
               .addOption(OPTION_MAILADDRESS_SENDING)
               .addOption(OPTION_PASSWORD_SENDING)
               .addOption(OPTION_IMAP_USER_NAME)
               .addOption(OPTION_SMTP_USER_NAME)
               .addOption(OPTION_IMAP_LOGIN_MECHANISMS)
               .addOption(OPTION_SMTP_LOGIN_MECHANISMS)
               .addOption(OPTION_CHECK_INTERVAL)
               .addOption(OPTION_SEND_TIMEOUT)
               .addOption(OPTION_MAX_MESSAGE_SIZE);
        
    }

    @Override
    public void checkCLIParameters() throws IllegalArgumentException {

        try {
            // Check integer
            Integer.valueOf(getCLI().getOptionValue(OPTION_IMAP_PORT));
            Integer.valueOf(getCLI().getOptionValue(OPTION_SMTP_PORT));

            if (getCLI().hasOption(OPTION_MAX_MESSAGE_SIZE)) {
                Integer.valueOf(getCLI().getOptionValue(OPTION_MAX_MESSAGE_SIZE));
            }
            if (getCLI().hasOption(OPTION_SEND_TIMEOUT)) {
                Integer.valueOf(getCLI().getOptionValue(OPTION_SEND_TIMEOUT));
            }
            if (getCLI().hasOption(OPTION_CHECK_INTERVAL)) {
                Integer.valueOf(getCLI().getOptionValue(OPTION_CHECK_INTERVAL));
            }

            // Check participant name and e-mail address
            new Participant(getCLI().hasOption(OPTION_PARTICIPANT_NAME)
                    ? getCLI().getOptionValue(OPTION_PARTICIPANT_NAME)
                    : "Creator", getCLI().getOptionValue(OPTION_MAILADDRESS_RECEIVING));

            // Check encryption for IMAP
            if (!(getCLI().getOptionValue(OPTION_IMAP_ENCRYPTION).equals(SSL_TLS) ||
                  getCLI().getOptionValue(OPTION_IMAP_ENCRYPTION).equals(START_TLS))) {
                throw new IllegalArgumentException(String.format("Please enter either %s or %s for IMAP encryption",
                                                                 SSL_TLS,
                                                                 START_TLS));
            }

            // Check encryption for SMTP
            if (!(getCLI().getOptionValue(OPTION_SMTP_ENCRYPTION).equals(SSL_TLS) ||
                  getCLI().getOptionValue(OPTION_SMTP_ENCRYPTION).equals(START_TLS))) {
                throw new IllegalArgumentException(String.format("Please enter either %s or %s for SMTP encryption",
                                                                 SSL_TLS,
                                                                 START_TLS));
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Arguments were not correct!", e);
        }
    }

    @Override
    public ConnectionSettings getConnectionSettings(String email) {
        // Set email address and password either from the receiving parameter or from different receiving and sending parameters
        ConnectionSettingsIMAP connectionIMAPSettings = new ConnectionSettingsIMAP(email,
                                                                                   getCLI().hasOption(OPTION_MAILADDRESS_SENDING)
                                                                                   ? getCLI().getOptionValue(OPTION_MAILADDRESS_SENDING)
                                                                                           : getCLI().getOptionValue(OPTION_MAILADDRESS_RECEIVING),
                                                                                           null);
        connectionIMAPSettings.setPasswordStore(new PasswordStore(getCLI().getOptionValue(OPTION_PASSWORD_RECEIVING),
                                                                  getCLI().getOptionValue(OPTION_PASSWORD_SENDING)));
        // Set remaining parameters
        connectionIMAPSettings.setIMAPServer(getCLI().getOptionValue(OPTION_IMAP_SERVER))
        .setIMAPPort(Integer.valueOf(getCLI().getOptionValue(OPTION_IMAP_PORT)))
        .setSSLTLSIMAP(getCLI().getOptionValue(OPTION_IMAP_ENCRYPTION).equals(SSL_TLS))
        .setIMAPUserName(getCLI().hasOption(OPTION_IMAP_USER_NAME) ? getCLI().getOptionValue(OPTION_IMAP_USER_NAME) : null)
        .setSMTPServer(getCLI().getOptionValue(OPTION_SMTP_SERVER))
        .setSMTPPort(Integer.valueOf(getCLI().getOptionValue(OPTION_SMTP_PORT)))                                              
        .setSSLTLSSMTP(getCLI().getOptionValue(OPTION_SMTP_ENCRYPTION).equals(SSL_TLS))
        .setSMTPUserName(getCLI().hasOption(OPTION_SMTP_USER_NAME) ? getCLI().getOptionValue(OPTION_SMTP_USER_NAME) : null)
        .setIMAPAuthMechanisms(getCLI().getOptionValue(OPTION_IMAP_LOGIN_MECHANISMS))
        .setSMTPAuthMechanisms(getCLI().getOptionValue(OPTION_SMTP_LOGIN_MECHANISMS))
        .setMaxMessageSize(getCLI().hasOption(OPTION_MAX_MESSAGE_SIZE) ? Integer.valueOf(getCLI().getOptionValue(OPTION_MAX_MESSAGE_SIZE))* 1024 * 1024 : Resources.EMAIL_MAX_MESSAGE_SIZE_DEFAULT)
        .setEmailSendTimeout(getCLI().hasOption(OPTION_SEND_TIMEOUT) ? Integer.valueOf(getCLI().getOptionValue(OPTION_SEND_TIMEOUT)) * 1000 : Resources.TIMEOUT_SEND_EMAILS_DEFAULT)
        .setCheckInterval(getCLI().hasOption(OPTION_CHECK_INTERVAL) ? Integer.valueOf(getCLI().getOptionValue(OPTION_CHECK_INTERVAL)) * 1000 : Resources.INTERVAL_CHECK_MAILBOX_DEFAULT );

        // Return
        return connectionIMAPSettings;
    }

}
