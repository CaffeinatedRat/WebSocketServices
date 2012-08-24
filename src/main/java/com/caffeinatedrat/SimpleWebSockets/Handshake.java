/**
 * 
 */
package com.caffeinatedrat.SimpleWebSockets;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.caffeinatedrat.SimpleWebSockets.Exceptions.EndOfStreamException;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;
import com.caffeinatedrat.SimpleWebSockets.Util.WebSocketsReader;

import net.iharder.Base64;

/**
 * @author CaffeinatedRat
 * 
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
    private static final String SEC_WEBSOCKET_KEY_HEADER = "SEC-WEBSOCKET-KEY";
    //private static final String SEC_WEBSOCKET_VERSION_HEADER = "SEC-WEBSOCKET-VERSION";
    
    // ----------------------------------------------
    //  Member Vars (fields)
    // ----------------------------------------------

    private Map<String,String> headerFields = new HashMap<String,String>();
    private Socket socket;
    private int timeoutInMilliseconds;
    private boolean checkOrigin;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
  
    /**
     * Returns an array of supported versions.
     * @return An array of supported versions.
     */
    public static String[] getSupportedVersions()
    {
        return WEBSOCKET_SUPPORTED_VERSIONS.split(",");
    }
    
    /**
     * Returns all of headers found in the handshake.
     * @return A map that contains the header and its value.
     */    
    public Map<String, String> getHeaders()
    {
        return headerFields;
    }
    
    /**
     * Sets the timeout value for the handshake.
     * @param timeout The time in milliseconds before the handshake fails.
     */
    public void setTimeOut(int timeout)
    {
        this.timeoutInMilliseconds = timeout; 
    }

    /**
     * Gets the timeout value for the handshake.
     * @return timeout The time in milliseconds before the handshake fails.
     */    
    public int getTimeOut()
    {
        return this.timeoutInMilliseconds;
    }

    /**
     * Sets check origin flag.
     * @param checkOrigin The check origin flag.
     */            
    public void setCheckOrigin(boolean checkOrigin)
    {
        this.checkOrigin = checkOrigin;
    }
    
    /**
     * Gets check origin flag.
     * @return The check origin flag.
     */        
    public boolean getCheckOrigin()
    {
        return this.checkOrigin;
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public Handshake(Socket socket)
    {
        this(socket, 1000);
    }
    
    public Handshake(Socket socket, int timeout)
    {
        this.socket = socket;
        this.timeoutInMilliseconds = timeout;
        this.checkOrigin = false;
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Performs the actual handshake and will return false if the handshake has failed.
     * @return Returns true if the handshake was successful. 
     */
    public boolean processRequest()
    {
        Logger.debug("Handshaking...");
        
        WebSocketsReader inputStream;
        PrintWriter outputStream;
        
        int preserveOriginalTimeout = 0;
        
        try
        {
            //Set the timeout for the handshake.
            preserveOriginalTimeout = this.socket.getSoTimeout();
            this.socket.setSoTimeout(this.timeoutInMilliseconds);
            
            inputStream = new WebSocketsReader(socket.getInputStream());
            outputStream = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), ENCODING_TYPE), true);
            
            //Buffer the HTTP handshake request.
            byte[] buffer = new byte[Globals.READ_CHUNK_SIZE];
            int len = 0;
            StringBuilder stringBuffer = new StringBuilder();
            
            while ( (stringBuffer.lastIndexOf("\r\n\r\n") == -1) && ( (len = inputStream.read(buffer, 0, Globals.READ_CHUNK_SIZE)) > 0 ) )
            {
                stringBuffer.append(new String(buffer, 0, len));
            }
            
            if(len == -1)
                throw new EndOfStreamException();
                            
            Logger.verboseDebug(MessageFormat.format("------Buffered Request------\r\n{0}", stringBuffer.toString()));
            
            //Break up the HTTP request by newline constants.
            String[] headerLines = stringBuffer.toString().split("\r\n");
            if(headerLines.length > 0)
            {
                //Verify that the HTTP verb and version are correct.
                if( (headerLines[0] !="") && (headerLines[0].toUpperCase().startsWith("GET")) && (headerLines[0].toUpperCase().contains("HTTP/1.1")) )
                {
                    //Collect all of the header fields sent during the initial handshake request.
                    headerFields = new HashMap<String,String>();
                    for(int i = 1; i < headerLines.length; i++)
                    {
                        //Break the header into a key and value pair and ignore malformed headers.
                        String[] tokens = headerLines[i].split(":");
                        if(tokens.length == 2)
                            headerFields.put(tokens[0].trim().toUpperCase(), tokens[1].trim());
                    }
                     
                    //Origin check: http://tools.ietf.org/html/rfc6455#section-4.2.2
                    if ( (!getCheckOrigin()) || ((headerFields.containsKey(ORIGIN_HEADER)) && (!headerFields.get(ORIGIN_HEADER).equalsIgnoreCase("null"))) ) 
                    {
                        //TO-DO: 
                        //To check the origin against a whitelist if one exists.
                        //Adds an additional timecomplexity of O(n).
                        
                        //TO-DO
                        //Add Version negotiation: http://tools.ietf.org/html/rfc6455#section-4.4
                        
                        //Generate the accept key.
                        String acceptKey = "";
                        if(headerFields.containsKey(SEC_WEBSOCKET_KEY_HEADER))
                        {
                            String requestKey = headerFields.get(SEC_WEBSOCKET_KEY_HEADER) + WEBSOCKET_GUID;
                            
                            try
                            {
                                MessageDigest md = MessageDigest.getInstance(HASHING_ALGORITHM);
                                byte[] keyArray = requestKey.getBytes(ENCODING_TYPE);
                                byte[] hashedKey = md.digest(keyArray);
                                acceptKey = Base64.encodeBytes(hashedKey);
                            }
                            catch(NoSuchAlgorithmException noAlgorithmException)
                            {
                                outputStream.println("HTTP/1.1 500 Internal Server Error");
                                Logger.severe(MessageFormat.format("Unable to find the hashing algorithm {0}.  The accept key cannot be created.", HASHING_ALGORITHM));
                                return false;
                            }
                            catch(java.lang.NoClassDefFoundError noClassException)
                            {
                                outputStream.println("HTTP/1.1 500 Internal Server Error");
                                Logger.severe(MessageFormat.format("Unable to find the vital class {0} to generate a Base64 value.", noClassException.getMessage()));
                                return false;
                            }
    
                            //Respond to the client with the proper handshake.
                            outputStream.println("HTTP/1.1 101 Switching Protocols");
                            outputStream.println("Upgrade: websocket");
                            outputStream.println("Connection: Upgrade");
                            outputStream.println(MessageFormat.format("Sec-WebSocket-Version: {0}", WEBSOCKET_SUPPORTED_VERSIONS));
                            outputStream.println(MessageFormat.format("Sec-WebSocket-Accept: {0}", acceptKey));
                            outputStream.println();
                            outputStream.flush();
                            
                            return true;
                        }
                        //END OF if(headerFields.containsKey("SEC-WEBSOCKET-KEY"))...
                    }
                    else
                    {
                        outputStream.println("HTTP/1.1 403 Forbidden");
                        Logger.debug("Unable to verify the origin.");
                        return false;
                    }
                    //END OF if(headerFields.containsKey(ORIGIN_HEADER))...
                }
                //END OF if( (headerLines[0] !="") && (headerLines[0].toUpperCase().startsWith("GET")) && (headerLines[0].toUpperCase().contains("HTTP/1.1")) )...
            }
            //END OF if(headerLines.length > 0)...

            Logger.verboseDebug(MessageFormat.format("------Buffered Request------\r\n{0}", stringBuffer.toString()));
            
            //If we've reached this point then return a bad request to the client and terminate the handshake.
            outputStream.println("HTTP/1.1 400 Bad Request");
            outputStream.println("Upgrade: websocket");
            outputStream.println("Connection: Upgrade");
            outputStream.println(MessageFormat.format("Sec-WebSocket-Version: {0}", WEBSOCKET_SUPPORTED_VERSIONS));
            outputStream.println();
            outputStream.flush();
        }
        //If any one of these exceptions occur then an invalid handshake has been sent and we will ignore the request.
        catch(EndOfStreamException eofsException)
        {
            Logger.verboseDebug("Invalid handshake.  The client has unexpectedly disconnected.");
        }
        catch(SocketTimeoutException socketTimeout)
        {
            Logger.verboseDebug(MessageFormat.format("A socket timeout has occured. {0}", socketTimeout.getMessage()));
        }
        catch(IOException ioException)
        {
            Logger.verboseDebug(MessageFormat.format("Unable to read or write to the streams. {0}", ioException.getMessage()));
        }
        finally
        {
            try
            {
                //Reset the original timeout.
                this.socket.setSoTimeout(preserveOriginalTimeout);
            }
            catch(IOException ie) {}
        }
        
        return false;
    }
}