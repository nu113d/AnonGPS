package com.application.anongps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//Shows the map and all the info for the tracking user
public class MapActivity extends AppCompatActivity {
    private MapView map = null;
    private String name, uuid, key, iv;
    private Decryptor decryptor;
    private LocalDB localDB;
    FirebaseDatabase database = FirebaseDatabase.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView nameTxt = (TextView) findViewById(R.id.nameView);
        TextView timeTxt = (TextView) findViewById(R.id.timeView);
        TextView latTxt = (TextView) findViewById(R.id.latTxt);
        TextView lonTxt = (TextView) findViewById(R.id.lonTxt);
        TextView altTxt = (TextView) findViewById(R.id.altTxt);
        TextView speedTxt = (TextView) findViewById(R.id.speedTxt);

        //for whom did this Activity opened
        Intent intent = getIntent();
        name = intent.getStringExtra("Name");
        nameTxt.setText(name);

        localDB = new LocalDB(this);
        loadNameData(name); //load data from SQLite db
        decryptor = new Decryptor(key, iv);

        //set User-Agent as needed (https://osmdroid.github.io/osmdroid/Important-notes-on-using-osmdroid-in-your-app.html)
        Context ctx = getApplicationContext();
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        map = (MapView) findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        MapController mMapController = (MapController) map.getController();
        mMapController.setZoom(12);
        GeoPoint point = new GeoPoint(0, 0);
        Marker marker = new Marker(map);
        marker.setInfoWindow(null);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        DatabaseReference deviceRef = database.getInstance().getReference("devices").child(uuid);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Device dev = dataSnapshot.getValue(Device.class);
                if(dev == null){
                    Toast.makeText(getApplicationContext(), "Device not found" , Toast.LENGTH_SHORT).show();
                    timeTxt.setText("-");
                    latTxt.setText(getString(R.string.Lat, "-"));
                    lonTxt.setText(getString(R.string.Lon, "-"));
                    altTxt.setText(getString(R.string.Alt, "-"));
                    speedTxt.setText(getString(R.string.Speed, "-"));
                }else{
                    //decrypt data
                    long time = Long.parseLong(dev.getTime());
                    String date = timeToDate(time);
                    String decLat = decryptor.decrypt(dev.getLat());
                    String decLon = decryptor.decrypt(dev.getLon());
                    String decAlt = decryptor.decrypt(dev.getAlt());
                    String decSpeed = decryptor.decrypt(dev.getSpeed());

                    //update views
                    timeTxt.setText(date);
                    latTxt.setText(getString(R.string.Lat, decLat));
                    lonTxt.setText(getString(R.string.Lon, decLon));
                    altTxt.setText(getString(R.string.Alt, decAlt));
                    speedTxt.setText(getString(R.string.Speed, decSpeed));

                    //update map
                    //Notice: If the keys have changed we cannot find the new coordinates
                    //So we notify the user, close the map and delete it from the local db
                    try{
                        point.setCoords(Float.parseFloat(decLat), Float.parseFloat(decLon));
                        mMapController.animateTo(point);
                        marker.setPosition(point);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        map.getOverlays().add(marker);
                    }
                    catch (NullPointerException e){
                        Toast.makeText(getApplicationContext(), "Device is using different keys. Please add the new Device ID" , Toast.LENGTH_LONG).show();
                        LocalDB localDb = new LocalDB(getApplicationContext());
                        localDb.delete(name);
                        deviceRef.removeEventListener(this);
                        finish();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error. Cannot connect to database" , Toast.LENGTH_SHORT).show();
            }
        };
        deviceRef.addValueEventListener(postListener);
    }
    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    private void loadNameData(String name) {
        Cursor cursor = localDB.fetchNameData(name);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                 uuid = cursor.getString(cursor.getColumnIndexOrThrow(LocalDB.COLUMN_UUID));
                 key =  cursor.getString(cursor.getColumnIndexOrThrow(LocalDB.COLUMN_KEY));
                 iv =  cursor.getString(cursor.getColumnIndexOrThrow(LocalDB.COLUMN_IV));

            } while (cursor.moveToNext());
            cursor.close();
        }

    }

    private String timeToDate(long time) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }

    //back menu arrow
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}