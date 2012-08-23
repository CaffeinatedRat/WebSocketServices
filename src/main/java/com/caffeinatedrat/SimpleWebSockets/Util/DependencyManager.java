package com.caffeinatedrat.SimpleWebSockets.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import com.caffeinatedrat.SimpleWebSockets.Globals;

public class DependencyManager {
    
    public static boolean ExtractJar(Object object, String jarName, String destination)
    {
        InputStream inputStream = null;
        try
        {
            //Check if the file exists before we attempt to go through the extraction process.
            File file = new File(destination + "/" + jarName);
            if(!file.exists())
            {
                Logger.info(MessageFormat.format("Unpacking {0}...", jarName));
                
                //Verify the plugin folder exists and create it if it does not.
                File directory = new File(destination);
                if (!directory.exists())
                    if(!directory.mkdirs())
                        throw new IOException(MessageFormat.format("Could not create the directory {0}.", directory.getAbsolutePath()));
                
                //Create the file.
                if(!file.exists())
                    if(!file.createNewFile())
                        throw new IOException(MessageFormat.format("Could not create the file {0}.", file.getAbsolutePath()));
                
                //Get the jar file within the jar file.
                inputStream = object.getClass().getResourceAsStream("/" + jarName);
                FileOutputStream output = new FileOutputStream(file);
                
                //Build the jar file.
                byte[] data = new byte[Globals.READ_CHUNK_SIZE];
                int len = 0;
                while (inputStream.available() > 0)
                {
                    len = inputStream.read(data, 0, Globals.READ_CHUNK_SIZE);
                    output.write(data, 0, len);
                }
            }

            return true;
        }
        catch(IOException io)
        {
            Logger.verboseDebug(io.getMessage());
        }
        finally
        {
            try
            {
                if(inputStream != null)
                    inputStream.close();
            }
            catch(IOException io)
            {
                Logger.verboseDebug(io.getMessage());
            }
        }
        
       return false;
    }
}
