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
import java.util.Date;
import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class ServiceLayer {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private org.bukkit.Server minecraftServer;
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------    
    
    public ServiceLayer (org.bukkit.Server minecraftServer) {
        this.minecraftServer = minecraftServer;
    }
    
    /**
     * Determine which service to execute.
     * @param service The name of the requested service.  If the service does not exist then nothing is done.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */    
    public boolean execute(String service, StringBuilder responseBuffer) {
        
        if (service.equalsIgnoreCase("WHO")) {
            return who(responseBuffer);
        }
        else if (service.equalsIgnoreCase("INFO")) {
            return info(responseBuffer);
        }
        else if (service.equalsIgnoreCase("PLUGINS")) {
            return plugins(responseBuffer);
        }
        else if (service.equalsIgnoreCase("WHITELIST")) {
            return whiteList(responseBuffer);
        }
        else {
            //Unknown command...do nothing.
            return false;
        }
    }
    
    /**
     * Provides a list of the players that are currently online and the maximum number of players allowed.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    protected boolean who(StringBuilder responseBuffer) {
        
        Player[] players = this.minecraftServer.getOnlinePlayers();
        
        responseBuffer.append(MessageFormat.format("\"MaxPlayers\": \"{0}\",", this.minecraftServer.getMaxPlayers()));
        responseBuffer.append("\"Players\": [");
        
        for (int i = 0; i < players.length; i++) {
            
            if(i > 0) { 
                responseBuffer.append(",");
            }
            
            String timePlayed = "Never";
            long timeSpan = new Date().getTime() - players[i].getLastPlayed();
            timePlayed = MessageFormat.format("{0}d {1}h {2}m {3}s",
                        (timeSpan / 3600000L / 24),
                        (timeSpan / 3600000L % 60),
                        (timeSpan / 60000L % 60),
                        (timeSpan / 1000L % 60));
            
            responseBuffer.append("{");
            responseBuffer.append(MessageFormat.format("\"name\": \"{0}\", \"onlineTime\": \"{1}\"", players[i].getName(), timePlayed));
            responseBuffer.append("}");
        }
        
        responseBuffer.append("]");
        
        return true;
    }
    
    /**
     * Provides information about the server.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */    
    protected boolean info(StringBuilder responseBuffer) {
        
        responseBuffer.append(MessageFormat.format("\"name\": \"{0}\", \"serverName\": \"{1}\", \"version\": \"{2}\", \"bukkitVersion\": \"{3}\", \"worldType\": \"{4}\", \"allowsNether\": {5}, \"allowsEnd\": {6}, \"allowsFlight\": {7}, \"isWhiteListed\": {8}, \"motd\": \"{9}\", \"gameMode\": \"{10}\", \"port\": \"{11}\", \"ipAddress\": \"{12}\""
                , this.minecraftServer.getName()
                , this.minecraftServer.getServerName()
                , this.minecraftServer.getVersion()
                , this.minecraftServer.getBukkitVersion()
                , this.minecraftServer.getWorldType()
                , this.minecraftServer.getAllowNether()
                , this.minecraftServer.getAllowEnd()
                , this.minecraftServer.getAllowFlight()
                , this.minecraftServer.hasWhitelist()
                , this.minecraftServer.getMotd()
                , this.minecraftServer.getDefaultGameMode().toString()
                , this.minecraftServer.getPort()
                , this.minecraftServer.getIp()
                ));
        
        return true;
    }
    
    /**
     * Provides a list of all available plug-ins.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */    
    protected boolean plugins(StringBuilder responseBuffer) {
        
        Plugin[] plugins = this.minecraftServer.getPluginManager().getPlugins();
        
        responseBuffer.append("\"Plugins\": [");
        
        for (int i = 0; i < plugins.length; i++) {
            
            PluginDescriptionFile pluginDescriptor =  plugins[i].getDescription();

            if(i > 0) {
                responseBuffer.append(",");
            }
            
            responseBuffer.append("{");
            responseBuffer.append(MessageFormat.format("\"name\": \"{0}\", \"description\": \"{1}\", \"author\": \"{2}\", \"version\": \"{3}\""
                    , plugins[i].getName()
                    , pluginDescriptor.getDescription().replaceAll("(\r\n|\n)", "")
                    , pluginDescriptor.getAuthors().toString()
                    , pluginDescriptor.getVersion()));
            responseBuffer.append("}");
        }
        
        responseBuffer.append("]");
        
        return true;
    }
    
    /**
     * Provides a list of all white-listed players.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    protected boolean whiteList(StringBuilder responseBuffer) {

        Set<OfflinePlayer> whiteListedPlayers = this.minecraftServer.getWhitelistedPlayers();
        
        responseBuffer.append("\"Whitelist\": [");
        
        int i = 0;
        for(OfflinePlayer offlinePlayer : whiteListedPlayers) {
        
            if(i++ > 0) {
                responseBuffer.append(",");
            }

            String lastPlayed = "Never";
            if(offlinePlayer.isOnline()) {
                lastPlayed = "Now";
            }
            else {
                if (offlinePlayer.getLastPlayed() > 0) {
                    
                    long timeSpan = new Date().getTime() - offlinePlayer.getLastPlayed();
                    
                    lastPlayed = MessageFormat.format("{0}d {1}h {2}m {3}s",
                                (timeSpan / 3600000L / 24),
                                (timeSpan / 3600000L % 60),
                                (timeSpan / 60000L % 60),
                                (timeSpan / 1000L % 60));
                }
            }
            
            responseBuffer.append("{");
            responseBuffer.append(MessageFormat.format("\"name\": \"{0}\", \"isOnline\": {1}, \"lastPlayed\": \"{2}\""
                    , offlinePlayer.getName()
                    , offlinePlayer.isOnline()
                    , lastPlayed));
            
            responseBuffer.append("}");
        }
        
        responseBuffer.append("]");
        
        return true;
    }
}
