/**
* Copyright (c) 2012-2015, Ken Anderson <caffeinatedrat at gmail dot com>
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
import java.util.UUID;

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
    private static final String SEC_WEBSOCKET_ACCEPT_HEADER = "SEC-WEBSOCKET-ACCEPT";
    private static final String UPGRADE_HEADER = "UPGRADE";
    private static final String CONNECTION_HEADER = "CONNECTION";
    private static final String SEC_WEBSOCKET_VERSION_HEADER = "SEC-WEBSOCKET-VERSION";
    
    // ----------------------------------------------
    //  Member Vars (fields)
    // ----------------------------------------------

    private Map<String,String> requestHeaderFields = new HashMap<String,String>();
    private Socket socket;
    private int timeoutInMilliseconds;
    private boolean checkOrigin;
    private HashSet<String> whitelist;
    private String rawHandshakeRequest;
    private String securityWebSocketKey = "";
    private String origin = "";
    private String userAgent = "";
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
  
    /**
     * Returns an array of supported versions.
     * @return an array of supported versions.
     */
    public static String[] getSupportedVersions() {
        return WEBSOCKET_SUPPORTED_VERSIONS.split(",");
    }
    
    /**
     * Returns all of request headers found in the handshake.
     * @return a map that contains the header and its value.
     */
    public Map<String, String> getRequestHeaders() {
        return this.requestHeaderFields;
    }
    
    /**
     * Sets the timeout value for the handshake.
     * @param the time in milliseconds before the handshake fails.
     */
    public void setTimeOut(int timeout) {
        this.timeoutInMilliseconds = timeout; 
    }

    /**
     * Gets the timeout value for the handshake.
     * @return the time in milliseconds before the handshake fails.
     */
    public int getTimeOut() {
        return this.timeoutInMilliseconds;
    }

    /**
     * Gets check origin flag.
     * @return the check origin flag.
     */
    public boolean getCheckOrigin() {
        return this.checkOrigin;
    }
    
    /**
     * Returns the raw handshake request that was sent by the client.
     * @return the raw handshake request that was sent by the client.
     */
    public String getRawRequestRequest() {
        return this.rawHandshakeRequest;
    }
    
    /**
     * Returns the security websocket key either generated by us or sent to us via a request.
     * @return the security websocket key either generated by us or sent to us via a request.
     */
    public String getSecurityWebSocketKey() {
        return this.securityWebSocketKey;
    }
    
    /**
     * Sets the origin for a handshake request.
     * @param origin the origin sending the request.
     */
    public void setOrigin(String origin) {
       this.origin = origin;
    }
    
    /**
     * Gets the origin for a handshake request.
     * @return the origin sending the request.
     */
    public String getOrigin() {
       return this.origin;
    }    
    
    /**
     * Sets the user agent field that is sent when making a request to a server end-point.
     * @param the user agent field that is sent when making a request to a server end-point.
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Gets the user agent field that is sent when making a request to a server end-point.
     * @return the user agent field that was sent when a request was made.
     */
    public String getUserAgent() {
        return this.userAgent;
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
        
        //TODO: Generate this key.
        //If we are negotiating a request then the key is provided by the client.
        //That key will be retained for the lifetime of that handshake.
        //If we are negotiating a response then the key is provided by us.
        //this.securityWebSocketKey = "";
    }
    
    //Copy constructor
    public Handshake(Handshake handshake) {
        
        this.socket = handshake.socket;
        this.timeoutInMilliseconds = handshake.timeoutInMilliseconds;
        this.checkOrigin = handshake.checkOrigin;
        this.whitelist = handshake.whitelist;
        this.rawHandshakeRequest = handshake.rawHandshakeRequest;
        this.securityWebSocketKey = handshake.securityWebSocketKey;
        
        this.requestHeaderFields = new HashMap<String, String>(handshake.requestHeaderFields);
        
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Negotiates a handshake request from an endpoint and will return true if successful.
     * @return true if the handshake request was successfully negotiated. 
     */
    //Suppress this warning as closing the stream after the event is completed will also close the socket.
    @SuppressWarnings("resource")
    public boolean negotiateRequest() {
        
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

            this.rawHandshakeRequest = stringBuffer.toString();
            
            Logger.verboseDebug(MessageFormat.format("------Buffered Request------\r\n{0}", this.rawHandshakeRequest));
            
            //Break up the HTTP request by newline constants.
            String[] headerLines = this.rawHandshakeRequest.toString().split("\r\n");
            if (headerLines.length > 0) {
                
                //Verify that the HTTP verb and version are correct.
                if ( (headerLines[0] !="") && (headerLines[0].toUpperCase().startsWith("GET")) && (headerLines[0].toUpperCase().contains("HTTP/1.1")) ) {
                
                    //Collect all of the header fields sent during the initial handshake request.
                    this.requestHeaderFields = new HashMap<String,String>();
                    for (int i = 1; i < headerLines.length; i++) {

                        // -- CR (9-16-12) --- Fixed an issue where a colon may be present in the value.
                        //Break the header into a key and value pair and ignore malformed headers.
                        String[] tokens = headerLines[i].split(":", 2);
                        if (tokens.length == 2) {
                            this.requestHeaderFields.put(tokens[0].trim().toUpperCase(), tokens[1].trim());
                        }
                    }
                    
                    //Origin check: http://tools.ietf.org/html/rfc6455#section-4.2.2
                    //NOTE: Verify that if the origin is being checked that it is valid and not null.
                    boolean verified = false;
                    if ( !getCheckOrigin() ) {
                        verified = true;
                    }
                    else {
                        
                        Logger.verboseDebug(MessageFormat.format("ORIGIN: {0}", this.requestHeaderFields.get(ORIGIN_HEADER)));
                        
                        //Verify that the origin is valid.
                        if ( (this.requestHeaderFields.containsKey(ORIGIN_HEADER) && (!this.requestHeaderFields.get(ORIGIN_HEADER).equalsIgnoreCase("null"))) ) {
                            
                            //Are we using a white-list?  If so, verify the origin is in the white-list.
                            if(this.whitelist != null) {
                                
                                Logger.verboseDebug("White-list: enabled");

                                //Strip out the protocol before checking.
                                String[] tokens = this.requestHeaderFields.get(ORIGIN_HEADER).split("//", 2);
                                String origin = ((tokens.length == 2) ? tokens[1] : tokens[0]).toUpperCase();
                                if (this.whitelist.contains(origin)) {
                                    verified = true;
                                }
                                else {
                                    Logger.debug(MessageFormat.format("The origin {0} is not white-listed.", this.requestHeaderFields.get(ORIGIN_HEADER)));
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
                        
                        //TODO Version Negotiation: http://tools.ietf.org/html/rfc6455#section-4.4
                        
                        //Generate the accept key.
                        String acceptKey = "";
                        if (this.requestHeaderFields.containsKey(SEC_WEBSOCKET_KEY_HEADER)) {
                            
                            this.securityWebSocketKey = this.requestHeaderFields.get(SEC_WEBSOCKET_KEY_HEADER);
                            String securityKey = this.securityWebSocketKey + WEBSOCKET_GUID;
                            
                            try {
                                MessageDigest md = MessageDigest.getInstance(HASHING_ALGORITHM);
                                byte[] keyArray = securityKey.getBytes(ENCODING_TYPE);
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
                            outputStream.print(MessageFormat.format("{0}: websocket\r\n", UPGRADE_HEADER));
                            outputStream.print(MessageFormat.format("{0}: Upgrade\r\n", CONNECTION_HEADER));
                            outputStream.print(MessageFormat.format("{0}: {1}\r\n", SEC_WEBSOCKET_VERSION_HEADER, WEBSOCKET_SUPPORTED_VERSIONS));
                            outputStream.print(MessageFormat.format("{0}: {1}\r\n", SEC_WEBSOCKET_ACCEPT_HEADER, acceptKey));
                            outputStream.print("\r\n");
                            outputStream.flush();
                            
                            return true;
                        }
                        //END OF if (this.requestHeaderFields.containsKey(SEC_WEBSOCKET_KEY_HEADER)) {...
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
            outputStream.print(MessageFormat.format("{0}: websocket\r\n", UPGRADE_HEADER));
            outputStream.print(MessageFormat.format("{0}: Upgrade\r\n", CONNECTION_HEADER));
            outputStream.print(MessageFormat.format("{0}: {1}\r\n", SEC_WEBSOCKET_VERSION_HEADER, WEBSOCKET_SUPPORTED_VERSIONS));
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
    
    /**
     * Negotiates a handshake response from an endpoint and will return true if successful.
     * @return true if the handshake was negotiated successful.
     */
    @SuppressWarnings("resource")
    public boolean negotiateResponse() {
        
        WebSocketsReader inputStream = null;
        int preserveOriginalTimeout = 0;
        
        try {
            
            String remoteAddress = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
            
            Logger.debug(MessageFormat.format("Handshake response from {0}...", remoteAddress));
            
            //Set the timeout for the handshake.
            preserveOriginalTimeout = this.socket.getSoTimeout();
            this.socket.setSoTimeout(this.timeoutInMilliseconds);
            
            inputStream = new WebSocketsReader(socket.getInputStream());
            
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

            String rawHandshakeResponse = stringBuffer.toString();
            
            Logger.verboseDebug(MessageFormat.format("------Buffered Response------\r\n{0}", rawHandshakeResponse));
            
            //Break up the HTTP request by newline constants.
            String[] headerLines = rawHandshakeResponse.toString().split("\r\n");
            if (headerLines.length > 0) {
                
                //Verify that the HTTP verb and version are correct.
                if ( (headerLines[0] !="") && (headerLines[0].toUpperCase().startsWith("HTTP/1.1 101")) && (headerLines[0].toUpperCase().contains("SWITCHING PROTOCOLS"))  ) {
                
                    //Collect all of the header fields sent during the initial handshake request.
                    Map<String, String> responseHeaderFields = new HashMap<String,String>();
                    for (int i = 1; i < headerLines.length; i++) {

                        // -- CR (9-16-12) --- Fixed an issue where a colon may be present in the value.
                        //Break the header into a key and value pair and ignore malformed headers.
                        String[] tokens = headerLines[i].split(":", 2);
                        if (tokens.length == 2) {
                            responseHeaderFields.put(tokens[0].trim().toUpperCase(), tokens[1].trim());
                        }
                    }
                    
                    //TODO Version negotiation: http://tools.ietf.org/html/rfc6455#section-4.4
                    String upgradeHeader = responseHeaderFields.get(UPGRADE_HEADER).toString();
                    String connectionHeader = responseHeaderFields.get(CONNECTION_HEADER).toString();
                    
                    //Determine if we have an upgrade & connection header available.
                    if (upgradeHeader.equalsIgnoreCase("websocket") && connectionHeader.equalsIgnoreCase("upgrade")){
                    
                        if (responseHeaderFields.containsKey(SEC_WEBSOCKET_ACCEPT_HEADER)) {
    
                            //Generate the client accept key.
                            String clientAcceptKey = "";
                            try {
                                MessageDigest md = MessageDigest.getInstance(HASHING_ALGORITHM);
                                byte[] keyArray = (this.securityWebSocketKey + WEBSOCKET_GUID).getBytes(ENCODING_TYPE);
                                byte[] hashedKey = md.digest(keyArray);
                                clientAcceptKey = Base64.encodeBytes(hashedKey);
                            }
                            catch (NoSuchAlgorithmException noAlgorithmException) {
                                Logger.severe(MessageFormat.format("Unable to find the hashing algorithm {0}.  The accept key cannot be created.", HASHING_ALGORITHM));
                                return false;
                            }
                            catch (java.lang.NoClassDefFoundError noClassException) {
                                Logger.severe(MessageFormat.format("Unable to find the vital class {0} to generate a Base64 value.", noClassException.getMessage()));
                                return false;
                            }
                            
                            String serverAcceptKey = responseHeaderFields.get(SEC_WEBSOCKET_ACCEPT_HEADER);
                            
                            //Verify that the client's key matches that of the server.
                            if (serverAcceptKey.equalsIgnoreCase(clientAcceptKey)) {
                                return true;
                            }
                            else {
                                Logger.verboseDebug(MessageFormat.format("The server's security key {0} does not match the client's {1}.", serverAcceptKey, clientAcceptKey));
                            }
                        }
                        //END OF if (responseHeaderFields.containsKey(SEC_WEBSOCKET_ACCEPT_HEADER)) {...
                    }
                    //END OF if (upgradeHeader.equalsIgnoreCase("websocket") && connectionHeader.equalsIgnoreCase("upgrade")){...
                }
                //END OF if( (headerLines[0] !="") && (headerLines[0].toUpperCase().startsWith("GET")) && (headerLines[0].toUpperCase().contains("HTTP/1.1")) )...
            }
            //END OF if(headerLines.length > 0)...

        }
        //If any one of these exceptions occur then an invalid handshake has been sent and we will ignore the request.
        catch (EndOfStreamException eofsException) {
            Logger.verboseDebug("Invalid handshake.  The server has unexpectedly disconnected.");
        }
        catch (SocketTimeoutException socketTimeout) {
            Logger.verboseDebug(MessageFormat.format("A socket timeout has occured. {0}", socketTimeout.getMessage()));
        }
        catch (IOException ioException) {
            Logger.verboseDebug(MessageFormat.format("Unable to read or write to the streams. {0}", ioException.getMessage()));
        }
        catch (Exception ex) {
            Logger.verboseDebug(MessageFormat.format("{0}", ex.getMessage()));
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
    
    /**
     * Attempts to perform a handshake request with an endpoint.
     * @return true if the request was successfully created and negotiated by the endpoint.
     */
    public boolean createRequest() {
        
        PrintWriter outputStream = null;
        int preserveOriginalTimeout = 0;
        
        try {
            
            String remoteAddress = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
            int port = ((InetSocketAddress)socket.getRemoteSocketAddress()).getPort();
            
            Logger.debug(MessageFormat.format("Handshaking to {0}...", remoteAddress));
            
            //Set the timeout for the handshake.
            preserveOriginalTimeout = this.socket.getSoTimeout();
            this.socket.setSoTimeout(this.timeoutInMilliseconds);
            
            String key = UUID.randomUUID().toString();
            
            //Generate the client accept key.
            this.securityWebSocketKey = "";
            try {
                MessageDigest md = MessageDigest.getInstance(HASHING_ALGORITHM);
                byte[] keyArray = key.getBytes(ENCODING_TYPE);
                byte[] hashedKey = md.digest(keyArray);
                this.securityWebSocketKey = Base64.encodeBytes(hashedKey);
            }
            catch (NoSuchAlgorithmException noAlgorithmException) {
                Logger.severe(MessageFormat.format("Unable to find the hashing algorithm {0}.  The accept key cannot be created.", HASHING_ALGORITHM));
                return false;
            }
            catch (java.lang.NoClassDefFoundError noClassException) {
                Logger.severe(MessageFormat.format("Unable to find the vital class {0} to generate a Base64 value.", noClassException.getMessage()));
                return false;
            }
            
            outputStream = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), ENCODING_TYPE), true);
            outputStream.print("GET / HTTP/1.1\r\n");
            outputStream.print(MessageFormat.format("Host: {0}:{1,number,#}\r\n", remoteAddress, port));
            outputStream.print(MessageFormat.format("{0}: {1}\r\n", ORIGIN_HEADER, this.origin));
            outputStream.print(MessageFormat.format("{0}: websocket\r\n", UPGRADE_HEADER));
            outputStream.print(MessageFormat.format("{0}: Upgrade\r\n", CONNECTION_HEADER));
            outputStream.print(MessageFormat.format("{0}: {1}\r\n", SEC_WEBSOCKET_KEY_HEADER, this.securityWebSocketKey));
            outputStream.print(MessageFormat.format("{0}: {1}\r\n", SEC_WEBSOCKET_VERSION_HEADER, WEBSOCKET_SUPPORTED_VERSIONS));
            outputStream.print(MessageFormat.format("User-Agent: {0}\r\n", this.userAgent));
            outputStream.print("\r\n");
            outputStream.flush();
            
            return true;
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
    
    /**
     * Forwards a handshake request to a specific socket.
     */
    public void forwardRequest() {
        
        PrintWriter outputStream;
        
        try {
            
            outputStream = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream(), ENCODING_TYPE), true);
            
            outputStream.print(getRawRequestRequest());
            outputStream.flush();
            
        } catch (UnsupportedEncodingException e) {
            Logger.verboseDebug(MessageFormat.format("The encoding type {0} is not supported.  Handshake failed.", ENCODING_TYPE));
        } catch (IOException e) {
            //Do nothing...
        }
    }
    
    /**
     * Clones a handshake using the same request information but a different socket.
     * @param socket the socket to apply to the cloned handshake.
     * @return a cloned handshake with a new socket.
     */
    public Handshake cloneHandshake(Socket socket) {
        
        Handshake handshake = new Handshake(this);
        handshake.socket = socket;
        return handshake;
        
    }
}
