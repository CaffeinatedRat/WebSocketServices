package com.caffeinatedrat.SimpleWebSockets.Util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.caffeinatedrat.SimpleWebSockets.Exceptions.EndOfStreamException;

public class WebSocketsReader extends BufferedInputStream {

    // ----------------------------------------------
    // Member Vars
    // ----------------------------------------------
    
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    
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
        throws IOException, EndOfStreamException
    {
        try
        {
            //Continue to read until the total number of bytes defined in the len parameter have been read.
            int totalLengthRead = 0;         
            int currentLenRead = 0;
            while ( (totalLengthRead < len) && ((currentLenRead = super.read(b, off + totalLengthRead, len - totalLengthRead)) > 0) )
                totalLengthRead += currentLenRead;
            
            if(currentLenRead == -1)
                throw new EndOfStreamException();
        }
        catch(IOException io)
        {
            throw io;
        }
    }
}
