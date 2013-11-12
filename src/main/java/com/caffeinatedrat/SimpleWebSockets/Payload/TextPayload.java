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

package com.caffeinatedrat.SimpleWebSockets.Payload;

/**
 * An extend payload class to handle text.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class TextPayload extends Payload {

    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Read-Only property that returns the payload as an array of strings.  If the payload is empty the array will be empty, not null.
     * @return The payload as an array of strings.  If the payload is empty the array will be empty, not null.
     */
    public String[] getTextPayload() {

        if (rawPayload != null) {
        
            String[] payload = new String[rawPayload.length];
    
            int count = 0;
            for(byte[] payloadChunk : rawPayload) {
                
                if ( (payloadChunk != null && payloadChunk.length > 0) ) {
                    
                    payload[count++] = new String(payloadChunk, 0, payloadChunk.length);
                    
                }
                
            }
            //END OF for(byte[] payloadChunk : rawPayload) {...
            
            return payload;
            
        }
        //END OF if (rawPayload != null) {...

        //Return a safe empty array rather than null.
        return new String[0];
        
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public TextPayload(byte[][] rawPayload) {
        super(rawPayload);
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Overrides the built in toString method to coalesce the payload data as one contiguous string.
     * NOTE: Only use this method if you expect the payload to be small.
     * @return The payload data as one contiguous string.
     */
    @Override
    public String toString() {
        
        StringBuilder stringbuilder = new StringBuilder();
        
        if (rawPayload != null) {
            
            for(byte[] payloadChunk : rawPayload) {
                
                if ( (payloadChunk != null && payloadChunk.length > 0) ) {
                    
                    stringbuilder.append(new String(payloadChunk, 0, payloadChunk.length));
                    
                }
                
            }
            //END OF for(byte[] payloadChunk : rawPayload) {...
            
            return stringbuilder.toString();
            
        }
        //END OF if (rawPayload != null) {...
        
        //If the payload is empty then we will return an empty string.
        return "";
    }
    
}
