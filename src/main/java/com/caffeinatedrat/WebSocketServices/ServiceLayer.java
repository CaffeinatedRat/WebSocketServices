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

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.caffeinatedrat.SimpleWebSockets.Session;
import com.caffeinatedrat.SimpleWebSockets.Payload.Payload;
import com.caffeinatedrat.SimpleWebSockets.Responses.*;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

public class ServiceLayer {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private org.bukkit.Server minecraftServer;
    private WebSocketServicesConfiguration config;
    private Map<String, Long> loginTimes;
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ServiceLayer (org.bukkit.Server minecraftServer, Map<String, Long> loginTimes, WebSocketServicesConfiguration config) {
        
        this.minecraftServer = minecraftServer;
        this.config = config;
        this.loginTimes = loginTimes;
        
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Determine which text service to execute.
     * Precondition: The name should be lower-case.
     * @param service The name of the requested service.  If the service does not exist then nothing is done.
     * @param arguments Any arguments included with the service.
     * @param session The current session.
     * @return True if the service was successfully executed.
     */
    public boolean executeText(String service, String arguments, Session session) {
        
        if (session == null) {
            
            throw new IllegalArgumentException("The session is invalid (null).");
            
        }
        
        try {

            //Handle special conditions for the testing services.
            if(service.equals("fragmentationtest")) {
                
                return fragmentationtest(arguments, session);
                
            }
            else
            {
                
                session.response = new TextResponse();
                
                // --- CR (7/21/13) --- We're still going to perform a lower-case check here in the event someone attempts to call this method explicitly.
                if (!service.equalsIgnoreCase("executeText") && !service.equalsIgnoreCase("executeBinary")) {
                    
                    Method method = this.getClass().getDeclaredMethod(service, String.class, TextResponse.class);
                    method.setAccessible(true);
                    return (Boolean)method.invoke(this, arguments, session.response);
                    
                }
                
            }
            //END OF if(service.equalsIgnoreCase("fragmentationtest")) {...
        }
        catch(java.lang.NoSuchMethodException ex) {
            
            //If the method is not found then do nothing.
            
        }
        catch(Exception ex) {
            Logger.verboseDebug(ex.getMessage());
        }
        
        return false;
    }
    
    /**
     * Determine which binary service to execute.
     * @param service The name of the requested service.  If the service does not exist then nothing is done.
     * @param session The current session.
     * @return True if the service was successfully executed.
     */    
    public boolean executeBinary(Payload payload, Session session) {

        if (session == null) {
            
            throw new IllegalArgumentException("The session is invalid (null).");
            
        }
        
        byte[] data = payload.get(0);
        
        try {
        
            session.response = new BinaryResponse();
            
            if(data.length > 0) {
                
                byte controlByte = data[0];
                if(controlByte == 0x01) {
                    return binaryfragmentationtest(data, (BinaryResponse)session.response);
                }
                
            }
            
        }
        catch(Exception ex) {
            Logger.verboseDebug(ex.getMessage());
        }

        return false;
    }
    

    
    /**
     * Provides information about the server.
     * @param arguments Any arguments included with the service.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    protected boolean info(String arguments, TextResponse response) {

        //Get the normal world and assume it is the first in the list.
        List<World> worlds = this.minecraftServer.getWorlds();
        World world = worlds.get(0);

        Hashtable<String, Object> collection = response.getCollection();
        
        collection.put("name", this.minecraftServer.getName().replaceAll("(\r\n|\n)", ""));
        collection.put("serverTypeName", this.minecraftServer.getName().replaceAll("(\r\n|\n)", ""));
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
     * @param arguments Any arguments included with the service.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */    
    protected boolean plugins(String arguments, TextResponse response) {
        
        Plugin[] plugins = this.minecraftServer.getPluginManager().getPlugins();

        Hashtable<String, Object> masterCollection = response.getCollection();
        
        List<Hashtable<String, Object>> listofPlugins = new ArrayList<Hashtable<String, Object>>();
        masterCollection.put("Plugins", listofPlugins);
        
        for (int i = 0; i < plugins.length; i++) {
            
            // --- CR (2/6/13) --- Catch and ignore any unexpected errors from the plugin.
            try {
            
                PluginDescriptionFile pluginDescriptor =  plugins[i].getDescription();
                Hashtable<String, Object> collection = new Hashtable<String, Object>();
                collection.put("name", plugins[i].getName());
                
                // --- CR (2/6/13) --- NOTE: The description can be null.
                String description = "none";
                if (pluginDescriptor.getDescription() != null) {

                    description =  pluginDescriptor.getDescription().toString().replaceAll("(\r\n|\n)", "");
                    
                    // --- CR (3/19/13) --- Fixed an issue with double quotes in the plug-ins description.
                    description = description.replace("\"", "\\\"");

                }

                // --- CR (2/6/13) --- NOTE: I'm not sure if the authors can be null but lets check against it and make sure that there is at least one author.
                String author = "unknown";
                if ( ( pluginDescriptor.getAuthors() != null) && (pluginDescriptor.getAuthors().size() > 0 ) ) {

                    author = pluginDescriptor.getAuthors().toString();

                }

                collection.put("description", description);
                collection.put("author", author);
                collection.put("version", pluginDescriptor.getVersion());

                listofPlugins.add(collection);
            }
            catch (Exception ex) {
                
                //Do nothing...
                Logger.verboseDebug(ex.getMessage());
                
            }
        }
        //END OF for (int i = 0; i < plugins.length; i++) {...
        
        return true;
        
    }
    
    /**
     * Provides a list of the players that are currently online and the maximum number of players allowed.
     * @param arguments Any arguments included with the service.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    protected boolean who(String arguments, TextResponse response) {
        
        Player[] players = this.minecraftServer.getOnlinePlayers();
        
        Hashtable<String, Object> masterCollection = response.getCollection();
        List<Hashtable<String, Object>> listofPlayers = new ArrayList<Hashtable<String, Object>>();
        
        masterCollection.put("MaxPlayers", this.minecraftServer.getMaxPlayers());
        masterCollection.put("Players", listofPlayers);
        
        for (int i = 0; i < players.length; i++) {
            
            //Determine the last time the player was online.
            String timePlayed = "Never";
            
            // --- CR (10/9/12) --- Hopefully fixed an issue with the player time when a player has never logged in.
            // NOTE: The last played time will be zero if a player has never logged in; however, the first played time will contain the time value we need.
//            long lastPlayedTime = players[i].getLastPlayed();
//            if (lastPlayedTime == 0L) {
//                
//                lastPlayedTime = players[i].getFirstPlayed();
//                
//            }
            
            //long timeSpan = new Date().getTime() - lastPlayedTime;
            
            //Get the player's name.
            String playerName = players[i].getName();
            
            // --- CR (4/28/13) --- We will now manage the amount of time a player has been online since the server does not do this.
            // NOTE: There may be synchronization issues, but we do not really care as these requests can be 'dirty'.
            long timeSpan = 0L;
            if (this.loginTimes.containsKey(playerName)) {

                timeSpan = new Date().getTime() - this.loginTimes.get(playerName);

            }

            //Make sure the timespan is valid.
            if (timeSpan > 0L) {
                
                //--- CR (4/24/13) [1.1.9] --- Fixed the way the hour was being calculated, by modulating by 24 and not 60.
                timePlayed = MessageFormat.format("{0}d {1}h {2}m {3}s",
                            (timeSpan / 3600000L / 24),
                            (timeSpan / 3600000L % 24),
                            (timeSpan / 60000L % 60),
                            (timeSpan / 1000L % 60));
                
            }
            
            //Determine the environment the player is in.
            String environment = players[i].getWorld().getEnvironment().toString();

            Hashtable<String, Object> collection = new Hashtable<String, Object>();
            
            // --- CR (3/18/13) --- Added the integer timespan property, so that the client can handle custom processing.
            collection.put("name", playerName);
            collection.put("onlineTime", timePlayed);
            collection.put("onlineTimeSpan", timeSpan);
            collection.put("environment", environment);
            collection.put("isOperator", players[i].isOp());
            
            listofPlayers.add(collection);
        }

        return true;
        
    }
    
    /**
     * Provides a list of all white-listed players.
     * @param arguments Any arguments included with the service.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    protected boolean whitelist(String arguments, TextResponse response) {
    	
        Set<OfflinePlayer> whiteListedPlayers = this.minecraftServer.getWhitelistedPlayers();
        
        Hashtable<String, Object> masterCollection = response.getCollection();
        List<Hashtable<String, Object>> listofWhitelistedPlayers = new ArrayList<Hashtable<String, Object>>();
        masterCollection.put("Whitelist", listofWhitelistedPlayers);
        
        for(OfflinePlayer offlinePlayer : whiteListedPlayers) {
        
            // --- CR (3/18/13) --- Preserve the timespan.
            long timeSpan = 0L;
            
            String lastPlayed = "Never";
            if(offlinePlayer.isOnline()) {
                lastPlayed = "Now";
            }
            else {
                if (offlinePlayer.getLastPlayed() > 0) {
                    
                    timeSpan = new Date().getTime() - offlinePlayer.getLastPlayed();
                    
                    // --- CR (4/24/13) [1.1.9] --- Fixed the way the hour was being calculated, by modulating by 24 and not 60.
                    lastPlayed = MessageFormat.format("{0}d {1}h {2}m {3}s",
                                (timeSpan / 3600000L / 24),
                                (timeSpan / 3600000L % 24),
                                (timeSpan / 60000L % 60),
                                (timeSpan / 1000L % 60));
                }
            }
            //END OF if(offlinePlayer.isOnline()) {...
            
            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            
            // --- CR (3/18/13) --- Added the integer timespan property, so that the client can handle custom processing.
            properties.put("name", offlinePlayer.getName());
            properties.put("isOnline",  offlinePlayer.isOnline());
            properties.put("lastPlayed",  lastPlayed);
            properties.put("lastPlayedTimeSpan", timeSpan);
            properties.put("isOperator", offlinePlayer.isOp());
            properties.put("hasPlayed", offlinePlayer.hasPlayedBefore());
            
            listofWhitelistedPlayers.add(properties);
        }
        //END OF for(OfflinePlayer offlinePlayer : whiteListedPlayers) {...

        return true;
        
    }
    
    /**
     * Provides a list of all offline players
     * @param arguments Any arguments included with the service.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    protected boolean offlineplayers(String arguments, TextResponse response) {
    	
        OfflinePlayer[] offlinePlayers = this.minecraftServer.getOfflinePlayers();
        
        Hashtable<String, Object> masterCollection = response.getCollection();
        List<Hashtable<String, Object>> listOfOfflinePlayers = new ArrayList<Hashtable<String, Object>>();
        masterCollection.put("OfflinePlayers", listOfOfflinePlayers);
        
        for(OfflinePlayer offlinePlayer : offlinePlayers) {
        
            long timeSpan = 0L;
            
            String lastPlayed = "Never";
            if(offlinePlayer.isOnline()) {
                lastPlayed = "Now";
            }
            else {
                if (offlinePlayer.getLastPlayed() > 0) {
                    
                    timeSpan = new Date().getTime() - offlinePlayer.getLastPlayed();
                    
                    // --- CR (4/24/13) [1.1.9] --- Fixed the way the hour was being calculated, by modulating by 24 and not 60.
                    lastPlayed = MessageFormat.format("{0}d {1}h {2}m {3}s",
                                (timeSpan / 3600000L / 24),
                                (timeSpan / 3600000L % 24),
                                (timeSpan / 60000L % 60),
                                (timeSpan / 1000L % 60));
                }
            }
            //END OF if(offlinePlayer.isOnline()) {...
            
            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            
            // --- CR (3/18/13) --- Added the integer timespan property, so that the client can handle custom processing.
            properties.put("name", offlinePlayer.getName());
            properties.put("isOnline", offlinePlayer.isOnline());
            properties.put("lastPlayed", lastPlayed);
            properties.put("lastPlayedTimeSpan", timeSpan);
            properties.put("isOperator", offlinePlayer.isOp());
            properties.put("isWhitelisted", offlinePlayer.isWhitelisted());
            properties.put("hasPlayed", offlinePlayer.hasPlayedBefore());
            
            listOfOfflinePlayers.add(properties);
        }
        //END OF for(OfflinePlayer offlinePlayer : offlinePlayers) {...
        
        return true;
    }
    
    /**
     * Queries information about a single player.
     * @param arguments Any arguments included with the service.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    protected boolean player(String arguments, TextResponse response) {
        
        if ( (arguments == null) || (arguments == "") )
        {
            return true;
        }
        
        Hashtable<String, Object> masterCollection = response.getCollection();
        
        // --- CR (6/22/13) --- Fixed how the JSON structure is nested.
        Hashtable<String, Object> locationInfo = new Hashtable<String, Object>();
        masterCollection.put("location", locationInfo);
        
        OfflinePlayer offlinePlayerInfo = this.minecraftServer.getOfflinePlayer(arguments);
        
        if (offlinePlayerInfo != null) {
            
            masterCollection.put("isWhiteListed", offlinePlayerInfo.isWhitelisted());
            masterCollection.put("isBanned", offlinePlayerInfo.isBanned());
            masterCollection.put("isOnline", offlinePlayerInfo.isOnline());
            masterCollection.put("isOperator", offlinePlayerInfo.isOp());
            masterCollection.put("firstPlayed", offlinePlayerInfo.getFirstPlayed());
            masterCollection.put("lastPlayed", offlinePlayerInfo.getLastPlayed());
            masterCollection.put("hasPlayedBefore", offlinePlayerInfo.hasPlayedBefore());
            
            if (offlinePlayerInfo.isOnline()) {
                
                Player onlinePlayerInfo = this.minecraftServer.getPlayer(arguments);
                
                masterCollection.put("level", onlinePlayerInfo.getLevel());
                masterCollection.put("health", onlinePlayerInfo.getHealth());
                masterCollection.put("experience", onlinePlayerInfo.getExp());
                masterCollection.put("isSleeping", onlinePlayerInfo.isSleeping());
                masterCollection.put("isDead", onlinePlayerInfo.isDead());
                
                int blockX = 0, blockY = 0, blockZ = 0;
                if (onlinePlayerInfo.getLocation() != null) {
                    
                    blockX = onlinePlayerInfo.getLocation().getBlockX();
                    blockY = onlinePlayerInfo.getLocation().getBlockY();
                    blockZ = onlinePlayerInfo.getLocation().getBlockZ();
                }
                
                // --- CR (6/22/13) --- Fixed how the JSON structure is nested.
                //masterCollection.put("location.x", blockX);
                //masterCollection.put("location.y", blockY);
                //masterCollection.put("location.z", blockZ);
                locationInfo.put("x", blockX);
                locationInfo.put("y", blockY);
                locationInfo.put("z", blockZ);
                
                String environmentName = "UNKNOWN";
                World world = onlinePlayerInfo.getWorld();
                if (world != null) {
                    
                    Environment environment = world.getEnvironment();
                    if (environment != null) {
                        
                        environmentName = environment.name();
                        
                    }
                    
                }
                
                masterCollection.put("environment", environmentName);
                
                String weatherTypeName = "CLEAR";
                
                // --- CR (6/22/13) --- Check for the enum type for attempting to use it as it will cause a reflection exception.
                try
                {
                    //Check if this enum exists, as it does not in earlier versions.
                    Class.forName("org.bukkit.WeatherType");
                    
                    WeatherType weatherType = onlinePlayerInfo.getPlayerWeather();
                    if ( (weatherType != null) ) {
                
                        weatherTypeName = weatherType.name();
                    }
                }
                catch(ClassNotFoundException ex)
                {
                    //Ignore this exception...it's caused by older versions.
                }
                
                masterCollection.put("weather", weatherTypeName);
                
                long timeSpan = 0L;
                if (this.loginTimes.containsKey(offlinePlayerInfo.getName())) {

                    timeSpan = new Date().getTime() - this.loginTimes.get(arguments);

                }
                
                masterCollection.put("onlineTimeSpan", timeSpan);
                
            }
            else {
                
                //Set the default values for an offline player.  These are required to maintain consistency with the JSON structure.
                masterCollection.put("level", 0);
                masterCollection.put("health", 0);
                masterCollection.put("experience", 0.0f);
                masterCollection.put("isSleeping", false);
                masterCollection.put("isDead", false);
                
                // --- CR (6/22/13) --- Fixed how the JSON structure is nested.
                //masterCollection.put("location.x", 0);
                //masterCollection.put("location.y", 0);
                //masterCollection.put("location.z", 0);
                
                locationInfo.put("x", 0);
                locationInfo.put("y", 0);
                locationInfo.put("z", 0);
                
                masterCollection.put("environment", "UNKNOWN");
                masterCollection.put("weather", "CLEAR");
                masterCollection.put("onlineTimeSpan", 0L);
                
            }
            
        }
        
        return true;
    }    

    /**
     * The simplest and most light-weight service that simply responds with alive and the server time.
     * Currently the browsers do not support the websocket ping operation so this is the current substitute.
     * @param arguments Any arguments included with the service.
     * @param responseBuffer The buffer that the JSON response data will be stored in.
     * @return True if the service was successfully executed.
     */
    protected boolean ping(String arguments, TextResponse response) {
        
        //Get the normal world and assume it is the first in the list.
        List<World> worlds = this.minecraftServer.getWorlds();
        World world = worlds.get(0);

        Hashtable<String, Object> collection = response.getCollection();
        
        String version = this.config.getPlugInfo().getDescription().getVersion();
        
        collection.put("pong", "alive");
        collection.put("serverTime", world.getTime());
        collection.put("wssVersion", version);

        return true;
    }

    /**
     * Performs a fragmentation test for a text response.
     * @param arguments Any arguments included with the service.
     * @param session The current session.
     * @return True if the service was successfully executed.
     */
    protected boolean fragmentationtest(String arguments, Session session) {
        
        if (arguments.equalsIgnoreCase("binary")) {
        
            session.response = new BinaryResponse();
            
            byte[] newData = new byte[65535];
            for (int i = 0; i < 65535; i++) {
                
                newData[i] = (byte) (i % 10);
                
            }
            
            ((BinaryResponse)session.response).enqueue(newData);
            
            //Test fragementation
            newData = new byte[10];
            for (int i = 0; i < 10; i++) {
                
                newData[i] = (byte) (i % 10);
                
            }
            
            ((BinaryResponse)session.response).enqueue(newData);
        }
        //Handle all other conditions as text.
        else {
        
            session.response = new TextResponse();
            
            Hashtable<String, Object> collection = ((TextResponse)session.response).getCollection();
            
            StringBuilder fragmentationData = new StringBuilder();
    
            for(long i = 0; i < 70000; i++) {
                
                fragmentationData.append(String.valueOf(i % 10));
                
            }
    
            fragmentationData.append("--end");
            
            collection.put("Response", fragmentationData.toString());
            
        }

        return true;
    }

    /**
     * Performs a fragmentation test for a binary response.
     * @param response A binary response
     * @return True if the service was successfully executed.
     */    
    protected boolean binaryfragmentationtest(byte[] data, BinaryResponse response) {
        
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
