package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.Utilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
        //Load countries and codes for ListPreference
        Map<String, String> countriesAndZips = Utilities.readZipCodes(getActivity());
        final ListPreference zipCodes = (ListPreference) findPreference("pref_zipCode");
        CharSequence[] countriesChar = countriesAndZips.keySet().toArray(new CharSequence[0]);
        zipCodes.setEntries(countriesChar);
        List<String> codes = new ArrayList<>(countriesAndZips.values());
        CharSequence[] codesChar = codes.toArray(new CharSequence[codes.size()]);
        zipCodes.setEntryValues(codesChar);


    }
}


