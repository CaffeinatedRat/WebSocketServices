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

package com.caffeinatedrat.SimpleWebSockets;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A simple response object for binary data.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class BinaryResponse extends Response {
    
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private Queue<byte[]> data;
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public BinaryResponse() {
        this.data = new LinkedList<byte[]>();
    }
    
    public BinaryResponse(BinaryResponse response) {
        
        if(response == null) {
            throw new NullPointerException();
        }
        
        this.data = new LinkedList<byte[]>();
        
        for(byte[] oldData : response.data) {
            
            byte[] newData = new byte[oldData.length];
            
            for(int i = 0; i < oldData.length; i++) {
                newData[i] = oldData[i];
            }

            this.data.add(newData);
        }
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Removes and returns the byte array at the front of the queue.
     * @return The byte array that was available on the front of the queue.
     */
    public byte[] dequeue() {
        return this.data.poll();
    }

    /**
     * Adds a byte array to the back of the queue.
     * @param data The byte array to add to the queue.
     */
    public void enqueue(byte[] data) {
        this.data.offer(data);
    }
    
    /**
     * Returns, but does not remove, the byte array at the front of the queue.
     * @return The byte array in front of the queue.
     */
    public byte[] front() {
        return this.data.peek();
    }
    
    /**
     * Returns, but does not remove, the byte array at the back of the queue.
     * @return The byte array in the back of the queue.
     */
    public byte[] back() {
        return ((LinkedList<byte[]>)this.data).getLast();
    }
    
    /**
     * Returns true if the queue is empty.
     * @return True if the queue is empty.
     */
    public boolean isEmpty() {
        return this.data.isEmpty();
    }
    
    /**
     * Returns the size of the queue.
     * @return The size of the queue.
     */
    public int size() {
        return this.data.size();
    }
}
