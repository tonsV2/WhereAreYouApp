package com.snot.whereareyou.database;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by snot on 6/3/13.
 */
public class History {

    // SQL convention says Table name should be "singular", so not Exercises
    public static final String TABLE_NAME = "History";
    // Naming the id column with an underscore is good to be consistent
    // with other Android things. This is ALWAYS needed
    public static final String COL_ID = "_id";
    // These fields can be anything you want.
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";
    public static final String COL_PHONE_NUMBER = "phone_number";
    public static final String COL_TIMESTAMP = "ts";

    // For database projection so order is consistent
    public static final String[] FIELDS = {
    	COL_ID,
	COL_LATITUDE,
	COL_LONGITUDE,
	COL_PHONE_NUMBER,
	COL_TIMESTAMP
	};

    /*
     * The SQL code that creates a Table for storing Exercises in.
     * Note that the last row does NOT end in a comma like the others.
     * This is a common source of error.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COL_ID + " INTEGER PRIMARY KEY,"
                    + COL_LATITUDE + " TEXT NOT NULL,"
                    + COL_LONGITUDE + " TEXT NOT NULL,"
                    + COL_PHONE_NUMBER + " TEXT NOT NULL,"
                    + COL_TIMESTAMP + " TIMESTAMP NOT NULL default current_timestamp"
                    + ")";

    // Fields corresponding to database columns
    public long id = -1;
    public String latitude = "";
    public String longitude = "";
    public String phoneNumber = "";
    public String timestamp = "";

    /**
     * No need to do anything, fields are already set to default values above
     */
    public History() {
    }

    /**
     * Convert information from the database into a Exercise object.
     */
    public History(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.id = cursor.getLong(0);
        this.latitude = cursor.getString(1);
        this.longitude = cursor.getString(2);
        this.phoneNumber = cursor.getString(3);
        this.timestamp = cursor.getString(4);
    }

    /**
     * Return the fields in a ContentValues object, suitable for insertion
     * into the database.
     */
    public ContentValues getContent() {
        final ContentValues values = new ContentValues();
        // Note that ID is NOT included here
        values.put(COL_LATITUDE, latitude);
        values.put(COL_LONGITUDE, longitude);
        values.put(COL_PHONE_NUMBER, phoneNumber);
        values.put(COL_TIMESTAMP, timestamp);

        return values;
    }

}

