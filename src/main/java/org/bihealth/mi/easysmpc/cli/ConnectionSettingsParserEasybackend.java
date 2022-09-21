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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.PasswordStore;
import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasybackend;

public class ConnectionSettingsParserEasybackend extends ConnectionSettingsParser {
    
    /** Command line option */
    private static final Option OPTION_SERVER_URL         = Option.builder("i")
                                                                  .desc("Server URL")
                                                                  .longOpt("server-url")
                                                                  .required(true)
                                                                  .hasArg(true)
                                                                  .build();

    /** Command line option */
    private static final Option OPTION_PASSWORD           = Option.builder("p")
                                                                  .desc("Password")
                                                                  .longOpt("password")
                                                                  .required(true)
                                                                  .hasArg(true)
                                                                  .build();

    /** Command line option */
    private static final Option OPTION_AUTH_SERVER_URL    = Option.builder("v")
                                                                  .desc("Auth server URL")
                                                                  .longOpt("auth-server-url")
                                                                  .required(false)
                                                                  .hasArg(true)
                                                                  .build();

    /** Command line option */
    private static final Option OPTION_AUTH_REALM         = Option.builder("m")
                                                                  .desc("Auth realm")
                                                                  .longOpt("auth-realm")
                                                                  .required(false)
                                                                  .hasArg(true)
                                                                  .build();

    /** Command line option */
    private static final Option OPTION_AUTH_CLIENT_ID     = Option.builder("s")
                                                                  .desc("Auth client id")
                                                                  .longOpt("auth-client-id")
                                                                  .required(false)
                                                                  .hasArg(true)
                                                                  .build();

    /** Command line option */
    private static final Option OPTION_AUTH_CLIENT_SECRET = Option.builder("x")
                                                                  .desc("Auth client secret")
                                                                  .longOpt("client-secret")
                                                                  .required(false)
                                                                  .hasArg(true)
                                                                  .build();

    /** Command line option */
    private static final Option OPTION_PROXY_URL          = Option.builder("y")
                                                                  .desc("Proxy URL")
                                                                  .longOpt("proxy-url")
                                                                  .required(false)
                                                                  .hasArg(true)
                                                                  .build();
    
    /**
     * Creates a new instance
     * 
     * @param args
     * @param options
     * @throws ParseException
     */
    public ConnectionSettingsParserEasybackend(String[] args, Options options) throws ParseException {
        super(args, options);
    }

    @Override
    protected void addOptions(Options options) {
        // Add options
        options.addOption(OPTION_SERVER_URL)
               .addOption(OPTION_PASSWORD)
               .addOption(OPTION_AUTH_SERVER_URL)
               .addOption(OPTION_AUTH_REALM)
               .addOption(OPTION_AUTH_CLIENT_ID)
               .addOption(OPTION_AUTH_CLIENT_SECRET)
               .addOption(OPTION_PROXY_URL);
    }

    @Override
    public void checkCLIParameters() throws IllegalArgumentException {

        try {
            // Check parameters
            ConnectionSettingsEasybackend.checkURL(getCLI().getOptionValue(OPTION_SERVER_URL));

            if (getCLI().hasOption(OPTION_AUTH_SERVER_URL)) {
                new URL(getCLI().getOptionValue(OPTION_SERVER_URL));
            }

            if (getCLI().hasOption(OPTION_PROXY_URL)) {
                new URL(getCLI().getOptionValue(OPTION_PROXY_URL));
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Arguments were not correct!", e);
        }
    }

    @Override
    public ConnectionSettings getConnectionSettings(Participant self) {
               
        try {
            // Set mandatory parameters
            ConnectionSettingsEasybackend result = new ConnectionSettingsEasybackend(self, null).setAPIServer(new URL(getCLI().getOptionValue(OPTION_SERVER_URL)));
            result.setPasswordStore(new PasswordStore(getCLI().getOptionValue(OPTION_PASSWORD)));
            
            // Set optional parameters
            if (getCLI().hasOption(OPTION_AUTH_SERVER_URL)) {
                result.setAuthServer(new URL(getCLI().getOptionValue(OPTION_SERVER_URL)));
            }

            if (getCLI().hasOption(OPTION_AUTH_REALM)) {
                result.setRealm((getCLI().getOptionValue(OPTION_AUTH_REALM)));
            }

            if (getCLI().hasOption(OPTION_AUTH_CLIENT_ID)) {
                result.setClientId(getCLI().getOptionValue(OPTION_AUTH_CLIENT_ID));
            }

            if (getCLI().hasOption(OPTION_AUTH_CLIENT_SECRET)) {
                result.setClientSecret(getCLI().getOptionValue(OPTION_AUTH_CLIENT_SECRET));
            }

            if (getCLI().hasOption(OPTION_PROXY_URL)) {
                result.setAuthServer(new URL(getCLI().getOptionValue(OPTION_PROXY_URL)));
            }

            // Return
            return result;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Unable to create connection settings object");
        }
    }
}
