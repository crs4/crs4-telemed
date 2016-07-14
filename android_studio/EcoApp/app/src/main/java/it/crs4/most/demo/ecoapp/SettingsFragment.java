package it.crs4.most.demo.ecoapp;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by vitto on 14/07/16.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
