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
package org.bihealth.mi.easysmpc.spreadsheet;

/**
 * Abstract class for spreadsheet functions
 * 
 * @author Felix Wirth
 *
 */
public abstract class ScriptFunction {
    
    private SpreadsheetCell cell;

    /**
     * Creates a new instance
     */
    public ScriptFunction(String values) {
        String[] splitValues = values.split(";");
        for (String valuePart : splitValues) {

        }
        
    }
    
    public static ScriptFunction createFunction(String text) {
        // Check
        // TODO Improve with a regex
        if(text == null || text.length() < 2 ||  text.startsWith("=") || text.contains("(") || text.contains(")")) {
            throw new IllegalArgumentException("Script does not start with the correct sign!");
        }
        
        String functionName = text.substring(1, text.indexOf("(") - 1 ).toUpperCase();
        
        try {
            ScriptFunctionType function = ScriptFunctionType.valueOf(functionName);
            switch (function) {
            case MEAN:
                return new ScriptMean(text.substring(text.indexOf("(") - 1, text.length() - 1));
            default:
                throw new IllegalArgumentException(String.format("Unknown script function %s",functionName));
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to understand script", e);
        }
    }
    
    /**
     * Possible script function names
     * 
     * @author Felix Wirth
     *
     */
    public enum ScriptFunctionType {
        MEAN, SECRET_ADD
    }
}
