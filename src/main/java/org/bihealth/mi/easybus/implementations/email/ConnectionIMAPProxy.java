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
package org.bihealth.mi.easybus.implementations.email;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import com.github.markusbernhardt.proxy.ProxySearch;

/**
 * Try to create IMAP proxy. In parts derived from
 * k9mail-library/src/main/java/com/fsck/k9/mail/store/imap/ImapStoreUriCreator.java
 * 
 * @author Fabian Prasser
 */
public class ConnectionIMAPProxy {

    /**
     * Get proxy for email connection
     * @param settings
     */
    public static Pair<String, Integer> getProxy(ConnectionIMAPSettings settings) {
        try {
                
            // TODO: More options, including STARTTLS would be available
            String scheme = "imap+ssl+";
                   
            // TODO: Other auth types would be available
            String userInfo = "PLAIN:" + settings.getIMAPEmailAddress() + ":" + settings.getIMAPPassword();
            
            // TODO: Other paths would be available
            String path = "/1|";

            // Construct URI
            URI uri = new URI(scheme, userInfo, settings.getIMAPServer(), settings.getIMAPPort(), path, null, null);

            // Search process
            ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();

            // Selector
            ProxySelector proxySelector = proxySearch.getProxySelector();

            // Install this ProxySelector as default ProxySelector for all connections.
            ProxySelector.setDefault(proxySelector);

            // Get list of proxies from default ProxySelector available for given URL
            List<Proxy> proxies = null;
            if (ProxySelector.getDefault() != null) {
                proxies = ProxySelector.getDefault().select(uri);
            }

            // Find first proxy for HTTP/S. Any DIRECT proxy in the list returned is only second choice
            if (proxies != null) {
                for (Proxy proxy : proxies) {
                    if (proxy.type() == Proxy.Type.HTTP) {
                        InetSocketAddress addr = (InetSocketAddress)proxy.address();
                        return new Pair<>(addr.getHostName(), addr.getPort());
                    }
                }
            }
         
            // Nothing found
            return null;
            
        } catch (Exception e) {
            
            // Something went wrong
            return null;
        }
    }
}
