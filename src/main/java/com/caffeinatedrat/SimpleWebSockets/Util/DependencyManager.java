/**
* Copyright (c) 2012, Ken Anderson <caffeinatedrat@gmail.com>
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLClassLoader;

import com.caffeinatedrat.SimpleWebSockets.Globals;

/**
 * A utility class that manages the extractions of jars within the running jar. 
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class DependencyManager {

    public static boolean isJarExtracted(String jarName, String destination) {
        File file = new File(destination + "/" + jarName);
        return file.exists();
    }
    
    // NOTE: Use of the boolean return type may not be necessary due to the exceptions that are thrown during all failures, consider removing this in the future.
    public static boolean extractJar(Object object, String jarName, String destination) {
        
        InputStream inputStream = null;
        
        try {
            //Check if the file exists before we attempt to go through the extraction process.
            File file = new File(destination + "/" + jarName);

            Logger.info(MessageFormat.format("Unpacking {0}...", jarName));
                
            //Verify the plugin folder exists and create it if it does not.
            File directory = new File(destination);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new IOException(MessageFormat.format("Could not create the directory {0}.", directory.getAbsolutePath()));
                }
            }
            
            //Create the file.
            if(!file.exists()) {
                if(!file.createNewFile()) {
                    throw new IOException(MessageFormat.format("Could not create the file {0}.", file.getAbsolutePath()));
                }
            }
            
            //Get the jar file within the jar file.
            inputStream = object.getClass().getResourceAsStream("/" + jarName);
            if(inputStream != null) {
	            try (FileOutputStream output = new FileOutputStream(file)) {
	                //Build the jar file.
	                byte[] data = new byte[Globals.READ_CHUNK_SIZE];
	                int len = 0;
	                while (inputStream.available() > 0) {
	                    len = inputStream.read(data, 0, Globals.READ_CHUNK_SIZE);
	                    output.write(data, 0, len);
	                }
	            }
            }
            else {
            	throw new IOException(MessageFormat.format("The dependency /{0} was not found.", jarName));
            }

            return true;
        }
        catch (IOException io) {
            Logger.severe(io.getMessage());
        }
        finally {
            
            try {
                if(inputStream != null) {
                    inputStream.close();
                }
            }
            catch (IOException io) {
                Logger.severe(io.getMessage());
            }
        }

       return false;
    }
    
//    public static boolean loadJar(String jarPath, String className) {
//    
//        try {
// 
//            URL url = new URL(jarPath);
//            URL[] urls = new URL[]{url};
//            ClassLoader cl = new URLClassLoader(urls);
//            Class cls = cl.loadClass("net.iharder.Base64");
//            
//            return true;
//        }
//        catch (ClassNotFoundException cnfe){
//        
//        }
//        catch (MalformedURLException me) {
//            
//        }
//        
//        return false;
//    }
}
