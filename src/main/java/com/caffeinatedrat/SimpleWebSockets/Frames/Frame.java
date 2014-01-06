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

package com.caffeinatedrat.SimpleWebSockets.Frames;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.caffeinatedrat.SimpleWebSockets.Exceptions.EndOfStreamException;
import com.caffeinatedrat.SimpleWebSockets.Exceptions.InvalidFrameException;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;
import com.caffeinatedrat.SimpleWebSockets.Util.WebSocketsReader;

/**
 * Handles the websockets frames.
 * http://tools.ietf.org/html/rfc6455#section-5.1
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class Frame {

    // ----------------------------------------------
    // Constants
    // ----------------------------------------------
    public static final long MAX_PAYLOAD_SIZE = 65536;
    
    // ----------------------------------------------
    // Data Types
    // ----------------------------------------------
    
    //WebSockets version 13 OpCodes.
    //http://tools.ietf.org/html/rfc6455#section-5.2
    public enum OPCODE {
        
        //0-2 are valid data frames.
        //Per the RFC, Non-control frames have a the most significant bit set to 1.
        CONTINUATION_DATA_FRAME(0, true),
        TEXT_DATA_FRAME(1, true),
        BINARY_DATA_FRAME(2, true),
        
        //3-7 are reserved for future non-control frames.
        RESERVED1_DATA_FRAME(3, true),
        RESERVED2_DATA_FRAME(4, true),
        RESERVED3_DATA_FRAME(5, true),
        RESERVED5_DATA_FRAME(6, true),
        RESERVED6_DATA_FRAME(7, true),
        
        //8-10 are valid control frames.
        //http://tools.ietf.org/html/rfc6455#section-5.5
        CONNECTION_CLOSE_CONTROL_FRAME(8, false),
        PING_CONTROL_FRAME(9, false),
        PONG_CONTROL_FRAME(10, false),
        
        //11-15 are reserved control frames.
        RESERVED1_CONTROL_FRAME(11, true),
        RESERVED2_CONTROL_FRAME(12, true),
        RESERVED3_CONTROL_FRAME(13, true),
        RESERVED4_CONTROL_FRAME(14, true),
        RESERVED5_CONTROL_FRAME(15, true);
        
        private final int opCode;
        
        // --- CR(11/10/13) --- Not sure what I was doing with this before...but make it reserved for now.
        @SuppressWarnings("unused")
        private final boolean reserved;
        
        OPCODE(int opCode, boolean reserved) {
            this.opCode = opCode;
            this.reserved = reserved;
        }
        
        public int getValue() {
            return this.opCode;
        }
        
        // --- CR(11/10/13) --- Added a way of detecting control frames based on the RFC that do not require manually assigning the frame type.
        //Per the RFC, Control frames have a the most significant bit set to 1.
        //http://tools.ietf.org/html/rfc6455#section-5.5
        public boolean isControlFrame() {
            return ((this.opCode & 8) == 8);
        }
    }
    
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private WebSocketsReader reader = null;
    private Socket socket;
    
    private byte controlByte;
    private byte descriptorByte;
    private long payloadLength;
    private byte[] mask = null;
    private byte[] payload = null;
    
    private int timeoutInMilliseconds;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    public boolean isFinalFragment() {
        return ((this.controlByte & 0x80) == 0x80); 
    }
    
    public void setFinalFragment() {
        this.controlByte |= 0x80;
    }
    
    public void clearFinalFragment() {
        this.controlByte &= (~0x80);
    }    
    
    public boolean isReservedValid() {
        return ((this.controlByte >> 4 & 0x07) == 0x00) ? true : false;
    }
    
    public void setReserved1() {
        this.controlByte |= 0x40;
    }
    
    public void clearReserved1() {
        this.controlByte &= (~0x40);
    }
    
    public void setReserved2() {
        this.controlByte |= 0x20;
    }
    
    public void clearReserved2() {
        this.controlByte &= (~0x20);
    }    
    
    public void setReserved3() {
        this.controlByte |= 0x10;
    }

    public void clearReserved3() {
        this.controlByte &= (~0x10);
    }    
    
    public OPCODE getOpCode() {
        return (OPCODE.values()[(this.controlByte & 0x0F)]);
    }
    
    public void setOpCode(OPCODE opCode) {
        this.controlByte = (byte)((this.controlByte & 0xF0) | opCode.getValue());
    }
    
    public boolean isMasked() {
        return ((this.descriptorByte & 0x80) == 0x80);
    }
    
    public byte[] getMask() {
        return this.mask;
    }
    
    public void setMask(byte[] mask) {
        this.mask = mask;
        this.descriptorByte |= 0x80;
    }
    
    public void clearMask() {
        this.mask = null;
        this.descriptorByte &= (~0x80);
    }
    
    public long getPayloadLength() {
        return this.payloadLength;
    }
    
    public byte[] getPayloadAsBytes() {
        return this.payload;
    }
    
    public String getPayloadAsString() {
        
        if ( (this.payload != null) && (this.payload.length > 0 ) ) {
            return new String(this.payload, 0, this.payload.length);
        }

        return "";
    }
    
    public void setPayload(byte[] payload) {
        
        if(payload != null) {
            this.payloadLength = payload.length;
            this.payload = payload;
        }
        
    }
    
    public void setPayload(String payload) {
        
        if ( (payload != null) && (payload.length() > 0) ) {
            
            try {
                
                // --- CR (2/21/13) --- Force into UTF-8 and get the correct payload length.
                this.payload = payload.getBytes("UTF-8");
                //this.payloadLength = payload.length();
                this.payloadLength = this.payload.length;
                
            }
            catch(UnsupportedEncodingException exception) {
                
                Logger.verboseDebug(exception.getMessage());
                
            }
        }
        
    }
    
    /**
     * Sets the timeout value for the frame.
     * @param timeout The time in milliseconds before the frame fails.
     */
    public void setTimeOut(int timeout) {
        this.timeoutInMilliseconds = timeout; 
    }

    /**
     * Gets the timeout value for the frame.
     * @return timeout The time in milliseconds before the frame fails.
     */
    public int getTimeOut() {
        return this.timeoutInMilliseconds;
    }
    
    
    /**
     * Read-Only property for retrieving the WebSocketsReader used to read the frames.
     * @return the WebSocketsReader used to read the frames.
     */
    public WebSocketsReader getReader() {
        return this.reader;
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public Frame(Socket socket) throws InvalidFrameException {
        
        this(socket, 1000);
        
    }
    
    public Frame(Socket socket, int timeout) throws InvalidFrameException {
        
        if (socket == null) {
            
            throw new IllegalArgumentException("The socket is invalid (null).");
            
        }
        
        this.socket = socket;
        
        //Initialize to zero.
        this.controlByte = 0x00;
        this.descriptorByte = 0x00;
        this.payloadLength = 0L; 
        
        this.timeoutInMilliseconds = timeout;
        
        try {
        
            this.reader = new WebSocketsReader(this.socket.getInputStream());
            
        }
        catch (IOException ioException) {
            
            Logger.verboseDebug(MessageFormat.format("Unable to read or write to the streams. {0}", ioException.getMessage()));
            throw new InvalidFrameException();
            
        }
        
    }
    
    /**
     * Copy constructor
     */
    public Frame(Frame frame) {
        
        this.socket = frame.socket;
        this.controlByte = frame.controlByte;
        this.descriptorByte = frame.descriptorByte;
        this.mask = (frame.mask != null) ? frame.mask.clone() : null;
        this.payloadLength = frame.payloadLength;
        this.payload = (frame.payload != null) ? frame.payload.clone() : null;
        this.timeoutInMilliseconds = frame.timeoutInMilliseconds;
        
    }
    
    /**
     * Copy constructor with new Socket.
     */
    public Frame(Frame frame, Socket socket) {
        
        this.socket = socket;
        this.controlByte = frame.controlByte;
        this.descriptorByte = frame.descriptorByte;
        this.mask = (frame.mask != null) ? frame.mask.clone() : null;
        this.payloadLength = frame.payloadLength;
        this.payload = (frame.payload != null) ? frame.payload.clone() : null;
        this.timeoutInMilliseconds = frame.timeoutInMilliseconds;
        
    }    
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------

    
    /**
     * Reads a websocket frame.
     * @throws InvalidFrameException occurs when the frame is invalid due to an incomplete frame being sent by the client.
     */
    public void read()
        throws InvalidFrameException {
        
        int preserveOriginalTimeout = 0;
        
        try {

            //Set the timeout for the frame.
            preserveOriginalTimeout = this.socket.getSoTimeout();
            this.socket.setSoTimeout(this.timeoutInMilliseconds);

            //Suppress this warning as closing the stream after the event is completed will also close the socket.
            //WebSocketsReader inputStream = new WebSocketsReader(socket.getInputStream());
            WebSocketsReader inputStream = this.reader;
            
            byte[] buffer = new byte[8];
            inputStream.readFully(buffer, 0, 2);
            
            //The control byte determines if there is fragmentation and the opcode of the frame.
            this.controlByte = buffer[0];
            
            //Verify that the frame is valid.
            if (!isReservedValid()) {
                
                // --- CR(11/10/13) --- Added details on why this frame is invalid.
                throw new InvalidFrameException("The reserved bits are invalid.");
                
            }
            
            //The description byte determines if the frame data is masked and the size of the application data.
            this.descriptorByte = buffer[1];
            this.payloadLength = (descriptorByte & 0x7F);
            
            //http://tools.ietf.org/html/rfc6455#section-5.2
            //Per the RFC if the payloadLen described in the description byte is 126 then the payload length can be up to 2^16 in size.
            //An example can be found at http://tools.ietf.org/html/rfc6455#section-5.7
            if (this.payloadLength == 126) {
                //NOTE: Websockets uses a big endian byte order.
                inputStream.readFully(buffer, 0, 2);
                
                // --- CR (7/19/13) --- Fixed a major issue where the cast is preserving the signed value.
                this.payloadLength = (long)((((int)buffer[0] & 0xFF) << 8) | ((int)buffer[1] & 0xFF));
            }
            //Per the RFC if the payloadLen described in the description byte is 127 then the payload length can be up to 2^64 in size.
            //An example can be found at http://tools.ietf.org/html/rfc6455#section-5.7
            //NOTE: Chrome does not support this at the time (See note below).
            //NOTE2: When calculated this length has the capability of being 2^64 in length of contiguous memory, 1 zettabyte...not ready for this type of this memory usage.
            else if (this.payloadLength == 127) {
                
                //NOTE: Websockets uses a big endian byte order.
                inputStream.readFully(buffer, 0, 8);
                //Unrolled loop since the loop's size is static and to improve performance slightly.
                this.payloadLength = (long)((((long)buffer[0] & 0xFF) << 56) | (((long)buffer[1] & 0xFF) << 48) | (((long)buffer[2] & 0xFF) << 40) | (((long)buffer[3]& 0xFF) << 32) | (((long)buffer[4] & 0xFF) << 24) | (((long)buffer[5] & 0xFF) << 16) | (((long)buffer[6]& 0xFF) << 8) | (buffer[7]& 0xFF));
                
            }
            
            //Read the mask if one exists.
            if (isMasked()) {
                //Lazily load the mask only when it is necessary.
                mask = new byte[4];
                inputStream.readFully(this.mask, 0, 4);
            }
            
            //Read the payload only if the length is greater than zero.
            if (this.payloadLength > 0) {

                //Read the application data up to 2^16 (65K)
                //At this time we will not support frames that contain data that is greater 65K bytes in size since we do not want to consume a minimal amount of contiguous memory from the heap.
                //This is supposed to be a game server, so we do not want to consume more memory or cycles than is necessary.
                //This may be expand in the future to handle more complex client applications such as webgl & websockets.
                
                //Truncation is okay but note it in debug mode.
                if (this.payloadLength > MAX_PAYLOAD_SIZE) {
                    this.payloadLength = MAX_PAYLOAD_SIZE;
                    Logger.verboseDebug(MessageFormat.format("The payload is greater than {0} and is being truncated.", MAX_PAYLOAD_SIZE));
                }
                
                //Create the payload and read it.
                this.payload = new byte[(int)this.payloadLength];
                inputStream.readFully(this.payload, 0, (int)this.payloadLength);
                
                //Unmask Zorro!
                if (this.isMasked()) {
                    for(int i = 0; i < this.payloadLength; i++) {
                        this.payload[i] =  (byte)(this.payload[i] ^ this.mask[i % 4]);
                    }
                }
            }
            //END OF if(this.payloadLength > 0)...

        }
        catch (SocketTimeoutException socketTimeout) {
            Logger.verboseDebug(MessageFormat.format("A socket timeout has occured.  {0}", socketTimeout.getMessage()));
            throw new InvalidFrameException("The frame was not completed in time.");
        }
        catch (EndOfStreamException eofsException) {
            Logger.verboseDebug("Invalid frame.  The client has unexpectedly disconnected.");
            throw new InvalidFrameException("The client terminated the connection before completing the frame.");
        }
        catch (IOException ioException) {
            Logger.verboseDebug(MessageFormat.format("Unable to read or write to the streams. {0}", ioException.getMessage()));
            throw new InvalidFrameException();
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
    }
    
    /**
     * Writes a websocket frame.
     * @throws InvalidFrameException occurs when the frame is invalid due to an incomplete frame being sent by the client.
     */
    public void write()
        throws InvalidFrameException {
        
        int preserveOriginalTimeout = 0;
        
        try {
            //Set the timeout for the frame.
            preserveOriginalTimeout = this.socket.getSoTimeout();
            this.socket.setSoTimeout(this.timeoutInMilliseconds);

            OutputStream outputStream = socket.getOutputStream();

            int totalLengthToWrite = 2 + (isMasked() ? 4 : 0) ;
            
            //Write the application data up to 2^16 (65K)
            //At this time we will not support frames that contain data that is greater 65K bytes in size since we do not want to consume a minimal amount of contiguous memory from the heap.
            //This is supposed to be a game server, so we do not want to consume more memory or cycles than is necessary.
            //This may be expand in the future to handle more complex client applications such as webgl & websockets.
            
            //Truncation is okay but note it in debug mode.
            if (this.payloadLength > MAX_PAYLOAD_SIZE) {
                this.payloadLength = MAX_PAYLOAD_SIZE;
                Logger.verboseDebug(MessageFormat.format("The payload is greater than {0} and is being truncated.", MAX_PAYLOAD_SIZE));
            }
            
            if (this.payloadLength > 65535) {
                this.descriptorByte |= 0x7F;
                totalLengthToWrite += 8;
            }
            else if (this.payloadLength > 125) {
                this.descriptorByte |= 0x7E;
                totalLengthToWrite += 2;
            }
            else {
                this.descriptorByte |= (byte)this.payloadLength;
            }
            
            byte[] bufferedPayload = new byte[totalLengthToWrite + (int)this.payloadLength];
            int byteOffset = 2;
            bufferedPayload[0] = this.controlByte;
            bufferedPayload[1] = this.descriptorByte;

            if (this.payloadLength > 65535) {
              //NOTE: Websockets uses a big endian byte order.
                bufferedPayload[2] = (byte)(((this.payloadLength >> 56L) & 0x000000FF));
                bufferedPayload[3] = (byte)(((this.payloadLength >> 48L) & 0x000000FF));
                bufferedPayload[4] = (byte)(((this.payloadLength >> 40L) & 0x000000FF));
                bufferedPayload[5] = (byte)(((this.payloadLength >> 32L) & 0x000000FF));
                bufferedPayload[6] = (byte)(((this.payloadLength >> 24L) & 0x000000FF));
                bufferedPayload[7] = (byte)(((this.payloadLength >> 16L) & 0x000000FF));
                bufferedPayload[8] = (byte)(((this.payloadLength >> 8L) & 0x000000FF));
                bufferedPayload[9] = (byte)(this.payloadLength & 0x000000FF);
                byteOffset = 10;
            }
            else if (this.payloadLength > 125) {
              //NOTE: Websockets uses a big endian byte order.
                bufferedPayload[2] = (byte)((this.payloadLength >> 8L) & 0x00FF);
                bufferedPayload[3] = (byte)(this.payloadLength & 0x00FF);
                byteOffset = 4;
            }

            if (this.isMasked()) {
                
                //Unrolled loop.
                bufferedPayload[byteOffset++] = this.mask[0];
                bufferedPayload[byteOffset++] = this.mask[1];
                bufferedPayload[byteOffset++] = this.mask[2];
                bufferedPayload[byteOffset++] = this.mask[3];
                
            }
            
            for (int i = 0; i < this.payloadLength; i++) {
                
                if (this.isMasked()) {

                    bufferedPayload[byteOffset + i] =  (byte)(this.payload[i] ^ this.mask[i % 4]);
                    
                }
                else {
                    
                    bufferedPayload[byteOffset + i] = this.payload[i];
                    
                }
                
            }
            
            outputStream.write(bufferedPayload, 0, (int)bufferedPayload.length);
            outputStream.flush();
        }
        catch (SocketTimeoutException socketTimeout) {
            Logger.verboseDebug(MessageFormat.format("A socket timeout has occured.  {0}", socketTimeout.getMessage()));
            throw new InvalidFrameException("The frame was not completed in time.");
        }
        catch(IOException ioException) {
            Logger.verboseDebug(MessageFormat.format("Unable to read or write to the streams. {0}", ioException.getMessage()));
            throw new InvalidFrameException();
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
    }
    
}
