package com.application.anongps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ClipboardManager;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class TrackActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    String[] frequency = { "30 seconds", "1 minute", "2 minutes", "5 minutes", "10 minutes", "30 minutes"};
    String selectedFrequency; //the above selection of the user (saved in Preferences)
    int updateInterval; //converted frequency to ms (passed to location service)
    String key, iv, uuid;
    String deviceID;
    Encryptor encryptor;
    boolean locationPermissionGranted = false;
    boolean delRecords = false;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button copyBtn = (Button) findViewById(R.id.copyBtn);
        Button shareBtn = (Button) findViewById(R.id.shareBtn);
        Button resetBtn = (Button) findViewById(R.id.resetBtn);
        Button help1Btn = (Button) findViewById(R.id.help1);
        Button qrBtn = (Button) findViewById(R.id.QRBtn);
        TextView idTxtView = (TextView) findViewById(R.id.IdText);
        Spinner spin = (Spinner) findViewById(R.id.timeSpinner);
        Switch masterSwitch = findViewById(R.id.masterSwitch);
        Switch recordSwitch = findViewById(R.id.recordSwitch);
        Group idViewsGroup = findViewById(R.id.group);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("com.application.anongps", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        spin.setOnItemSelectedListener(this);

        //check permissions
        getLocationPermission();

        //check if gps is enabled
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
        //check if service is already running
        Intent mainIntent = getIntent();  //intent from MainActivity
        boolean isServiceRunning = mainIntent.getBooleanExtra("SWITCH ON", false);
        if(isServiceRunning){
            masterSwitch.setChecked(true);
        }

        //load preferences
        key = pref.getString("Key", null);
        iv = pref.getString("IV", null);
        uuid = pref.getString("uuid", null);
        selectedFrequency = pref.getString("Update Interval", "30 seconds");
        delRecords = pref.getBoolean("del Records", false);
        deviceID = uuid+key+iv;

        if(delRecords){
            recordSwitch.setChecked(true);
        }

        //populate timeSpinner
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,frequency);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        if (!selectedFrequency.equals("30 minutes")){
            int spinnerPosition = adapter.getPosition(selectedFrequency);
            spin.setSelection(spinnerPosition);
        }

        if (masterSwitch.isChecked()) {
            showIdViews(idViewsGroup, idTxtView, spin, deviceID, recordSwitch);
        } else {
            hideIdViews(idViewsGroup, idTxtView, spin, recordSwitch);
        }

        //copy id button
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textToCopy = idTxtView.getText().toString();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", textToCopy);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(), "ID copied to clipboard", Toast.LENGTH_SHORT).show();

            }
        });
        //share button
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = uuid+key+iv;
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share using"));

            }
        });
        //qr button
        qrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent qrIntent = new Intent(getApplicationContext(), QRActivity.class);
                qrIntent.putExtra("ID", deviceID);
                startActivity(qrIntent);
            }
        });

        //delete records help button
        help1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                okAlert("Help", "If enabled, your location data will be deleted from the database when location is not shared. Others won't be able to find you at all." +
                        "If disabled, your data will be deleted automatically after 2 days of inactivity", true);
            }
        });

        //reset keys button
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(TrackActivity.this);
                builder.setMessage("By resetting your cryptographic keys the GPS service will stop and all the devices tracking your location will need to be paired again. Reset?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick( final DialogInterface dialog, final int id) {
                                masterSwitch.setChecked(false);
                                editor.remove("Key");
                                editor.remove("IV");
                                editor.commit();
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();

            }
        });
        //master switch listener
        masterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    //check loaded preference values
                    if(key == null){ //if no keys exist
                        encryptor = new Encryptor();  //generate new keys
                        key = encryptor.getKey();
                        iv = encryptor.getIV();
                        editor.putString("Key", key);
                        editor.putString("IV", iv);
                    }
                    else{
                        encryptor = new Encryptor(key, iv); // else load saved keys
                    }
                    if(uuid == null){
                        encryptor.genUUID();
                        uuid = encryptor.getUuid();
                        editor.putString("uuid", uuid);
                    }
                    else{
                        encryptor.setUuid(uuid);
                    }
                    editor.putString("Update Interval", selectedFrequency);
                    editor.putBoolean("del Records", delRecords);
                    editor.apply();
                    deviceID = uuid+key+iv;
                    startLocationService(true);
                    showIdViews(idViewsGroup, idTxtView, spin, deviceID, recordSwitch);
                } else {
                    startLocationService(false);
                    hideIdViews(idViewsGroup, idTxtView, spin, recordSwitch);
                }
            }
        });
        //del records switch listener
        recordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                delRecords = isChecked;
            }
        });

    }
    //spinner view methods
    @Override
    public void onItemSelected(AdapterView<?> arg0, View spin, int index, long id) {
        selectedFrequency = frequency[index];
        switch (selectedFrequency){
            case "30 seconds":
                updateInterval = 30 * 1000;  //30 sec
                break;
            case "1 minute":
                updateInterval = 60 * 1000; //1 min
                break;
            case "2 minutes":
                updateInterval = 120 * 1000; //2 min
                break;
            case "5 minutes":
                updateInterval = 300 * 1000; //5 min
                break;
            case "10 minutes":
                updateInterval = 600 * 1000; //10 min
                break;
            case "30 minutes":
                updateInterval = 1800 * 1000; //30 min
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {}
    @Override
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

    private void startLocationService(boolean start){
        Intent serviceIntent = new Intent(this, LocationService.class);
        serviceIntent.putExtra("ENCRYPTOR", encryptor);
        serviceIntent.putExtra("INTERVAL", updateInterval);
        serviceIntent.putExtra("DEL", delRecords);
        if(start){
            TrackActivity.this.startForegroundService(serviceIntent);
        }
        else{
            stopService(serviceIntent); //calls onDestroy of LocationService
        }

    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }
    //callback of requestPermissions()
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted
                locationPermissionGranted = true;
            } else {
                // Location permission denied
                okAlert("Location Permission Required" ,
                        "This app obviously requires location permission to function properly so please grant location access. Don't worry, your location is always end-to-end encrypted", false);
            }
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick( final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void okAlert(String title, String message, boolean isHelp){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(!isHelp){
                    finish();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }

    private void showIdViews(Group idViewsGroup, TextView idTxt, Spinner spin, String deviceID, Switch switchRecord) {
        spin.setEnabled(false);
        switchRecord.setEnabled(false);
        idTxt.setText(deviceID);
        idViewsGroup.setVisibility(View.VISIBLE);
    }

    private void hideIdViews(Group idViewsGroup, TextView idTxt, Spinner spin, Switch switchRecord) {
        spin.setEnabled(true);
        switchRecord.setEnabled(true);
        idViewsGroup.setVisibility(View.GONE);
    }
}