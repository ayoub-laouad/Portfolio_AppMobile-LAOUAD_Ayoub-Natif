package com.example.portfolioapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BootReceiver", "Received boot event: " + intent.getAction());

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Check location permission
            if (hasLocationPermission(context)) {
                Log.d("BootReceiver", "Starting GPSTrackingService...");
                startGpsService(context);
            } else {
                Log.e("BootReceiver", "Location permission not granted");
            }
        }
    }

    private boolean hasLocationPermission(Context context) {
        // Check both foreground and background permissions (Android 10+)
        boolean foregroundGranted =
                context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean backgroundGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundGranted =
                    context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

        return foregroundGranted && backgroundGranted;
    }

    private void startGpsService(Context context) {
        Intent serviceIntent = new Intent(context, GPSTrackingService.class);

        // For Android 8+, use startForegroundService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        Log.d("BootReceiver", "Service started");
    }
}
