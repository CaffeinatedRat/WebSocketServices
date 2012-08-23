package com.caffeinatedrat.SimpleWebSockets.Exceptions;

public class EndOfStreamException extends Exception {
    
    /**
     * 
     */
    private static final long serialVersionUID = 8949430179267127830L;

    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------    
    public EndOfStreamException()
    {
        super("The end of the stream has been encountered.");
    }
}
