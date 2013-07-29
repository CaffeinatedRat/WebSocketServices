/**
* Copyright (c) 2012-2013, Ken Anderson <caffeinatedrat at gmail dot com>
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

package com.caffeinatedrat.SimpleWebSockets;

/**
 * A class that maintains globally used data.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class Globals {
    
    // ----------------------------------------------
    // Constants
    // ----------------------------------------------
    public static final int READ_CHUNK_SIZE = 1024;
    public static final String PLUGIN_FOLDER = "plugins/WebSocketServices";
    public static final String WHITE_LIST_FILENAME = "White-list.txt";
    
    // ----------------------------------------------
    // Global Vars.
    // ----------------------------------------------
    public static int debugLevel = 0;

    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    public static boolean isDebug() { return (debugLevel > 0); }
    public static boolean isVerboseDebug() { return ((debugLevel & 0x02) == 0x02); }
    
    // ----------------------------------------------
    // Data Types.
    // ----------------------------------------------
}
