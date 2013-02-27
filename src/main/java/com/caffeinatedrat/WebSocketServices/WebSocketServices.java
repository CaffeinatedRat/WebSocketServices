/**
* Copyright (c) 2012, Ken Anderson <caffeinatedrat at gmail dot com>
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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.caffeinatedrat.SimpleWebSockets.*;
//import com.caffeinatedrat.WebSocketServices.Test.TestServer;

/**
 * The bukkit entry point for the WebSocketServices.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class WebSocketServices extends JavaPlugin {
	
    // ----------------------------------------------
	// Member Vars (fields)
    // ----------------------------------------------
	
    private Server server = null;
    private static Map<String, IApplicationLayer> registeredApplicationLayers = new HashMap<String, IApplicationLayer>();
    
    // ----------------------------------------------
    // Events
    // ----------------------------------------------
    
    /*
     * This is called when your plug-in is enabled
     */
    @Override
    public void onEnable() {
        
        saveDefaultConfig();
        
        WebSocketServicesConfiguration config = new WebSocketServicesConfiguration(this);
        
        Globals.debugLevel = config.getDebugLevel();
        ApplicationLayer applicationLayer = new ApplicationLayer(getServer(), config, registeredApplicationLayers);
        
        server = new Server(config.getPortNumber(), applicationLayer, config.getIsWhiteListed(), config.getMaximumConnections());
        server.setHandshakeTimeout(config.getHandshakeTimeOut());
        server.setFrameTimeoutTolerance(config.getFrameTimeOutTolerance());
        server.setOriginCheck(config.getCheckOrigin());
        server.setPingable(config.getIsPingable());
        
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
     * This is called during a phase when all plug-ins have been loaded but not enabled.
     */    
    @Override
    public void onLoad() {

    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    public static void registerApplicationLayer(Plugin plugin, IApplicationLayer applicationLayer) {
        if (plugin == null) {
            throw new NullPointerException("The plugin cannot be null.");
        }
        
        registeredApplicationLayers.put(plugin.getName(), applicationLayer);
    }

    /*
     * Stand-alone entry-point for testing...
     */
    public static void main(String[] args) {

        //TestServer.start();
    }
}
