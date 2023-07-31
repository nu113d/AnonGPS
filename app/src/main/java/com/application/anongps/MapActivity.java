package com.application.anongps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    private MapController  mMapController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //set User-Agent as needed (https://osmdroid.github.io/osmdroid/Important-notes-on-using-osmdroid-in-your-app.html)
        Context ctx = getApplicationContext();
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        map = (MapView) findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mMapController = (MapController) map.getController();
        mMapController.setZoom(8);
        GeoPoint gPt = new GeoPoint(51500000, -150000);
        mMapController.setCenter(gPt);
        Marker marker = new Marker(map);
        marker.setPosition(gPt);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
    }
    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }
}