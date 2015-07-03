package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import java.io.Serializable;


@SuppressLint("NewApi")
public class SettingsFragment extends PreferenceFragment implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.options);
    }
}


