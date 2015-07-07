package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.Utilities;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Created by Ruler in 2014.
 */
public class SettingsActivity extends AppCompatActivity {

    @InjectView(R.id.toolbar_settings) Toolbar toolbarSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);
        ButterKnife.inject(this);

        if (toolbarSettings != null) {
            setSupportActionBar(toolbarSettings);
            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(Html.fromHtml("<b>" + getSupportActionBar().getTitle() + "</b>"));
            }
        }

        getFragmentManager().beginTransaction()
            .replace(R.id.settings_fragment_container, new SettingsFragment())
            .commit();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }
}
