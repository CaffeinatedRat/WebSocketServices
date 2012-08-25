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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.caffeinatedrat.SimpleWebSockets.*;
import com.caffeinatedrat.SimpleWebSockets.Util.*;
import com.caffeinatedrat.WebSocketServices.Test.TestServer;

/**
 * The bukkit entry point for the WebSocketServices.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class WebSocketServices extends JavaPlugin {

    private Server server = null;
    
    /*
     * This is called when your plug-in is enabled
     */
    @Override
    public void onEnable() {
        
        saveDefaultConfig();
        
        FileConfiguration config = getConfig();
        
        int portNumber = config.getInt("websocket.portNumber", 4000);
        int maximumNumberOfThreads = config.getInt("websocket.maximumThreads", 32);
        Globals.debugLevel = config.getBoolean("websocket.debug", false) ? 1 : 0;
        Globals.debugLevel = config.getBoolean("websocket.verboseDebug", false) ? 3 : 0;
        
        server = new Server(portNumber, new ApplicationLayer(this.getServer()), maximumNumberOfThreads);
        server.setHandshakeTimeout(config.getInt("websockets.handshakeTimeOutInMilliseconds", 1000));
        server.setFrameWaitTimeOut(config.getInt("websockets.frameWaitTimeOutInMilliseconds", 15000));
        server.start();
    }
    
    /*
     * This is called when your plug-in shuts down
     */
    @Override
    public void onDisable() {
        // save the configuration file, if there are no values, write the defaults.
        server.Shutdown();
    }
    
    /*
     * This is called when the plug-in loads.
     */    
    @Override
    public void onLoad() {
        Register();
    }
    
    public void Register() {
        if (!DependencyManager.ExtractJar(this, "base64-3.8.1.jar", "plugins/WebSocketServices")) {
            Logger.severe("Unable to extract the base64-3.8.1.jar.");
        }
    }
    
    /*
     * Stand-alone entry-point.
     */
    public static void main(String[] args) {
        
        if(args.length > 0) {
            
            if (args[0].equalsIgnoreCase("test")) {
                TestServer.start();
            }
            else if (args[0].equalsIgnoreCase("register")) {
                new WebSocketServices().Register();
            }
        }
        else {
            new WebSocketServices().Register();
        }
        //END OF if(args.length > 0)
    }
}
