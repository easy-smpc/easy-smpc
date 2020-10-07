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
package de.tu_darmstadt.cbs.app;

import javax.swing.JLabel;
import javax.swing.JPanel;

import lombok.Getter;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 */

public class PerspectiveFinalize extends Perspective {

    /**
     * To be removed
     */
    @Getter
    private JLabel myResult;

    /**
     * Creates the perspective
     * @param app
     */
    protected PerspectiveFinalize(App app) {
        super(app, Resources.getString("PerspectiveFinalize.0")); //$NON-NLS-1$
    }

    /**
     * Creates and adds UI elements
     */
    @Override
    protected void createContents(JPanel panel) {
        // TODO Auto-generated method stub
        this.myResult = new JLabel("Placeholder"); //TODO: Replace entirely
        panel.add(myResult);

    }
}
