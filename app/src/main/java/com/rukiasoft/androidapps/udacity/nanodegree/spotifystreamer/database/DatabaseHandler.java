package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.SpotifyStreamerConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    //DataBase
    private SQLiteDatabase mDB = null;
    private DatabaseOpenHelper mDbHelper;
    private final Context context;
    private final static String TAG = "DatabaseHandler";

    public DatabaseHandler(Context context) {
        this.context = context;
    }

    public long storeRecentSearch(String search){
        //Log.d(TAG, "STOREDATA");
        long result = 0;
        try {
            mDbHelper = new DatabaseOpenHelper(context);
            mDB = mDbHelper.getWritableDatabase();
            if (!tableExists(SpotifyStreamerConstants.TABLE_SEARCHES)) {
                mDbHelper.createLinksTable();
            }
            if(checkIfSearchExists(search) == false){
                ContentValues values = new ContentValues();
                values.put(DatabaseOpenHelper.SAVED_SEARCH, search);
                values.put(DatabaseOpenHelper.DATE, System.currentTimeMillis());
                result = mDB.insert(SpotifyStreamerConstants.TABLE_SEARCHES, null, values);
                if (result < 0)
                    Log.d(TAG, "Error inserting data");

            }
            mDB.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    boolean tableExists(String tableName) {

        Cursor cursor = mDB.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    private Boolean checkIfSearchExists(String search){
        try {
            String column;
            String[] args;
            column = DatabaseOpenHelper.SAVED_SEARCH.concat("=?");
            args = new String[]{search};

            Cursor c = mDB.query(SpotifyStreamerConstants.TABLE_SEARCHES,
                    DatabaseOpenHelper.columnsStoredSearches, column, args, null, null,
                    null);
            String searchSaved = "";
            if (c.moveToFirst()) {
                do {
                    if(c.getString(1) != null)
                        searchSaved = c.getString(1);

                } while (c.moveToNext());
            }
            c.close();
            return (!searchSaved.equals(""));


        } catch (Exception e) {
            //Log.d(TAG, "error en checkIfZipExists");
            return false;
        }
    }

    public List<String> getStoredSearches() {
        List<String> storedSearches = new ArrayList<>();
        try {
            mDbHelper = new DatabaseOpenHelper(context);
            mDB = mDbHelper.getWritableDatabase();
            String column = null;
            String[] args = null;
            String order = DatabaseOpenHelper.DATE + " DESC";


            Cursor c = mDB.query(SpotifyStreamerConstants.TABLE_SEARCHES,
                    DatabaseOpenHelper.columnsStoredSearches, column, args, null, null,
                    order);
            if (c.moveToFirst()) {
                do {
                    String search = c.getString(1);
                    storedSearches.add(search);
                } while (c.moveToNext());
            }
            c.close();
            mDB.close();
        } catch (Exception e) {
            Log.d(TAG, "getPlayerList");
            throw e;
        }
        return storedSearches;
    }

    public void deleteStoredSearches(){
        try {
            mDbHelper = new DatabaseOpenHelper(context);
            mDB = mDbHelper.getWritableDatabase();
            if (!tableExists(SpotifyStreamerConstants.TABLE_SEARCHES)) {
                mDbHelper.createLinksTable();
            }
            mDB.delete(SpotifyStreamerConstants.TABLE_SEARCHES, null, null);
                        mDB.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

