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
 * A simple payload class.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class Payload {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    protected byte[][] rawPayload; 
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Read-Only property that returns the raw payload as a jagged array.
     * @return the raw payload as a jagged array.
     */
    public byte[][] getRawPayload() {
        return this.rawPayload;
    }
    
    /**
     * Read-Only property that returns the depth of the payload.
     * @return the depth of the payload.
     */    
    public int getDepth() {
        
        return (this.rawPayload != null) ? this.rawPayload.length : 0;
        
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public Payload(byte[][] rawPayload) {
        this.rawPayload = rawPayload;
    }

    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Safely returns a byte array at the specified depth in the payload.
     * TODO: Use a compacted symmetrical array to reduce the jagged nature...
     * @return a byte array at the specified depth in the payload.
     */
    public byte[] get(int index) {
        
        if (index < this.rawPayload.length) {
            return this.rawPayload[index];
        }
        
        //Safely return an empty array.
        return new byte[0];
    }
    
    /**
     * Flattens the jagged array into one contiguous byte array.
     * TODO: Use a compacted symmetrical array to reduce the jagged nature...
     * @return one contiguous byte array.
     */
    public byte[] flatten() {
        
        if (this.rawPayload != null) {
        
            //Calculate the length first.
            int length = 0;
            for(int i = 0; i < this.rawPayload.length; i++) {
                
                if (this.rawPayload[i] != null) {
                
                    length += this.rawPayload[i].length;
                    
                }
                
            }
            //END OF for(int i = 0; i < this.rawPayload.length; i++) {...
            
            byte[] flattenedArray = new byte[length];
        
            //Need to keep track of the current count of bytes since this is a non-symmetrical array we cannot calculate the index ahead of time.
            int count = 0;
            for(int i = 0; i < this.rawPayload.length; i++) {
                
                if (this.rawPayload[i] != null) {
                
                    for (int j = 0; j < this.rawPayload.length; j++) {
                    
                        flattenedArray[count++] = this.rawPayload[i][j];

                    }
                    
                }
                //END OF if (this.rawPayload[i] != null) {...
                
            }
            //END OF for(int i = 0; i < this.rawPayload.length; i++) {...
         
            return flattenedArray;
            
        }
        
        //Return a safe empty array rather than null.
        return new byte[0];
        
    }
    
}

