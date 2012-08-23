/**
 * 
 */
package com.caffeinatedrat.SimpleWebSockets;

import java.net.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.LinkedList;

import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

/**
 * @author CaffeinatedRat
 *
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
    private int frameWaitTimeOutInMilliseconds; 
    
    private IApplicationLayer applicationLayer;
    
    //Keep track of all threads.
    private LinkedList<Connection> threads = null;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------    
    
    /**
     * Returns the port the server is listening on.
     * @return The port the server is listening on.
     */
    public int getPort()
    {
        return this.port;
    }
    
    /**
     * Returns the maximum number of threads the server will support concurrently.
     * @return The maximum number of threads the server will support concurrently.
     */
    public int getMaximumThreads()
    {
        return this.maximumThreads;
    }
    
    /*
     * Returns the handshake timeout in milliseconds.
     */
    public int getHandshakeTimeout()
    {
        return handshakeTimeOutInMilliseconds;
    }
    
    /**
     * Sets the handshake timeout in milliseconds.
     * @param timeout The timeout handshake in milliseconds.
     */
    public void setHandshakeTimeout(int timeout)
    {
        this.handshakeTimeOutInMilliseconds = timeout;
    }
    
    /**
     * Returns the amount of time in milliseconds that a connection will wait for a frame.
     * @return The frame timeout in milliseconds.
     */
    public int getFrameWaitTimeout()
    {
        return this.frameWaitTimeOutInMilliseconds;
    }
    
    /**
     * Sets the amount of time in milliseconds that a connection will wait for a frame.
     * @param timeout The frame timeout in milliseconds.
     */
    public void setFrameWaitTimeOut(int timeout)
    {
        this.frameWaitTimeOutInMilliseconds = timeout;
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------

    public Server(int port, IApplicationLayer applicationLayer)
    {
        this(port, applicationLayer, 32);
    }
    
    public Server(int port, IApplicationLayer applicationLayer, int maximumThreads)
    {
        if(applicationLayer == null)
            throw new IllegalArgumentException("The applicationLayer is invalid (null).");
        
        this.isServerRunning = true;
        this.threads = new LinkedList<Connection>();
        
        //Properties
        this.port = port;
        this.applicationLayer = applicationLayer;
        this.maximumThreads = maximumThreads;
        this.handshakeTimeOutInMilliseconds = 1000;
        this.frameWaitTimeOutInMilliseconds = 3000;
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Begin running the websocket server.
     */
    @Override
    public void run()
    {
        try
        {
            Logger.info(MessageFormat.format("WebSocket server listening on port {0}...", this.port));
            
            serverSocket = new ServerSocket(this.port);
            
            //Waiting for the server socket to close or until the server is shutdown manually.
            while ( (this.isServerRunning) && (!serverSocket.isClosed()) )
            {
                //Wait for incoming connections.
                Socket socket = serverSocket.accept();
                
                //Try to reclaim any threads if we are exceeding our maximum.
                //TimeComplexity: O(n) -- Where n is the number of threads valid or invalid.
                //NOTE: Minimal unit testing has been done here...more testing is required.
                if(threads.size() + 1 > this.maximumThreads)
                    for(int i = 0; i < threads.size(); i ++)
                        if(!threads.get(i).isAlive())
                            threads.remove(i);

                //Make sure we have enough threads before accepting.
                //NOTE: Minimal unit testing has been done here...more testing is required.
                if(threads.size() + 1 <= this.maximumThreads)
                {
                    Connection t = new Connection(socket, this.applicationLayer);
                    t.start();
                    threads.add(t);
                }
                else
                {
                    Logger.debug("The server has reached its thread maximum...");
                }
            }
            //END OF while ( (this.isServerRunning) && (!serverSocket.isClosed()) )...
            
            Logger.info("WebSocket server stopping...");
        }
        catch(IOException ioException)
        {
            Logger.info(MessageFormat.format("The port {0} could not be opened for WebSockets.", this.port));
            Logger.debug(ioException.getMessage());
            
            //Close all threads.
            //TimeComplexity: O(n) -- Where n is the number of threads valid or invalid.
            for(Connection t : threads)
                if(t.isAlive())
                    t.close();
        }
    }
    
    /**
     * Begins shutting down the server.
     */
    public void Shutdown()
    {
        this.isServerRunning = false;
        try { this.serverSocket.close(); } catch(IOException io) { }
    }
}
