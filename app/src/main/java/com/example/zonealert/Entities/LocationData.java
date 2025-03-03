package com.example.zonealert.Entities;

public class LocationData {

    private int totalVisits;
    private long totalDuration;
    private double averageDuration;

    public LocationData(int totalVisits, long totalDuration, double averageDuration) {
        this.totalVisits = totalVisits;
        this.totalDuration = totalDuration;
        this.averageDuration = averageDuration;
    }

    //for new visit in a location
    public void addVisit(long duration) {
        totalVisits++;
        totalDuration += duration;
        averageDuration = (double)totalDuration/totalVisits;
    }

    //in case the user is still in the same location the duration will be updated
    public void updateLastVisitDuration(long oldDuration, long newDuration) {
        totalDuration = totalDuration - oldDuration + newDuration;
        averageDuration = (double)totalDuration/totalVisits;
    }

    public int getTotalVisits() {
        return totalVisits;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public double getAverageDuration() {
        return averageDuration;
    }

}
