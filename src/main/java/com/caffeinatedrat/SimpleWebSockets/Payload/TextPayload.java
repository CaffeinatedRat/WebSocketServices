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
 * An extend payload class to handle text.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class TextPayload extends Payload {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    protected String[] transformedPayload;
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Read-Only property that returns the payload as an array of strings.  If the payload is empty the array will be empty, not null.
     * @return The payload as an array of strings.  If the payload is empty the array will be empty, not null.
     */
    public String[] getTextPayload() {

        return (this.transformedPayload != null) ? this.transformedPayload: new String[0];
        
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public TextPayload(byte[][] payloadFragments) {
        super(payloadFragments);
        
        transform();
        
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Overrides the built in toString method to coalesce the payload data as one contiguous string.
     * TimeComplexity: O(n) --- Where n is the number of fragments.
     * NOTE: Only use this method if you expect the payload to be small.
     * @return The payload data as one contiguous string.
     */
    @Override
    public String toString() {
        
        StringBuilder stringbuilder = new StringBuilder();
        
        if (this.transformedPayload != null) {
            
            for(String fragment : this.transformedPayload) {
                
                stringbuilder.append(fragment);
                
            }
            //END OF for(String fragment : this.transformedPayload) {...
            
            return stringbuilder.toString();
            
        }
        //END OF if (rawPayload != null) {...
        
        //If the payload is empty then we will return an empty string.
        return "";
    }
    
    /**
     * Safely returns a string at the specified depth in the payload.
     * @return a byte array at the specified depth in the payload.
     */
    public String getString(int index) {
        
        if (transformedPayload != null) {
            
            if (index < this.transformedPayload.length) {
                
                return transformedPayload[index];
                
            }
            
        }
        
        //Safely return an empty string.
        return new String();
    }
    
    /**
     * Safely sets a string within the payload depending on the index.
     * @param index The index in the payload.
     * @param string The string to set.
     */
    public void setString(int index, String string) {
        
        if (this.transformedPayload != null) {
            
            if ( (index > 0) && (index < this.transformedPayload.length) ) {
                
                this.transformedPayload[index] = string;
                
            }
            
        }

    }
    
    /**
     * Performs a transformation on the payload type.
     * TimeComplexity: O(n) --- Where n is the number of fragments.
     * @return true if the transformation was successful.
     */
    @Override
    protected void transform() {
        
        //Prevent reuse of transform.
        if (this.transformedPayload != null) {
            return;
        }
        
        if (this.payloadFragments != null) {
            
            transformedPayload = new String[this.payloadFragments.length];
    
            int count = 0;
            for(byte[] fragment : this.payloadFragments) {
                
                if ( (fragment != null && fragment.length > 0) ) {
                    
                    transformedPayload[count++] = new String(fragment, 0, fragment.length);
                    
                }
                
            }
            //END OF for(byte[] payloadChunk : this.payloadFragments) {...
            
        }
        //END OF if (this.payloadFragments != null) {...
        
    }
    
}
