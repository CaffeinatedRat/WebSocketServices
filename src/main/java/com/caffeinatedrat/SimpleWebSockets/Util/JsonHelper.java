/**
* Copyright (c) 2012, Ken Anderson <caffeinatedrat at gmail dot com>
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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

public class JsonHelper {

    public static String serialize(Hashtable<String, Object> collection) {
        
        StringBuilder jsonBuffer = new StringBuilder();
        
        //Integer depth = new Integer(0);
        internalSerialization(collection, jsonBuffer, 0, 10);
        
        return jsonBuffer.toString();
    }
    
    /**
     * Manages the JSON Serialization.
     * @param collection A Hashtable that contains the name of the item that'll contain a value or another Hashtable.
     * @param jsonBuffer The serialized JSON string data thus far.
     * @param depth The current level of recursion.
     * @param maxDepth The maximum recursive depth to prevent a stack overflow. 
     * @return True if serialization was completed, while it is false if the depth exceeds the maxDepth.
     */    
    private static boolean internalSerialization(Hashtable<String, Object> collection, StringBuilder jsonBuffer, int depth, int maxDepth) {
        
        // --- CR (2/7/13) --- We will now log when the maximum depth is exceeded in verbose mode.
        //Add a depth restriction to prevent overflow.
        if (depth > maxDepth) {
            
            Logger.verboseDebug(MessageFormat.format("The maximum recursive depth has been reached. The collection {0} cannot be serialized.", collection.toString()));
            
            return false;
        }
        
        jsonBuffer.append("{");
        
        int i = 0;
        for(Map.Entry<String, Object> element : collection.entrySet() ) {
            
            //Comma delimit multiple items.
            if (i++ > 0) {
                jsonBuffer.append(",");
            }
            
            //Always construct the key.
            jsonBuffer.append(MessageFormat.format("\"{0}\":", element.getKey()));

            if (element.getValue() instanceof Collection) {
                
                try {
                    
                    Collection internalCollection = (Collection)element.getValue();
                    
                    jsonBuffer.append("[");
                    
                    int j = 0;
                    for(Object object : internalCollection) {
                        
                        if (object instanceof Hashtable){
                            
                            /*
                            //Comma delimit multiple items.
                            if (j++ > 0) {
                                
                                jsonBuffer.append(",");
                                
                            }
                            */

                            // --- CR (2/7/13) --- A flag has been added indicating whether the max depth has been reached.
                            // If the maximum depth has been reached then we want to omit the delimiter for this instance.
                            Hashtable<String, Object> internalElement = (Hashtable<String, Object>)object;
                            boolean completed = internalSerialization(internalElement, jsonBuffer, depth + 1, maxDepth);
                            
                            // --- CR (2/7/13) --- A comma delimiter will be added only if internal serialization was successful, did not exceed the max recursive depth, and if it is not the last item int he collection.
                            if ( (completed) && (++j < internalCollection.size()) ) {
                                
                                jsonBuffer.append(",");
                                
                            }
                            
                        }
                    }
                    
                    jsonBuffer.append("]");
                }
                catch(Exception ex) {
                    
                    //Do nothing if the element cannot be formatted.
                }
            }
            else {
                //Wrap string items in double quotes.
                if (element.getValue() instanceof String) {
                    jsonBuffer.append(MessageFormat.format("\"{0}\"", element.getValue()));
                }
                //For most numbers we do not need any formatting or localization but rather the value itself.
                //IE: The long value 65535 should be serialized as 65535 and not 65,535.
                else if (element.getValue() instanceof Number) {
                    
                    // --- CR (3/4/13) --- Support Double & Floats...there is a better way to do this I'm sure.
                    if ( (element.getValue() instanceof Double) || (element.getValue() instanceof Float) ) {
                        jsonBuffer.append(MessageFormat.format("{0}", element.getValue()));
                    }
                    else {
                        jsonBuffer.append(MessageFormat.format("{0,number,#}", element.getValue()));
                    }
                }
                //Handle all other cases.
                else {
                    jsonBuffer.append(MessageFormat.format("{0}", element.getValue()));
                }
                //END OF if (element.getValue() instanceof String) {...
            }
            //END OF if (element.getValue() instanceof Collection) {...
        }
        //END OF for(Map.Entry<String, Object> element : collection.entrySet() ) {...
        
        jsonBuffer.append("}");
        
        return true;
    }
}
