/**
* Copyright (c) 2012, Ken Anderson <caffeinatedrat@gmail.com>
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

package com.caffeinatedrat.SimpleWebSockets.Util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.caffeinatedrat.SimpleWebSockets.Exceptions.EndOfStreamException;

/**
 * An extended version of the BufferedInputStream that blocks until the entire length of the stream is read.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class WebSocketsReader extends BufferedInputStream {

    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    /**
     * Instantiates a WebSocketsReader object.
     * 
     * @param in
     * @see java.io.BufferedInputStream(InputStream in, int size)
     */    
    public WebSocketsReader(InputStream in) {
        super(in);
    }

    /**
     * Instantiates a WebSocketsReader object.
     * 
     * @param in
     * @param size
     * @see java.io.BufferedInputStream(InputStream in, int size)
     */     
    public WebSocketsReader(InputStream in, int size) {
        super(in, size);
    }

    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Guarantees that the full number of bytes are read based on the len argument.
     * Setting the SoTimeout value is recommended so that the blocking will fail over n number of seconds.
     * TimeComplexity O(n) -- where n is the number of bytes to read.
     * 
     * @param b destination buffer.
     * @param off offset at which to start storing bytes.
     * @param len maximum number of bytes to read.
     * @throws IOException if this input stream has been closed by invoking its close() method, or an I/O error occurs.
     * @see java.io.BufferedInputStream#read(byte[], int, int)
     */
    public void readFully(byte[] b, int off, int len)
        throws IOException, EndOfStreamException {
        
        try {
            //Continue to read until the total number of bytes defined in the len parameter have been read.
            int totalLengthRead = 0;
            int currentLenRead = 0;
            while ( (totalLengthRead < len) && ((currentLenRead = super.read(b, off + totalLengthRead, len - totalLengthRead)) > 0) ) {
                totalLengthRead += currentLenRead;
            }
            
            if(currentLenRead == -1) {
                throw new EndOfStreamException();
            }
        }
        catch(IOException io) {
            throw io;
        }
    }
}
