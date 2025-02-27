package com.example.zonealert.Entities;

import java.util.ArrayList;
import java.util.List;

public class MyLocation {
    private double lat;
    private double lon;
    private double altitude;
    private List<Visit> visits;

    public MyLocation() {
        this.visits = new ArrayList<>();
    }

    public double getLat() { return lat; }
    public MyLocation setLat(double lat) { this.lat = lat; return this; }

    public double getLon() { return lon; }
    public MyLocation setLon(double lon) { this.lon = lon; return this; }

    public double getAltitude() { return altitude; }
    public MyLocation setAltitude(double altitude) { this.altitude = altitude; return this; }

    public List<Visit> getVisits() { return visits; }
    public MyLocation setVisits(List<Visit> visits) { this.visits = visits; return this; }
    public MyLocation addVisit(Visit visit){
        visits.add(visit);
        return this;
    }
}


