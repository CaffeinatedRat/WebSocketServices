/**
* Copyright (c) 2015, Ken Anderson <caffeinatedrat at gmail dot com>
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

package com.caffeinatedrat.WebSocketServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Manages a command for a service.  This allows for batch service calls.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class ServiceCommand {

    // ----------------------------------------------
    // Constants
    // ----------------------------------------------
    
    private static final String COMMANDS = "commands";
    private static final String COMMAND = "command";
    private static final String ARGUMENT = "argument";
    
    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private String command = "";
    private String argument = "";
    
    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    public String getCommand() {
        return this.command;
    }
    
    public String getArgument() {
        return this.argument;
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ServiceCommand(String command, String argument) {
        
        this.command = (command == null) ? "" : command;
        this.argument = (argument == null) ? "" : argument;
        
    }
    
    public ServiceCommand(String commandString) {
        
        //Normalize the command & argument fields.
        commandString = commandString.replaceAll("\"(?i)command\"", "\"command\"").replaceAll("\"(?i)arguments\"", "\"arguments\"");
        
        try {
            
            JSONParser parser = new JSONParser();
            @SuppressWarnings("rawtypes")
            Map jsonResults = (Map)parser.parse(commandString);
                           
            if (jsonResults.containsKey(COMMAND)) {
                                
                this.command = (String)jsonResults.get(COMMAND);
                if (jsonResults.containsKey(ARGUMENT)) {
                    this.argument = (String)jsonResults.get(ARGUMENT);
                }
                
            }
            
        } catch (ParseException e) {

            // --- CR (7/21/13) --- Force the serviceName to lower-case to get rid of all lower-case checking headaches.
            // --- CR (3/3/13) --- Prepare for handling arguments for text services.
            //Separate the service name and arguments from the incoming data.
            String[] tokens = commandString.split(" ", 2);
            String serviceName = tokens[0].toLowerCase();
            
            this.command = serviceName;
            
            if (tokens.length > 1) {
                this.argument = tokens[1];
            }
            
        }
        
    }
    
    public static List<ServiceCommand> parseServiceCommands(String commandString) {
        
        List<ServiceCommand> serviceCommands = new ArrayList<ServiceCommand>();
        
        //Normalize the commands field.
        commandString = commandString.replaceFirst("\"(?i)commands\"", "\"commands\"").replaceAll("\"(?i)command\"", "\"command\"").replaceAll("\"(?i)arguments\"", "\"arguments\"");
        
        try {
            
            JSONParser parser = new JSONParser();
            @SuppressWarnings("rawtypes")
            Map jsonResults = (Map)parser.parse(commandString);
            
            if (jsonResults.containsKey(COMMANDS)) {
                
                JSONArray commands = (JSONArray)jsonResults.get(COMMANDS);
                for(int i = 0; i < commands.size(); i++) {
                    
                    @SuppressWarnings("rawtypes")
                    Map commandPair = (Map)commands.get(i);
                    
                    String command = "";
                    String argument = "";
                    if (commandPair.containsKey(COMMAND)) {
                        
                        command = (String)commandPair.get(COMMAND);
                        if (jsonResults.containsKey(ARGUMENT)) {
                            argument = (String)jsonResults.get(ARGUMENT);
                        }
                        
                    }
                    
                    serviceCommands.add(new ServiceCommand(command, argument));
                }
                
                return serviceCommands;
                
            }
            
        } catch (ParseException e) {

        }
        
        //If we reach this point then the JSON string did not contain the commands field.
        serviceCommands.add(new ServiceCommand(commandString));
        
        return serviceCommands;
        
    }
    
}
