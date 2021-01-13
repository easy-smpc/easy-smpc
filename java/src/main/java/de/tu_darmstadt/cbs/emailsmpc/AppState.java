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
package de.tu_darmstadt.cbs.emailsmpc;

/**
 * Enum for the app state
 * @author Tobias Kussel
 */
public enum AppState {
    
    /** The none. */
    NONE, 
 /** The starting. */
 STARTING, 
 /** The participating. */
 PARTICIPATING, 
 /** The entering values. */
 ENTERING_VALUES, 
 /** The initial sending. */
 INITIAL_SENDING, 
 /** The sending share. */
 SENDING_SHARE, 
 /** The recieving share. */
 RECIEVING_SHARE, 
 /** The sending result. */
 SENDING_RESULT, 
 /** The recieving result. */
 RECIEVING_RESULT, 
 /** The finished. */
 FINISHED
}
