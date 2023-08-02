package com.application.anongps;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.Manifest;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private Encryptor encryptor;
    private RemoteDB db;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private String encLat, encLon, encSpeed, encAlt;
    private int updateInterval;
    private boolean deleteRecords;

    @Override
    public IBinder onBind(Intent intent) {

       return  null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "onLocationResult: got location result.");

                Location location = locationResult.getLastLocation();

                if (location != null) {
                    String lat = String.valueOf(location.getLatitude());
                    String lon = String.valueOf(location.getLongitude());
                    String speed = String.valueOf(location.getSpeed());
                    String alt = String.valueOf(location.getAltitude());
                    String time = String.valueOf(location.getTime());
                    Toast.makeText(getApplicationContext(), lat + " " + lon , Toast.LENGTH_SHORT).show();

                    encLat = encryptor.encrypt(lat);
                    encLon = encryptor.encrypt(lon);
                    encSpeed = encryptor.encrypt(speed);
                    encAlt = encryptor.encrypt(alt);
                    db.saveData(encLat, encLon, encAlt, encSpeed,time);
                }
            }
        };

        String CHANNEL_ID = "channel_01";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "Channel",
                NotificationManager.IMPORTANCE_DEFAULT);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("AnonGPS")
                .setContentText("GPS Service is active").build();

        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        encryptor = (Encryptor) intent.getSerializableExtra("ENCRYPTOR");
        updateInterval = intent.getIntExtra("INTERVAL", 30000);
        deleteRecords = intent.getBooleanExtra("DEL", false);
        db = new RemoteDB(encryptor.getUuid());
        getLocation();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if(deleteRecords){
            db.deleteData(encryptor.getUuid());
        }
        Log.d(TAG, "stopping LocationService.");
        stopSelf();
    }

    private void getLocation() {

        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        //LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, updateInterval)
                .setIntervalMillis(updateInterval)
                .setWaitForAccurateLocation(false)
                .build();


        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }
        Log.d(TAG, "getLocation: getting location information.");
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }

}