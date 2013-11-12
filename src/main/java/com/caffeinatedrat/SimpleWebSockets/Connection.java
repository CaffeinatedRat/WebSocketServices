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

import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import com.caffeinatedrat.SimpleWebSockets.Frames.*;
import com.caffeinatedrat.SimpleWebSockets.Payload.*;
import com.caffeinatedrat.SimpleWebSockets.Responses.*;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;
import com.caffeinatedrat.SimpleWebSockets.Exceptions.*;

/**
 * Handles a single websocket connection with the server.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class Connection extends Thread implements IFrameEvent {
    
    // ----------------------------------------------
    // Statics
    // ----------------------------------------------
    private static AtomicLong counter = new AtomicLong(0L);
    
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private Socket socket;
    private IMasterApplicationLayer masterApplicationLayer;
    private com.caffeinatedrat.SimpleWebSockets.Server webSocketServer;
    private String id;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /*
     * Returns the simple websocket server.
     */
    public com.caffeinatedrat.SimpleWebSockets.Server getWebSocketServer() {
        return this.webSocketServer;
    }
          
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public Connection(Socket socket, IMasterApplicationLayer masterApplicationLayer, com.caffeinatedrat.SimpleWebSockets.Server webSocketServer) {
        if (masterApplicationLayer == null) {
            throw new IllegalArgumentException("The masterApplicationLayer is invalid (null).");
        }

        if (webSocketServer == null) {
            throw new IllegalArgumentException("The webSocketServer is invalid (null).");
        }
        
        this.socket = socket;
        this.masterApplicationLayer = masterApplicationLayer;
        this.webSocketServer = webSocketServer;
        
        //Create the connection id.
        this.id = MessageFormat.format("{0}-{1,number,#}", counter.incrementAndGet(), new Date().getTime());
        
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Begins managing an individual connection.
     */
    @Override
    public void run() {
        
        Logger.verboseDebug(MessageFormat.format("A new thread {0} has spun up...", this.getName()));
        Logger.debug(MessageFormat.format("Connection {0} established.", this.id));
        
        try {
            
            try {
                
                // --- CR (11/11/13) --- For readability...
                Server theServer = getWebSocketServer();
                
                Handshake handshake = new Handshake(this.socket, theServer.getHandshakeTimeout(), theServer.isOriginChecked(), theServer.getWhiteList());
                
                if (handshake.negotiateRequest()) {
                    
                    Logger.debug("Handshaking successful.");
                    
                    //Get the connection idle timeout value.
                    //int idleTimeout = getWebSocketServer().getIdleTimeOut();
                    
                    //Get the start time for our connection.
                    //Date startTime = new Date();
                    
                    FrameReader frame = new FrameReader(this.socket, this, theServer.getFrameTimeoutTolerance(), theServer.getMaximumFragmentationSize()); 
                    
                    // --- CR (8/6/13) --- Get the peer ip address.
                    String remoteAddress = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
                   
                    // --- CR (7/21/13) --- The response wrapper has now become connection data, and has been moved outside the loop.
                    ConnectionData connectionData = new ConnectionData(this.id, remoteAddress);
                    boolean continueListening = true;
                    while ( (!socket.isClosed()) && (continueListening) ) {
                        
                        //TODO: Add management of fragmented frames.
                        //RFC: http://tools.ietf.org/html/rfc6455#section-5.4
                        
                        // --- CR (11/11/13) --- Begin reading one or more frames and block only if there is data available.
                        // If the return value is true then data was available and read.
                        if (frame.read()) {
                            
                            //Determine what type of operation has been requested.
                            //NOTE: Standard browsers currently do not support ping events.
                            switch (frame.getFrameType()) {
                            
                                //RFC: http://tools.ietf.org/html/rfc6455#section-5.6
                                case TEXT_DATA_FRAME: {
                                    
                                    TextPayload textPayload = frame.getTextPayload();
                                    
                                    //Only construct this if logging is set to debug.
                                    if (Logger.isDebug()) {
                                        
                                        Logger.debug(textPayload.toString());
                                    }
                                    
                                    if (masterApplicationLayer != null) {
                                        
                                        masterApplicationLayer.onTextFrame(textPayload, connectionData);
                                        
                                    }
                                    // END OF if(masterApplicationLayer != null)...
                                }
                                break;
                                
                                //RFC: http://tools.ietf.org/html/rfc6455#section-5.6
                                case BINARY_DATA_FRAME: {
                                    
                                    if (masterApplicationLayer != null) {
                                        
                                        masterApplicationLayer.onBinaryFrame(frame.getPayload(), connectionData);
                                        
                                    }
                                    // END OF if (masterApplicationLayer != null) {...
                                }
                                break;
                                
                                //Terminate the connection.
                                //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.1
                                case CONNECTION_CLOSE_CONTROL_FRAME: {
                                    
                                    if (masterApplicationLayer != null) {
                                        
                                        masterApplicationLayer.onClose();
                                        
                                    }
                                    
                                    close();
                                    break;
                                }
                                    
                                //Respond to the ping.
                                //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.2
                                case PING_CONTROL_FRAME: {
                                    
                                    //We already have the logic in this method.  Just reuse it.
                                    //NOTE: Per the RFC, control frames can never be fragmented.
                                    //http://tools.ietf.org/html/rfc6455#section-5.4
                                    onControlFrame(Frame.OPCODE.PING_CONTROL_FRAME, frame.getPayload());
                                    
                                }
                                break;
                                    
                                //What?  Who did we ping?  Ignore this for now.
                                //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.3
                                case PONG_CONTROL_FRAME: {
                                    
                                    //We already have the logic in this method.  Just reuse it.
                                    //NOTE: Per the RFC, control frames can never be fragmented.
                                    //http://tools.ietf.org/html/rfc6455#section-5.4
                                    onControlFrame(Frame.OPCODE.PONG_CONTROL_FRAME, frame.getPayload());
                                    
                                }
                                break;
                                
                                default:
                                    break;
                                    
                            }
                            //END OF switch(frame.getOpCode())...
                            
                        }
                        //There is no new frame available so we'll perform any idle tasks.
                        else {
                            
                            if (masterApplicationLayer != null) {
                                
                                masterApplicationLayer.onIdle(connectionData);
                                
                                try {
                                    Thread.sleep(500);
                                }
                                catch(InterruptedException ie) {
                                    
                                }
                            }
                            //END OF if (applicationLayer != null) {...
                            
                        }
                        //END OF if (frame.read()) {
                        
                        //The session & response must both be valid in order to respond to the client.
                        Session session = connectionData.getSession();
                        if ( (session != null) && ( session.response != null ) ) {
                            
                            //The connectionData needs to be in an active state, otherwise it's ignored.
                            if (connectionData.getState() > 0) {
                                
                                if (session.response instanceof TextResponse){
                                    
                                    writeTextResponseFrame(session.response.toString());
                                    
                                }
                                else if (session.response instanceof BinaryResponse) {
                                    
                                    writeBinaryResponseFrame((BinaryResponse)session.response);
                                    
                                }
                                
                                //Reset the state.
                                connectionData.resetState();
                            }
                            //END OF if (connectionData.getState() > 0) {...
                            
                        }
                        //END OF if ( (session != null) && ( session.response != null ) ) {...

                        //Determine if we need to persist the connection or terminate it.
                        continueListening = !connectionData.isConnectionClosing();
                        
                        if (!continueListening) {
                            
                            //Firefox needs to know we're closing or it will throw an error, while Chrome could care less.
                            //Technically according to the RFC we should be sending a close frame before terminating the connection anyways.
                            writeCloseFrame("Bye bye...");
                            
                        }
                        else {
                            
                            //Disable blocking...
                            frame.startNonBlocking();
                            
                        }

                    }
                    //END OF while(!socket.isClosed())...
                }
                else {
                    
                    Logger.debug("Handshaking failure.");
                    writeCloseFrame("The handshaking has failed.");
                    
                }
                //END OF if(handshake.processRequest())...
            }
            catch (InvalidFrameException invalidFrame) {
                
                Logger.debug(MessageFormat.format("The frame is invalid for the following reasons: {0}", invalidFrame.getMessage()));
                
                try {
                    
                    writeCloseFrame("The frame was invalid.");
                }
                catch(InvalidFrameException ife) {
                    //Do nothing...
                }
                
            }

            Logger.debug(MessageFormat.format("Connection {0} terminated.", this.id));
            Logger.verboseDebug(MessageFormat.format("Thread {0} has spun down...", this.getName()));
        }
        finally {
            close();
        }
    }
    
    /**
     * Close the connection.
     */
    public void close() {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        }
        catch(IOException io) {
            //Do nothing...
        }
    }
    
    
    // ----------------------------------------------
    // Frame specific handling.
    // ----------------------------------------------
    
    /**
     * Close the connection.
     * @param message The message to include in the close frame as to why the frame is closing.
     * @throws InvalidFrameException occurs when the frame is invalid due to an incomplete frame being sent by the client.
     */
    private void writeCloseFrame(String message) throws InvalidFrameException {
        
        if (this.socket != null) {
        
            //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.1
            Frame closeFrame = new Frame(this.socket);
            closeFrame.setFinalFragment();
            closeFrame.setOpCode(Frame.OPCODE.CONNECTION_CLOSE_CONTROL_FRAME);
            closeFrame.setPayload(message);
            closeFrame.write();
            
        }
        
    }
    
    /**
     * Writes a binary response frame to the client.
     * @param response The binary response to write to the client.
     * @throws InvalidFrameException occurs when the frame is invalid due to an incomplete frame being sent by the client.
     */
    private void writeBinaryResponseFrame(BinaryResponse response) throws InvalidFrameException {

        //Keep track of our first frame.
        Boolean firstFrame = true;
        
        //Added basic fragmentation support; however, this puts the burden on the application layer to handle its fragments.
        //RFC: http://tools.ietf.org/html/rfc6455#section-5.4
        while (!response.isEmpty()) {
            
            Frame responseFrame = new Frame(this.socket);
            
            if (firstFrame) {
                
                responseFrame.setOpCode(Frame.OPCODE.BINARY_DATA_FRAME);
                firstFrame = false;
                
            }
            else {
                
                responseFrame.setOpCode(Frame.OPCODE.CONTINUATION_DATA_FRAME);
                
            }
            //Set the final fragment.
            if (response.size() == 1) {
                
                responseFrame.setFinalFragment();
                
            }
            
            responseFrame.setPayload(response.dequeue());
            responseFrame.write();
            
        }
        //END OF while(!response.isEmpty()) {...

    }
    
    /**
     * Writes a text response frame to the client.
     * @param fragment The string value to return the client.
     * @throws InvalidFrameException occurs when the frame is invalid due to an incomplete frame being sent by the client.
     */
    private void writeTextResponseFrame(String fragment) throws InvalidFrameException {
    
        // --- CR (8/10/13) --- Do not write an empty fragment.
        if (fragment.length() == 0) {
            return;
        }
        
        Boolean firstFrame = true;

        //Fragment the frame if the response is too large.
        //RFC: http://tools.ietf.org/html/rfc6455#section-5.4
        while (fragment.length() > Frame.MAX_PAYLOAD_SIZE) {

            Frame responseFrame = new Frame(this.socket);
            responseFrame.setOpCode( firstFrame ? Frame.OPCODE.TEXT_DATA_FRAME : Frame.OPCODE.CONTINUATION_DATA_FRAME);
            responseFrame.setPayload(fragment.substring(0, (int)Frame.MAX_PAYLOAD_SIZE));
            responseFrame.write();
            
            fragment = fragment.substring((int)Frame.MAX_PAYLOAD_SIZE);
            
            //No longer the first frame.
            firstFrame = false;
        }

        //Send the final frame.
        Frame responseFrame = new Frame(this.socket);
        responseFrame.setFinalFragment();
        responseFrame.setOpCode( firstFrame ? Frame.OPCODE.TEXT_DATA_FRAME : Frame.OPCODE.CONTINUATION_DATA_FRAME);
        responseFrame.setPayload(fragment);
        responseFrame.write();
        
    }
    
    // ----------------------------------------------
    // Events
    // ----------------------------------------------
    
    public void onControlFrame(Frame.OPCODE opcode, Payload payload) throws InvalidFrameException  {
        
        switch(opcode) {
        
            case PING_CONTROL_FRAME: {
                
                //Is pinging supported?
                if(!getWebSocketServer().isPingable()) {
                    
                    break;
                    
                }
                
                if (masterApplicationLayer != null) {
                    
                    masterApplicationLayer.onPing(payload);
                    
                }
                
                Frame responseFrame = new Frame(this.socket);
                responseFrame.setFinalFragment();
                responseFrame.setOpCode(Frame.OPCODE.PONG_CONTROL_FRAME);
                
                //Send any body data per the spec.
                responseFrame.setPayload(payload.get(0));
                responseFrame.write();
            }
            break;
                
            case PONG_CONTROL_FRAME: {
                
                if (masterApplicationLayer != null) {
                    
                    masterApplicationLayer.onPong();
                    
                }
                
                Logger.verboseDebug("A pong frame was received.");
            }
            break;
            
        }
        //END OF switch(opcode) {...
        
    }

}
