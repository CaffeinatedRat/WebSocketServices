/**
* Copyright (c) 2013, Ken Anderson <caffeinatedrat at gmail dot com>
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

/**
 * Mutated the response wrapper into the ConnectionData class.
 * Manages the data for a connection including the session data for the connection.
 * Only one session and extension can be attached to a connection.
 * The purpose behind this is to reduce the complexity of having to manage both server-side & client-side logic for multiple extensions per connection.
 * It makes the server overly complex and will not be implemented at this time.
 * @author CaffeinatedRat
 */
public class ConnectionData {

    // ----------------------------------------------
    //  Member Vars (fields)
    // ----------------------------------------------
    
    private Session session = null;
    private String sessionName = "";
    private final Date startTime = new Date();
    private int state = 0;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Returns the name of the session.
     * @return the name of the session.
     */
    public String getSessionName() {
        
        return this.sessionName;
        
    }    
    
    /**
     * Returns the session associated with this connection.
     * @return the session associated with this connection.
     */
    public Session getSession() {
        
        return this.session;
        
    }

    /**
     * Returns the start time of when the connection was established.
     * @return the start time of when the connection was established.
     */
    public Date getStartTime() {
        
        return this.startTime;
        
    }
    
    /**
     * Returns the state of the connection.
     * @return the state of the connection.
     */
    public int getState() {
        
        return this.state;
        
    }
    
    /**
     * Returns the current connection state.
     * @return the current connection state.
     */
    public void setState() {
        
        this.state = 1;
        
    }
    
    /**
     * Resets the connection state to inactive.
     */
    public void resetState() {
        
        this.state = 0;
        
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    public ConnectionData() {
        
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Creates a single session for this connection.
     * @param sessionName the name of session.
     * @return a newly created session for this connection.
     */
    public Session startSession(String sessionName) {
        
        if (this.session == null) {
            
            this.session = new Session(this.startTime);
            this.sessionName = sessionName;
            
        }
        
        return this.session;
        
    }
}
