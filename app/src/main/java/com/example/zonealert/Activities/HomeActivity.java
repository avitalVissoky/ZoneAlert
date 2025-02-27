package com.example.zonealert.Activities;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.zonealert.DbControllers.LocationsController;
import com.example.zonealert.R;
import com.example.zonealert.Services.ServiceLocation;
import com.example.zonealert.Entities.MyLocation;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private MaterialButton btnStart ;

    private static final int REQUEST_FINE_LOCATION = 1001;
    private static final int REQUEST_BACKGROUND_LOCATION = 1002;
    private static final int REQUEST_POST_NOTIFICATION = 1003;
    LocationsController locationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        locationController = new LocationsController(this);

        //locationController.clearSPLocation();

        initView();

        requestForegroundLocation();
        requestPostNotification();

        btnStart.setOnClickListener(view -> {
            if (ServiceLocation.isServiceRunningRightRow()) {
                stopMyService();
                btnStart.setText("Start Tracking");
            } else
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            )
            {
                startMyService();
                btnStart.setText("Stop Tracking");
            } else {
                Log.e("MyService", "No permissions ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION and ACCESS_BACKGROUND_LOCATION!");
            }
        });
    }

    // Request foreground location first
    private void requestForegroundLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION);
        } else {
            // Already granted, proceed to check background location
            requestBackgroundLocation();
        }
    }

    private void requestPostNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_POST_NOTIFICATION);
            }
        }
    }

    // Request background location separately
    private void requestBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+ requires this check
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        REQUEST_BACKGROUND_LOCATION);
            }
        }
    }

    // Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Foreground location granted, now request background location
                requestBackgroundLocation();
            } else {
                Toast.makeText(this, "Foreground location permission denied!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_BACKGROUND_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Background location permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Background location permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void initView(){
        btnStart = findViewById(R.id.btnStart);
    }

    private void isServiceRunning(){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE)){
            Log.d("pttt",service.service.getClassName());
        }
    }


    private void stopMyService() {
        serviceAction(ServiceLocation.STOP_FOREGROUND_SERVICE);
    }

    private void serviceAction(String action){
        Intent intent = new Intent(HomeActivity.this, ServiceLocation.class);
        intent.setAction(action);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            startForegroundService(intent);
        }
        else{
            startService(intent);
        }
    }

    private void startMyService() {
        serviceAction(ServiceLocation.START_FOREGROUND_SERVICE);
    }


   @Override
   protected void onStart() {
       super.onStart();
        IntentFilter intentFilter = new IntentFilter(ServiceLocation.BROADCAST_NEW_LOCATION_DETECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,intentFilter);
        registerReceiver(myReceiver,intentFilter,Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null&& intent.getAction().equals(ServiceLocation.BROADCAST_NEW_LOCATION_DETECTED)){
                String jsonLocation = intent.getStringExtra(ServiceLocation.BROADCAST_LOCATION_KEY);

                MyLocation myLocation = new Gson().fromJson(jsonLocation, MyLocation.class);
                    locationController.saveLocation(myLocation);
                Log.d("home activity","last location lat: "+myLocation.getLat()+" long: "+myLocation.getLon());
                locationController.listenToRequestLocations();
                SharedPreferences sharedPreferences = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE);
                Map<String, ?> allEntries = sharedPreferences.getAll();
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    Log.d("SharedPreferences", entry.getKey() + ": " + entry.getValue().toString());
                }
            }
        }
    };

}