package it.crs4.most.demo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;


public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "SettingsFragment";
    private String[] mRoles;
    private CheckBoxPreference mArEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preferences);

        mRoles = getActivity().getResources().getStringArray(R.array.roles_entries_values);

        EditTextPreference configServerAddr = (EditTextPreference) findPreference("config_server_address");
        configServerAddr.setSummary(configServerAddr.getText());

        EditTextPreference configServerPort = (EditTextPreference) findPreference("config_server_port");
        configServerPort.setSummary(configServerPort.getText());

        mArEnabled = (CheckBoxPreference) findPreference("ar_enabled");

        ListPreference role = (ListPreference) findPreference("role_preference");
        role.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                enabledArPreference((String) newValue);
                return true;
            }
        });

        EditTextPreference deviceID = (EditTextPreference) findPreference("device_id");
        deviceID.setSummary(Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID));

        if (role.getValue() != null) {
            enabledArPreference(role.getValue());
        }
    }

    private void enabledArPreference(String value) {
        if (value.equals(mRoles[0])) {
            mArEnabled.setEnabled(true);
        }
        else {
            mArEnabled.setEnabled(false);
        }
    }
}

