package com.caffeinatedrat.SimpleWebSockets.Exceptions;

public class InvalidFrameException extends Exception {
    
    /**
     * 
     */
    private static final long serialVersionUID = 8949430179267127830L;

    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------    
    public InvalidFrameException()
    {
        super("This frame is invalid.");
    }
}
