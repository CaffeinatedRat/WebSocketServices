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

import java.net.Socket;

import com.caffeinatedrat.SimpleWebSockets.Exceptions.InvalidFrameException;
import com.caffeinatedrat.SimpleWebSockets.Payload.TextPayload;
import com.caffeinatedrat.SimpleWebSockets.Responses.BinaryResponse;

public class FrameWriter {
    
    // ----------------------------------------------
    // Constants
    // ----------------------------------------------
    
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    //private Socket socket;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public FrameWriter(Socket socket) throws InvalidFrameException {
    
        if (socket == null) {
            
            throw new IllegalArgumentException("The socket is invalid (null).");
            
        }
        
        //this.socket = socket;
        
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------

    /**
     * Writes a close frame to the endpoint.
     * @param socket A valid socket.
     * @param message The message to include in the close frame as to why the frame is closing.
     * @throws InvalidFrameException occurs when the frame is invalid due to an incomplete frame being sent by the endpoint.
     */
    public static void writeClose(Socket socket, String message) throws InvalidFrameException {
        
        if ( (socket != null) && (!socket.isClosed()) ) {
        
            //RFC: http://tools.ietf.org/html/rfc6455#section-5.5.1
            Frame closeFrame = new Frame(socket);
            closeFrame.setFinalFragment();
            closeFrame.setOpCode(Frame.OPCODE.CONNECTION_CLOSE_CONTROL_FRAME);
            closeFrame.setPayload(message);
            closeFrame.write();
            
        }
        
    }
    
    /**
     * Writes a binary response frame to the endpoint.
     * @param socket A valid socket.
     * @param response The binary response to write to the endpoint.
     * @throws InvalidFrameException occurs when the frame is invalid due to an incomplete frame being sent by the endpoint.
     */
    public static void writeBinary(Socket socket, BinaryResponse response) throws InvalidFrameException {

        //Keep track of our first frame.
        Boolean firstFrame = true;
        
        //Added basic fragmentation support; however, this puts the burden on the application layer to handle its fragments.
        //RFC: http://tools.ietf.org/html/rfc6455#section-5.4
        while (!response.isEmpty()) {
            
            Frame responseFrame = new Frame(socket);
            
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
     * Writes a text response frame to the endpoint.
     * @param socket A valid socket.
     * @param payload The payload to write.
     * @throws InvalidFrameException occurs when the frame is invalid due to an incomplete frame being sent by the endpoint.
     */
    @SuppressWarnings("unused")
    private static void writeText(Socket socket, TextPayload payload) throws InvalidFrameException {
        
        int payloadDepth = payload.getDepth();
        
        //If there are no fragments then do not write.
        if (payloadDepth == 0) {
            return;
        }
        
        for (int i = 0; i < payloadDepth; i++) {
            
            writeText(socket, payload.getString(i), (i == payloadDepth - 1));
            
        }
        
    }
    
    /**
     * Writes a text response frame to the endpoint.
     * @param socket A valid socket.
     * @param fragment The string value to return the endpoint.
     * @param lastFragment True if this is the last fragment in the payload.
     * @throws InvalidFrameException occurs when the frame is invalid due to an incomplete frame being sent by the endpoint.
     */
    public static void writeText(Socket socket, String fragment, Boolean lastFragment) throws InvalidFrameException {
    
        // --- CR (8/10/13) --- Do not write an empty fragment.
        if (fragment.length() == 0) {
            return;
        }
        
        Boolean firstFrame = true;

        //Fragment the frame if the response is too large.
        //RFC: http://tools.ietf.org/html/rfc6455#section-5.4
        while (fragment.length() > Frame.MAX_PAYLOAD_SIZE) {

            Frame responseFrame = new Frame(socket);
            responseFrame.setOpCode( firstFrame ? Frame.OPCODE.TEXT_DATA_FRAME : Frame.OPCODE.CONTINUATION_DATA_FRAME);
            responseFrame.setPayload(fragment.substring(0, (int)Frame.MAX_PAYLOAD_SIZE));
            responseFrame.write();
            
            fragment = fragment.substring((int)Frame.MAX_PAYLOAD_SIZE);
            
            //No longer the first frame.
            firstFrame = false;
        }

        //Send the final frame.
        Frame responseFrame = new Frame(socket);
        
        if (lastFragment) {
            responseFrame.setFinalFragment();
        }
        
        responseFrame.setOpCode( firstFrame ? Frame.OPCODE.TEXT_DATA_FRAME : Frame.OPCODE.CONTINUATION_DATA_FRAME);
        responseFrame.setPayload(fragment);
        responseFrame.write();
        
    }
    
}
