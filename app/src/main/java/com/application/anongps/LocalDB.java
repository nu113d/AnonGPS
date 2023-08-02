package com.application.anongps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDB extends SQLiteOpenHelper {
     static final String DATABASE_NAME = "Anon";
     static final String TABLE_NAME = "devices";
     static final String COLUMN_UUID = "uuid";
     static final String COLUMN_NAME = "name";
     static final String COLUMN_KEY = "masterKey";
     static final String COLUMN_IV = "iv";

    public LocalDB(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_UUID + " TEXT PRIMARY KEY," +
                COLUMN_NAME + " TEXT, " +
                COLUMN_KEY + " TEXT, " +
                COLUMN_IV + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addDevice(String uuid, String name, String key, String iv) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UUID, uuid);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_KEY, key);
        values.put(COLUMN_IV, iv);
        long insertResult = db.insert(TABLE_NAME, null, values);
        db.close();
        return insertResult;
    }
    public Cursor fetchAllNames() {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = new String[] { LocalDB.COLUMN_NAME };
        Cursor cursor = db.query(LocalDB.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchNameData(String name) {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = new String[] { LocalDB.COLUMN_UUID, LocalDB.COLUMN_KEY, LocalDB.COLUMN_IV };
        Cursor cursor = db.query(LocalDB.TABLE_NAME, columns, LocalDB.COLUMN_NAME + "= '" + name + "'", null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
    public void delete(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(LocalDB.TABLE_NAME, LocalDB.COLUMN_NAME + "= '" + name + "'", null);
    }

}

