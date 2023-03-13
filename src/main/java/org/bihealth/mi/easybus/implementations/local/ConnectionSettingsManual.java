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
package org.bihealth.mi.easybus.implementations.local;

import org.bihealth.mi.easybus.ConnectionSettings;

public class ConnectionSettingsManual extends ConnectionSettings {

    /** SVUID */
    private static final long serialVersionUID = -1296045964851773114L;

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public boolean isValid(boolean usePasswordProvider) {
        return true;
    }

    @Override
    public int getCheckInterval() {
        return 0;
    }

    @Override
    public int getSendTimeout() {
        return 0;
    }

    @Override
    public int getMaxMessageSize() {
        return 0;
    }

    @Override
    public ExchangeMode getExchangeMode() {
        return ExchangeMode.MANUAL;
    }
}
