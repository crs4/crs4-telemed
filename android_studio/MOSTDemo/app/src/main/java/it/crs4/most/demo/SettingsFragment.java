package it.crs4.most.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;

import it.crs4.most.demo.eco.AREcoTeleconsultationActivity;
import it.crs4.most.demo.eco.BaseEcoTeleconsultationActivity;
import it.crs4.most.demo.eco.CalibrateARActivity;
import it.crs4.most.demo.eco.EcoTeleconsultationActivity;

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "SettingsFragment";
    private String[] mRoles;
    private CheckBoxPreference mArEnabled;
    private ListPreference mArEyes;
    private Preference mCalibrateAR;
    private EditTextPreference mARLowFilter;
    private Preference mClearCalibration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preferences);

        mRoles = getActivity().getResources().getStringArray(R.array.roles_entries_values);

        EditTextPreference configServerAddr = (EditTextPreference) findPreference("config_server_address");
        configServerAddr.setSummary(configServerAddr.getText());
        configServerAddr.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (QuerySettings.getConfigServerAddress(getActivity()) != null &&
                    !QuerySettings.getConfigServerAddress(getActivity()).equals(newValue)) {
                    resetLogin();
                }
                return true;
            }
        });

        EditTextPreference configServerPort = (EditTextPreference) findPreference("config_server_port");
        configServerPort.setSummary(configServerPort.getText());
        configServerPort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!QuerySettings.getConfigServerPort(getActivity()).equals(newValue)) {
                    resetLogin();
                }
                return true;
            }
        });

        mArEnabled = (CheckBoxPreference) findPreference("ar_enabled");
        mArEyes= (ListPreference) findPreference("ar_eyes");
        mCalibrateAR = findPreference("ar_calibrate");
        mARLowFilter = (EditTextPreference) findPreference("ar_low_filter_level");
        mClearCalibration = findPreference("ar_clear_calibration");

        mCalibrateAR.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i;
                Activity activity = getActivity();
                boolean isArEnabled = QuerySettings.isArEnabled(activity);
                if (isArEnabled) {
                    i = new Intent(activity, CalibrateARActivity.class);
                    activity.startActivity(i);
                }
                return true;
            }
        });

        mClearCalibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                QuerySettings.clearCalibration(getActivity());
                return true;
            }
        });

        mArEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue && Device.isEyeWear()){
                    mArEyes.setEnabled(true);
                    mCalibrateAR.setEnabled(true);
                    mClearCalibration.setEnabled(true);
                }
                mARLowFilter.setEnabled((boolean) newValue);
                return true;
            }
        });


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

    private void resetLogin() {
        QuerySettings.setAccessToken(getActivity(), null);
        QuerySettings.setUser(getActivity(), null);
    }

    private void enabledArPreference(String value) {
        boolean toEnable = value.equals(mRoles[0]);
        mArEnabled.setEnabled(toEnable);
        mARLowFilter.setEnabled(toEnable);
        if (Device.isEyeWear()){
            mArEyes.setEnabled(toEnable);
            mCalibrateAR.setEnabled(toEnable);
            mClearCalibration.setEnabled(toEnable);
        }
    }
}

