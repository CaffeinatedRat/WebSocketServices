/**
* Copyright (c) 2012, Ken Anderson <caffeinatedrat@gmail.com>
* All rights reserved.
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE AUTHOR AND CONTRIBUTORS BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.caffeinatedrat.WebSocketServices;

import java.text.MessageFormat;

import com.caffeinatedrat.SimpleWebSockets.BinaryResponse;
import com.caffeinatedrat.SimpleWebSockets.IApplicationLayer;
import com.caffeinatedrat.SimpleWebSockets.TextResponse;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

/**
 * The application layer that manages the available web services.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */

public class ApplicationLayer implements IApplicationLayer {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private org.bukkit.Server minecraftServer;
    private WebSocketServicesConfiguration config;
    private ServiceLayer serviceLayer;
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ApplicationLayer(org.bukkit.Server minecraftServer, WebSocketServicesConfiguration config) {
        this.minecraftServer = minecraftServer;
        this.config = config;
        this.serviceLayer = new ServiceLayer(this.minecraftServer);
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    public void onTextFrame(String text, TextResponse response) {
        
        //TODO: Extract into a json formatter.
        StringBuilder responseBuffer = new StringBuilder();
        
        //Determine if the service is available and if it is then generate a response.
        if(config.isServiceEnabled(text)) {

            responseBuffer.append("{");
            
            if(serviceLayer.execute(text, responseBuffer)) {
                responseBuffer.append(((responseBuffer.length() > 0) ? "," : "") + "\"Status\": \"SUCCESSFUL\"");
            }
            else {
                responseBuffer.append(((responseBuffer.length() > 0) ? "," : "") + "\"Status\": \"FAILURE\"");
            }
            
            responseBuffer.append("}");
            
        }
        //The service is not available so send a NA status.
        else {
            
            Logger.verboseDebug(MessageFormat.format("Service {0} has been disabled.", text));
            
            responseBuffer.append("{");
            responseBuffer.append("\"Status\": \"NOT AVAILABLE\"");
            responseBuffer.append("}");
            
        }
        
        response.data = responseBuffer.toString();
    }

    public void onBinaryFrame(byte[] data, BinaryResponse response) {
        // TODO Auto-generated method stub

    }

    public void onClose() {
        // TODO Auto-generated method stub

    }

    public void onPing(byte[] data) {
        // TODO Auto-generated method stub

    }

    public void onPong() {
        // TODO Auto-generated method stub
    }

}
