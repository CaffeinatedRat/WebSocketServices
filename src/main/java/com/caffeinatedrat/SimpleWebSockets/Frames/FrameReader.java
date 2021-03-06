/**
* Copyright (c) 2013-2015, Ken Anderson <caffeinatedrat at gmail dot com>
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

import java.io.IOException;
import java.net.Socket;
import java.text.MessageFormat;

import com.caffeinatedrat.SimpleWebSockets.Exceptions.InvalidFrameException;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;
import com.caffeinatedrat.SimpleWebSockets.Util.WebSocketsReader;

import com.caffeinatedrat.SimpleWebSockets.Payload.*;

public class FrameReader {

    /**
     * A simple, small thread that allows for a non-blocking instance of a frame.
     * When activity is detected and the buffer contains data then the frame will start blocking again to read important data.
     * We do not need a full-fledged non-blocking architecture at this time, since most of the time the framework is blocking.
     */
    public class EventThread extends Thread {
        
        private WebSocketsReader reader;
        private volatile boolean dataAvailable;
        
        public EventThread(WebSocketsReader reader) {
          
            this.reader = reader;
            this.dataAvailable = false;
            
        }
        
        public synchronized boolean isDataAvailable() {
            return this.dataAvailable;
        }
        
        @Override
        public void run() {
            
            try {
                
                Logger.verboseDebug(MessageFormat.format("A new EventThread {0} has spun up...", this.getName()));
                
                //Mark the beginning of the stream.
                reader.mark(0);
            
                reader.read();
                
                //Reset to the beginning of the stream.
                reader.reset();
                
                //Note that data is available...we don't care if this value is dirty at the time we are checking...
                synchronized(this){
                    this.dataAvailable = true;
                }
                
                Logger.verboseDebug(MessageFormat.format("EventThread {0} has spun down...", this.getName()));
                
            } catch (IOException e) {
                //Logger.verboseDebug(MessageFormat.format("Unable to monitor for frame data. {0}", e.getMessage()));
            }
        }
    }
    
    // ----------------------------------------------
    // Constants
    // ----------------------------------------------
    public static final long MAX_SUPPORTED_FRAGEMENTATION_SIZE = 128; 
    
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------

    //Frame & fragmentation management.
    protected Frame frame = null;
    protected Frame.OPCODE frameType;
    private byte[][] payloadFragments;
    protected int maxNumberOfFragmentedFrames = 2;

    //Event management.
    protected IFrameEvent frameEventLayer = null;
    
    //Non-blocking & threading management. 
    protected EventThread eventThread = null;
    protected boolean initialBlocking = true;
    protected boolean blocking = true;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Read-Only property that determines if the FrameReader is blocking.
     * @return true if the FrameReader is blocking.
     */
    public boolean isBlocking() {
        return this.blocking || this.initialBlocking;
    }
    
    /**
     * Read-Only property that determines the number of contiguous fragmented frames that will be accepted before a connection is severed.
     * @return The number of contiguous fragmented frames that will be accepted before a connection is severed.
     */
    public int getMaxNumberOfFragmentedFrames() {
        return this.maxNumberOfFragmentedFrames;
    }
    
    /**
     * Read-Only property that returns the type of the frame.
     * @return the type of the frame.
     */
    public Frame.OPCODE getFrameType() {
        return this.frameType;
    }
    
    /**
     * Read-Only property that returns the Payload.
     * @return The Payload.
     */
    public Payload getPayload() {
        
        return new Payload(this.payloadFragments);
        
    }
    
    /**
     * Read-Only property that returns the TextPayload.
     * @return The TextPayload.
     */    
    public TextPayload getTextPayload() {
        
        return new TextPayload(this.payloadFragments);
        
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public FrameReader(Socket socket, IFrameEvent frameEventLayer) throws InvalidFrameException {
        
        this(socket, frameEventLayer, 1000, 2);
        
    }
    
    public FrameReader(Socket socket, IFrameEvent frameEventLayer, int timeout, int maxNumberOfFragmentedFrames) throws InvalidFrameException {
    
        if (socket == null) {
            
            throw new IllegalArgumentException("The socket is invalid (null).");
            
        }
        
        //Restrict the fragmentation to a valid fragment value and to the total Maximum size supported.
        if ( (maxNumberOfFragmentedFrames < 1) && (maxNumberOfFragmentedFrames > MAX_SUPPORTED_FRAGEMENTATION_SIZE) ) {
         
            maxNumberOfFragmentedFrames = 2;
            
        }

        this.frame = new Frame(socket, timeout);
        this.frameEventLayer = frameEventLayer;
        this.maxNumberOfFragmentedFrames = maxNumberOfFragmentedFrames;
        
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------

    /**
     * Reads a one or many frames.
     * @return true if a frame has been read or false if nothing was available.
     * @throws InvalidFrameException occurs when the frame is invalid due to an incomplete frame being sent by the client.
     */
    public boolean read()
            throws InvalidFrameException {
        
        //Allocate a buffer the size of our maximum number of fragmented frames. 
        byte[][] rawPayloadFragments = new byte[maxNumberOfFragmentedFrames][];
        @SuppressWarnings("unused")
        long totalCount = 0L;
        
        // --- CR (7/20/13) --- Start blocking only if a frame is available or this is the initial request, which requires blocking.
        // --- CR (11/18/13) --- The FrameReader will remain in blocking mode indefinitely unless told to stop by the method stopBlocking.
        if (isAvailable() || this.initialBlocking || this.blocking) {
            
            //If we enter this block, we are either:
            //1) Blocking until we receive a frame during the initial opening of the connection.
            //2) Blocking to receive a new frame.
            this.frame.read();
            
            //Get the first frame type.
            this.frameType = this.frame.getOpCode();

            //Verify that our frame is valid.  For example, fragmented control frames are not supported per the RFC.
            //http://tools.ietf.org/html/rfc6455#section-5.4
            if( !this.frame.isFinalFragment() && this.frameType.isControlFrame()) {
                
                throw new InvalidFrameException("A control frame cannot be fragmented.");
                
            }

            //Add the first block of payload data to our collection of fragments.
            rawPayloadFragments[0] = this.frame.getPayloadAsBytes();
            totalCount = this.frame.getPayloadLength();
            
            // --- CR (11/11/13) --- Support for fragmented frames.
            //http://tools.ietf.org/html/rfc6455#section-5.4
            
            //NOTE: We have already read one frame so we start the count at 1.
            //Continue to read frames until we find the final fragment or until we reach the maximum number of fragments.
            int fragmentCount = 1;
            while( !this.frame.isFinalFragment() && fragmentCount < this.maxNumberOfFragmentedFrames ) {
                
                //Read our next frame.
                this.frame.read();
                
                Frame.OPCODE currentFrameType = this.frame.getOpCode(); 
                
                //Per the RFC, control frames can be interleaved between fragmented frames of a non-control frame.
                //http://tools.ietf.org/html/rfc6455#section-5.4
                if (currentFrameType.isControlFrame()) {
                    
                    //If a close connection frame occurs then we terminate the read.
                    if (currentFrameType == Frame.OPCODE.CONNECTION_CLOSE_CONTROL_FRAME) {
                        
                        //Set the frametype to closed.
                        this.frameType = Frame.OPCODE.CONNECTION_CLOSE_CONTROL_FRAME;
                        
                        //Release the previous payload and construct a new one with a capacity of only one.
                        if (rawPayloadFragments[0].length > 1) {
                            rawPayloadFragments = new byte[1][];
                        }
                        
                        //Add to the payload.
                        rawPayloadFragments[0] = this.frame.getPayloadAsBytes();
                        totalCount = this.frame.getPayloadLength();
                        
                        //Terminate the loop.
                        break;
                        
                    }
                    //Some other control frame has occurred, so invoke the FrameEventLayer if one was provided.
                    else {

                        if (frameEventLayer != null) {
                            
                            frameEventLayer.onControlFrame(currentFrameType, new Payload(new byte[][] {this.frame.getPayloadAsBytes()} ));
                            
                        }
                        
                    }
                    // END OF if (currentFrameType == Frame.OPCODE.CONNECTION_CLOSE_CONTROL_FRAME) {...
                    
                }
                else if (currentFrameType == Frame.OPCODE.CONTINUATION_DATA_FRAME){
                    
                    //Add to the payload.
                    rawPayloadFragments[fragmentCount] = this.frame.getPayloadAsBytes();
                    totalCount += this.frame.getPayloadLength();
                }
                else  {
                    
                    //Invalid fragmentation.
                    throw new InvalidFrameException(MessageFormat.format("The fragmentation stream contained an invalid non-control frame with the OpCode {0}.", currentFrameType.getValue()));
                    
                }
                
                fragmentCount++;
                
            }
            //END OF while( !this.frame.isFinalFragment() && fragmentCount < this.maxNumberOfFragmentedFrames ) {...
            
            //Determine if the loop terminated due to too many fragments.
            if (fragmentCount > this.maxNumberOfFragmentedFrames) {
                
                //Invalid fragmentation.
                throw new InvalidFrameException(MessageFormat.format("Fragmentation exceeded {0}.", this.maxNumberOfFragmentedFrames));
                
            }
            
            //Blocking has ended unless something is available for future reads.
            this.initialBlocking = false;
            
            //Condense the payload.
            this.payloadFragments = new byte[fragmentCount][];
            for(int i= 0; i < fragmentCount; i++) {
                
                this.payloadFragments[i] = new byte[rawPayloadFragments[i].length];
                java.lang.System.arraycopy(rawPayloadFragments[i], 0, this.payloadFragments[i], 0, rawPayloadFragments[i].length);
                
            }
            
            //The initial read has been completed.
            return true;
            
        }
        else {
            
            //Nothing is available to read...
            return false;
            
        }
        //END OF if (isAvailable() || this.initialBlocking || this.blocking) {...
        
    }
    
    /**
     * Returns true if there is data available for the frame.
     * @return true if there is data available for the frame.
     */
    protected boolean isAvailable() {
        
        if ( (this.eventThread != null) && (this.eventThread.isDataAvailable()) ) {
            
            this.eventThread = null;
            return true;
            
        }
        
        return false;
    }
    
    /**
     * Stops the FrameReader from blocking when it reads.
     */
    public void stopBlocking() {
        
        if ( (this.eventThread == null) && (this.frame.getReader() != null) ) {
        
            this.eventThread = new EventThread(this.frame.getReader());
            this.eventThread.start();
        
        }

        this.blocking = false;
        
    }
 
    /**
     * This method will cause the FrameReader to start blocking again.
     */
    public void startBlocking() {

        if (this.eventThread != null) {
            try {
                this.eventThread.join(10000);
                
            } catch (InterruptedException e) {
                //Do nothing with this exception.
                //Timing out has no significant effect on this method.
            }
            
            this.eventThread = null;
        }
        

        this.blocking = true;
    }
    
}
