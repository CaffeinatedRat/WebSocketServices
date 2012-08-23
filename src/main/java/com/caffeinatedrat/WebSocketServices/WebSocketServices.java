/**
 * 
 */
package com.caffeinatedrat.WebSocketServices;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.caffeinatedrat.SimpleWebSockets.*;
import com.caffeinatedrat.SimpleWebSockets.Util.*;
import com.caffeinatedrat.WebSocketServices.Test.TestServer;

/**
 * @author CaffeinatedRat
 *
 */
public class WebSocketServices extends JavaPlugin {

    private Server server;
    
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
    public void onLoad()
    {
        if(!DependencyManager.ExtractJar(this, "base64-3.8.1.jar", "plugins/WebSocketServices"))
            Logger.severe("Unable to extract the base64-3.8.1.jar.");
    }
    
    /*
     * For testing...
     */
    public static void main(String[] args)
    {
        if(args.length > 0)
        {
            if(args[0].equalsIgnoreCase("test"))
            {
                TestServer.start();
            }
            else if (args[0].equalsIgnoreCase("register"))
            {
                if(!DependencyManager.ExtractJar(new WebSocketServices(), "base64-3.8.1.jar", "plugins/WebSocketServices"))
                {
                    Logger.severe("Unable to extract the base64-3.8.1.jar.");
                }
                else
                {
                    Logger.info("The base64-3.8.1.jar has been extracted.");
                }
            }
        }
        else
        {
            if(!DependencyManager.ExtractJar(new WebSocketServices(), "base64-3.8.1.jar", "plugins/WebSocketServices"))
            {
                Logger.severe("Unable to extract the base64-3.8.1.jar.");
            }
            else
            {
                Logger.info("The base64-3.8.1.jar has been extracted.");
            }
        }
        //END OF if(args.length > 0)
    }
}
