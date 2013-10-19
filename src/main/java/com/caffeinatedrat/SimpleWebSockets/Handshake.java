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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.caffeinatedrat.SimpleWebSockets.Exceptions.EndOfStreamException;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;
import com.caffeinatedrat.SimpleWebSockets.Util.WebSocketsReader;

import net.iharder.Base64;

/**
 * Handles the websockets handshaking mechanism.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class Handshake {

    // ----------------------------------------------
    //  Constants
    // ----------------------------------------------

    private static final String ENCODING_TYPE = "UTF-8";
    private static final String HASHING_ALGORITHM = "SHA-1";
    
    //Supported WebSocket versions delimited by a comma.
    private static final String WEBSOCKET_SUPPORTED_VERSIONS = "13";
    private static final String WEBSOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    
    private static final String ORIGIN_HEADER = "ORIGIN";
    //private static final String HOST_HEADER = "HOST";
    private static final String SEC_WEBSOCKET_KEY_HEADER = "SEC-WEBSOCKET-KEY";
    //private static final String SEC_WEBSOCKET_VERSION_HEADER = "SEC-WEBSOCKET-VERSION";
    
    // ----------------------------------------------
    //  Member Vars (fields)
    // ----------------------------------------------

    private Map<String,String> headerFields = new HashMap<String,String>();
    private Socket socket;
    private int timeoutInMilliseconds;
    private boolean checkOrigin;
    private HashSet<String> whitelist;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
  
    /**
     * Returns an array of supported versions.
     * @return An array of supported versions.
     */
    public static String[] getSupportedVersions() {
        return WEBSOCKET_SUPPORTED_VERSIONS.split(",");
    }
    
    /**
     * Returns all of headers found in the handshake.
     * @return A map that contains the header and its value.
     */
    public Map<String, String> getHeaders() {
        return headerFields;
    }
    
    /**
     * Sets the timeout value for the handshake.
     * @param timeout The time in milliseconds before the handshake fails.
     */
    public void setTimeOut(int timeout) {
        this.timeoutInMilliseconds = timeout; 
    }

    /**
     * Gets the timeout value for the handshake.
     * @return timeout The time in milliseconds before the handshake fails.
     */
    public int getTimeOut() {
        return this.timeoutInMilliseconds;
    }

    /**
     * Gets check origin flag.
     * @return The check origin flag.
     */
    public boolean getCheckOrigin() {
        return this.checkOrigin;
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public Handshake(Socket socket) {
        this(socket, 1000, true, null);
    }
    
    public Handshake(Socket socket, int timeout, boolean checkOrigin, HashSet<String> whitelist) {
        this.socket = socket;
        this.timeoutInMilliseconds = timeout;
        this.checkOrigin = checkOrigin;
        this.whitelist = whitelist;
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Performs the actual handshake and will return false if the handshake has failed.
     * @return Returns true if the handshake was successful. 
     */
    //Suppress this warning as closing the stream after the event is completed will also close the socket.
    @SuppressWarnings("resource")
    public boolean processRequest() {
        
        WebSocketsReader inputStream = null;
        PrintWriter outputStream = null;
        int preserveOriginalTimeout = 0;
        
        try {
            
            String remoteAddress = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
            
            Logger.debug(MessageFormat.format("Handshaking from {0}...", remoteAddress));
            
            //Set the timeout for the handshake.
            preserveOriginalTimeout = this.socket.getSoTimeout();
            this.socket.setSoTimeout(this.timeoutInMilliseconds);
            
            inputStream = new WebSocketsReader(socket.getInputStream());
            outputStream = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), ENCODING_TYPE), true);
            
            //Buffer the HTTP handshake request.
            byte[] buffer = new byte[Globals.READ_CHUNK_SIZE];
            int len = 0;
            StringBuilder stringBuffer = new StringBuilder();
            
            while ( (stringBuffer.lastIndexOf("\r\n\r\n") == -1) && ( (len = inputStream.read(buffer, 0, Globals.READ_CHUNK_SIZE)) > 0 ) ) {
                stringBuffer.append(new String(buffer, 0, len));
            }
            
            if (len == -1) {
                throw new EndOfStreamException();
            }
                            
            Logger.verboseDebug(MessageFormat.format("------Buffered Request------\r\n{0}", stringBuffer.toString()));
            
            //Break up the HTTP request by newline constants.
            String[] headerLines = stringBuffer.toString().split("\r\n");
            if (headerLines.length > 0) {
                
                //Verify that the HTTP verb and version are correct.
                if ( (headerLines[0] !="") && (headerLines[0].toUpperCase().startsWith("GET")) && (headerLines[0].toUpperCase().contains("HTTP/1.1")) ) {
                
                    //Collect all of the header fields sent during the initial handshake request.
                    headerFields = new HashMap<String,String>();
                    for (int i = 1; i < headerLines.length; i++) {

                        // -- CR (9-16-12) --- Fixed an issue where a colon may be present in the value.
                        //Break the header into a key and value pair and ignore malformed headers.
                        String[] tokens = headerLines[i].split(":", 2);
                        if (tokens.length == 2) {
                            headerFields.put(tokens[0].trim().toUpperCase(), tokens[1].trim());
                        }
                    }
                    
                    //Origin check: http://tools.ietf.org/html/rfc6455#section-4.2.2
                    //NOTE: Verify that if the origin is being checked that it is valid and not null.
                    boolean verified = false;
                    if ( !getCheckOrigin() ) {
                        verified = true;
                    }
                    else {
                        
                        Logger.verboseDebug(MessageFormat.format("ORIGIN: {0}", headerFields.get(ORIGIN_HEADER)));
                        
                        //Verify that the origin is valid.
                        if ( (headerFields.containsKey(ORIGIN_HEADER) && (!headerFields.get(ORIGIN_HEADER).equalsIgnoreCase("null"))) ) {
                            
                            //Are we using a white-list?  If so, verify the origin is in the white-list.
                            if(this.whitelist != null) {
                                
                                Logger.verboseDebug("White-list: enabled");

                                //Strip out the protocol before checking.
                                String[] tokens = headerFields.get(ORIGIN_HEADER).split("//", 2);
                                String origin = ((tokens.length == 2) ? tokens[1] : tokens[0]).toUpperCase();
                                if (this.whitelist.contains(origin)) {
                                    verified = true;
                                }
                                else {
                                    Logger.debug(MessageFormat.format("The origin {0} is not white-listed.", headerFields.get(ORIGIN_HEADER)));
                                }
                            }
                            else {
                                Logger.verboseDebug("White-list: disabled");
                                verified = true;
                            }
                        }
                        //END OF if ( (headerFields.containsKey(ORIGIN_HEADER) && (!headerFields.get(ORIGIN_HEADER).equalsIgnoreCase("null"))) ) {...
                    }
                    //END OF if ( !getCheckOrigin() ) {...
                    
                    if (verified) {
                        
                        //TODO
                        //Add Version negotiation: http://tools.ietf.org/html/rfc6455#section-4.4
                        
                        //Generate the accept key.
                        String acceptKey = "";
                        if (headerFields.containsKey(SEC_WEBSOCKET_KEY_HEADER)) {
                            
                            String requestKey = headerFields.get(SEC_WEBSOCKET_KEY_HEADER) + WEBSOCKET_GUID;
                            
                            try {
                                MessageDigest md = MessageDigest.getInstance(HASHING_ALGORITHM);
                                byte[] keyArray = requestKey.getBytes(ENCODING_TYPE);
                                byte[] hashedKey = md.digest(keyArray);
                                acceptKey = Base64.encodeBytes(hashedKey);
                            }
                            catch (NoSuchAlgorithmException noAlgorithmException) {
                                outputStream.print("HTTP/1.1 500 Internal Server Error\r\n");
                                Logger.severe(MessageFormat.format("Unable to find the hashing algorithm {0}.  The accept key cannot be created.", HASHING_ALGORITHM));
                                return false;
                            }
                            catch (java.lang.NoClassDefFoundError noClassException) {
                                outputStream.print("HTTP/1.1 500 Internal Server Error\r\n");
                                Logger.severe(MessageFormat.format("Unable to find the vital class {0} to generate a Base64 value.", noClassException.getMessage()));
                                return false;
                            }
                            

                            //CR (10/26/12) --- There was an issue here with the println method, where on linux boxes the newline is simply a linefeed(\n), while on windows machines the newline would be a Carriage Return-Line Feed (\r\n).
                            // We need to force the newline to be a Carriage Return-Line Feed (\r\n) to meet HTTP specification.
                            //Respond to the client with the proper handshake.
                            outputStream.print("HTTP/1.1 101 Switching Protocols\r\n");
                            outputStream.print("Upgrade: websocket\r\n");
                            outputStream.print("Connection: Upgrade\r\n");
                            outputStream.print(MessageFormat.format("Sec-WebSocket-Version: {0}\r\n", WEBSOCKET_SUPPORTED_VERSIONS));
                            outputStream.print(MessageFormat.format("Sec-WebSocket-Accept: {0}\r\n", acceptKey));
                            outputStream.print("\r\n");
                            outputStream.flush();
                            
                            return true;
                        }
                        //END OF if(headerFields.containsKey("SEC-WEBSOCKET-KEY"))...
                    }
                    else {
                        outputStream.print("HTTP/1.1 403 Forbidden\r\n");
                        Logger.debug("Unable to verify the origin.");
                        return false;
                    }
                    //END OF if (verified) { ...
                }
                //END OF if( (headerLines[0] !="") && (headerLines[0].toUpperCase().startsWith("GET")) && (headerLines[0].toUpperCase().contains("HTTP/1.1")) )...
            }
            //END OF if(headerLines.length > 0)...

            //If we've reached this point then return a bad request to the client and terminate the handshake.
            outputStream.print("HTTP/1.1 400 Bad Request\r\n");
            outputStream.print("Upgrade: websocket\r\n");
            outputStream.print("Connection: Upgrade\r\n");
            outputStream.print(MessageFormat.format("Sec-WebSocket-Version: {0}\r\n", WEBSOCKET_SUPPORTED_VERSIONS));
            outputStream.print("\r\n");
            outputStream.flush();
        }
        //If any one of these exceptions occur then an invalid handshake has been sent and we will ignore the request.
        catch (EndOfStreamException eofsException) {
            Logger.verboseDebug("Invalid handshake.  The client has unexpectedly disconnected.");
        }
        catch (SocketTimeoutException socketTimeout) {
            Logger.verboseDebug(MessageFormat.format("A socket timeout has occured. {0}", socketTimeout.getMessage()));
        }
        catch (IOException ioException) {
            Logger.verboseDebug(MessageFormat.format("Unable to read or write to the streams. {0}", ioException.getMessage()));
        }
        finally {
            try {
                //Reset the original timeout.
                this.socket.setSoTimeout(preserveOriginalTimeout);
            }
            catch(IOException ie) {
                //Do nothing...
            }
        }
        
        return false;
    }
}
