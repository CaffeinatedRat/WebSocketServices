/**
* Copyright (c) 2012-2013, Ken Anderson <caffeinatedrat at gmail dot com>
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

import com.caffeinatedrat.SimpleWebSockets.IApplicationLayer;
import com.caffeinatedrat.SimpleWebSockets.ResponseWrapper;
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
    
    public ApplicationLayer(org.bukkit.Server minecraftServer, Map<String, Long> loginTimes, WebSocketServicesConfiguration config, Map<String, IApplicationLayer> applicationLayers) {
        
        this.minecraftServer = minecraftServer;
        this.config = config;
        this.serviceLayer = new ServiceLayer(this.minecraftServer, loginTimes, config);
        this.registeredApplicationLayers = applicationLayers;

    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    public void onTextFrame(String text, ResponseWrapper responseWrapper) {

        // --- CR (3/3/13) --- Prepare for handling arguments for text services.
        String[] tokens = text.split(" ", 2);
        String serviceName = tokens[0];
        String arguments = null;

        if (tokens.length > 1) {

            arguments = tokens[1];
            
        }
        
        // --- CR (6/22/13) --- We are separating extensions from built-in services.
        //Determine if the service is available and if it is then generate a response.
        Boolean service = config.isServiceEnabled(serviceName);
        
        //The built-in service was found in the configuration file and is not an extension.
        if(service != null) {

            //Execute the built-in services.
            if(service) {
                
                //Right now the webservices will be treated as first-class services, while other plug-ins will only be handled if the webservice does not exist.
                if(serviceLayer.executeText(serviceName, arguments, responseWrapper)) {
                    
                    if (responseWrapper.response instanceof TextResponse) {
                    
                        ((TextResponse)responseWrapper.response).getCollection().put("Status", "SUCCESSFUL");
                        return;
                    }
                    
                }

            }
            //END OF if(service) {...

            Logger.verboseDebug(MessageFormat.format("Service {0} has been disabled.", serviceName));

        }
        //The service was not found, determine if this is an extension and run it accordingly.
        else {
            
            // --- CR (6/23/2013) --- Major change in the way the extensions are handled.
            // Extensions are no longer handled by default, they must be listed in the config.yml.
            // This allows the administrator to control what extensions can run and even control the names of the services invoked for an extension.
            // This can prevent service names from conflicting from two or more extensions that use the same service name.
            // For example, if two extensions use the service name "Map" to invoke both extensions then the extension name can be changed.
            // extensions.Map: Plugin1
            // extensions.Map1: Plugin2
            
            String extensionName = config.getExtensionName(serviceName);
            if ((extensionName != null) && (extensionName != "") ) {
                
                IApplicationLayer applicationLayer = this.registeredApplicationLayers.get(extensionName);
                if (applicationLayer != null) {
                    
                    applicationLayer.onTextFrame(arguments, responseWrapper);
                    
                    if (responseWrapper.response != null) {
                        
                        //The plug-in name is appended to all other data.
                        if (responseWrapper.response instanceof TextResponse) {
                            
                            ((TextResponse)responseWrapper.response).getCollection().put("ExtensionName", extensionName);
                            return;
                        }
                        
                    }
                    else {
                        
                        Logger.verboseDebug(MessageFormat.format("Extension {0} has a null response.", serviceName));
                        
                    }
                    //END OF if (responseWrapper.response != null) {...
                    
                }
                else {
                    
                    Logger.verboseDebug(MessageFormat.format("Extension {0} has not registered its application layer.", serviceName));
                    
                }
                //END OF if (applicationLayer != null) {...
                
            }
            else {
                
                Logger.verboseDebug(MessageFormat.format("Extension {0} has been disabled.", serviceName));
                
            }
            //END OF if ((extensionName != null) && (extensionName != "") ) {...

            //Adds an O(n) operation.
/*            Iterator<Entry<String, IApplicationLayer>> iterator = this.registeredApplicationLayers.entrySet().iterator();
            while (iterator.hasNext()) {
                
                Map.Entry<String, IApplicationLayer> pairs = (Map.Entry<String, IApplicationLayer>)iterator.next();
                ((IApplicationLayer)pairs.getValue()).onTextFrame(text, responseWrapper);
                
                //The plug-in name is appended to all other data.
                if (responseWrapper.response instanceof TextResponse) {
                    
                    ((TextResponse)responseWrapper.response).getCollection().put("PluginName", pairs.getKey());
                    
                }
            }*/

        }
        //END OF if(service != null) {...
        
        //--- CR (6/22/13) --- Mute or hide responses from a disabled service.
        if(!config.getMuteDisabledServices()) {
            
            responseWrapper.response = new TextResponse();
            
            ((TextResponse)responseWrapper.response).getCollection().put("Status", "NOT AVAILABLE");
            
        }

    }

    public void onBinaryFrame(byte[] data, ResponseWrapper responseWrapper) {

        //This is temporary until some standard is created...
        if (config.isServiceEnabled("binaryfragmentationtest")) {
            
            //Right now the webservices will be treated as first-class services, while other plug-ins will only be handled if the webservice does not exist.
            if(!serviceLayer.executeBinary(data, responseWrapper)) {
                
                //Adds an O(n) operation.
                Iterator<Entry<String, IApplicationLayer>> iterator = this.registeredApplicationLayers.entrySet().iterator();
                while (iterator.hasNext()) {
                    
                    Map.Entry<String, IApplicationLayer> pairs = (Map.Entry<String, IApplicationLayer>)iterator.next();
                    ((IApplicationLayer)pairs.getValue()).onBinaryFrame(data, responseWrapper);
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
