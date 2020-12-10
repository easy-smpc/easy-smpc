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
package de.tu_darmstadt.cbs.app.importdata;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implements an extractor for data
 * 
 * @author Felix Wirth
 */
public abstract class Extractor {
    /** Exact number rows or columns */
    protected static final int    EXACT_ROW_COLUMNS_LENGTH = 2;
    /** List of extracted data */
    protected Map<String, String> extractedData            = new LinkedHashMap<String, String>();

    
    /**
     * Extract the data
     *
     * @return the data
     */
    public abstract Map<String, String> getExtractedData() throws IllegalArgumentException;

}
