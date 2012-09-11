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

import org.bukkit.entity.Player;

import com.caffeinatedrat.SimpleWebSockets.BinaryResponse;
import com.caffeinatedrat.SimpleWebSockets.IApplicationLayer;
import com.caffeinatedrat.SimpleWebSockets.TextResponse;

/**
 * The application layer that manages the available web services.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */

public class ApplicationLayer implements IApplicationLayer {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private org.bukkit.Server minecraftServer;
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ApplicationLayer(org.bukkit.Server minecraftServer) {
        this.minecraftServer = minecraftServer;
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    public void onTextFrame(String text, TextResponse response) {
        
        //Default all commands to terminate the connection.
        response.closeConnection = true;
        
        if (text.equalsIgnoreCase("WHO")) {
            
            //TODO: Extract into a json formatter.
            StringBuilder stringBuffer = new StringBuilder();
            Player[] players = this.minecraftServer.getOnlinePlayers();
            stringBuffer.append("{");
            stringBuffer.append(MessageFormat.format("\"MaxPlayers\": \"{0}\",", this.minecraftServer.getMaxPlayers()));
            stringBuffer.append("\"Players\": [");
            
            for (int i = 0; i < players.length; i++) {
                if(i > 0) stringBuffer.append(",");
                stringBuffer.append("{");
                stringBuffer.append(MessageFormat.format("\"name\": \"{0}\", \"onlineTime\": \"{1}s\"", players[i].getName(), players[i].getPlayerTime()));
                stringBuffer.append("}");
            }
            
            stringBuffer.append("]}");
            
            response.data = stringBuffer.toString();
        }
        else if (text.equalsIgnoreCase("VERSION")) {
            
            //TODO: Extract into a json formatter.
            StringBuilder stringBuffer = new StringBuilder();
            
            stringBuffer.append("{");
            stringBuffer.append(MessageFormat.format("\"version\": \"{0}\", \"bukkitversion\": \"{1}\"", this.minecraftServer.getVersion(), this.minecraftServer.getBukkitVersion()));
            stringBuffer.append("}");

            response.data = stringBuffer.toString();
        }
        else if (text.equalsIgnoreCase("NAME")) {
            this.minecraftServer.getName();
            
            //TODO: Extract into a json formatter.
            StringBuilder stringBuffer = new StringBuilder();
            
            stringBuffer.append("{");
            stringBuffer.append(MessageFormat.format("\"name\": \"{0}\", \"servername\": \"{1}\"", this.minecraftServer.getName(), this.minecraftServer.getServerName()));
            stringBuffer.append("}");

            response.data = stringBuffer.toString();
        }
        else {
            //Unknown command...do nothing.
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
