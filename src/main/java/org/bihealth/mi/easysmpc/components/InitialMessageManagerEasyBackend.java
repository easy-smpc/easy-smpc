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
package org.bihealth.mi.easysmpc.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasyBackend;

import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.MessageInitial;

/**
 * Message manager for EasyBackend
 * @author Felix Wirth
 *
 */
public class InitialMessageManagerEasyBackend extends InitialMessageManager {
    
    /** Settings */
    private final ConnectionSettingsEasyBackend settings;

    /**
     * Creates a new instance
     * 
     * @param actionUupdateMessage
     * @param actionError
     * @param settings
     */
    public InitialMessageManagerEasyBackend(Consumer<List<MessageInitialWithIdString>> updateMessage,
                                            Consumer<String> actionError,
                                            ConnectionSettingsEasyBackend settings) {
        super(updateMessage, actionError);
        // Store
        this.settings = settings;
    }

    @Override
    public List<MessageInitialWithIdString> retrieveMessages() throws IllegalStateException {
        // TODO Actual implement
        List<MessageInitialWithIdString> dummy = new ArrayList<>();
        try {
            String messageString = "H4sIAAAAAAAAAJ1VW46jRhT1RJMoP/lIpEjZQKRILXUDNtNjRS1NgQ02jXGDDQX1E0EVbcyrGfMw8JFFZB1ZSLaQRWQPueCZnswoiZTUh1UP17m3zj338Osfk8/L0+R7Fl5X9U/MP2Vl5bPqmgbldZj5x7TMCnq9CcvSP4S//bJrv777Qfhs8nI9+bIMcxae1gt98pL5lV9NvtFjv/FvUj8/3Oyq0zE//KhPvj2F9Fgcw7xaDnCIsROAvZ38PHmhT756PjT8LBw322ICo/ri99OWQ5JrdEhuE7ITOebME8/VKjq1IrIya0/Qej1zKg+nJRX4s9eJBnGN3sMs3WGxYGoaBZIfz/zm3l/oBB2QujORTjMn9l2pIFjkdtgKELKOPhZ7htAJc0Os+aexmjGW8NdY8+dYZpbWW+6MFqoz84QoCjKW7vE8Ia6Jtnbb0Iw2XpY+EayUTD00AU4amPch1njazReBINYEG5yP57UtOBzkXZPpJrHcKPZVpSaCYwSClQ4xJFWMAuwAMdrGzxTB281LD4u5zhschTxIX8F72jPkDnhGQeE+WxkccBjsVSvVpxbvClZEM75nqpIweR57+Qby4SMft33gSrHOSxHNrcIT0jNgc0NcGTAYtkTHTpfUlK4QMmYUDUPh6eE9b/+tRiNv8+Ixp5tzZosp1AeFqxFUotM1uh/yzT/NVxzyrT/KN+FTOjUi4En28e3plX0l0uool+c1QskzZy1azRAaOHSNJ8gxhXq3/8YJFZwYtMJRqCnN7MZ0NdBUVBEXuOEvGNtpdAYcyRt1Ov9nDnIj9aYQc3W5pydK57vW01BLqNMe6twRG8d+HjdalmyBC3kP5NKVVlzqTd7Xu/Lgns5pBeHSmqlOTtz17fBfloG+3r1z4UbngUNFLSKWLesAK5wvi8AP04LcSoGrLn7oHlevkturllsgRDSEoiPktAimTs2WCZIxvHfZpqBnzof1UiAFzQ1un83rMHO6gNNEiL9lmIe7bRlMGdTB2gM/MuDAHukhXg3noMkUNMBq2OeJCu9BWmDmUYck6a3nkkjPWugbWu9z0ApgR287B6izZ+tSBnpn9OHm49EMP7NRK8xdD/Dy6WHjXiFv7WzR5c54Kt383QhXxoWnTw9G3Ntxytwlejyg/QCzRMLZ1J1e4BThtR2Vj07/EBfO7DFfI0OYd6QTwUOi1MPtwHPl78QUNCF6udPridEEGaynVgG9PvADNRdz2llKqIIeYE5sviGqjZ6HBG9aAT9I/IgfC4t8YH7423LsGQn4JIZpi45ptzC3bOgxbuBFV6oN4IIHOI8ENBbAvoctTs+MI+3EFHRRBCujouqmtuEcvGfwxijIzVvezZ+OoXve3dgf+ERGD/1e/D9M6z5qmZQ9CeAfl94Ezxy9wcRWAr3W0+nI+QJIXwaZAr12WY9vlKKR03c+Av7qnU3wSjp1OILBE0ZdKdE7jBD60ht91r3kAufCxpaKIDd40G2sZ1Y69m5+0cIqN5GqOnA/vfjKcB5raPw2uFZ6iQnkg1cjdJhtOEXexJpsmnd3b14va7maLZEscNpuLu/vNw+Sa3uRTe7pebkye+ScrvbsppHKu2ryXeiX3fhhZWHDvznmZV2Fw6KavAj+BGiyQ3+SBwAA";
            String data = Message.deserializeMessage(messageString).data;
            MessageInitial messagein = MessageInitial.decodeMessage(Message.getMessageData(data));
            dummy.add(new MessageInitialWithIdString(messagein, "4712", messageString));
            dummy.add(new MessageInitialWithIdString(dummy.get(0).getMessage(), "4813", dummy.get(0).getMessageString()));
        } catch (IllegalArgumentException | ClassNotFoundException | IOException e) {
            System.out.println("Error");
        }
        dummy.add(new MessageInitialWithIdString(dummy.get(0).getMessage(), "4812", dummy.get(0).getMessageString()));
        return dummy;
    }

    @Override
    public void deleteMessage(String id) throws IllegalStateException {
        // TODO Auto-generated method stub
    }
}
