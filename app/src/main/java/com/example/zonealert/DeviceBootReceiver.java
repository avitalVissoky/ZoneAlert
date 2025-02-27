package com.example.zonealert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.zonealert.Services.ServiceLocation;

public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DeviceBootReceiver", "Device boot completed");

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Start the foreground service after boot
            Intent serviceIntent = new Intent(context, ServiceLocation.class);
            context.startService(serviceIntent);
        }
    }
}
