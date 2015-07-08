package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.ToolbarAndRefreshActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Raúl Feliz on 2014 for the Udacity Nanodegree.
 */
public class Utilities {
    static private Toast toast = null;

    /**
     * Show Toas. If a Toast is shown, replaces the text instead of creating a new one. In case of multiple Toast
     * in a row, only one with several texts will be shown, instead of a lot of them sequentially;
     */
    public static void showToast(final Context context, final String text) {
        //only show if context is an instance of Activity
        if (!(context instanceof Activity)) {
            return;
        }
        try {
            if (toast != null) {
                if (toast.getView().isShown()) {
                    toast.setText(text);
                    toast.show();
                    return;
                }
            }
            toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Force to hide keyboard if showing
     */
    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String formatSongTime(int seconds){
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public static Boolean getBooleanFromPreferences(Context context, String name) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(name, false);

    }

    public static String getZipFromPreferences(Context context) {
        //Load zip from preferences. If not set, load default locale
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_zipCode", Locale.getDefault().getCountry());

    }

    /**
     * Read the countries and their zip codes from a file stored in assets folder
     */
    public static Map<String, String> readZipCodes(Context context){
        AssetManager assetManager = context.getAssets();
        Map<String, String> map = new HashMap<>();
        map.put("country", Locale.getDefault().getCountry());
        try {
            InputStream csvStream = assetManager.open("iso_3166_2_countries.csv");
            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
            CSVReader csvReader = new CSVReader(csvStreamReader);
            String[] line;

            // throw away the header
            csvReader.readNext();

            while ((line = csvReader.readNext()) != null) {
                map.put(line[1], line[10]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //sorted map
        Map<String, String> treeMap = new TreeMap<>(map);

        return treeMap;
    }

    /**
     * set the refresh layout to be shown in the activity
     */
    public static void setRefreshLayout(Activity activity, SwipeRefreshLayout refreshLayout){
        if(activity instanceof ToolbarAndRefreshActivity) {
            ((ToolbarAndRefreshActivity) activity).setRefreshLayout(refreshLayout);
            ((ToolbarAndRefreshActivity) activity).disableRefreshLayoutSwipe();
        }
    }

    /**
     * set the refresh layout to be shown in the activity only if it doesn't exist
     */
    public static void setRefreshLayoutIfNeeded(Activity activity, SwipeRefreshLayout refreshLayout){
        if((activity instanceof ToolbarAndRefreshActivity) && ((ToolbarAndRefreshActivity) activity).getRefreshLayout() == null) {
            ((ToolbarAndRefreshActivity) activity).setRefreshLayout(refreshLayout);
            ((ToolbarAndRefreshActivity) activity).disableRefreshLayoutSwipe();
        }
    }

    /**
     * set the refresh layout to be shown in the activity
     */
    public static void showRefreshLayout(Activity activity, SwipeRefreshLayout refreshLayout){
        if(activity instanceof ToolbarAndRefreshActivity) {
            ((ToolbarAndRefreshActivity) activity).setRefreshLayout(refreshLayout);
            ((ToolbarAndRefreshActivity) activity).showRefreshLayoutSwipeProgress();
        }
    }

    /**
     * set the refresh layout to be shown in the activity
     */
    public static void hideRefreshLayout(Activity activity, SwipeRefreshLayout refreshLayout){
        if(activity instanceof ToolbarAndRefreshActivity) {
            ((ToolbarAndRefreshActivity) activity).setRefreshLayout(refreshLayout);
            ((ToolbarAndRefreshActivity) activity).hideRefreshLayoutSwipeProgress();
        }
    }



}

