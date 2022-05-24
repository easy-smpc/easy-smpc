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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.dataimport.ImportFile;

import de.tu_darmstadt.cbs.emailsmpc.Study;
import de.tu_darmstadt.cbs.emailsmpc.Study.StudyState;
/**
 * EasySMPC command line version
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class Main {
    /** The mailbox check interval in milliseconds */
    public static final int     MAILBOX_CHECK_INTERVAL  = 3000;
    /** Encryption ssl/tls */
    private static final String SSL_TLS                 = "SSLTLS";
    /** Encryption starttls */
    private static final String START_TLS               = "STARTTLS";
    /** Logger */
    private static final Logger LOGGER                  = LogManager.getLogger(Main.class);
    /** Command line option */
    private static final Option OPTION_CREATE                = Option.builder("c")
                                                                     .desc("Create a new study")
                                                                     .longOpt("create")
                                                                     .hasArg(false)
                                                                     .required(false)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_PARTICIPATE           = Option.builder("p")
                                                                     .desc("Participate in a study")
                                                                     .longOpt("participate")
                                                                     .hasArg(false)
                                                                     .required(false)
                                                                     .build();

    /** Command line option */
    private static final Option OPTION_RESUME                = Option.builder("g")
                                                                     .desc("Load and resume an existing study")
                                                                     .longOpt("resume")
                                                                     .hasArg(false)
                                                                     .required(false)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_CREATE_REQUIRED       = Option.builder(OPTION_CREATE.getOpt())
                                                                     .desc(OPTION_CREATE.getDescription())
                                                                     .longOpt(OPTION_CREATE.getLongOpt())
                                                                     .hasArg(OPTION_CREATE.hasArg())
                                                                     .required(true)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_PARTICIPATE_REQUIRED  = Option.builder(OPTION_PARTICIPATE.getOpt())
                                                                     .desc(OPTION_PARTICIPATE.getDescription())
                                                                     .longOpt(OPTION_PARTICIPATE.getLongOpt())
                                                                     .hasArg(OPTION_PARTICIPATE.hasArg())
                                                                     .required(true)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_RESUME_REQUIRED       = Option.builder(OPTION_RESUME.getOpt())
                                                                     .desc(OPTION_RESUME.getDescription())
                                                                     .longOpt(OPTION_RESUME.getLongOpt())
                                                                     .hasArg(OPTION_RESUME.hasArg())
                                                                     .required(true)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_DATA_FILE             = Option.builder("d")
                                                                     .desc("Data file(s) when creating or participating")
                                                                     .longOpt("data-files")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_PARTICIPANTS          = Option.builder("f")
                                                                     .desc("Participants file")
                                                                     .longOpt("participants-file")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_BINS_NAMES            = Option.builder("b")
                                                                     .desc("Variable names files")
                                                                     .longOpt("variables-files")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();
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
    private static final Option OPTION_SELF_SIGNED           = Option.builder("r")
                                                                     .desc("Accept self-signed certificates")
                                                                     .longOpt("self-signed")
                                                                     .required(false)
                                                                     .hasArg(false)
                                                                     .build();
    /** Command line option */
    private static final Option OPTION_PARTICIPANT_NAME      = Option.builder("o")
                                                                     .desc("Name of participant")
                                                                     .longOpt("participant-name")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();

    /** Command line option */
    private static final Option OPTION_STUDY_NAME            = Option.builder("l")
                                                                     .desc("Study name")
                                                                     .longOpt("study-name")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();

    /** Command line option */
    private static final Option OPTION_DATA_COLUMN           = Option.builder("h")
                                                                     .desc("Indicates column-oriented data")
                                                                     .longOpt("column-oriented")
                                                                     .required(false)
                                                                     .hasArg(false)
                                                                     .build();

    /** Command line option */
    private static final Option OPTION_HAS_HEADER            = Option.builder("e")
                                                                     .desc("Indicates that data has a header to skip")
                                                                     .longOpt("has-header")
                                                                     .required(false)
                                                                     .hasArg(false)
                                                                     .build();

    /** Command line option */
    private static final Option OPTION_SKIP_COLUMNS          = Option.builder("j")
                                                                     .desc("Indicates the numbers of columns to skip")
                                                                     .longOpt("skip-columns")
                                                                     .required(false)
                                                                     .hasArg(true)
                                                                     .build();

    /** Command line option */
    private static final Option OPTION_RESUME_FILE           = Option.builder("k")
                                                                     .desc("Data file to resume")
                                                                     .longOpt("resume-file")
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
    
    /** Options when in creating mode */
    private static Options      optionsCreate                = new Options();
    /** Options when in participating mode */
    private static Options      optionsParticipate           = new Options();
    /** Options when in resuming mode */
    private static Options      optionsResume                = new Options();

    /**
     * Starts an EasySMPC process
     *
     * @param args
     */
    public static void main(String[] args) {
        // Prefer IPv6 if network (e.g. e-mail) is used
        System.getProperties().setProperty("java.net.preferIPv6Addresses", "true");

        // Prepare
        CommandLine cli;
        Options optionsInitial = new Options();
        optionsCreate = new Options();
        optionsParticipate = new Options();
        optionsResume = new Options();
	    
        // Options initial
        optionsInitial.addOption(OPTION_CREATE)
                      .addOption(OPTION_PARTICIPATE)
                      .addOption(OPTION_RESUME);

        // Add options when creating
        optionsCreate.addOption(OPTION_CREATE_REQUIRED)
                     .addOption(OPTION_STUDY_NAME)
                     .addOption(OPTION_DATA_FILE)
                     .addOption(OPTION_BINS_NAMES)
                     .addOption(OPTION_MAILADDRESS_RECEIVING)
                     .addOption(OPTION_PARTICIPANTS)
                     .addOption(OPTION_BINS_NAMES)
                     .addOption(OPTION_PASSWORD_RECEIVING)
                     .addOption(OPTION_IMAP_SERVER)
                     .addOption(OPTION_IMAP_PORT)
                     .addOption(OPTION_IMAP_ENCRYPTION)
                     .addOption(OPTION_SMTP_SERVER)
                     .addOption(OPTION_SMTP_PORT)
                     .addOption(OPTION_SMTP_ENCRYPTION)
                     .addOption(OPTION_SELF_SIGNED)
                     .addOption(OPTION_DATA_COLUMN)
                     .addOption(OPTION_HAS_HEADER)
                     .addOption(OPTION_SKIP_COLUMNS)
                     .addOption(OPTION_MAILADDRESS_SENDING)
                     .addOption(OPTION_PASSWORD_SENDING)
                     .addOption(OPTION_IMAP_USER_NAME)
                     .addOption(OPTION_SMTP_USER_NAME)
                     .addOption(OPTION_IMAP_LOGIN_MECHANISMS)
                     .addOption(OPTION_SMTP_LOGIN_MECHANISMS);

        // Add options when participating
        optionsParticipate.addOption(OPTION_PARTICIPATE_REQUIRED)
                          .addOption(OPTION_STUDY_NAME)
                          .addOption(OPTION_PARTICIPANT_NAME)
                          .addOption(OPTION_DATA_FILE)
                          .addOption(OPTION_MAILADDRESS_RECEIVING)
                          .addOption(OPTION_PASSWORD_RECEIVING)
                          .addOption(OPTION_IMAP_SERVER)
                          .addOption(OPTION_IMAP_PORT)
                          .addOption(OPTION_IMAP_ENCRYPTION)
                          .addOption(OPTION_SMTP_SERVER)
                          .addOption(OPTION_SMTP_PORT)
                          .addOption(OPTION_SMTP_ENCRYPTION)
                          .addOption(OPTION_SELF_SIGNED)
                          .addOption(OPTION_DATA_COLUMN)
                          .addOption(OPTION_HAS_HEADER)
                          .addOption(OPTION_SKIP_COLUMNS)
                          .addOption(OPTION_MAILADDRESS_SENDING)
                          .addOption(OPTION_PASSWORD_SENDING)
                          .addOption(OPTION_IMAP_USER_NAME)
                          .addOption(OPTION_SMTP_USER_NAME)
                          .addOption(OPTION_IMAP_LOGIN_MECHANISMS)
                          .addOption(OPTION_SMTP_LOGIN_MECHANISMS);

        // Add options when resuming
        optionsResume.addOption(OPTION_RESUME_REQUIRED)
                     .addOption(OPTION_RESUME_FILE)
                     .addOption(OPTION_PASSWORD_RECEIVING);
	    
        try {
            // Parse arguments
            CommandLineParser parser = new DefaultParser();
            
            // Check exactly create, participate or load
            cli = parser.parse(optionsInitial, args, true);
            if (!((cli.hasOption(OPTION_CREATE) & !cli.hasOption(OPTION_PARTICIPATE) & !cli.hasOption(OPTION_RESUME)) |
                  (!cli.hasOption(OPTION_CREATE) & cli.hasOption(OPTION_PARTICIPATE) & !cli.hasOption(OPTION_RESUME)) |
                 (!cli.hasOption(OPTION_CREATE) & !cli.hasOption(OPTION_PARTICIPATE) & cli.hasOption(OPTION_RESUME)))) {
                throw new ParseException("Please pass either \"-create\", \"-participate\" or \"-resume\"");
            }
            
            // Choose correct options
            cli = returnSpecificCli(cli, parser, args);            
            
            // Check minimal participants of three
            if(cli.hasOption(OPTION_PARTICIPANTS) && UserProcessCreating.createParticipantsFromCSVString(cli.getOptionValue(OPTION_PARTICIPANTS)).length < 3){
                throw new ParseException(String.format("Please provide at least three participants in the option \"-%s\" respective \"-%s\"", OPTION_PARTICIPANTS.getLongOpt(), OPTION_PARTICIPANTS.getOpt()));
            }
            
            // Check arguments
            checkCliArguments(cli);
            
        } catch (ParseException | IllegalArgumentException e) {
            // Log exception
            LOGGER.error("Unable to parse CLI arguments", e);
            
            // Output help message
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("easy-smpc-cli", optionsCreate);
            
            // Throw exception
            throw new IllegalStateException("Unable to parse CLI arguments");
        }
                
        // Start creation if applicable
        if (cli.hasOption(OPTION_CREATE)) {

            new UserProcessCreating(cli.getOptionValue(OPTION_STUDY_NAME),
                             UserProcessCreating.createParticipantsFromCSVString(cli.getOptionValue(OPTION_PARTICIPANTS)),
                             getDataFromFiles(cli.getOptionValue(OPTION_BINS_NAMES),
                                              !cli.hasOption(OPTION_DATA_COLUMN),
                                              true,
                                              cli.hasOption(OPTION_HAS_HEADER),
                                              cli.hasOption(OPTION_SKIP_COLUMNS) ? Integer.valueOf(cli.getOptionValue(OPTION_SKIP_COLUMNS)) : 0),
                             getDataFromFiles(cli.getOptionValue(OPTION_DATA_FILE),
                                              !cli.hasOption(OPTION_DATA_COLUMN),
                                              false,
                                              cli.hasOption(OPTION_HAS_HEADER),
                                              cli.hasOption(OPTION_SKIP_COLUMNS) ? Integer.valueOf(cli.getOptionValue(OPTION_SKIP_COLUMNS)) : 0),
                             getConnectionIMAPSettingsFromCLI(cli),
                             MAILBOX_CHECK_INTERVAL);
            
            // Done
            return;
        }
        
        // Start participating if applicable
        if (cli.hasOption(OPTION_PARTICIPATE)) {

            // Prepare
            Participant participant;
            try {
                participant = new Participant(cli.getOptionValue(OPTION_PARTICIPANT_NAME),
                                              cli.getOptionValue(OPTION_MAILADDRESS_RECEIVING));
            } catch (BusException e1) {
                throw new IllegalArgumentException(e1);
            }
            
            // Create participating user
            UserProcessParticipating participatingUser = new UserProcessParticipating(cli.getOptionValue(OPTION_STUDY_NAME),
                                                                        participant,
                                                                        getDataFromFiles(cli.getOptionValue(OPTION_DATA_FILE),
                                                                                         !cli.hasOption(OPTION_DATA_COLUMN),
                                                                                         false,
                                                                                         cli.hasOption(OPTION_HAS_HEADER),
                                                                                         cli.hasOption(OPTION_SKIP_COLUMNS) ? Integer.valueOf(cli.getOptionValue(OPTION_SKIP_COLUMNS)) : 0),
                                                                  getConnectionIMAPSettingsFromCLI(cli),
                                                                  MAILBOX_CHECK_INTERVAL);
           
            // Wait for participant to be initialized
            LOGGER.info("Waiting for initial email to participate");
            while(participatingUser.getModel() == null || participatingUser.getModel().getState() == StudyState.NONE) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
            
            // Done
            return ;
        }
        
        // Start resuming if applicable
        if (cli.hasOption(OPTION_RESUME)) {
            try {
                
                // Create study from file and set password(s)
                Study study = Study.loadModel(new File(cli.getOptionValue(OPTION_RESUME_FILE)));                
                study.getConnectionIMAPSettings().setIMAPPassword(cli.getOptionValue(OPTION_PASSWORD_RECEIVING));
                study.getConnectionIMAPSettings()
                     .setIMAPPassword(cli.hasOption(OPTION_PASSWORD_SENDING)
                             ? cli.getOptionValue(OPTION_PASSWORD_SENDING)
                             : cli.getOptionValue(OPTION_PASSWORD_RECEIVING));
                
                // Start process
                new UserProcess(study, MAILBOX_CHECK_INTERVAL);
            } catch (ClassNotFoundException | IllegalArgumentException | IOException e) {
                LOGGER.error("Unable to resume with given file", e);
            }

            // Done
            return;
        }
	}
	
    /**
     * Return the options suiting the mode chosen by the user
     * 
     * @param CLI
     * @param parser
     * @param args
     * @return
     * @throws ParseException 
     */
    private static CommandLine returnSpecificCli(CommandLine cli, CommandLineParser parser, String[] args) throws ParseException {
        
        // Return create
        if (cli.hasOption(OPTION_CREATE)) {
            return parser.parse(optionsCreate, args);
        }
        
        // Return participate
        if (cli.hasOption(OPTION_PARTICIPATE)) {
            return parser.parse(optionsParticipate, args);
        }

        // Return resume
        if (cli.hasOption(OPTION_RESUME)) {
            return parser.parse(optionsResume, args);
        }
        
        // Default null
        return null;
    }

    /**
     * Strips different file names from a string and reads from all of them
     * 
     * @param filenames
     * @param rowOrientedData
     * @param oneRowCol
     * @param hasHeader
     * @param skipCol 
     * @return
     * @throws IllegalStateException
     */
    public static Map<String, String>
           getDataFromFiles(String filenames,
                            boolean rowOrientedData,
                            boolean oneRowCol,
                            boolean hasHeader, int skipCol) throws IllegalStateException {
        // Prepare
        Map<String, String> result = new HashMap<>();
        String[] split = filenames.split(",");
        
        // Loop over file names
        for(String filename : split) {
            // Add one file's result to the result map
            result.putAll(getDataFromFile(new File(filename), rowOrientedData, oneRowCol, hasHeader, skipCol));
        }
        
        // Return
        return result;
    }

    /**
     * Check parameters in CLI and throws an exception if wrong
     * 
     * @param cli
     * @return
     */
	public static boolean checkCliArguments(CommandLine cli) throws IllegalArgumentException {
        
        // Check only for create and participate
        if (cli.hasOption(OPTION_RESUME)) { return true; }

        try {
            // Check integer
            Integer.valueOf(cli.getOptionValue(OPTION_IMAP_PORT));
            Integer.valueOf(cli.getOptionValue(OPTION_SMTP_PORT));
            if (cli.hasOption(cli.getOptionValue(OPTION_SKIP_COLUMNS))) {
                Integer.valueOf(cli.getOptionValue(OPTION_SKIP_COLUMNS));
            }

            // Check participant name and e-mail address
            new Participant(cli.hasOption(OPTION_PARTICIPANT_NAME)
                    ? cli.getOptionValue(OPTION_PARTICIPANT_NAME)
                    : "Creator", cli.getOptionValue(OPTION_MAILADDRESS_RECEIVING));

            // Check encryption for IMAP
            if (!(cli.getOptionValue(OPTION_IMAP_ENCRYPTION).equals(SSL_TLS) ||
                  cli.getOptionValue(OPTION_IMAP_ENCRYPTION).equals(START_TLS))) {
                throw new IllegalArgumentException(String.format("Please enter either %s or %s for IMAP encryption",
                                                                 SSL_TLS,
                                                                 START_TLS));
            }

            // Check encryption for SMTP
            if (!(cli.getOptionValue(OPTION_SMTP_ENCRYPTION).equals(SSL_TLS) ||
                  cli.getOptionValue(OPTION_SMTP_ENCRYPTION).equals(START_TLS))) {
                throw new IllegalArgumentException(String.format("Please enter either %s or %s for SMTP encryption",
                                                                 SSL_TLS,
                                                                 START_TLS));
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Arguments were not correct!", e);
        }

        // Return true if no exception
        return true;     
    }

    /**
     *  Reads IMAP connection settings from command line options
     * 
     * @param CLI
     * @return
     */
    public static ConnectionIMAPSettings getConnectionIMAPSettingsFromCLI(CommandLine cli) {
        
        // Set email address and password either from the receiving parameter or from different receiving and sending parameters
        ConnectionIMAPSettings connectionIMAPSettings = new ConnectionIMAPSettings(cli.getOptionValue(OPTION_MAILADDRESS_RECEIVING),
                                                                                   cli.hasOption(OPTION_MAILADDRESS_SENDING) ? cli.getOptionValue(OPTION_MAILADDRESS_SENDING) : cli.getOptionValue(OPTION_MAILADDRESS_RECEIVING), null).setIMAPPassword(cli.getOptionValue(OPTION_PASSWORD_RECEIVING))
                                                                                   .setSMTPPassword(cli.hasOption(OPTION_PASSWORD_SENDING) ? cli.getOptionValue(OPTION_PASSWORD_SENDING) : cli.getOptionValue(OPTION_PASSWORD_RECEIVING));     
        // Set remaining parameters
        connectionIMAPSettings.setIMAPServer(cli.getOptionValue(OPTION_IMAP_SERVER))
                                               .setIMAPPort(Integer.valueOf(cli.getOptionValue(OPTION_IMAP_PORT)))
                                               .setSSLTLSIMAP(cli.getOptionValue(OPTION_IMAP_ENCRYPTION).equals(SSL_TLS))
                                               .setIMAPUserName(cli.hasOption(OPTION_IMAP_USER_NAME) ? cli.getOptionValue(OPTION_IMAP_USER_NAME) : null)
                                               .setSMTPServer(cli.getOptionValue(OPTION_SMTP_SERVER))
                                               .setSMTPPort(Integer.valueOf(cli.getOptionValue(OPTION_SMTP_PORT)))                                              
                                               .setSSLTLSSMTP(cli.getOptionValue(OPTION_SMTP_ENCRYPTION).equals(SSL_TLS))
                                               .setSMTPUserName(cli.hasOption(OPTION_SMTP_USER_NAME) ? cli.getOptionValue(OPTION_SMTP_USER_NAME) : null)
                                               .setAcceptSelfSignedCertificates(cli.hasOption(OPTION_SELF_SIGNED))
                                               .setIMAPAuthMechanisms(cli.getOptionValue(OPTION_IMAP_LOGIN_MECHANISMS))
                                               .setSMTPAuthMechanisms(cli.getOptionValue(OPTION_SMTP_LOGIN_MECHANISMS));
        
        // Return
        return connectionIMAPSettings;
    }

    /**
     * Reads data from a file
     * 
     * @param file
     * @param rowOrientedData
     * @param oneRowCol
     * @param hasHeader
     * @param skipCol
     * @return
     */
    public static Map<String, String> getDataFromFile(File file,
                                                      boolean rowOrientedData,
                                                      boolean oneRowCol,
                                                      boolean hasHeader,
                                                      int skipCol) {
        
        // Check
        if (file == null) { return null; }
        
        // Load data map
        try {
            return ImportFile.forFile(file, rowOrientedData, oneRowCol, hasHeader, skipCol).getData();
        } catch (IOException e) {
            LOGGER.error(String.format("Error loading file. Does the file %s exists and is accessible?", file.getAbsolutePath()));
            throw new IllegalStateException(e);
        } catch (IllegalArgumentException e) {
            LOGGER.error(String.format("Error loading data from file %s. Please ensure that the file contains exactly two columns or rows containing data in the first sheet. The first sheet must not contain other data.", file.getAbsoluteFile()));
            throw new IllegalStateException(e);
        }
    }
}