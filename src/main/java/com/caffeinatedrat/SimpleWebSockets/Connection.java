/**
 * 
 */
package com.caffeinatedrat.SimpleWebSockets;

import java.io.*;
import java.net.*;
import java.text.MessageFormat;

import com.caffeinatedrat.SimpleWebSockets.Frame.OPCODE;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;
import com.caffeinatedrat.SimpleWebSockets.Exceptions.*;

/**
 * @author CaffeinatedRat
 *
 */
public class Connection extends Thread {
    
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private Socket socket;
    private IApplicationLayer applicationLayer;
    private int handshakeTimeOutInMilliseconds;
    private int frameWaitTimeOutInMilliseconds;

    //private org.bukkit.Server minecraftServer;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------    
    
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
    
    public Connection(Socket socket, IApplicationLayer applicationLayer)
    {
        if(applicationLayer == null)
            throw new IllegalArgumentException("The applicationLayer is invalid (null).");
        
        this.socket = socket;
        this.applicationLayer = applicationLayer;
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    @Override
    public void run()
    {
        Logger.verboseDebug(MessageFormat.format("A new thread {0} has spun up...", this.getName()));
        
        try
        {
            try
            {
                Handshake handshake = new Handshake(this.socket, this.handshakeTimeOutInMilliseconds);
                handshake.setCheckOrigin(false);
                if(handshake.processRequest())
                {
                    Logger.debug("Handshaking successful.");
                    
                    boolean continueListening = true;
                    while(!socket.isClosed() && continueListening)
                    {
                        //TO-DO: Add management of fragmented frames.
                        //RFC: http://tools.ietf.org/html/rfc6455#section-5.4
                        
                        //Wait for the next frame.
                        Frame frame = new Frame(this.socket, this.frameWaitTimeOutInMilliseconds);
                        frame.Read();
                        
                        switch(frame.getOpCode())
                        {
                            //RFC: http://tools.ietf.org/html/rfc6455#section-5.6
                            case TEXT_DATA_FRAME:
                            {
                                String text = frame.getPayloadAsString();
                                Logger.debug(text);
                                
                                if(applicationLayer != null)
                                {
                                    //TO-DO: Add support for fragmentation.
                                    //RFC: http://tools.ietf.org/html/rfc6455#section-5.4
                                    Frame responseFrame = new Frame(this.socket);
                                    responseFrame.setFinalFragment();
                                    responseFrame.setOpCode(OPCODE.TEXT_DATA_FRAME);
                                    
                                    TextResponse response = new TextResponse();
                                    applicationLayer.onTextFrame(text, response);
                                    
                                    responseFrame.setPayload(response.data);
                                    responseFrame.Write();
                                    
                                    continueListening = !response.closeConnection;
                                }
                                // END OF if(applicationLayer != null)...
                            }
                            break;
                            
                            //RFC: http://tools.ietf.org/html/rfc6455#section-5.6
                            case BINARY_DATA_FRAME:
                            {
                                byte[] data = frame.getPayloadAsBytes();
                                
                                if(applicationLayer != null)
                                {
                                    //TO-DO: Add support for fragmentation.
                                    //RFC: http://tools.ietf.org/html/rfc6455#section-5.4
                                    Frame responseFrame = new Frame(this.socket);
                                    responseFrame.setFinalFragment();
                                    responseFrame.setOpCode(OPCODE.BINARY_DATA_FRAME);
                                    
                                    BinaryResponse response = new BinaryResponse();
                                    applicationLayer.onBinaryFrame(data, response);
                                    
                                    responseFrame.setPayload(response.data);
                                    responseFrame.Write();
                                    
                                    continueListening = !response.closeConnection;
                                }
                                // END OF if(applicationLayer != null)...
                            }
                            break;
                            
                            //Terminate the connection.
                            //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.1
                            case CONNECTION_CLOSE_CONTROL_FRAME:
                            {
                                if(applicationLayer != null)
                                    applicationLayer.onClose();
                                
                                close();
                                break;
                            }
                                
                            //Respond to the ping.
                            //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.2
                            case PING_CONTROL_FRAME:
                            {
                                if(applicationLayer != null)
                                    applicationLayer.onPing(frame.getPayloadAsBytes());
                                
                                Frame responseFrame = new Frame(this.socket);
                                responseFrame.setFinalFragment();
                                responseFrame.setOpCode(OPCODE.PONG_CONTROL_FRAME);
                                
                                //Send any body data per the spec.
                                responseFrame.setPayload(frame.getPayloadAsBytes());
                                responseFrame.Write();
                            }
                            break;
                                
                            //What?  Who did we ping?  Ignore this for now.
                            //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.3
                            case PONG_CONTROL_FRAME:
                            {
                                if(applicationLayer != null)
                                    applicationLayer.onPong();
                                
                                Logger.verboseDebug("A pong frame was received.");
                            }
                            break;
                        }
                        //END OF switch(frame.getOpCode())...
                    }
                    //END OF while(!socket.isClosed())...
                }
                else
                {
                    Logger.debug("Handshaking failure.");
                }
                //END OF if(handshake.processRequest())...
            }
            catch(InvalidFrameException invalidFrame)
            {
                Logger.debug("The frame is invalid.");
            }

            Logger.debug("Connection terminated.");
            Logger.verboseDebug(MessageFormat.format("Thread {0} has spun down...", this.getName()));
        }
        finally
        {
            close();
        }
    }
    
    public void close()
    {
        try
        {
            if (this.socket != null)
                this.socket.close();
        }
        catch(IOException io) { }
    }
}
