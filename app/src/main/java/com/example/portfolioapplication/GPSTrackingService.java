package com.example.portfolioapplication;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GPSTrackingService extends Service implements LocationListener {
    private static final long MIN_TIME_MS = 0;
    private static final float MIN_DISTANCE_M = 0;
    private LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "location_channel",
                    "Location Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(this, "location_channel")
                    .setContentTitle("Location Tracking")
                    .setContentText("Tracking your location in background")
                    .setSmallIcon(R.drawable.ic_like)
                    .build();

            startForeground(1, notification);
        }


        try {
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.d("GPS", "GPS enabled: " + gpsEnabled); // Add this
            Log.d("GPS", "Network enabled: " + networkEnabled);
            if (!gpsEnabled && !networkEnabled) {
                Log.e("GPS", "No location providers available");
                stopSelf();
                return START_NOT_STICKY;
            }

            if (gpsEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_MS,
                        MIN_DISTANCE_M,
                        this
                );
            }

            if (networkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_MS,
                        MIN_DISTANCE_M,
                        this
                );
            }

            // Get last known location immediately
            Location lastLocation = getLastKnownLocation();
            if (lastLocation != null) {
                saveLocationToDatabase(lastLocation);
            }
        } catch (SecurityException e) {
            Log.e("GPS", "Permission error", e);
            stopSelf();
        }
        Log.d("GPS", "Starting service with permissions granted: " +
                (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED));
        return START_STICKY;
    }


    private Location getLastKnownLocation() {
        Location location = null;
        try {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (SecurityException e) {
            Log.e("GPS", "Last known location error", e);
        }
        if (location != null) {
            Log.d("GPSTrackingService", "Last known location: " + location.getLatitude() + ", " + location.getLongitude());
        } else {
            Log.d("GPSTrackingService", "Last known location is null");
        }
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d("GPSTrackingService", "Location: " + location.getLatitude() + ", " + location.getLongitude());
            saveLocationToDatabase(location);
        } else {
            Log.e("GPSTrackingService", "Location is null");
        }
    }

    private void saveLocationToDatabase(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String timestamp = String.valueOf(System.currentTimeMillis());
        Log.d("Database", "Attempting to save location: "
                + latitude + "," + longitude + " at " + timestamp);

        SQLiteHelper sqliteHelper = new SQLiteHelper(GPSTrackingService.this); // You should pass the context here

        // Save the location in the database using the instance
        sqliteHelper.saveLocation(latitude, longitude, timestamp);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}
