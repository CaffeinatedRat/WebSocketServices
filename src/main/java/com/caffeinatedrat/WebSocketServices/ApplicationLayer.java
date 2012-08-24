package com.caffeinatedrat.WebSocketServices;

import java.text.MessageFormat;

import org.bukkit.entity.Player;

import com.caffeinatedrat.SimpleWebSockets.BinaryResponse;
import com.caffeinatedrat.SimpleWebSockets.IApplicationLayer;
import com.caffeinatedrat.SimpleWebSockets.TextResponse;

public class ApplicationLayer implements IApplicationLayer {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private org.bukkit.Server minecraftServer;
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ApplicationLayer(org.bukkit.Server minecraftServer)
    {
        this.minecraftServer = minecraftServer;
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    public void onTextFrame(String text, TextResponse response) {
        if(text.equalsIgnoreCase("WHO"))
        {
            //To-do: Extract into a json formatter.
            StringBuilder stringBuffer = new StringBuilder();
            Player[] players = this.minecraftServer.getOnlinePlayers();
            stringBuffer.append("{");
            stringBuffer.append(MessageFormat.format("\"MaxPlayers\": \"{0}\",", this.minecraftServer.getMaxPlayers()));
            stringBuffer.append("\"Players\": [");
            
            for(int i = 0; i < players.length; i++)
            {
                if(i > 0) stringBuffer.append(",");
                stringBuffer.append("{");
                stringBuffer.append(MessageFormat.format("\"name\": \"{0}\", \"onlineTime\": \"{1}s\"", players[i].getName(), players[i].getPlayerTime()));
                stringBuffer.append("}");
            }
            
            stringBuffer.append("]}");
            
            response.data = stringBuffer.toString();
            response.closeConnection = true;
        }
        else if(text.equalsIgnoreCase("VERSION")){
            
            //To-do: Extract into a json formatter.
            StringBuilder stringBuffer = new StringBuilder();
            
            stringBuffer.append("{");
            stringBuffer.append(MessageFormat.format("\"version\": \"{0}\", \"bukkitversion\": \"{1}\"", this.minecraftServer.getVersion(), this.minecraftServer.getBukkitVersion()));
            stringBuffer.append("}");

            response.data = stringBuffer.toString();
            response.closeConnection = true;
        }
        else if(text.equalsIgnoreCase("NAME")){
            this.minecraftServer.getName();
            
            //To-do: Extract into a json formatter.
            StringBuilder stringBuffer = new StringBuilder();
            
            stringBuffer.append("{");
            stringBuffer.append(MessageFormat.format("\"name\": \"{0}\", \"servername\": \"{1}\"", this.minecraftServer.getName(), this.minecraftServer.getServerName()));
            stringBuffer.append("}");

            response.data = stringBuffer.toString();
            response.closeConnection = true;
        }
        else
        {
            //Unknown command, close the connection.
            response.closeConnection = true;
        }
    }

    public void onBinaryFrame(byte[] data, BinaryResponse response) {
        // TODO Auto-generated method stub

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

}