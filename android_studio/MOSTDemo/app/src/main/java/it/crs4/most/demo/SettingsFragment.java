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
import android.support.annotation.IntegerRes;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

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
    private RESTClient restClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preferences);
        mArEnabled = (CheckBoxPreference) findPreference("ar_enabled");
        mArEyes= (ListPreference) findPreference("ar_eyes");
        mCalibrateAR = findPreference("ar_calibrate");
        mARLowFilter = (EditTextPreference) findPreference("ar_low_filter_level");
        mClearCalibration = findPreference("ar_clear_calibration");

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

        String configServerIP = QuerySettings.getConfigServerAddress(getActivity());

        final String accessToken = QuerySettings.getAccessToken(getActivity());
        if (accessToken == null) {
            Log.d(TAG, "accessToken null, is user logged?");
            mArEyes.setEnabled(false);
        }
        else {
            mArEyes.setEnabled(true);
            restClient = new RESTClient(getActivity(), configServerIP, Integer.valueOf(configServerPort.getText()));
            mArEyes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    restClient.setARPreferences(
                        accessToken,
                        newValue.toString(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "set AR eyes");
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "Error setting AR eyes");
                            }
                            }
                    );
                    return true;
                }
            });
            restClient.getARPreferences(
                    accessToken,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "get AR eyes");
                            try {
                                JSONObject responseJson = new JSONObject(response);
                                JSONObject data = responseJson.getJSONObject("data");
                                Log.d(TAG, response.toString());
                                String eye = data.getString("eye");
                                mArEyes.setValue(eye);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "Error retrieving AR eyes");
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error retrieving AR eyes");
                        }
                    });
        }


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


        Preference.OnPreferenceChangeListener arEnablingListener =  new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mARLowFilter.setEnabled((boolean) newValue);
                if (Device.isEyeWear()){
                    mArEyes.setEnabled((boolean) newValue);
                    mCalibrateAR.setEnabled((boolean) newValue);
                    mClearCalibration.setEnabled((boolean) newValue);
                }
                return true;
            }
        };
        mArEnabled.setOnPreferenceChangeListener(arEnablingListener);

        //for initialization
        arEnablingListener.onPreferenceChange(mArEnabled, mArEnabled.isChecked());


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

        EditTextPreference calibratedX = (EditTextPreference) findPreference("ar_calibrated_x");
        EditTextPreference calibratedY = (EditTextPreference) findPreference("ar_calibrated_y");

        float [] calibration = QuerySettings.getARCalibration(getActivity());

        calibratedX.setSummary(String.valueOf(calibration[0]));
        calibratedY.setSummary(String.valueOf(calibration[1]));


        if (role.getValue() != null) {
            enabledArPreference(role.getValue());
        }
    }

    private void resetLogin() {
        QuerySettings.setAccessToken(getActivity(), null);
        QuerySettings.setUser(getActivity(), null);
    }

    private void enabledArPreference(String value) {
        boolean isEcographist = value.equals(mRoles[0]);
        mArEnabled.setEnabled(isEcographist);

        if (isEcographist && Device.isEyeWear()) {
            mArEyes.setEnabled(true);
            mCalibrateAR.setEnabled(true);
            mClearCalibration.setEnabled(true);
        }
        else {
            mArEnabled.setChecked(true);
            mARLowFilter.setEnabled(true);

            mArEyes.setEnabled(false);
            mCalibrateAR.setEnabled(false);
            mClearCalibration.setEnabled(false);

        }
    }
}

