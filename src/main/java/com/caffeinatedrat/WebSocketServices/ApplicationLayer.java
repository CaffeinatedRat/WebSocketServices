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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
    private Map<String, IApplicationLayer> registeredApplicationLayers = null;
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ApplicationLayer(org.bukkit.Server minecraftServer, WebSocketServicesConfiguration config,  Map<String, IApplicationLayer> applicationLayers) {
        this.minecraftServer = minecraftServer;
        this.config = config;
        this.serviceLayer = new ServiceLayer(this.minecraftServer);
        this.registeredApplicationLayers = applicationLayers;
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
            
            //Right now the webservices will be treated as first-class services, while other plug-ins will only be handled if the webservice does not exist.
            if(serviceLayer.executeText(text, responseBuffer)) {
                
                responseBuffer.append(((responseBuffer.length() > 0) ? "," : "") + "\"Status\":\"SUCCESSFUL\"");
                
            }
            else {

                // --- CR (10/9/12) --- Removed the failure status for now and replaced it with registered application layers.
                //responseBuffer.append(((responseBuffer.length() > 0) ? "," : "") + "\"Status\": \"FAILURE\"");
                
                //Adds an O(n) operation.
                Iterator<Entry<String, IApplicationLayer>> iterator = this.registeredApplicationLayers.entrySet().iterator();
                while (iterator.hasNext()) {
                    
                    Map.Entry<String, IApplicationLayer> pairs = (Map.Entry<String, IApplicationLayer>)iterator.next();
                    ((IApplicationLayer)pairs.getValue()).onTextFrame(text, response);
                    
                    //The plug-in name is appended to all other data.
                    responseBuffer.append(MessageFormat.format("\"PluginName\":\"{0}\",", pairs.getKey()));
                    responseBuffer.append(response.data);
                }
            }

            responseBuffer.append("}");
        }
        //The service is not available so send a NA status.
        else {
            Logger.verboseDebug(MessageFormat.format("Service {0} has been disabled.", text));
            
            responseBuffer.append("{");
            responseBuffer.append("\"Status\":\"NOT AVAILABLE\"");
            responseBuffer.append("}");
        }
        
        response.data = responseBuffer.toString();
    }

    public void onBinaryFrame(byte[] data, BinaryResponse response) {

        //This is temporary until some standard is created...
        if (config.isServiceEnabled("binaryfragmentationtest")) {
            
            //Right now the webservices will be treated as first-class services, while other plug-ins will only be handled if the webservice does not exist.
            if(!serviceLayer.executeBinary(data, response)) {
                
                //Adds an O(n) operation.
                Iterator<Entry<String, IApplicationLayer>> iterator = this.registeredApplicationLayers.entrySet().iterator();
                while (iterator.hasNext()) {
                    
                    Map.Entry<String, IApplicationLayer> pairs = (Map.Entry<String, IApplicationLayer>)iterator.next();
                    ((IApplicationLayer)pairs.getValue()).onBinaryFrame(data, response);
                }
                //END OF while (iterator.hasNext()) {...
            }
            //END OF if(!serviceLayer.executeBinary(data, response)) {...
        }
        //END OF if (config.isServiceEnabled("binaryfragmentationtest")) {...
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
