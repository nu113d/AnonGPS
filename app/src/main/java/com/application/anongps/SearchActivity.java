package com.application.anongps;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class SearchActivity extends AppCompatActivity {
    private ArrayList<String> namesList;
    private ArrayAdapter<String> namesAdapter;
    private ListView listView;
    private EditText nameText, idText;
    private TextView noDevicesText;
    private LocalDB localDB;
    private String uuid, key, iv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button addButton = findViewById(R.id.addButton);
        Button pasteButton = findViewById(R.id.pasteButton);

        nameText = findViewById(R.id.nameText);
        idText = findViewById(R.id.uidText);
        noDevicesText = findViewById(R.id.noDataText);
        listView = findViewById(R.id.nameListView);

        localDB = new LocalDB(this);
        namesList = new ArrayList<>();
        namesAdapter = new ArrayAdapter<>(this, R.layout.devices_list_layout, namesList);
        listView.setAdapter(namesAdapter);

        loadNames();

        // Register context menu for the ListView items (long-press action)
        registerForContextMenu(listView);

        if (namesList.isEmpty()){
            noDevicesText.setVisibility((View.VISIBLE));
            listView.setVisibility(View.GONE);
        }
        else{
            noDevicesText.setVisibility(View.GONE);
        }

        // add button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idText.getText().toString().trim();
                String name = nameText.getText().toString().trim();

                // Device ID = 32 chars uuid + 64 chars key + 32 chars IV = 128 chars
                if (id.length() == 128) {
                    uuid = id.substring(0, 32);
                    key = id.substring(32, 96);
                    iv = id.substring(96, 128);
                    addName(name);
                    nameText.setText("");
                    idText.setText("");
                }
                else{
                    okAlert("Error", "Invalid ID. Make sure you copied the correct one");
                }
            }
        });

        //paste button
        pasteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //code taken from https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#Paste
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String pasteData = "";

                // If the clipboard doesn't contain data, disable the paste menu item.
                // If it does contain data, decide whether you can handle the data.
                if (!(clipboard.hasPrimaryClip())) {
                    pasteButton.setEnabled(false);
                } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
                    // Disables the paste menu item, since the clipboard has data but it isn't plain text.
                    pasteButton.setEnabled(false);
                } else {
                    // Enables the paste menu item, since the clipboard contains plain text.
                    pasteButton.setEnabled(true);
                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                    pasteData = item.getText().toString();
                    idText.setText(pasteData);
                }
            }
        });

        // Item click listener to open MapActivity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedName = namesList.get(position);
                openMapActivity(selectedName);
            }
        });
    }
//-------------------------end of OnCreate-------------------------//
    private void loadNames() {
        Cursor cursor = localDB.fetchAllNames();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(LocalDB.COLUMN_NAME));
                namesList.add(name);
            } while (cursor.moveToNext());
            cursor.close();
        }
        namesAdapter.notifyDataSetChanged();
    }

    private void addName(String name) {
        if (name.isEmpty()){
            //generate a random name
            Random random = new Random();
            name = "Anon" + (random.nextInt(99999 - 10000 + 1) + 10000);
        }
        long result = localDB.addDevice(uuid, name, key, iv);
        if (result == -1){
            okAlert("Error", "Device ID already exists");
        }
        else{
            namesList.add(name);
            namesAdapter.notifyDataSetChanged();
        }
        listView.setVisibility(View.VISIBLE);
        noDevicesText.setVisibility((View.GONE));
    }

    private void deleteName(String name) {
        namesList.remove(name);
        namesAdapter.notifyDataSetChanged();
        localDB.delete(name);
    }

    private void openMapActivity(String name) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("Name", name);
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.nameListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            String selectedName = namesList.get(info.position);
            menu.setHeaderTitle(selectedName);
            menu.add(0, v.getId(), 0, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals("Delete")) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            String selectedName = namesList.get(info.position);
            deleteName(selectedName);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
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

    private void okAlert(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }
}