package com.example.portfolioapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "gps_tracker.db";
    private static final int DB_VERSION = 3;

    public SQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the locations table
        String createTableQuery = "CREATE TABLE IF NOT EXISTS locations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "latitude DOUBLE, " +
                "longitude DOUBLE, " +
                "timestamp TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the table if it exists and recreate it
        db.execSQL("DROP TABLE IF EXISTS locations");
        onCreate(db);
    }

    // Save location into SQLite database
    public void saveLocation(double latitude, double longitude, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("timestamp", timestamp);

        long result = db.insert("locations", null, values);
        if (result == -1) {
            Log.e("Database", "Insert failed");
        } else {
            Log.d("Database", "Insert succeeded, row ID: " + result);
        }
        db.close();
    }


    // Get all locations from the database
    public List<Location> getAllLocations() {
        List<Location> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM locations", null);
        Log.d("Database", "Number of entries in cursor: " + cursor.getCount());
        if (cursor != null) {
            // Check if the cursor has columns
            int latitudeIndex = cursor.getColumnIndex("latitude");
            int longitudeIndex = cursor.getColumnIndex("longitude");
            int timestampIndex = cursor.getColumnIndex("timestamp");

            // Ensure that the columns exist before accessing the data
            if (latitudeIndex != -1 && longitudeIndex != -1 && timestampIndex != -1) {
                while (cursor.moveToNext()) {
                    double latitude = cursor.getDouble(latitudeIndex);
                    double longitude = cursor.getDouble(longitudeIndex);
                    String timestamp = cursor.getString(timestampIndex);
                    locations.add(new Location(latitude, longitude, timestamp));
                }
            } else {
                Log.e("Database", "One or more columns are missing.");
            }

            cursor.close();
        } else {
            Log.e("Database", "Cursor is null, no data found.");
        }

        db.close();
        return locations;
    }

}
