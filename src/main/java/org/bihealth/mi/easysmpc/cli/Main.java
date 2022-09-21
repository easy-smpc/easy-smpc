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
import org.apache.commons.lang3.EnumUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.ConnectionSettings.ConnectionTypes;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.PasswordStore;
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

    /** Logger */
    private static final Logger LOGGER                       = LogManager.getLogger(Main.class);
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
    private static final Option OPTION_CONNECTION_TYPE       = Option.builder("r")
                                                                     .desc("Connection type")
                                                                     .longOpt("connection-type")
                                                                     .hasArg(true)
                                                                     .required(true)
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
    private static final Option OPTION_PASSWORD_RECEIVING    = Option.builder("p")
                                                                     .desc("Password for (receiving) email box")
                                                                     .longOpt("password")
                                                                     .required(true)
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
    private static final Option OPTION_MAILADDRESS_RECEIVING = Option.builder("a")
                                                                     .desc("(receiving) E-mail address")
                                                                     .longOpt("email")
                                                                     .required(true)
                                                                     .hasArg(true)
                                                                     .build();

    /** Default parser */
    private static CommandLineParser parser                       = new DefaultParser();

    /**
     * Starts an EasySMPC process
     *
     * @param args
     */
    public static void main(String[] args) {
        // Prefer IPv6
        System.getProperties().setProperty("java.net.preferIPv6Addresses", "true");

        // Prepare
        CommandLine cli;
        Options optionsInitial = new Options();

        // Options initial
        optionsInitial.addOption(OPTION_CREATE)
        .addOption(OPTION_PARTICIPATE)
        .addOption(OPTION_RESUME);
	    
        // Check exactly create, participate or resume
        try {
            cli = parser.parse(optionsInitial, args, true);
            if (!(
                   (cli.hasOption(OPTION_CREATE) && !cli.hasOption(OPTION_PARTICIPATE) && !cli.hasOption(OPTION_RESUME)) ||
                   (!cli.hasOption(OPTION_CREATE) && cli.hasOption(OPTION_PARTICIPATE) && !cli.hasOption(OPTION_RESUME)) ||
                   (!cli.hasOption(OPTION_CREATE) && !cli.hasOption(OPTION_PARTICIPATE) && cli.hasOption(OPTION_RESUME))
                )) {
                throw new ParseException("Please pass either \"-create\", \"-participate\" or \"-resume\" as the first argument");
            }
        } catch (ParseException e) {
            // Log exception
            LOGGER.error("Unable to parse CLI arguments", e);

            // Output help message
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("easy-smpc-cli", optionsInitial);

            // Throw exception
            throw new IllegalStateException("Unable to parse CLI arguments");
        }

        // Proceed in creation mode
        if (cli.hasOption(OPTION_CREATE)) {
            proceedCreate(args);
        }

        // Proceed in participation mode
        if (cli.hasOption(OPTION_PARTICIPATE)) {
            proceedParticipate(args);
        }

        // Proceed in resume mode
        if (cli.hasOption(OPTION_RESUME)) {
            proceedResume(args);
        }
	}
	
    /**
     * Proceed processing in creation mode
     * 
     * @param args
     */
    private static void proceedCreate(String[] args) {
    
        // Prepare 
        Options options = new Options();
        CommandLine cli;
        ConnectionSettingsParser connectionSettingsParser = null;
        Participant self;

        // Set generic options for creating
        options.addOption(OPTION_CREATE_REQUIRED)
        .addOption(OPTION_CONNECTION_TYPE)
        .addOption(OPTION_STUDY_NAME)
        .addOption(OPTION_DATA_FILE)
        .addOption(OPTION_BINS_NAMES)
        .addOption(OPTION_PARTICIPANTS)
        .addOption(OPTION_DATA_COLUMN)
        .addOption(OPTION_HAS_HEADER)
        .addOption(OPTION_SKIP_COLUMNS);

        try {

            // Parse to get connection type
            cli = parser.parse(new Options().addOption(OPTION_CREATE).addOption(OPTION_CONNECTION_TYPE), args, true);

            // Get connection type
            if (cli.getOptionValue(OPTION_CONNECTION_TYPE).toUpperCase().equals(ConnectionTypes.MANUAL.toString()) ||
                    !EnumUtils.isValidEnum(ConnectionTypes.class, cli.getOptionValue(OPTION_CONNECTION_TYPE).toUpperCase())) {
                throw new ParseException(String.format("Please provide a correct connection type. Correct types are %s", java.util.Arrays.asList(ConnectionTypes.values())));
            }

            // Set correct connection settings parser
            switch(ConnectionTypes.valueOf(cli.getOptionValue(OPTION_CONNECTION_TYPE).toUpperCase())) {
            case EASYBACKEND:
                connectionSettingsParser = new ConnectionSettingsParserEasybackend(args, options);
                break;
            case EMAIL:
                connectionSettingsParser = new ConnectionSettingsParserEmail(args, options);
                break;
            case MANUAL:
                throw new ParseException(String.format("Please provide a correct connection type. Correct types are %s", java.util.Arrays.asList(ConnectionTypes.values())));
            default:
                throw new ParseException(String.format("Please provide a correct connection type. Correct types are %s", java.util.Arrays.asList(ConnectionTypes.values())));
            }

            // Parse all parameters including the connection setting specific ones
            cli = connectionSettingsParser.getCLI();
            
            
            // Check minimal participants of three
            if(UserProcessCreating.createParticipantsFromCSVString(cli.getOptionValue(OPTION_PARTICIPANTS)).length < 3){
                throw new ParseException(String.format("Please provide at least three participants in the option \"-%s\" respective \"-%s\"", OPTION_PARTICIPANTS.getLongOpt(), OPTION_PARTICIPANTS.getOpt()));
            }

            // Check skip columns is integer
            if (cli.hasOption(cli.getOptionValue(OPTION_SKIP_COLUMNS))) {
                Integer.valueOf(cli.getOptionValue(OPTION_SKIP_COLUMNS));
            }
            
            try {
                de.tu_darmstadt.cbs.emailsmpc.Participant participant = UserProcessCreating.createParticipantsFromCSVString(cli.getOptionValue(OPTION_PARTICIPANTS))[0];
                self = new Participant(participant.name, participant.emailAddress );
            } catch (BusException e) {
                throw new IllegalArgumentException(e);
            }

            // Check connection settings parameter
            connectionSettingsParser.checkCLIParameters();
        }
        catch (ParseException | IllegalArgumentException e) {
            // Log exception
            LOGGER.error("Unable to parse CLI arguments", e);

            // Output help message
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("easy-smpc-cli", connectionSettingsParser != null ? connectionSettingsParser.getOptions() : options);

            // Throw exception
            throw new IllegalStateException("Unable to parse CLI arguments");
        }

        // Checks successful, start process
        new UserProcessCreating(cli.getOptionValue(OPTION_STUDY_NAME),
                                UserProcessCreating.createParticipantsFromCSVString(cli.getOptionValue(OPTION_PARTICIPANTS)),
                                getDataFromFiles(cli.getOptionValue(OPTION_BINS_NAMES),
                                                 !cli.hasOption(OPTION_DATA_COLUMN),
                                                 true,
                                                 cli.hasOption(OPTION_HAS_HEADER),
                                                 cli.hasOption(OPTION_SKIP_COLUMNS)
                                                 ? Integer.valueOf(cli.getOptionValue(OPTION_SKIP_COLUMNS))
                                                         : 0),
                                getDataFromFiles(cli.getOptionValue(OPTION_DATA_FILE),
                                                 !cli.hasOption(OPTION_DATA_COLUMN),
                                                 false,
                                                 cli.hasOption(OPTION_HAS_HEADER),
                                                 cli.hasOption(OPTION_SKIP_COLUMNS)
                                                 ? Integer.valueOf(cli.getOptionValue(OPTION_SKIP_COLUMNS))
                                                         : 0),
                                connectionSettingsParser.getConnectionSettings(self));
    }

    /**
     * Proceed processing in participating mode
     * 
     * @param args
     */
    private static void proceedParticipate(String[] args) {
        // Prepare 
        Options options = new Options();
        CommandLine cli;
        ConnectionSettingsParser connectionSettingsParser = null;
        Participant self;

        // Add options when participating
        options.addOption(OPTION_PARTICIPATE_REQUIRED)
        .addOption(OPTION_CONNECTION_TYPE)
        .addOption(OPTION_STUDY_NAME)
        .addOption(OPTION_PARTICIPANT_NAME)
        .addOption(OPTION_DATA_FILE)
        .addOption(OPTION_DATA_COLUMN)
        .addOption(OPTION_HAS_HEADER)
        .addOption(OPTION_SKIP_COLUMNS)
        .addOption(OPTION_MAILADDRESS_RECEIVING);

        try {
            // Parse to get connection type
            cli = parser.parse(new Options().addOption(OPTION_PARTICIPATE).addOption(OPTION_CONNECTION_TYPE), args, true);

            // Get connection type
            if (cli.getOptionValue(OPTION_CONNECTION_TYPE).toUpperCase().equals(ConnectionTypes.MANUAL.toString()) ||
                    !EnumUtils.isValidEnum(ConnectionTypes.class, cli.getOptionValue(OPTION_CONNECTION_TYPE).toUpperCase())) {
                throw new ParseException(String.format("Please provide a correct connection type. Correct types are %s", java.util.Arrays.asList(ConnectionTypes.values())));
            }

            // Set correct connection settings parser
            switch(ConnectionTypes.valueOf(cli.getOptionValue(OPTION_CONNECTION_TYPE).toUpperCase())) {
            case EASYBACKEND:
                connectionSettingsParser = new ConnectionSettingsParserEasybackend(args, options);
                break;
            case EMAIL:
                connectionSettingsParser = new ConnectionSettingsParserEmail(args, options);
                break;
            case MANUAL:
                throw new ParseException(String.format("Please provide a correct connection type. Correct types are %s", java.util.Arrays.asList(ConnectionTypes.values())));
            default:
                throw new ParseException(String.format("Please provide a correct connection type. Correct types are %s", java.util.Arrays.asList(ConnectionTypes.values())));
            }

            // Parse all parameters including the connection setting specific ones
            cli = connectionSettingsParser.getCLI();

            try {
                self = new Participant(cli.getOptionValue(OPTION_PARTICIPANT_NAME),
                                              cli.getOptionValue(OPTION_MAILADDRESS_RECEIVING));
            } catch (BusException e) {
                throw new IllegalArgumentException(e);
            }

            // Check skip columns is integer
            if (cli.hasOption(cli.getOptionValue(OPTION_SKIP_COLUMNS))) {
                Integer.valueOf(cli.getOptionValue(OPTION_SKIP_COLUMNS));
            }

            // Check connection settings parameter
            connectionSettingsParser.checkCLIParameters();
        }
        catch (ParseException | IllegalArgumentException e) {
            // Log exception
            LOGGER.error("Unable to parse CLI arguments", e);

            // Output help message
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("easy-smpc-cli", connectionSettingsParser != null ? connectionSettingsParser.getOptions() : options);

            // Throw exception
            throw new IllegalStateException("Unable to parse CLI arguments");
        }

        // Create participating user
        UserProcessParticipating participatingUser = new UserProcessParticipating(cli.getOptionValue(OPTION_STUDY_NAME),
                                                                                  self,
                                                                                  getDataFromFiles(cli.getOptionValue(OPTION_DATA_FILE),
                                                                                                   !cli.hasOption(OPTION_DATA_COLUMN),
                                                                                                   false,
                                                                                                   cli.hasOption(OPTION_HAS_HEADER),
                                                                                                   cli.hasOption(OPTION_SKIP_COLUMNS) ? Integer.valueOf(cli.getOptionValue(OPTION_SKIP_COLUMNS)) : 0),
                                                                                  connectionSettingsParser.getConnectionSettings(self));

        // Wait for participant to be initialized
        LOGGER.info("Waiting for initial message to participate");
        while(participatingUser.getModel() == null || participatingUser.getModel().getState() == StudyState.NONE) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    /**
         * Proceed processing in resuming mode
         * 
         * @param args
         */
        private static void proceedResume(String[] args) {
            // Prepare 
            Options options = new Options();
            CommandLine cli;
            
            // Add options when resuming
            options.addOption(OPTION_RESUME_REQUIRED)
                   .addOption(OPTION_RESUME_FILE)
                   .addOption(OPTION_PASSWORD_RECEIVING)
                   .addOption(OPTION_PASSWORD_SENDING);
            
            // Get CLI
            try {
                cli = parser.parse(options, args);
            } catch (ParseException e) {
                // Log exception
                LOGGER.error("Unable to parse CLI arguments", e);

                // Output help message
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("easy-smpc-cli", options);

                // Throw exception
                throw new IllegalStateException("Unable to parse CLI arguments");
            }
    
            try {
                // Create study from file and set password(s)
                Study study = Study.loadModel(new File(cli.getOptionValue(OPTION_RESUME_FILE)));
                study.getConnectionSettings()
                     .setPasswordStore(new PasswordStore(cli.getOptionValue(OPTION_PASSWORD_RECEIVING),
                                                         cli.getOptionValue(OPTION_PASSWORD_SENDING)));
    
                // Start process
                new UserProcess(study);
            } catch (ClassNotFoundException | IllegalArgumentException | IOException e) {
                LOGGER.error("Unable to resume with given file", e);
            }
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