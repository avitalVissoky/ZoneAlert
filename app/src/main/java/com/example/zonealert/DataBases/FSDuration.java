package com.example.zonealert.DataBases;

import static com.example.zonealert.common.utils.generateLocationKey;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;

public class FSDuration {


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
                    sendDurationsToFirebase(userId,context);
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

    public static void sendDurationsToFirebase(String userId, Context context) {
        String KEY_TOTAL_LOCATION_DURATION = "total_location_duration";
        SharedPreferences prefs = context.getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonLocations = prefs.getString(KEY_TOTAL_LOCATION_DURATION, "");
        Type type = new TypeToken<HashMap<String,Long>>() {}.getType();
        HashMap<String,Long>  durations = jsonLocations.isEmpty() ? new HashMap<String,Long>() : gson.fromJson(jsonLocations, type);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference durationsRef = database.getReference("durations").child(userId);

        durationsRef.setValue(durations)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("FSLocation", "durstions sent successfully.");
                        } else {
                            Log.e("FSLocation", "Failed to send durations");
                        }
                    });
    }
}
