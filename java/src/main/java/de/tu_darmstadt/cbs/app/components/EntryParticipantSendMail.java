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
package de.tu_darmstadt.cbs.app.components;

/**
 * Display participants for sending mail only
 * 
 * @author Felix Wirth
 *
 */
public class EntryParticipantSendMail extends EntryParticipant {

    /** SVID */
    private static final long serialVersionUID = 7357425826027805679L;
    private String exchangeString;
    private boolean isOwnEntry;
    
    public EntryParticipantSendMail(String name, String email) {
        super(name, email, false, true);
    }
    
    public void setAddButtonText(String text){
        this.add.setText(text);
    }
    public void isOwnEntry(boolean isOwnEntry){
        this.isOwnEntry = true;
        this.add.setEnabled(false);
    }
    public boolean getOwnEntry(){
        return isOwnEntry;
    }
    
    public void setExchangeString(String exchangeString){
        this.exchangeString = exchangeString;
    }
    
    public String getExchangeString(){
        return this.exchangeString;
    }
}
