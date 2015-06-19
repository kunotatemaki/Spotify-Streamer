package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by Raúl Feliz on 2014 for the Udacity Nanodegree.
 */
public class Utilities {
    static private Toast toast = null;

    public static void showToast(final Context context, final String text) {

        //only show if context is an instance of Activity
        if (!(context instanceof Activity)) {
            return;
        }
        try {
            if (toast != null) {
                if (toast.getView().isShown()) {
                    toast.setText(text);
                    return;
                }
            }
            toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

