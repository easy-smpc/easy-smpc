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
package org.bihealth.mi.easybus.implementations.http;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;

import com.github.markusbernhardt.proxy.ProxySearch;

/**
 * Try to create a HTTP proxy. In parts derived from
 * k9mail-library/src/main/java/com/fsck/k9/mail/store/imap/ImapStoreUriCreator.java
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class ConnectionHTTPProxy {

    /**
     * Get proxy for HTTP(S) connection
     * 
     * @param uri
     */
    public static Proxy getProxy(URI uri) {
        try {

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
                        return proxy;
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
