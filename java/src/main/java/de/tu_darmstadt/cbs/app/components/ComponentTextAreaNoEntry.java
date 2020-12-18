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

import java.awt.Component;

import javax.swing.JPopupMenu;

/**
 * @author Felix Wirth
 *
 */
public class ComponentTextAreaNoEntry extends ComponentTextArea{

    /** SVID */
    private static final long serialVersionUID = -409784899589033224L;

    /**
     * Creates a new instance
     * @param text
     * @param parent
     */
    ComponentTextAreaNoEntry(String text, Component parent) {
        super(text, null);
        this.setComponentPopupMenu(new JPopupMenu());
        this.setLineWrap(true);
        this.setEditable(false);
        this.setBackground(parent.getBackground());
        
    }

}
