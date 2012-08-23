package com.caffeinatedrat.WebSocketServices.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.caffeinatedrat.SimpleWebSockets.Server;
import com.caffeinatedrat.SimpleWebSockets.Util.Logger;

public class TestServer {

    public static void start()
    {
        Server server = new Server(4000, new com.caffeinatedrat.WebSocketServices.Test.ApplicationLayer());
        server.start();
        
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        String commandLine = "";
        do
        {
            try
            {
                commandLine = bufferRead.readLine();
            }
            catch(IOException io)
            {
                Logger.debug(io.getMessage());
            }
        } while(!commandLine.equalsIgnoreCase("stop"));
        
        server.Shutdown();
    }
}
