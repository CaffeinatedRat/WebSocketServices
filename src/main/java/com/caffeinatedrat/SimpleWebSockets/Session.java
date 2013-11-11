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

package com.caffeinatedrat.SimpleWebSockets;

import java.util.Date;

import com.caffeinatedrat.SimpleWebSockets.Responses.Response;

public class Session {

    // ----------------------------------------------
    //  Member Vars (fields)
    // ----------------------------------------------
    public Object data = null;
    public Response response = null;
    private final ConnectionData connectionData;

    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Returns the start time of when the connection was established.
     * @return the start time of when the connection was established.
     */
    public Date getStartTime() {
        
        return this.connectionData.getStartTime();
        
    }
    
    /**
     * Returns the id of the connection the session is associated with.
     * @return the start time of when the session was established.
     */    
    public String getConnectionId() {
        
        return this.connectionData.getId();
        
    }
    
    /**
     * Returns the IP address of the peer.
     * @return the IP address of the peer.
     */
    public String getIPAddress() {
        
        return this.connectionData.getIPAddress();
        
    }    
    
    /**
     * Closes the connection.
     */
    public void closeConnection() {
        
        this.connectionData.closeConnection();
        this.data = null;
        
    }
    
    /**
     * Keeps the connection open.
     */
    public void persistConnection() {
        
        this.connectionData.persistConnection();
        
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    public Session(ConnectionData connectionData) {
        
        this.connectionData = connectionData;
        
    }
}
