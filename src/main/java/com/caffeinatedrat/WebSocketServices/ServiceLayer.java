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

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.caffeinatedrat.SimpleWebSockets.BinaryResponse;
import com.caffeinatedrat.SimpleWebSockets.TextResponse;
import com.caffeinatedrat.SimpleWebSockets.Util.JsonHelper;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

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
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Determine which text service to execute.
     * @param service The name of the requested service.  If the service does not exist then nothing is done.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    public boolean executeText(String service, TextResponse response) {
        
        try {
            
            if (!service.equalsIgnoreCase("executeText") && !service.equalsIgnoreCase("executeBinary")) {
                Method method = this.getClass().getDeclaredMethod(service.toLowerCase(), TextResponse.class);
                method.setAccessible(true);
                return (Boolean)method.invoke(this, response);
            }
        }
        catch(Exception ex) {
            Logger.verboseDebug(ex.getMessage());
        }
        
        return false;
    }
    
    /**
     * Determine which binary service to execute.
     * @param service The name of the requested service.  If the service does not exist then nothing is done.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */    
    public boolean executeBinary(byte[] data, BinaryResponse response) {

        if(data.length > 0) {
            byte controlByte = data[0];
            if(controlByte == 0x01) {
                return binaryFragmentationTest(data, response);
            }
        }

        return false;
    }
    

    
    /**
     * Provides information about the server.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */    
    protected boolean info(TextResponse response) {

        //Get the normal world and assume it is the first in the list.
        List<World> worlds = this.minecraftServer.getWorlds();
        World world = worlds.get(0);

        Hashtable<String, Object> collection = response.getCollection();
        
        collection.put("name", this.minecraftServer.getName().replaceAll("(\r\n|\n)", ""));
        collection.put("serverName", this.minecraftServer.getServerName());
        collection.put("version", this.minecraftServer.getVersion());
        collection.put("bukkitVersion", this.minecraftServer.getBukkitVersion());
        collection.put("worldType", this.minecraftServer.getWorldType());
        collection.put("allowsNether", this.minecraftServer.getAllowNether());
        collection.put("allowsEnd", this.minecraftServer.getAllowEnd());
        collection.put("allowsFlight", this.minecraftServer.getAllowFlight());
        collection.put("isWhiteListed", this.minecraftServer.hasWhitelist());
        collection.put("motd", this.minecraftServer.getMotd());
        collection.put("gameMode", this.minecraftServer.getDefaultGameMode().toString());
        collection.put("port", this.minecraftServer.getPort());
        collection.put("ipAddress", this.minecraftServer.getIp());
        collection.put("serverTime", world.getTime());
        
        return true;
    }
    
    /**
     * Provides a list of all available plug-ins.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */    
    protected boolean plugins(TextResponse response) {
        
        Plugin[] plugins = this.minecraftServer.getPluginManager().getPlugins();

        Hashtable<String, Object> masterCollection = response.getCollection();
        
        List<Hashtable<String, Object>> listofPlugins = new ArrayList<Hashtable<String, Object>>();
        masterCollection.put("Plugins", listofPlugins);
        
        for (int i = 0; i < plugins.length; i++) {
            PluginDescriptionFile pluginDescriptor =  plugins[i].getDescription();

            Hashtable<String, Object> collection = new Hashtable<String, Object>();
            collection.put("name", plugins[i].getName());
            collection.put("description", pluginDescriptor.getDescription().replaceAll("(\r\n|\n)", ""));
            collection.put("author", pluginDescriptor.getAuthors().toString());
            collection.put("version", pluginDescriptor.getVersion());
            
            listofPlugins.add(collection);
        }
        //END OF for (int i = 0; i < plugins.length; i++) {...
        
        return true;
    }
    
    /**
     * Provides a list of the players that are currently online and the maximum number of players allowed.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    protected boolean who(TextResponse response) {
        
        Player[] players = this.minecraftServer.getOnlinePlayers();
        
        Hashtable<String, Object> masterCollection = response.getCollection();
        List<Hashtable<String, Object>> listofPlayers = new ArrayList<Hashtable<String, Object>>();
        
        masterCollection.put("MaxPlayers", this.minecraftServer.getMaxPlayers());
        masterCollection.put("Players", listofPlayers);
        
        for (int i = 0; i < players.length; i++) {
            
            //Determine the last time the player was online.
            String timePlayed = "Never";
            long timeSpan = new Date().getTime() - players[i].getLastPlayed();
            timePlayed = MessageFormat.format("{0}d {1}h {2}m {3}s",
                        (timeSpan / 3600000L / 24),
                        (timeSpan / 3600000L % 60),
                        (timeSpan / 60000L % 60),
                        (timeSpan / 1000L % 60));
            
            //Get the player's name.
            String playerName = players[i].getName();
            
            //Determine the environment the player is in.
            String environment = players[i].getWorld().getEnvironment().toString();

            Hashtable<String, Object> collection = new Hashtable<String, Object>();
            
            collection.put("name", playerName);
            collection.put("onlineTime", timePlayed);
            collection.put("environment", environment);
            collection.put("isOperator", players[i].isOp());
            
            listofPlayers.add(collection);
        }

        return true;
    }
    
    /**
     * Provides a list of all white-listed players.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    protected boolean whitelist(TextResponse response) {
    	
        Set<OfflinePlayer> whiteListedPlayers = this.minecraftServer.getWhitelistedPlayers();
        
        Hashtable<String, Object> masterCollection = response.getCollection();
        List<Hashtable<String, Object>> listofWhitelistedPlayers = new ArrayList<Hashtable<String, Object>>();
        masterCollection.put("Whitelist", listofWhitelistedPlayers);
        
        for(OfflinePlayer offlinePlayer : whiteListedPlayers) {
        
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
            //END OF if(offlinePlayer.isOnline()) {...
            
            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            
            properties.put("name", offlinePlayer.getName());
            properties.put("isOnline",  offlinePlayer.isOnline());
            properties.put("lastPlayed",  lastPlayed);
            properties.put("isOperator", offlinePlayer.isOp());
            
            listofWhitelistedPlayers.add(properties);
        }
        //END OF for(OfflinePlayer offlinePlayer : whiteListedPlayers) {...

        return true;
    }
    
    /**
     * Provides a list of all offline players
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    protected boolean offlineplayers(TextResponse response) {
    	
        OfflinePlayer[] offlinePlayers = this.minecraftServer.getOfflinePlayers();
        
        Hashtable<String, Object> masterCollection = response.getCollection();
        List<Hashtable<String, Object>> listOfOfflinePlayers = new ArrayList<Hashtable<String, Object>>();
        masterCollection.put("OfflinePlayers", listOfOfflinePlayers);
        
        for(OfflinePlayer offlinePlayer : offlinePlayers) {
        
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
            //END OF if(offlinePlayer.isOnline()) {...
            
            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            
            properties.put("name", offlinePlayer.getName());
            properties.put("isOnline", offlinePlayer.isOnline());
            properties.put("lastPlayed", lastPlayed);
            properties.put("isOperator", offlinePlayer.isOp());
            properties.put("isWhitelisted", offlinePlayer.isWhitelisted());
            properties.put("hasPlayed", offlinePlayer.hasPlayedBefore());
            
            listOfOfflinePlayers.add(properties);
        }
        //END OF for(OfflinePlayer offlinePlayer : offlinePlayers) {...
        
        return true;
    }

    /**
     * The simplest and most light-weight service that simply responds with alive and the server time.
     * Currently the browsers do not support the websocket ping operation so this is the current substitute.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    protected boolean ping(TextResponse response) {
        
        //Get the normal world and assume it is the first in the list.
        List<World> worlds = this.minecraftServer.getWorlds();
        World world = worlds.get(0);

        Hashtable<String, Object> collection = response.getCollection();
        
        collection.put("pong", "alive");
        collection.put("serverTime", world.getTime());

        return true;
    }

    /**
     * Performs a fragmentation test for a text response.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    protected boolean fragmentationTest(TextResponse response) {

        Hashtable<String, Object> collection = response.getCollection();
        
        StringBuilder fragmentationData = new StringBuilder();
        
        //responseBuffer.append("\"Response\": \"");

        for(long i = 0; i < 70000; i++) {
            fragmentationData.append(String.valueOf(i % 10));
        }

        fragmentationData.append("--end\"");
        
        collection.put("Response", fragmentationData.toString());

        return true;
    }

    /**
     * Performs a fragmentation test for a binary response.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */    
    protected boolean binaryFragmentationTest(byte[] data, BinaryResponse response) {
        if (data.length > 1) {
            for (int i = 1; i < 4; i++) {
                byte[] newData = new byte[data.length - 1];
                for(int j = 0; j < data.length - 1; j++) {
                    newData[j] = (byte)((int)data[j+1] * i);
                }
                
                response.enqueue(newData);
            }
        }
        
        return true;
    }
}
