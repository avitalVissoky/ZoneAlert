package com.example.zonealert.DbControllers;
import android.content.Context;

import com.example.zonealert.DataBases.FSLocationData;
import com.example.zonealert.DataBases.SPLocation;
import com.example.zonealert.Entities.MyLocation;

import java.util.HashMap;

public class LocationsController
{
    private SPLocation spLocation;
    private FSLocationData fsLocationData;
    private HashMap<String,Integer> visitedLocations;
    private Context context;

    public LocationsController(Context context){
        this.context = context;
        spLocation = new SPLocation(context);
        fsLocationData = new FSLocationData();
        visitedLocations = new HashMap<>();
    }

    public void listenToRequestLocations(){
        fsLocationData.listenForRequestStatus("user1",context);
    }

    public HashMap<String,MyLocation> getSavedLocations(){
        return spLocation.getSavedLocations();
    }

    public void saveLocation(MyLocation myLocation){
        spLocation.updateLocation(myLocation);
    }
    public void clearSPLocation(){
        spLocation.clearSP();
    }
}
