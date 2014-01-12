/**
* Copyright (c) 2012-2014, Ken Anderson <caffeinatedrat at gmail dot com>
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

import java.net.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedList;

import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

/**
 * The websockets main server.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class Server extends Thread {
    
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private ServerSocket serverSocket;
    
    private boolean isServerRunning;
    private int port;
    private int maximumThreads;
    private int handshakeTimeOutInMilliseconds;
    private int frameTimeOutToleranceInMilliseconds;
    private int maximumFragmentationSize;
    private int idleTimeOutInMilliseconds;
    private boolean checkOrigin;
    private boolean pingable;
    private HashSet<String> whitelist = null;
    
    private IMasterApplicationLayer masterApplicationLayer;
    
    //Keep track of all threads.
    private LinkedList<Connection> threads = null;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------    
    
    /**
     * Returns the port the server is listening on.
     * @return The port the server is listening on.
     */
    public int getPort() {
        return this.port;
    }
    
    /**
     * Returns the maximum number of threads the server will support concurrently.
     * @return The maximum number of threads the server will support concurrently.
     */
    public int getMaximumThreads() {
        return this.maximumThreads;
    }
    
    /**
     * Returns the handshake timeout in milliseconds.
     * @return The handshake timeout in milliseconds.
     */
    public int getHandshakeTimeout() {
        return handshakeTimeOutInMilliseconds;
    }
        
    /**
     * Sets the handshake timeout in milliseconds.
     * @param timeout The timeout handshake in milliseconds.
     */
    public void setHandshakeTimeout(int timeout) {
        this.handshakeTimeOutInMilliseconds = timeout;
    }
    
    /**
     * Returns the amount of time in milliseconds that a connection will wait for a frame.
     * @return The frame timeout in milliseconds.
     */
    public int getFrameTimeoutTolerance() {
        return this.frameTimeOutToleranceInMilliseconds;
    }
    
    /**
     * Sets the amount of time in milliseconds that a connection will wait for a frame.
     * @param timeout The frame timeout in milliseconds.
     */
    public void setFrameTimeoutTolerance(int timeout) {
        this.frameTimeOutToleranceInMilliseconds = timeout;
    }
    
    /**
     * Returns the maximum fragmentation size.
     * @return The maximum fragmentation size.
     */
    public int getMaximumFragmentationSize() {
        return this.maximumFragmentationSize;
    }
    
    /**
     * Sets the maximum fragmentation size.
     * @param timeout The maximum fragmentation size.
     */
    public void setMaximumFragmentationSize(int maximumFragmentationSize) {
        this.maximumFragmentationSize = maximumFragmentationSize;
    }
    
    /**
     * Returns the amount of time in milliseconds that a connection will idle before terminating.
     * @return The idle timeout in milliseconds.
     */
    public int getIdleTimeOut() {
        return this.idleTimeOutInMilliseconds;
    }
    
    /**
     * Sets the amount of time in milliseconds that a connection will idle before terminating.
     * @param timeout The idle timeout in milliseconds.
     */
    public void setIdleTimeOut(int timeout) {
        this.idleTimeOutInMilliseconds = timeout;
    }
    
    /**
     * Determines if the origin is checked during the handshake.
     * @return If the origin is checked during handshaking.
     */
    public boolean isOriginChecked() {
        return this.checkOrigin;
    }
    
    /**
     * Enables or disables origin checking.
     * @param checkOrigin Enables or disables origin checking.
     */
    public void setOriginCheck(boolean checkOrigin) {
        this.checkOrigin = checkOrigin;
    }
    
    /**
     * Determines if the server is pingable via websockets.
     * @return If the server is pingable via websockets.
     */
    public boolean isPingable() {
        return this.pingable;
    }
    
    /**
     * Enables or disables the ability to ping the server via websockets.
     * @param isPingable Enables or disables ability to ping the server via websockets.
     */
    public void setPingable(boolean isPingable) {
        this.pingable = isPingable;
    }
    
    /**
     * Determines if the websockets server is publicly available or by white-list only.
     * @return the type of accessibility.
     */
    public boolean isWhiteListed() {
        return (this.whitelist != null);
    }
    
    public HashSet<String> getWhiteList()
    {
        return this.whitelist;
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------

    public Server(int port, IMasterApplicationLayer masterApplicationLayer) {
        this(port, masterApplicationLayer, false, 32);
    }
    
    public Server(int port, IMasterApplicationLayer masterApplicationLayer, boolean isWhiteListed) {
        this(port, masterApplicationLayer, isWhiteListed, 32);
    }    
    
    public Server(int port, IMasterApplicationLayer masterApplicationLayer, boolean isWhiteListed, int maximumThreads) {
        
        if(masterApplicationLayer == null) {
            throw new IllegalArgumentException("The masterApplicationLayer is invalid (null).");
        }
               
        this.isServerRunning = true;
        this.threads = new LinkedList<Connection>();
        
        //Properties
        this.port = port;
        this.masterApplicationLayer = masterApplicationLayer;
        this.maximumThreads = maximumThreads;
        this.handshakeTimeOutInMilliseconds = 1000;
        this.frameTimeOutToleranceInMilliseconds = 3000;
        this.maximumFragmentationSize = 2;
        this.checkOrigin = true;
        this.pingable = true;
        
        if(isWhiteListed)
        {
            this.whitelist = new HashSet<String>();
            if(!loadWhiteList())
            {
                //TODO: Create the white-list.txt and bail.
                Logger.severe("The white-list was not found...");
            }
        }
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Begin running the websocket server.
     */
    @Override
    public void run() {
        
        try {
            
            Logger.info(MessageFormat.format("WebSocket server listening on port {0}...", this.port));
            
            serverSocket = new ServerSocket(this.port);
            
            //Waiting for the server socket to close or until the server is shutdown manually.
            while ( (this.isServerRunning) && (!serverSocket.isClosed()) ) {

                //Wait for incoming connections.
                Socket socket = serverSocket.accept();

                //Try to reclaim any threads if we are exceeding our maximum.
                //TimeComplexity: O(n) -- Where n is the number of threads valid or invalid.
                //NOTE: Minimal unit testing has been done here...more testing is required.
                if (threads.size() + 1 > this.maximumThreads) {
                    for (int i = 0; i < threads.size(); i ++) {
                        if (!threads.get(i).isAlive()) {
                            threads.remove(i);
                        }
                    }
                }

                //Make sure we have enough threads before accepting.
                //NOTE: Minimal unit testing has been done here...more testing is required.
                if ( (threads.size() + 1) <= this.maximumThreads) {
                    
                    Connection t = new Connection(socket, this.masterApplicationLayer, this);
                    t.start();
                    threads.add(t);
                    
                }
                else {
                    Logger.debug("The server has reached its thread maximum...");
                }

            }
            //END OF while ( (this.isServerRunning) && (!serverSocket.isClosed()) )...
            
            Logger.info("WebSocket server stopping...");
            
        }
        catch (IOException ioException) {
            
            // --- CR (8/10/13) --- Only log the event if the server is still running.  This should prevent an error message from appearing when the server is restarted.
            if (this.isServerRunning) {
                
                Logger.info(MessageFormat.format("The port {0} could not be opened for WebSockets.", this.port));
                Logger.debug(ioException.getMessage());
                
            }
            
            //Close all threads.
            //TimeComplexity: O(n) -- Where n is the number of threads valid or invalid.
            for (Connection t : threads) {
                if (t.isAlive()) {
                    t.close();
                }
            }
            
        }
    }
    
    /**
     * Begins shutting down the server.
     */
    public void Shutdown() {
        
        this.isServerRunning = false;
        try {
            this.serverSocket.close();
        }
        catch(IOException io) {
            //Do nothing...
        }
        
    }
    
    /**
     * Attempts to load the white-list.
     */
    private boolean loadWhiteList() {
        
        File whitelistFile = new File(Globals.PLUGIN_FOLDER + "/" + Globals.WHITE_LIST_FILENAME);
        
        if (whitelistFile.exists()) {
            BufferedReader br = null;
            try {
                
                // --- CR (10-14/12) --- Removed the try-with-resources block to support backwards compatibility.
                //try (BufferedReader br = new BufferedReader(new FileReader(whitelistFile)))
                br = new BufferedReader(new FileReader(whitelistFile));
                if(br != null)
                {
                    String domain;
                    while ((domain = br.readLine()) != null) {
                        if (domain != "") {
                            this.whitelist.add(domain.toUpperCase());
                        }
                    }
                }

                br.close();

                return true;
            }
            catch(FileNotFoundException fnfe){}
            catch(IOException io) {}
            finally {
                try {
                    br.close();
                }
                catch (IOException io) {
                    
                }
            }
        }
        else {
            //The white-list was not found so create it.
            try {
                whitelistFile.createNewFile();
            }
            catch(IOException io) {
                Logger.debug(MessageFormat.format("Cannot create \"{0}/{1}\".", Globals.PLUGIN_FOLDER, Globals.WHITE_LIST_FILENAME));
            }
        }
        //END OF if(whitelistFile.exists())...
        
        return false;
        
    }
}
