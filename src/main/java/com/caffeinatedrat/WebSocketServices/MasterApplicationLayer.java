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

import com.caffeinatedrat.SimpleWebSockets.IMasterApplicationLayer;
import com.caffeinatedrat.SimpleWebSockets.IApplicationLayer;
import com.caffeinatedrat.SimpleWebSockets.ConnectionData;
import com.caffeinatedrat.SimpleWebSockets.Session;
import com.caffeinatedrat.SimpleWebSockets.TextResponse;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

/**
 * The application layer that manages the available web services.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */

public class MasterApplicationLayer implements IMasterApplicationLayer {

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
    
    public MasterApplicationLayer(org.bukkit.Server minecraftServer, Map<String, Long> loginTimes, WebSocketServicesConfiguration config, Map<String, IApplicationLayer> applicationLayers) {
        
        this.minecraftServer = minecraftServer;
        this.config = config;
        this.serviceLayer = new ServiceLayer(this.minecraftServer, loginTimes, config);
        this.registeredApplicationLayers = applicationLayers;

    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Performs an action on a text based frame.
     * @param text The action being requested.
     * @param connectionData The connection data associated with this connection.
     */
    public void onTextFrame(String text, ConnectionData connectionData) {
        
        // --- CR (7/21/13) --- Force the serviceName to lower-case to get rid of all lower-case checking headaches.
        // --- CR (3/3/13) --- Prepare for handling arguments for text services.
        //Separate the service name and arguments from the incoming data.
        String[] tokens = text.split(" ", 2);
        String serviceName = tokens[0].toLowerCase();
        String arguments = null;
        
        if (tokens.length > 1) {
            
            arguments = tokens[1];
            
        }
        
        //Reference to our session if one is ever established.
        Session session = null;
        
        // --- CR (6/22/13) --- We are separating extensions from built-in services.
        //Determine if the service is available and if it is then generate a response.
        Boolean service = config.isServiceEnabled(serviceName);
        
        //The built-in service was found in the configuration file and is not an extension.
        if(service != null) {
            
            //Execute the built-in services.
            if(service) {
                
                // --- CR (7/21/13) --- We're now dealing with session data based on the service name.
                // Do not create session data if we do not support the service that is calling.
                session = connectionData.startSession(serviceName);
                
                //Right now the webservices will be treated as first-class services, while other plug-ins will only be handled if the webservice does not exist.
                if(serviceLayer.executeText(serviceName, arguments, session)) {
                    
                    if (session.response instanceof TextResponse) {
                    
                        //This is temporary until actual states can be defined.  Set the connection as active.
                        connectionData.setState();
                        
                        ((TextResponse)session.response).getCollection().put("Status", "SUCCESSFUL");
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
            
            // --- CR (7/21/13) --- The null check is unnecessary since we have a default value of an empty string.
            String extensionName = config.getExtensionName(serviceName);
            if (extensionName != "") {
                
                // --- CR (7/21/13) --- We're now dealing with session data based on the service name.
                // Do not create session data if we do not support the service that is calling.
                session = connectionData.startSession(serviceName);
                
                IApplicationLayer applicationLayer = this.registeredApplicationLayers.get(extensionName);
                if (applicationLayer != null) {
                    
                    applicationLayer.onTextFrame(arguments, session);
                    
                    if (session.response != null) {
                        
                        //The plug-in name is appended to all other data.
                        if (session.response instanceof TextResponse) {
                            
                            //This is temporary until actual states can be defined.  Set the connection as active.
                            connectionData.setState();
                            
                            ((TextResponse)session.response).getCollection().put("ExtensionName", extensionName);
                            return;
                            
                        }
                        
                    }
                    else {
                        
                        Logger.verboseDebug(MessageFormat.format("Extension {0} has a null response.", serviceName));
                        
                    }
                    //END OF if (session.response != null) {...
                    
                }
                else {
                    
                    Logger.verboseDebug(MessageFormat.format("Extension {0} has not registered its application layer.", serviceName));
                    
                }
                //END OF if (applicationLayer != null) {...
                
            }
            else {
                
                Logger.verboseDebug(MessageFormat.format("Extension {0} has not been defined.", serviceName));
                
            }
            //END OF if (extensionName != "") {...
            
        }
        //END OF if(service != null) {...
        
        //--- CR (6/22/13) --- Mute or hide responses from a disabled service.
        if(!config.getMuteDisabledServices()) {
            
            //Verify the session is valid before attempting to modify the response.
            if (session != null) {
                
                session.response = new TextResponse();
                ((TextResponse)session.response).getCollection().put("Status", "NOT AVAILABLE");
                
            }
            //END OF if (session != null) {...
            
        }
        
    }

    public void onBinaryFrame(byte[] data, ConnectionData connectionData) {
        
        Session session = connectionData.getSession();
        
        //A session must be established before we will even accept binary frames.
        if (session != null) {
            
            //This is temporary until some standard is created...
            if (config.isServiceEnabled("binaryfragmentationtest")) {
                
                //Right now the webservices will be treated as first-class services, while other plug-ins will only be handled if the webservice does not exist.
                if(!serviceLayer.executeBinary(data, session)) {
                    
                    //Adds an O(n) operation.
                    Iterator<Entry<String, IApplicationLayer>> iterator = this.registeredApplicationLayers.entrySet().iterator();
                    while (iterator.hasNext()) {
                        
                        Map.Entry<String, IApplicationLayer> pairs = (Map.Entry<String, IApplicationLayer>)iterator.next();
                        ((IApplicationLayer)pairs.getValue()).onBinaryFrame(data, session);
                        
                    }
                    //END OF while (iterator.hasNext()) {...
                }
                //END OF if(!serviceLayer.executeBinary(data, response)) {...
            }
            //END OF if (config.isServiceEnabled("binaryfragmentationtest")) {...
            
        }
        //END OF if (session != null) {...
        
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

    public void onIdle(ConnectionData connectionData) {

        // --- CR (6/23/2013) --- Major change in the way the extensions are handled.
        // Extensions are no longer handled by default, they must be listed in the config.yml.
        // This allows the administrator to control what extensions can run and even control the names of the services invoked for an extension.
        // This can prevent service names from conflicting from two or more extensions that use the same service name.
        // For example, if two extensions use the service name "Map" to invoke both extensions then the extension name can be changed.
        // extensions.Map: Plugin1
        // extensions.Map1: Plugin2
        
        //A session must be established before we will even process any idle events.
        Session session = connectionData.getSession();
        if (session != null) {
            
            //Only extensions will call onIdle, as the built-in services will terminate when completed.
            String extensionName = config.getExtensionName(connectionData.getSessionName());
            if (extensionName != "") {
                
                for ( Map.Entry<String, IApplicationLayer> entry : this.registeredApplicationLayers.entrySet() ) { 
                
                    if (extensionName.equalsIgnoreCase(entry.getKey())) {
                    
                        IApplicationLayer applicationLayer = entry.getValue();
                        if (applicationLayer != null) {
                            
                            applicationLayer.onIdle(session);
                            
                            if (session.response != null) {
                                
                                connectionData.setState();
                            
                            }
                            //END OF if (session.response != null) {...
                            
                        }
                        else {
                            
                            Logger.verboseDebug(MessageFormat.format("Extension {0} has not registered its application layer.", entry.getKey()));
                            
                        }
                        //END OF if (applicationLayer != null) {...
                        
                    }
                    //END OF if (extensionName.equalsIgnoreCase(entry.getKey())) {...
                    
                }
                //END OF for ( Map.Entry<String, IApplicationLayer> entry : this.registeredApplicationLayers.entrySet() ) {...
                
            }
            //END OF if (extensionName != "") {...
            
        }
        //END OF if (session != null) {...
        
    }

}
