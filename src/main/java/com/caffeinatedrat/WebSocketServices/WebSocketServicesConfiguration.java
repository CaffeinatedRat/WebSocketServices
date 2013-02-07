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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

public class WebSocketServicesConfiguration extends YamlConfiguration {

    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    public int getPortNumber() {
        return getInt("websocket.portNumber", 25564);
    }
    
    public int getMaximumConnections() {
        return getInt("websocket.maximumConnections", 32);
    }
    
    public int getMaxDepth() {
        int maxNesting = getInt("websocket.maximumJSONSerializationNesting", 10);
        return (maxNesting > 10) ? 10 : maxNesting;
    }
    
    public boolean getIsWhiteListed() {
        return getBoolean("websocket.whitelist", false);
    }
    
    public int getDebugLevel() {
        return getInt("websocket.logging", 0);
    }

    public int getHandshakeTimeOut() {
        return getInt("websocket.handshakeTimeOutTolerance", 1000);
    }
    
    public int getFrameTimeOutTolerance() {
        return getInt("websocket.frameTimeOutTolerance", 15000); 
    }

    public boolean getCheckOrigin() {
        return getBoolean("websocket.checkOrigin", false);
    }
    
    public boolean getIsPingable() {
        return getBoolean("websocket.pingable", true);
    }
    
    public boolean isServiceEnabled(String service) {
        
        
        return getBoolean("services." + service.toLowerCase(), false);
        
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public WebSocketServicesConfiguration(JavaPlugin plugin) {
        super();
        
        try {
            load(new File(plugin.getDataFolder(), "config.yml"));
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            Logger.severe("Cannot load config.yml");
        } catch (InvalidConfigurationException e) {
            Logger.severe("Cannot load config.yml");
        }
    }
}
