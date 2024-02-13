package com.application.anongps;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

//TrackActivity: Track my device
//SearchActivity: Search for a device
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button trackBtn = (Button) findViewById(R.id.buttonTrack);
        Button searchBtn = (Button) findViewById(R.id.buttonSearch);

        if (isLocationServiceRunning()){
            startTrackActivity(true);
        }

        trackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTrackActivity(false);
            }
        });
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSearchActivity();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        Button trackBtn = (Button) findViewById(R.id.buttonTrack);

        if (isLocationServiceRunning()){
            trackBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startTrackActivity(isLocationServiceRunning());
                }
            });
        }
    }

    private void startTrackActivity(boolean LocServiceRunning) {
        Intent intent = new Intent(this, TrackActivity.class);
        intent.putExtra("SWITCH ON", LocServiceRunning);
        startActivity(intent);
    }
    private void startSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.application.anongps.LocationService".equals(service.service.getClassName())) {
                Log.d("TrackActivity", "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d("TrackActivity", "isLocationServiceRunning: location service is not running.");
        return false;
    }
}