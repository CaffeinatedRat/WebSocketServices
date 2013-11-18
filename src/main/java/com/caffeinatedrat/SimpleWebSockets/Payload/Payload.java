/**
* Copyright (c) 2013, Ken Anderson <caffeinatedrat at gmail dot com>
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
    protected byte[][] payloadFragments; 
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Read-Only property that returns the raw payload as a jagged array.
     * @return the raw payload as a jagged array.
     */
    public byte[][] getRawPayload() {
        return this.payloadFragments;
    }
    
    /**
     * Read-Only property that returns the depth of the payload.
     * @return the depth of the payload.
     */    
    public int getDepth() {
        
        return (this.payloadFragments != null) ? this.payloadFragments.length : 0;
        
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public Payload(byte[][] payloadFragments) {
        this.payloadFragments = payloadFragments;
    }

    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Safely returns a byte array at the specified depth in the payload.
     * @return a byte array at the specified depth in the payload.
     */
    public byte[] get(int index) {
        
        if (index < this.payloadFragments.length) {
            return this.payloadFragments[index];
        }
        
        //Safely return an empty array.
        return new byte[0];
    }
    
    /**
     * Performs a transformation on the payload type.
     * @return true if the transformation was successful.
     */
    protected void transform() {
        return;
    }
}

