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

public class MapActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    private String name, uuid, key, iv;
    private Device dev;
    private Decryptor decryptor;
    private RemoteDB remoteDB;
    private LocalDB localDB;
    private ScheduledExecutorService executorService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        executorService = Executors.newScheduledThreadPool(1);

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
        //load data from SQLite db
        loadNameData(name);
        decryptor = new Decryptor(key, iv);
        remoteDB = new RemoteDB(uuid);


        //set User-Agent as needed (https://osmdroid.github.io/osmdroid/Important-notes-on-using-osmdroid-in-your-app.html)
        Context ctx = getApplicationContext();
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        map = (MapView) findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        MapController mMapController = (MapController) map.getController();
        mMapController.setZoom(12);
        GeoPoint point = new GeoPoint(0, 0);
        Marker marker = new Marker(map);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                dev = remoteDB.getData();
                // Update the UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.d("inner run", "ui thread executed");
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
                            point.setCoords(Float.parseFloat(decLat), Float.parseFloat(decLon));
                            mMapController.animateTo(point);
                            marker.setPosition(point);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            map.getOverlays().add(marker);
                        }

                    }
                });

               Log.d("outer run", "thread executed");
            }
        }, 0, 30, TimeUnit.SECONDS);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
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