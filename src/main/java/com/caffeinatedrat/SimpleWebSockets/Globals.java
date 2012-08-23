package com.caffeinatedrat.SimpleWebSockets;

public class Globals {
    
    // ----------------------------------------------
    // Constants
    // ----------------------------------------------
    public static final int READ_CHUNK_SIZE = 1024;
    
    // ----------------------------------------------
    // Global Vars.
    // ----------------------------------------------
    public static int debugLevel = 3;   

    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    public static boolean isDebug() { return (debugLevel > 0); }
    public static boolean isVerboseDebug() { return ((debugLevel & 0x03) == 0x03); }
}
