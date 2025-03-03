package com.example.zonealert.Services;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.zonealert.Activities.HomeActivity;
import com.example.zonealert.R;
import com.example.zonealert.Entities.MyLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

public class ServiceLocation extends Service {

    public static final String BROADCAST_NEW_LOCATION_DETECTED= "com.example.zonealert.NEW_LOCATION_DETECTED";
    public static final String BROADCAST_LOCATION_KEY = "BROADCAST_LOCATION_KEY";

    public static final String START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE";
    public static final String STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE";

    public static String CHANNEL_ID = "com.example.zonealert.CHANNEL_ID_FOREGROUND";
    public static String MAIN_ACTION = "com.example.zonealert.Service_back.action.main";
    public static int NOTIFICATION_ID = 127;
    private int lastShownNotificationId = -1;
    private NotificationCompat.Builder notificationBuilder;

    private static boolean isServiceRunningRightRow = false;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private PowerManager.WakeLock wakeLock;
    private PowerManager powerManager;

    public static boolean isServiceRunningRightRow() {
        return isServiceRunningRightRow;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ServiceLocation", "onStartCommand()");

        if (intent == null) {
            stopForeground(true);
            return START_NOT_STICKY;
        }


        String action = intent.getAction();
        if (action.equals(START_FOREGROUND_SERVICE)) {
            if(isServiceRunningRightRow){
                return START_STICKY;
            }
            isServiceRunningRightRow = true;
            notifyToUserForForegroundService();
            startTrackLocation();
            return START_STICKY;
        } else if (action.equals(STOP_FOREGROUND_SERVICE)) {
            stopTrackLocation();
            stopForeground(true);
            stopSelf();
            isServiceRunningRightRow=false;
            return START_NOT_STICKY;
        }


        return START_STICKY;
    }

    private void startTrackLocation(){

        //Keep CPU working
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ZoneAlert:tag");
        wakeLock.acquire();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest.Builder(1000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setIntervalMillis(3000)
                    .setMinUpdateIntervalMillis(1000)
                    .setMaxUpdateDelayMillis(10000)
                    .build();

            // new Google API SDK v11 uses getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.myLooper());
        }
    }

    private LocationCallback locationCallBack = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location lastLocation = locationResult.getLastLocation();
            Log.d("ServiceLocation","locationCallBack");
            String jsonLastMyLocation = "";
            MyLocation myLocation = new MyLocation()
                    .setLat(lastLocation.getLatitude())
                    .setLon(lastLocation.getLongitude())
                    .setAltitude(lastLocation.getAltitude());

            jsonLastMyLocation = new Gson().toJson(myLocation);

            notificationBuilder.setContentText("Current lat: "+ String.format("%2f",lastLocation.getLatitude())+
                    "\nCurrent lon: "+String.format("%2f",lastLocation.getLongitude()));
            final NotificationManager notificationManager = (NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID,notificationBuilder.build());

            Intent intent = new Intent(BROADCAST_NEW_LOCATION_DETECTED);
            intent.putExtra(BROADCAST_LOCATION_KEY, jsonLastMyLocation);
            LocalBroadcastManager.getInstance(ServiceLocation.this).sendBroadcast(intent);
        }

        @Override
        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    };

    private void stopTrackLocation(){
        if(fusedLocationProviderClient != null){
            Task<Void> task = fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
            task.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d("ServiceLocation","stop location callback removed");
                        stopSelf();
                    }else {
                        Log.d("ServiceLocation","stop failed to remove location callback");
                    }
                }
            });
        }
        wakeLock.release();
    }



    private void notifyToUserForForegroundService() {
        // On notification click
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //make the new activity to be the only one. the past activities will not exist anymore
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder = getNotificationBuilder(this,
                CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top

        notificationBuilder
                .setContentIntent(pendingIntent) // Open activity
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_target_zone)
                .setContentTitle("Tracking Location")
                .setContentText("-")
        ;

        Notification notification = notificationBuilder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_LOCATION);
        }else {
            startForeground(NOTIFICATION_ID,notification);
        }

        if (NOTIFICATION_ID != lastShownNotificationId) {
            // Cancel previous notification
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = NOTIFICATION_ID;
    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String notifications_channel_description = "AlertZone app location channel";
        final NotificationManager nm = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(notifications_channel_description);
                nChannel.enableLights(true);
                nChannel.setLightColor(Color.BLUE);

                nm.createNotificationChannel(nChannel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
