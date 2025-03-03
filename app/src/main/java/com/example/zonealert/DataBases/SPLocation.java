package com.example.zonealert.DataBases;

import static com.example.zonealert.common.utils.generateLocationKey;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.example.zonealert.Entities.LocationData;
import com.example.zonealert.Entities.MyLocation;
import com.example.zonealert.Entities.Visit;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SPLocation {
    private static final String PREFS_NAME = "LocationPrefs";
    private static final String KEY_LOCATIONS = "locations";
    private static final String KEY_LAST_LOCATION = "last_location";
    private static final double MERGE_DISTANCE_THRESHOLD = 100;
    private static final String KEY_LOCATION_DATA = "location_data";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public SPLocation(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    private HashMap<String, LocationData> getLocationDataMap() {
        String json = sharedPreferences.getString(KEY_LOCATION_DATA, "");
        Type type = new TypeToken<HashMap<String, LocationData>>() {}.getType();
        return json.isEmpty() ? new HashMap<>() : gson.fromJson(json, type);
    }

    private void saveLocationDataMap(HashMap<String, LocationData> locationDataMap) {
        sharedPreferences.edit().putString(KEY_LOCATION_DATA, gson.toJson(locationDataMap)).apply();
    }

    public void updateLocation(MyLocation newLocation) {
        HashMap<String, MyLocation> locations = getSavedLocations();
        HashMap<String, LocationData> locationDataMap = getLocationDataMap();
        String newLocationKey ="";

        String lastLocationKey = sharedPreferences.getString(KEY_LAST_LOCATION, null);
        long currentTime = System.currentTimeMillis();
        boolean updated = false;

        // check if current location is in last location
        if (lastLocationKey != null) {
            MyLocation lastLocation = locations.get(lastLocationKey);
            if (lastLocation != null && isWithinRange(lastLocation, newLocation)) {
                // update last location visit duration
                List<Visit> visits = lastLocation.getVisits();
                if (!visits.isEmpty()) {
                    long prevDuration = visits.get((visits.size()-1)).getDuration();
                    visits.get(visits.size() - 1).setDuration(currentTime);
                    long newDuration = visits.get(visits.size()-1).getDuration();

                    LocationData locData = locationDataMap.get(lastLocationKey);
                    if(locData!=null){
                        locData.updateLastVisitDuration(prevDuration,newDuration);
                        locationDataMap.put(lastLocationKey,locData);
                    }
                }
                saveLocations(locations);
                newLocationKey=lastLocationKey;
                updated = true;
            }
        }

        // current location different from last location but already exist or location does not exist yet
        if (!updated) {
            boolean foundExisting = false;
            for (Map.Entry<String, MyLocation> entry : locations.entrySet()) {
                MyLocation loc = entry.getValue();
                if (isWithinRange(loc, newLocation)) {
                    //when the user returns to a location he has been at before.
                    //found existing location matching
                    loc.addVisit(new Visit(currentTime));
                    saveLocations(locations);
                    newLocationKey = entry.getKey();
                    sharedPreferences.edit().putString(KEY_LAST_LOCATION, newLocationKey).apply();
                    LocationData locData = locationDataMap.get(newLocationKey);
                    if(locData!=null){
                        locData.addVisit(0);
                        locationDataMap.put(newLocationKey,locData);
                    }
                    foundExisting = true;
                    break;
                }
            }

            if (!foundExisting) {
                // no existing location matching.. add new location
                newLocation.addVisit(new Visit(currentTime));
                newLocationKey = generateLocationKey(newLocation);
                locations.put(newLocationKey, newLocation);
                saveLocations(locations);
                LocationData newLocData = new LocationData(1,0,0);
                locationDataMap.put(newLocationKey, newLocData);
                sharedPreferences.edit().putString(KEY_LAST_LOCATION, newLocationKey).apply();
            }
        }
        saveLocationDataMap(locationDataMap);
    }

    public HashMap<String,MyLocation> getSavedLocations() {
        String json = sharedPreferences.getString(KEY_LOCATIONS, "");
        Type type = new TypeToken<HashMap<String,MyLocation>>() {}.getType();
        return json.isEmpty() ? new HashMap<String,MyLocation>() : gson.fromJson(json, type);
    }


    private void saveLocations(HashMap<String,MyLocation> locations) {
        sharedPreferences.edit().putString(KEY_LOCATIONS, gson.toJson(locations)).apply();
    }

    private boolean isWithinRange(MyLocation loc1, MyLocation loc2) {
        float[] results = new float[1];
        Location.distanceBetween(loc1.getLat(), loc1.getLon(), loc2.getLat(), loc2.getLon(), results);
        return results[0] < MERGE_DISTANCE_THRESHOLD;
    }

    public void clearSP (){
        sharedPreferences.edit().clear().apply();

    }
}
