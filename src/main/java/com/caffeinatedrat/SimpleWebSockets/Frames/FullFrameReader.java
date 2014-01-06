/**
* Copyright (c) 2013-2014, Ken Anderson <caffeinatedrat at gmail dot com>
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.caffeinatedrat.SimpleWebSockets.Exceptions.InvalidFrameException;
import com.caffeinatedrat.SimpleWebSockets.Payload.*;

public class FullFrameReader extends FrameReader {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------

    //Manage a collection of frames instead of a payload.
    protected List<Frame> frames = new ArrayList<Frame>();
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Not supported for this class.
     */
    @Override
    public Payload getPayload() {
        
        throw new UnsupportedOperationException("The method getPayload() is not supported for this class.");
        
    }
    
    /**
     * Not supported for this class.
     */
    @Override
    public TextPayload getTextPayload() {
        
        throw new UnsupportedOperationException("The method getTextPayload() is not supported for this class.");
        
    }    
    
    public List<Frame> getFrames() {
        
        return this.frames;
        
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public FullFrameReader(Socket socket, IFrameEvent frameEventLayer) throws InvalidFrameException {
        
        this(socket, frameEventLayer, 1000, 2);
        
    }
    
    public FullFrameReader(Socket socket, IFrameEvent frameEventLayer, int timeout, int maxNumberOfFragmentedFrames) throws InvalidFrameException {
    
        super(socket, null, timeout, maxNumberOfFragmentedFrames);
        
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------

    /**
     * Reads a full collection of frames instead of the payload.
     * @return true if a frame(s) has been read or false if nothing was available.
     * @throws InvalidFrameException occurs when the frame(s) is invalid due to an incomplete frame being sent by the client.
     */
    @Override
    public boolean read()
            throws InvalidFrameException {
        
        //Start blocking until indicated otherwise.
        if (isAvailable() || this.initialBlocking || this.blocking) {
            
            this.frames = new ArrayList<Frame>();
            
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

            //Add the first frame.
            this.frames.add(new Frame(frame));
            
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
                        
                        //Reset the frame collection.
                        if (this.frames.size() > 1) {
                            this.frames = new ArrayList<Frame>();
                        }

                        //Add a single frame.
                        this.frames.add(new Frame(this.frame));
                        
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
                    
                    this.frames.add(new Frame(this.frame));
                    
                }
                else  {
                    
                    //Invalid fragmentation.
                    throw new InvalidFrameException(MessageFormat.format("The fragmentation stream contained an invalid non-control frame with the OpCode {0}.", currentFrameType.getValue()));
                    
                }
                
                fragmentCount++;
                
            }
            //END OF while( !this.frame.isFinalFragment() && fragmentCount < this.maxNumberOfFragmentedFrames ) {...
            
            //Blocking has ended unless something is available for future reads.
            this.initialBlocking = false;
            
            //The initial read has been completed.
            return true;
            
        }
        else {
            
            //Nothing is available to read...
            return false;
            
        }
        //END OF if (isAvailable() || this.initialBlocking || this.blocking) {...
        
    }
    
}
