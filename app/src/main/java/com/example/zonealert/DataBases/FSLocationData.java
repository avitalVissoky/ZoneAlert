package com.example.zonealert.DataBases;

import static com.example.zonealert.common.utils.generateLocationKey;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.zonealert.Entities.LocationData;
import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;

public class FSLocationData {


    public interface CallBack<T> {
        void res(T res);
    }

    public static void listenForRequestStatus(String userId, Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference requestStatusRef = database.getReference("RequestStatus").child(userId);

        requestStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean requestStatus = snapshot.getValue(Boolean.class);
                if (requestStatus != null && requestStatus) {
                    Log.d("FSLocation", "RequestStatus is TRUE, sending data...");
                    //sendDataToFirebase(userId, context);
                    sendLocationsDataToFirebase(userId,context);
                    // after date sent- reset the flag in the db to false
                    requestStatusRef.setValue(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FSLocation", "Error listening to requestStatus", error.toException());
            }
        });
    }

        public static void sendLocationsDataToFirebase(String userId, Context context) {
        String KEY_LOCATION_DATA = "location_data";
        SharedPreferences prefs = context.getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonLocations = prefs.getString(KEY_LOCATION_DATA, "");
        Type type = new TypeToken<HashMap<String, LocationData>>() {}.getType();
        HashMap<String,LocationData>  locationsData = jsonLocations.isEmpty() ? new HashMap<String,LocationData>() : gson.fromJson(jsonLocations, type);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference durationsRef = database.getReference("locationsData").child(userId);

        durationsRef.setValue(locationsData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("FSLocation", "locations data sent successfully.");
                        } else {
                            Log.e("FSLocation", "Failed to send locations data");
                        }
                    });
    }
}
