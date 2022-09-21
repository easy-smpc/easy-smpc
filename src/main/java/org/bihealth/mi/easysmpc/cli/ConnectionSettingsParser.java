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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.Participant;

/**
 * An abstract class to obtain connection settings from CLI
 * 
 * @author Felix Wirth
 *
 */
public abstract class ConnectionSettingsParser {
    
    /** Possible cli options */
    private final Options options;
    /** CLI */
    private CommandLine cli;

    /**
     * Creates a new instance
     * 
     * @param args
     * @param options
     * @throws ParseException 
     */
    public ConnectionSettingsParser(String[] args, Options options) throws ParseException {
        this.options = options;
        addOptions(options);
        cli = new DefaultParser().parse(options, args);
    }
    
    /**
     * Adds the connection-specific parameters to CLI
     */
    protected abstract void addOptions(Options options);

    /**
     * Checks the connection-specific parameters
     * 
     * @param cli
     * @throws IllegalArgumentException
     */
    public abstract void checkCLIParameters() throws IllegalArgumentException;
    
    /**
     * Returns the connection settings
     * 
     * @param self - own user
     * @return
     */
    public abstract ConnectionSettings getConnectionSettings(Participant self);
    
    /**
     * Returns the options
     * 
     * @return
     */
    public Options getOptions() {
        return this.options;
    }
    
    /**
     * @return the cli
     */
    protected CommandLine getCLI() {
        return cli;
    }
}
