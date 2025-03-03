package com.example.zonealert.common;

import com.example.zonealert.Entities.MyLocation;

public class utils {

    public static String generateLocationKey(double lat, double lon, double alt){

        return String.valueOf(lat).replace(".", "_") + "-" +
                String.valueOf(lon).replace(".", "_");
    }

    public static String generateLocationKey(MyLocation location){
        double lat = location.getLat();
        double lon = location.getLon();
        double alt = location.getAltitude();

        return String.valueOf(lat).replace(".", "_") + "-" +
                String.valueOf(lon).replace(".", "_");
    }
}
