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

package com.caffeinatedrat.SimpleWebSockets;

import java.io.*;
import java.net.*;
import java.text.MessageFormat;

import com.caffeinatedrat.SimpleWebSockets.Frame.OPCODE;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;
import com.caffeinatedrat.SimpleWebSockets.Exceptions.*;

/**
 * Handles a single websocket connection with the server.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class Connection extends Thread {
    
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private Socket socket;
    private IApplicationLayer applicationLayer;
    private com.caffeinatedrat.SimpleWebSockets.Server webSocketServer; 
    
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
    
    public Connection(Socket socket, IApplicationLayer applicationLayer, com.caffeinatedrat.SimpleWebSockets.Server webSocketServer) {
        if (applicationLayer == null) {
            throw new IllegalArgumentException("The applicationLayer is invalid (null).");
        }

        if (webSocketServer == null) {
            throw new IllegalArgumentException("The webSocketServer is invalid (null).");
        }
        
        this.socket = socket;
        this.applicationLayer = applicationLayer;
        this.webSocketServer = webSocketServer;
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
        
        try {

            try {

                Handshake handshake = new Handshake(this.socket, getWebSocketServer().getHandshakeTimeout(), getWebSocketServer().isOriginChecked(), getWebSocketServer().getWhiteList());
                
                if (handshake.processRequest()) {
                    
                    Logger.debug("Handshaking successful.");
                    
                    boolean continueListening = true;
                    while ( (!socket.isClosed()) && (continueListening) ) {

                        //TODO: Add management of fragmented frames.
                        //RFC: http://tools.ietf.org/html/rfc6455#section-5.4
                        
                        //Wait for the next frame.
                        Frame frame = new Frame(this.socket, getWebSocketServer().getFrameTimeoutTolerance());
                        frame.Read();
                        
                        switch (frame.getOpCode()) {

                            //RFC: http://tools.ietf.org/html/rfc6455#section-5.6
                            case TEXT_DATA_FRAME:
                            {
                                String text = frame.getPayloadAsString();
                                Logger.debug(text);
                                
                                if (applicationLayer != null) {
                                    
                                    TextResponse response = new TextResponse();
                                    applicationLayer.onTextFrame(text, response);

                                    String fragment = response.data;
                                    Boolean firstFrame = true;

                                    //Fragment the frame if the response is too large.
                                    //RFC: http://tools.ietf.org/html/rfc6455#section-5.4
                                    while (fragment.length() > Frame.MAX_PAYLOAD_SIZE) {

                                        Frame responseFrame = new Frame(this.socket);
                                        responseFrame.setOpCode( firstFrame ? OPCODE.TEXT_DATA_FRAME : OPCODE.CONTINUATION_DATA_FRAME);
                                        responseFrame.setPayload(fragment.substring(0, (int)Frame.MAX_PAYLOAD_SIZE));
                                        responseFrame.Write();
                                        
                                        fragment = fragment.substring((int)Frame.MAX_PAYLOAD_SIZE);
                                        
                                        //No longer the first frame.
                                        firstFrame = false;
                                    }

                                    //Send the final frame.
                                    Frame responseFrame = new Frame(this.socket);
                                    responseFrame.setFinalFragment();
                                    responseFrame.setOpCode( firstFrame ? OPCODE.TEXT_DATA_FRAME : OPCODE.CONTINUATION_DATA_FRAME);
                                    responseFrame.setPayload(fragment);
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
                                
                                if (applicationLayer != null) {
                                    
                                    BinaryResponse response = new BinaryResponse();
                                    applicationLayer.onBinaryFrame(data, response);
                                    
                                    //Keep track of our first frame.
                                    Boolean firstFrame = true;
                                    
                                    //Added basic fragmentation support; however, this puts the burden on the application layer to handle its fragments.
                                    //RFC: http://tools.ietf.org/html/rfc6455#section-5.4
                                    while (!response.isEmpty()) {
                                        
                                        Frame responseFrame = new Frame(this.socket);
                                        
                                        if (firstFrame) {
                                            responseFrame.setOpCode(OPCODE.BINARY_DATA_FRAME);
                                            firstFrame = false;
                                        }
                                        else {
                                            responseFrame.setOpCode(OPCODE.CONTINUATION_DATA_FRAME);
                                        }
                                        //Set the final fragment.
                                        if (response.size() == 1) {
                                            responseFrame.setFinalFragment();
                                        }
                                        
                                        responseFrame.setPayload(response.dequeue());
                                        responseFrame.Write();
                                        
                                    }
                                    //END OF while(!response.isEmpty()) {...
                                    
                                    continueListening = !response.closeConnection;
                                }
                                // END OF if (applicationLayer != null) {...
                            }
                            break;
                            
                            //Terminate the connection.
                            //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.1
                            case CONNECTION_CLOSE_CONTROL_FRAME:
                            {
                                if (applicationLayer != null) {
                                    applicationLayer.onClose();
                                }
                                
                                close();
                                break;
                            }
                                
                            //Respond to the ping.
                            //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.2
                            case PING_CONTROL_FRAME:
                            {
                                //Is pinging supported?
                                if(!getWebSocketServer().isPingable()) {
                                    break;
                                }
                                
                                if (applicationLayer != null) {
                                    applicationLayer.onPing(frame.getPayloadAsBytes());
                                }
                                
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
                                if (applicationLayer != null) {
                                    applicationLayer.onPong();
                                }
                                
                                Logger.verboseDebug("A pong frame was received.");
                            }
                            break;
                            
                            default:
                                break;
                        }
                        //END OF switch(frame.getOpCode())...
                        
                        //Firefox needs to know we're closing or it will throw an error.
                        //Technically according to the RFC we should be sending a close frame before terminating the server anyways.
                        //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.1
                        if (!continueListening) {
                            Frame closeFrame = new Frame(this.socket);
                            closeFrame.setFinalFragment();
                            closeFrame.setOpCode(OPCODE.CONNECTION_CLOSE_CONTROL_FRAME);
                            closeFrame.setPayload("Bye bye...");
                            closeFrame.Write();
                        }
                    }
                    //END OF while(!socket.isClosed())...
                }
                else {
                    Logger.debug("Handshaking failure.");
                    
                    //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.1
                    Frame closeFrame = new Frame(this.socket);
                    closeFrame.setFinalFragment();
                    closeFrame.setOpCode(OPCODE.CONNECTION_CLOSE_CONTROL_FRAME);
                    closeFrame.setPayload("The handshaking has failed.");
                    closeFrame.Write();
                }
                //END OF if(handshake.processRequest())...
            }
            catch (InvalidFrameException invalidFrame) {
                Logger.debug("The frame is invalid.");
                
                try {
                    //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.1
                    Frame closeFrame = new Frame(this.socket);
                    closeFrame.setFinalFragment();
                    closeFrame.setOpCode(OPCODE.CONNECTION_CLOSE_CONTROL_FRAME);
                    closeFrame.setPayload("The frame was invalid.");
                    closeFrame.Write();
                }
                catch(InvalidFrameException ife) {
                    //Do nothing...
                }
            }

            Logger.debug("Connection terminated.");
            Logger.verboseDebug(MessageFormat.format("Thread {0} has spun down...", this.getName()));
        }
        finally {
            close();
        }
    }
    
    /**
     * Close the connection.
     */    
    public void close()
    {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        }
        catch(IOException io) {
            //Do nothing...
        }
    }
}
