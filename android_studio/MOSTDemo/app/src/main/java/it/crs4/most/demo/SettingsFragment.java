package it.crs4.most.demo;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import it.crs4.most.demo.eco.CalibrateARActivity;

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "SettingsFragment";
    private String[] mRoles;
    private CheckBoxPreference mArEnabled;
    private ListPreference mArEyes;
    private Preference mCalibrateAR;
    private EditTextPreference mARLowFilter;
//    private Preference mClearCalibration;
    private RESTClient restClient;
    private ListPreference role;
    private  String accessToken;
    private ListPreference cameraResolutionPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preferences);
        accessToken = QuerySettings.getAccessToken(getActivity());


        mArEnabled = (CheckBoxPreference) findPreference("ar_enabled");
        mArEyes = (ListPreference) findPreference("ar_eyes");
        mCalibrateAR = findPreference("ar_calibrate");
        mARLowFilter = (EditTextPreference) findPreference("ar_low_filter_level");
//        mClearCalibration = findPreference("ar_clear_calibration");

        role = (ListPreference) findPreference("role_preference");
        role.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                enabledArPreference((String) newValue);
                return true;
            }
        });

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

        setEnableEyeWearOptions(isEcographist() && mArEnabled.isChecked());
        if (isUserLogged() && Device.isEyeWear()) {

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

//        mClearCalibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                QuerySettings.clearCalibration(getActivity());
//                return true;
//            }
//        });

        cameraResolutionPreference = (ListPreference)this.findPreference("pref_cameraResolution");
        cameraResolutionPreference.setEnabled(mArEnabled.isEnabled() && isEcographist());

        try {
            Camera cam = Camera.open(0);
            Camera.Parameters e = cam.getParameters();
            List previewSizes = e.getSupportedPreviewSizes();
            cam.release();
            String camResolution = this.cameraResolutionPreference.getValue();
            boolean foundCurrentResolution = false;
            CharSequence[] entries = new CharSequence[previewSizes.size()];
            CharSequence[] entryValues = new CharSequence[previewSizes.size()];

            for(int i = 0; i < previewSizes.size(); ++i) {
                int w = ((Camera.Size)previewSizes.get(i)).width;
                int h = ((Camera.Size)previewSizes.get(i)).height;
                entries[i] = w + "x" + h ;
                entryValues[i] = w + "x" + h;
                if(entryValues[i].equals(camResolution)) {
                    foundCurrentResolution = true;
                }
            }

            this.cameraResolutionPreference.setEntries(entries);
            this.cameraResolutionPreference.setEntryValues(entryValues);
            if(!foundCurrentResolution) {
                this.cameraResolutionPreference.setValue(entryValues[0].toString());
                this.cameraResolutionPreference.setSummary(this.cameraResolutionPreference.getEntry());
            }
        } catch (RuntimeException ex) {
            Log.e("CameraPreferences", "buildResolutionListForCameraIndex(): Camera failed to open: " + ex.getLocalizedMessage());
        }



        Preference.OnPreferenceChangeListener arEnablingListener =  new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mARLowFilter.setEnabled((boolean) newValue);
                cameraResolutionPreference.setEnabled((boolean) newValue && isEcographist());
                setEnableEyeWearOptions((boolean) newValue && isEcographist());
                return true;
            }
        };
        mArEnabled.setOnPreferenceChangeListener(arEnablingListener);

        //for initialization
        arEnablingListener.onPreferenceChange(mArEnabled, mArEnabled.isChecked());

        EditTextPreference deviceID = (EditTextPreference) findPreference("device_id");
        deviceID.setSummary(Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID));
//        EditTextPreference calibratedX = (EditTextPreference) findPreference("ar_calibrated_x");
//        EditTextPreference calibratedY = (EditTextPreference) findPreference("ar_calibrated_y");
//        float [] calibration = QuerySettings.getARCalibration(getActivity());
//        calibratedX.setSummary(String.valueOf(calibration[0]));
//        calibratedY.setSummary(String.valueOf(calibration[1]));

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
        if (!mArEnabled.isChecked() && !isEcographist)
            mArEnabled.setChecked(true);

        mARLowFilter.setEnabled(mArEnabled.isChecked());
        setEnableEyeWearOptions(isEcographist && mArEnabled.isChecked());
        cameraResolutionPreference.setEnabled(mArEnabled.isChecked() && isEcographist);

    }

    private boolean isEcographist(){
        if (role.getValue() == null)
            return false;

        return role.getValue().equals(mRoles[0]);
    }

    private void setEnableEyeWearOptions(boolean value){
        boolean enabled = isUserLogged() && Device.isEyeWear() && value;
        mArEyes.setEnabled(enabled);
        mCalibrateAR.setEnabled(enabled);
//        mClearCalibration.setEnabled(enabled);
    }

    private boolean isUserLogged() {
        return accessToken != null;
    }
}

