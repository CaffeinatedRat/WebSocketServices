package com.caffeinatedrat.WebSocketServices.Test;

import com.caffeinatedrat.SimpleWebSockets.IApplicationLayer;
import com.caffeinatedrat.SimpleWebSockets.ResponseWrapper;

public class ApplicationLayer implements IApplicationLayer {

    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public ApplicationLayer()
    {
    }
    
    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
        
    public void onTextFrame(String text, ResponseWrapper response) {
//        if(text.equalsIgnoreCase("WHO"))
//        {
//            int maxPlayers = 10;
//            
//            //To-do: Extract into a json formatter.
//            StringBuilder stringBuffer = new StringBuilder();
//            stringBuffer.append("{");
//            stringBuffer.append(MessageFormat.format("\"MaxPlayers\": \"{0}\",", maxPlayers));
//            stringBuffer.append("\"Players\": [");
//            
//            for(int i = 0; i < maxPlayers; i++)
//            {
//                if(i > 0) stringBuffer.append(",");
//                stringBuffer.append("{");
//                stringBuffer.append(MessageFormat.format("\"name\": \"player{0}\", \"onlineTime\": \"{1}s\"", i, Calendar.getInstance().getTimeInMillis()));
//                stringBuffer.append("}");
//            }
//            
//            stringBuffer.append("]}");
//            
//            response.data = stringBuffer.toString();
//            response.closeConnection = true;
//        }
//        else if(text.equalsIgnoreCase("VERSION")){
//            
//            //To-do: Extract into a json formatter.
//            StringBuilder stringBuffer = new StringBuilder();
//            
//            stringBuffer.append("{");
//            stringBuffer.append("\"version\": \"1.0.0.0\", \"bukkit-version\": \"1.0.0.0\"");
//            stringBuffer.append("}");
//
//            response.data = stringBuffer.toString();
//            response.closeConnection = true;
//        }
    }

    public void onBinaryFrame(byte[] data, ResponseWrapper response) {
        // TODO Auto-generated method stub

    }

    public void onClose() {
        // TODO Auto-generated method stub

    }

    public void onPing(byte[] data) {
        // TODO Auto-generated method stub

    }

    public void onPong() {
        // TODO Auto-generated method stub

    }

}
