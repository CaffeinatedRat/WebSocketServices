package com.caffeinatedrat.SimpleWebSockets.Util;

public class Timer {

    private long startTimeInMilliseconds = 0L;
    private long elapsedTimeInMilliseconds = 0L;
    private boolean started = false;
       
    public Timer() { }
    
    public Timer(boolean start)
    {
        if(start)
            start();
    }
    
    /**
     * Starts the timer.
     */        
    public void start()
    {
        this.started = true;
        this.startTimeInMilliseconds = System.currentTimeMillis();
    }

    /**
     * Resets the timer to zero but does not stop it.
     */        
    public void reset()
    {
        this.started = false;
        this.startTimeInMilliseconds = 0L;
    }
    
    /**
     * Stops the timer.
     */            
    public void stop()
    {
        this.started = false;
        this.elapsedTimeInMilliseconds = (System.currentTimeMillis() - this.startTimeInMilliseconds);
    }
    
    /**
     * Returns the time in milliseconds that have elapsed since the timer was started.
     * @return The time in milliseconds that have elapsed since the timer was started.
     */
    public long getElapsedTime()
    {
        return (this.started) ? (System.currentTimeMillis() - this.startTimeInMilliseconds) : elapsedTimeInMilliseconds;
    }
}
