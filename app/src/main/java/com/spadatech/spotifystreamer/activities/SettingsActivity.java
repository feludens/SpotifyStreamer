package com.spadatech.spotifystreamer.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.spadatech.spotifystreamer.R;

public class SettingsActivity extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
 }

}
