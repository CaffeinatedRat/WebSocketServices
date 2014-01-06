/**
* Copyright (c) 2012-2014, Ken Anderson <caffeinatedrat at gmail dot com>
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
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;
import com.caffeinatedrat.WebSocketServices.Listener.*;

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
    private static Map<String, Long> loginTimes = new HashMap<String, Long>();
    
    // ----------------------------------------------
    // Events
    // ----------------------------------------------
    
    /*
     * This is called when your plug-in is enabled
     */
    @Override
    public void onEnable() {
        
        //Save the default configuration.
        saveDefaultConfig();

        //Register the listeners...
        getServer().getPluginManager().registerEvents(new PlayerListener(loginTimes), this);
        
        //Manage the configuration...
        WebSocketServicesConfiguration config = new WebSocketServicesConfiguration(this);
        Logger.debugLevel = config.getDebugLevel();
        
        IMasterApplicationLayer masterApplicationLayer = new MasterApplicationLayer(getServer(), loginTimes, config, registeredApplicationLayers);
        
        //Start-up the server with all the appropriate configuration values.
        server = new Server(config.getPortNumber(), masterApplicationLayer, config.getIsWhiteListed(), config.getMaximumConnections());
        server.setHandshakeTimeout(config.getHandshakeTimeOut());
        server.setFrameTimeoutTolerance(config.getFrameTimeOutTolerance());
        server.setOriginCheck(config.getCheckOrigin());
        server.setPingable(config.getIsPingable());
        server.setIdleTimeOut(config.getIdleConnectionTimeOut());
        server.setMaximumFragmentationSize(config.getMaximumFragmentationSize());
        
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
        
        // --- CR (7/21/13) --- Force the plug-in name to lower-case.
        registeredApplicationLayers.put(plugin.getName().toLowerCase(), applicationLayer);
        
    }
}
