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

/**
 * A simple timer class.
 *
 * @version 1.0.0.0
 * @author CaffeinatedRat
 */
public class Timer {

    // ----------------------------------------------
    // Member Vars (fields)
    // ----------------------------------------------
    
    private long startTimeInMilliseconds = 0L;
    private long elapsedTimeInMilliseconds = 0L;
    private boolean started = false;

    // ----------------------------------------------
    // Properties
    // ----------------------------------------------
    
    /**
     * Returns the time in milliseconds that have elapsed since the timer was started.
     * @return The time in milliseconds that have elapsed since the timer was started.
     */
    public long getElapsedTime() {
        return (this.started) ? (System.currentTimeMillis() - this.startTimeInMilliseconds) : elapsedTimeInMilliseconds;
    }
    
    // ----------------------------------------------
    // Constructors
    // ----------------------------------------------
    
    public Timer() {
        
    }

    public Timer(boolean start) {
        if(start) {
            start();
        }
    }

    // ----------------------------------------------
    // Methods
    // ----------------------------------------------
    
    /**
     * Starts the timer.
     */
    public void start() {
        this.started = true;
        this.startTimeInMilliseconds = System.currentTimeMillis();
    }

    /**
     * Resets the timer to zero but does not stop it.
     */
    public void reset() {
        this.started = false;
        this.startTimeInMilliseconds = 0L;
    }

    /**
     * Stops the timer.
     */
    public void stop() {
        this.started = false;
        this.elapsedTimeInMilliseconds = (System.currentTimeMillis() - this.startTimeInMilliseconds);
    }
}
