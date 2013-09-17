package com.snot.whereareyou.database;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by snot on 6/3/13.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static DatabaseHandler singleton;

    public static DatabaseHandler getInstance(final Context context) {
        if (singleton == null) {
            singleton = new DatabaseHandler(context);
        }
        return singleton;
    }

    private static final int DATABASE_VERSION = 1;
    // TODO: find better name...
    private static final String DATABASE_NAME = "whereareyou.sqlite3";

    private final Context context;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Good idea to use process context here
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(History.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    public synchronized History getHistory(final long id) {
        final SQLiteDatabase db = this.getReadableDatabase();
        final Cursor cursor = db.query(History.TABLE_NAME,
                History.FIELDS, History.COL_ID + " IS ?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor == null || cursor.isAfterLast()) {
            return null;
        }

        History item = null;
        if (cursor.moveToFirst()) {
            item = new History(cursor);
        }
        cursor.close();

        return item;
    }

    public synchronized boolean putHistory(final History history) {
        boolean success = false;
        int result = 0;
        final SQLiteDatabase db = this.getWritableDatabase();

        if (history.id > -1) {
            result += db.update(History.TABLE_NAME, history.getContent(),
                    History.COL_ID + " IS ?",
                    new String[] { String.valueOf(history.id) });
        }

        if (result > 0) {
            success = true;
        } else {
            // Update failed or wasn't possible, insert instead
            final long id = db.insert(History.TABLE_NAME, null,
                    history.getContent());

            if (id > -1) {
                history.id = id;
                success = true;
            }
        }
        if(success) {
            notifyProviderOnHistoryChange();
        }
        return success;
    }

    public synchronized int removeHistory(final History history) {
        final SQLiteDatabase db = this.getWritableDatabase();
        final int result = db.delete(History.TABLE_NAME,
                History.COL_ID + " IS ?",
                new String[] { Long.toString(history.id) });

        if (result > 0) {
            notifyProviderOnHistoryChange();
        }
        return result;
    }

    private void notifyProviderOnHistoryChange() {
        context.getContentResolver().notifyChange(Provider.URI_HISTORYS, null, false);
    }
}

