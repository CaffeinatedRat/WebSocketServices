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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

public class WebSocketServicesConfiguration extends YamlConfiguration {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private JavaPlugin pluginInfo;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Returns this plug-in's information.
     * @return This plug-in's information.
     */
    public JavaPlugin getPlugInfo() {
        
        return this.pluginInfo;
        
    }
    
    /**
     * Safely returns the port number.
     * @return Safely returns the port number.
     */
    public int getPortNumber() {
        
        int portNumber = getInt("websocket.portNumber", 25564);
        
        //Validate the port number.
        if (portNumber < 0 || portNumber > 65535) {
        
            Logger.warning(MessageFormat.format("The port number {0} is invalid, defaulting to port 25564.", portNumber));
            return 25564;
            
        }
        
        return portNumber;
        
    }
    
    /**
     * Safely returns the maximum number of connections.
     * @return Safely returns the maximum number of connections.
     */
    public int getMaximumConnections() {
        
        int maxConnections = getInt("websocket.maximumConnections", 32);
        
        //Validate
        if (maxConnections < 0) {
        
            Logger.warning(MessageFormat.format("The maximum number of connections {0} is invalid, defaulting to 32.", maxConnections));
            return 32;
            
        }
        
        return maxConnections;
    }
    
    public int getMaxDepth() {
        int maxNesting = getInt("websocket.maximumJSONSerializationNesting", 10);
        return (maxNesting > 10) ? 10 : maxNesting;
    }

    /**
     * Determines if the WebSocketServices plug-in is white-listed.
     * @return true if the WebSocketServices server is white-listed.
     */
    public boolean getIsWhiteListed() {
        
        return getBoolean("websocket.whitelist", false);

    }
    
    /**
     * Safely returns the logging level.
     * @return safely returns the logging level.
     */
    public int getDebugLevel() {
        int logLevel = getInt("websocket.logging", 0);
        
        //Validate
        if ( (logLevel < 0) || (logLevel > 2) ) {
        
            Logger.warning(MessageFormat.format("The log level {0} is invalid, defaulting to 0.", logLevel));
            return 0;
            
        }
        
        return logLevel;
        
    }

    /**
     * Safely returns the handshake timeout in milliseconds.
     * @return safely returns the handshake timeout in milliseconds.
     */
    public int getHandshakeTimeOut() {
        
        int timeout = getInt("websocket.handshakeTimeOutTolerance", 1000);
        
        //Validate
        if (timeout < 0) {
        
            Logger.warning(MessageFormat.format("The handshake timeout tolerance {0} is invalid, defaulting to 1000.", timeout));
            return 1000;
            
        }
        
        return timeout;
        
    }
    
    /**
     * Safely returns the frame timeout in milliseconds.
     * @return safely returns the frame timeout in milliseconds.
     */
    public int getFrameTimeOutTolerance() {
        
        int timeout = getInt("websocket.frameTimeOutTolerance", 3000);
        
        //Validate
        if (timeout < 0) {
        
            Logger.warning(MessageFormat.format("The frame timeout tolerance {0} is invalid, defaulting to 3000.", timeout));
            return 3000;
            
        }
        
        return timeout;
        
    }

    /**
     * Safely returns the maximum fragmentation size.
     * @return returns the maximum fragmentation size.
     */    
    public int getMaximumFragmentationSize() {
     
        int maximumFragmentationSize = getInt("websocket.maximumFragmentationSize", 2);
        
        //Validate
        if (maximumFragmentationSize < 0) {
        
            Logger.warning(MessageFormat.format("The maximum fragmentation size {0} is invalid, defaulting to 2.", maximumFragmentationSize));
            return 2;
            
        }
        
        return maximumFragmentationSize;
        
    }
    
    /**
     * Safely returns the idle timeout in milliseconds.
     * @return safely returns the idle timeout in milliseconds.
     */
    public int getIdleConnectionTimeOut() {
        
        int timeout = getInt("websocket.idleConnectionTimeOut", 3000);
        
        //Validate
        if (timeout < 0) {
        
            Logger.warning(MessageFormat.format("The idle connection timeout {0} is invalid, defaulting to 3000.", timeout));
            return 3000;
            
        }
        
        return timeout;
        
    }
    
    /**
     * Determines if the origin is checked when establishing a connection.
     * @return true if the origin is checked when establishing a connection.
     */
    public boolean getCheckOrigin() {
        return getBoolean("websocket.checkOrigin", false);
    }
    
    /**
     * Determines if the WebSocketServices server is pingable.
     * @return true if the WebSocketServices server is pingable.
     */
    public boolean getIsPingable() {
        return getBoolean("websocket.pingable", false);
    }

    /**
     * Determines if a disabled service should mute its response.
     * @return true if a disabled service should mute its response.
     */    
    public boolean getMuteDisabledServices() {
        return getBoolean("websocket.muteDisabledServices", false);
    }
    
    /**
     * Determines if a specified service is enabled.
     * Precondition: The serviceName should be lower-case.
     * @param the name of the service.
     * @return true if the service is enabled.
     */
    public Boolean isServiceEnabled(String serviceName) {
        
        fixIt();
        
        // --- CR (7/21/13) --- Removed the lower-case check as this is now a precondition.
        return (Boolean)get("services." + serviceName, null);
        
    }
    
    /**
     * Returns the name of the extension.
     * Precondition: The serviceName should be lower-case.
     * @param the name of the extension.
     * @return the name of the extension's plugin.
     */
    public String getExtensionName(String service) {
        
        // --- CR (7/21/13) --- Removed the lower-case check as this is now a precondition.
        // --- CR (7/21/13) --- Force the extension name to lower-case.
        return getString("extensions." + service, "").toLowerCase();
        
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public WebSocketServicesConfiguration(JavaPlugin plugin) {
        super();
        
        this.pluginInfo = plugin;
        
        try {
            load(new File(plugin.getDataFolder(), "config.yml"));
            
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            Logger.severe("Cannot load config.yml");
        } catch (InvalidConfigurationException e) {
            Logger.severe("Cannot load config.yml");
        }
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * A hacky way of fixing issues with case-sensitive service names that would plague the config.yml resource file.
     */
    private void fixIt() {
        
        if (!this.contains("services.offlineplayers")) {

            this.set("services.offlineplayers", this.getBoolean("services.offlinePlayers"));
            
        }
        
    }
}
