package com.example.zonealert.Entities;

public class Visit {
    private long startTime;
    private long duration;

    public Visit() {}

    public Visit(long startTime) {
        this.startTime = startTime;
        this.duration = 0;
    }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }


    public void setDuration(long currentTime) {
        duration = currentTime-startTime;
    }

    public long getDuration() {
        return duration;
    }
}
