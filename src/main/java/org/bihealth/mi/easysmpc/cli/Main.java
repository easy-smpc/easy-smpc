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
import org.bihealth.mi.easysmpc.AppPasswordProvider;
import org.bihealth.mi.easysmpc.dataimport.ImportFile;

import de.tu_darmstadt.cbs.emailsmpc.Study.StudyState;
/**
 * EasySMPC command line version
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class Main {
    /** The mailbox check interval in milliseconds */
    public static final int     MAILBOX_CHECK_INTERVAL   = 3000;
    /** Encryption ssl/tls */
    private static final String SSL_TLS                  = "SSLTLS";
    /** Encryption starttls */
    private static final String START_TLS                = "STARTTLS";
    /** Logger */
    private static final Logger LOGGER                   = LogManager.getLogger(Main.class);
    /** Command line option */
    private static final Option OPTION_CREATE            = Option.builder("c")
                                                                 .desc("Create a new study")
                                                                 .longOpt("create")
                                                                 .hasArg(false)
                                                                 .required(false)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_PARTICIPATE       = Option.builder("p")
                                                                 .desc("Participate in a study")
                                                                 .longOpt("participate")
                                                                 .hasArg(false)
                                                                 .required(false)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_DATA_FILES         = Option.builder("d")
                                                                 .desc("Data files")
                                                                 .longOpt("data-files")
                                                                 .required(true)
                                                                 .hasArg(true)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_PARTICIPANTS = Option.builder("f")
                                                                 .desc("Participants file")
                                                                 .longOpt("participants-file")
                                                                 .required(false)
                                                                 .hasArg(true)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_BINS_NAMES        = Option.builder("b")
                                                                 .desc("Variable names files")
                                                                 .longOpt("variables-files")
                                                                 .required(false)
                                                                 .hasArg(true)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_MAILADDRESS       = Option.builder("a")
                                                                 .desc("E-mail address")
                                                                 .longOpt("email")
                                                                 .required(true)
                                                                 .hasArg(true)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_PASSWORD          = Option.builder("p")
                                                                 .desc("Password for email box")
                                                                 .longOpt("password")
                                                                 .required(true)
                                                                 .hasArg(true)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_IMAP_SERVER       = Option.builder("i")
                                                                 .desc("IMAP server for email box")
                                                                 .longOpt("imap-server")
                                                                 .required(true)
                                                                 .hasArg(true)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_IMAP_PORT         = Option.builder("x")
                                                                 .desc("IMAP port for email box")
                                                                 .longOpt("imap-port")
                                                                 .required(true)
                                                                 .hasArg(true)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_IMAP_ENCRYPTION   = Option.builder("y")
                                                                 .desc("IMAP encryption for email box")
                                                                 .longOpt("imap-encryption")
                                                                 .required(true)
                                                                 .hasArg(true)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_SMTP_SERVER       = Option.builder("s")
                                                                 .desc("SMTP server for email box")
                                                                 .longOpt("smtp-server")
                                                                 .required(true)
                                                                 .hasArg(true)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_SMTP_PORT         = Option.builder("z")
                                                                 .desc("SMTP port for email box")
                                                                 .longOpt("smtp-port")
                                                                 .required(true)
                                                                 .hasArg(true)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_SMTP_ENCRYPTION   = Option.builder("q")
                                                                 .desc("SMTP encryption for email box")
                                                                 .longOpt("smtp-encryption")
                                                                 .required(true)
                                                                 .hasArg(true)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_USE_PROXY         = Option.builder("u")
                                                                 .desc("Use proxy")
                                                                 .longOpt("use-proxy")
                                                                 .required(false)
                                                                 .hasArg(false)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_SELF_SIGNED       = Option.builder("r")
                                                                 .desc("Accept self-signed certificates")
                                                                 .longOpt("use-proxy")
                                                                 .required(false)
                                                                 .hasArg(false)
                                                                 .build();
    /** Command line option */
    private static final Option OPTION_PARTICIPANT_NAME  = Option.builder("o")
                                                                 .desc("Name of participant")
                                                                 .longOpt("participant-name")
                                                                 .required(false)
                                                                 .hasArg(true)
                                                                 .build();
    
    /** Command line option */
    private static final Option OPTION_STUDY_NAME  = Option.builder("l")
                                                                 .desc("Study name")
                                                                 .longOpt("study-name")
                                                                 .required(true)
                                                                 .hasArg(true)
                                                                 .build();
    
    /** Command line option */
    private static final Option OPTION_DATA_COLUMN  = Option.builder("h")
                                                                 .desc("Indicates column-oriented data")
                                                                 .longOpt("column-oriented")
                                                                 .required(false)
                                                                 .hasArg(false)
                                                                 .build();
    
    /** Command line option */
    private static final Option OPTION_HAS_HEADER  = Option.builder("e")
                                                                 .desc("Indicates that data has a header to skip")
                                                                 .longOpt("has-header")
                                                                 .required(false)
                                                                 .hasArg(false)
                                                                 .build();

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
	    Options options = new Options();
	    	      
	    // Add options
        options.addOption(OPTION_CREATE )
               .addOption(OPTION_PARTICIPATE)
               .addOption(OPTION_DATA_FILES)
               .addOption(OPTION_MAILADDRESS)
               .addOption(OPTION_PARTICIPANTS)
               .addOption(OPTION_BINS_NAMES)
               .addOption(OPTION_PASSWORD)
               .addOption(OPTION_IMAP_SERVER)
               .addOption(OPTION_IMAP_PORT)
               .addOption(OPTION_IMAP_ENCRYPTION)
               .addOption(OPTION_SMTP_SERVER)
               .addOption(OPTION_SMTP_PORT)
               .addOption(OPTION_SMTP_ENCRYPTION)
               .addOption(OPTION_USE_PROXY)
               .addOption(OPTION_SELF_SIGNED)
               .addOption(OPTION_PARTICIPANT_NAME)
               .addOption(OPTION_STUDY_NAME)
               .addOption(OPTION_DATA_COLUMN)
               .addOption(OPTION_HAS_HEADER);
	    
        try {
            // Parse arguments
            CommandLineParser parser = new DefaultParser();
            cli = parser.parse(options, args);
            
            // Check create or participate
            if (cli.hasOption(OPTION_CREATE) && cli.hasOption(OPTION_PARTICIPATE) || !(cli.hasOption(OPTION_CREATE) || cli.hasOption(OPTION_PARTICIPATE))) {
                throw new ParseException("Please pass either \"create\" or \"participate\" as a first argument");
            }
            
            // Check parameters when creating
            if (cli.hasOption(OPTION_CREATE) && !(cli.hasOption(OPTION_BINS_NAMES) && cli.hasOption(OPTION_BINS_NAMES))) {
                throw new ParseException("Please pass participants file and bins names file when creating a study");
            }
            
            // Check parameters when participating
            if (cli.hasOption(OPTION_PARTICIPATE) && !cli.hasOption(OPTION_PARTICIPANT_NAME)) {
                throw new ParseException("Please pass name of participant when participarting in a study");
            }
            
            // Check arguments
            checkCliArguments(cli);
            
        } catch (ParseException | IllegalArgumentException e) {
            // Log exception
            LOGGER.error("Unable to parse cli arguments", e);
            
            // Output help message
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("easy-smpc-cli", options);
            
            // Throw exception
            throw new IllegalStateException("Unable to parse cli arguments");
        }
                
        // Start creation or participation
        if (cli.hasOption(OPTION_CREATE)) {

            new UserCreating(cli.getOptionValue(OPTION_STUDY_NAME),
                             UserCreating.createParticipantsFromCSVString(cli.getOptionValue(OPTION_PARTICIPANTS)),
                             getDataFromFiles(cli.getOptionValue(OPTION_BINS_NAMES),
                                              !cli.hasOption(OPTION_DATA_COLUMN),
                                              true,
                                              cli.hasOption(OPTION_HAS_HEADER)),
                             getDataFromFiles(cli.getOptionValue(OPTION_DATA_FILES),
                                              !cli.hasOption(OPTION_DATA_COLUMN),
                                              false,
                                              cli.hasOption(OPTION_HAS_HEADER)),
                             getConnectionIMAPSettingsFromCLI(cli),
                             MAILBOX_CHECK_INTERVAL);
        } else {
            
            // Prepare
            Participant participant;
            try {
                participant = new Participant(cli.getOptionValue(OPTION_PARTICIPANT_NAME),
                                              cli.getOptionValue(OPTION_MAILADDRESS));
            } catch (BusException e1) {
                throw new IllegalArgumentException(e1);
            }
            
            // Create participating user
            UserParticipating participatingUser = new UserParticipating(cli.getOptionValue(OPTION_STUDY_NAME),
                                                                        participant,
                                                                        getDataFromFiles(cli.getOptionValue(OPTION_DATA_FILES),
                                                                                         !cli.hasOption(OPTION_DATA_COLUMN),
                                                                                         false,
                                                                                         cli.hasOption(OPTION_HAS_HEADER)),
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
        } 
	}
	
    /**
     * Strips different file names from a string and reads from all of them
     * 
     * @param filenames
     * @param rowOrientedData
     * @param oneRowCol
     * @param hasHeader
     * @return
     * @throws IllegalStateException
     */
    public static Map<String, String>
           getDataFromFiles(String filenames,
                            boolean rowOrientedData,
                            boolean oneRowCol,
                            boolean hasHeader) throws IllegalStateException {
        // Prepare
        Map<String, String> result = new HashMap<>();
        String[] split = filenames.split(",");
        
        // Loop over file names
        for(String filename : split) {
            // Add one file's result to the result map
            result.putAll(getDataFromFile(new File(filename), rowOrientedData, oneRowCol, hasHeader));
        }
        
        // Return
        return result;
    }

    /**
     * Check parameters in cli and throws an exception if wrong
     * 
     * @param cli
     * @return
     */
	public static boolean checkCliArguments(CommandLine cli) throws IllegalArgumentException {
        
        try {
            // Check integer
            Integer.valueOf(cli.getOptionValue(OPTION_IMAP_PORT));
            Integer.valueOf(cli.getOptionValue(OPTION_SMTP_PORT));
            
            // Check participant name and e-mail address
            new Participant(cli.hasOption(OPTION_PARTICIPANT_NAME) ? cli.getOptionValue(OPTION_PARTICIPANT_NAME) : "Creator", cli.getOptionValue(OPTION_MAILADDRESS));            
            
            // Check encryption for IMAP
            if(!(cli.getOptionValue(OPTION_IMAP_ENCRYPTION).equals(SSL_TLS) || cli.getOptionValue(OPTION_IMAP_ENCRYPTION).equals(START_TLS))) {
                throw new IllegalArgumentException(String.format("Please enter either %s or %s for IMAP encryption", SSL_TLS, START_TLS));
            }
            
            // Check encryption for SMTP
            if(!(cli.getOptionValue(OPTION_SMTP_ENCRYPTION).equals(SSL_TLS) || cli.getOptionValue(OPTION_SMTP_ENCRYPTION).equals(START_TLS))) {
                throw new IllegalArgumentException(String.format("Please enter either %s or s% for SMTP encryption", SSL_TLS, START_TLS));
            }
            
        } catch(Exception e) {
            throw new IllegalArgumentException("Arguments were not correct!", e);
        }
        
        // Return true if no exception
        return true;        
    }

    /**
     *  Reads IMAP connection settings from command line options
     * 
     * @param cli
     * @return
     */
    public static ConnectionIMAPSettings getConnectionIMAPSettingsFromCLI(CommandLine cli) {

        return new ConnectionIMAPSettings(cli.getOptionValue(OPTION_MAILADDRESS), new AppPasswordProvider())
                .setPassword(cli.getOptionValue(OPTION_PASSWORD))
                .setIMAPServer(cli.getOptionValue(OPTION_IMAP_SERVER))
                .setIMAPPort(Integer.valueOf(cli.getOptionValue(OPTION_IMAP_PORT)))
                .setSSLTLSIMAP(cli.getOptionValue(OPTION_IMAP_ENCRYPTION).equals(SSL_TLS))
                .setSMTPServer(cli.getOptionValue(OPTION_SMTP_SERVER))
                .setSMTPPort(Integer.valueOf(cli.getOptionValue(OPTION_SMTP_PORT)))
                .setSSLTLSSMTP(cli.getOptionValue(OPTION_SMTP_ENCRYPTION).equals(SSL_TLS))
                .setSearchForProxy(cli.hasOption(OPTION_USE_PROXY))
                .setAcceptSelfSignedCertificates(cli.hasOption(OPTION_SELF_SIGNED));
    }

    /**
     * Reads data from a file
     * 
     * @param file
     * @param rowOrientedData
     * @param oneRowCol
     * @param hasHeader
     * @return
     */
    public static Map<String, String>
           getDataFromFile(File file, boolean rowOrientedData, boolean oneRowCol, boolean hasHeader) {
        
        // Check
        if (file == null) { return null; }
        
        // Load data map
        try {
            return ImportFile.forFile(file, rowOrientedData, oneRowCol, hasHeader).getData();
        } catch (IOException e) {
            LOGGER.error(String.format("Error loading file. Does the file %s exists and is accessible?", file.getAbsolutePath()));
            throw new IllegalStateException(e);
        } catch (IllegalArgumentException e) {
            LOGGER.error(String.format("Error loading data from file %s. Please ensure that the file contains exactly two columns or rows containing data in the first sheet. The first sheet must not contain other data.", file.getAbsoluteFile()));
            throw new IllegalStateException(e);
        }
    }
}