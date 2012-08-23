package com.caffeinatedrat.SimpleWebSockets.Util;

import java.text.MessageFormat;
import java.util.logging.Level;

import com.caffeinatedrat.SimpleWebSockets.Globals;

public class Logger {
    
    private static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("Minecraft");

    public static void severe(String msg)
    {
        LOGGER.log(Level.SEVERE, MessageFormat.format("[WebSocketServices] {0}", msg));
    }
    
    public static void warning(String msg)
    {
        LOGGER.log(Level.WARNING, MessageFormat.format("[WebSocketServices] {0}", msg));
    }
    
    public static void info(String msg)
    {
        LOGGER.log(Level.INFO, MessageFormat.format("[WebSocketServices] {0}", msg));
    }
    
    public static void debug(String msg)
    {
        if(Globals.isDebug())
            LOGGER.log(Level.INFO, MessageFormat.format("[WebSocketServices] {0}", msg));
    }
    
    public static void verboseDebug(String msg)
    {
        if(Globals.isVerboseDebug())
            LOGGER.log(Level.INFO, MessageFormat.format("[WebSocketServices] {0}", msg));
    }    
}