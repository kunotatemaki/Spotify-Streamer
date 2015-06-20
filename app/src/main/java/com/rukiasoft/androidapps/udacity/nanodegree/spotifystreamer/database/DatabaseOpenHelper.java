package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.SpotifyStreamerConstants;

import java.io.Serializable;

public class DatabaseOpenHelper extends SQLiteOpenHelper implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    final private static String TAG = "DatabaseOpenHelper";

    final public static String ID = "id";
    final public static String SAVED_SEARCH = "saved_search";
    final public static String DATE = "date";

    final public static String[] columnsStoredSearches = {ID, SAVED_SEARCH, DATE};

    final private static String CREATE_SEARCHES_CMD = "CREATE TABLE IF NOT EXISTS " + SpotifyStreamerConstants.TABLE_SEARCHES + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SAVED_SEARCH + " TEXT NOT NULL, "
            + DATE + " LONG)";

    final private static Integer VERSION = 1;
    final private Context mContext;


    public DatabaseOpenHelper(Context context) {
        super(context, SpotifyStreamerConstants.DATABASE_NAME, null, VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Log.d(TAG, "onCreate");
        db.execSQL(CREATE_SEARCHES_CMD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior,
                          int versionNueva) {

        db.execSQL("PRAGMA writable_schema = 1;");
        db.execSQL("delete from sqlite_master where type in ('table', 'index', 'trigger');");
        db.execSQL("PRAGMA writable_schema = 0;");
        db.execSQL("VACUUM;");
        db.execSQL("PRAGMA INTEGRITY_CHECK;");

        db.execSQL("DROP TABLE IF EXISTS " + SpotifyStreamerConstants.TABLE_SEARCHES);
        db.execSQL(CREATE_SEARCHES_CMD);

    }

    public void deleteDatabase() {
        //Log.d(TAG, "deleteDatabase");
        mContext.deleteDatabase(SpotifyStreamerConstants.DATABASE_NAME);
    }


    public void createLinksTable() {
        this.getWritableDatabase().execSQL(CREATE_SEARCHES_CMD);
    }


}
