package it.crs4.most.demo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "SettingsFragment";
    private ListPreference mTaskGroupPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preferences);
        EditTextPreference configServerAddr = (EditTextPreference) findPreference("config_server_address");
        configServerAddr.setSummary(configServerAddr.getText());

        EditTextPreference configServerPort = (EditTextPreference) findPreference("config_server_port");
        configServerPort.setSummary(configServerPort.getText());

        EditTextPreference deviceID = (EditTextPreference) findPreference("device_id");
        deviceID.setSummary(Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID));

        mTaskGroupPreference = (ListPreference) findPreference("select_task_group_preference");

        retrieveTaskgroups(QuerySettings.getConfigServerAddress(getActivity()),
                QuerySettings.getConfigServerPort(getActivity()));
        configServerAddr.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                retrieveTaskgroups(newValue.toString(),
                        QuerySettings.getConfigServerPort(getActivity()));
                preference.setSummary(newValue.toString());
                return true;
            }
        });
        configServerPort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(newValue.toString());
                retrieveTaskgroups(QuerySettings.getConfigServerAddress(getActivity()),
                        newValue.toString());
                return true;
            }
        });
    }

    private void retrieveTaskgroups(String addressValue, String portValue) {
        if (addressValue == null) {
            return;
        }
        RemoteConfigReader configReader = new RemoteConfigReader(
                getActivity(),
                addressValue,
                Integer.valueOf(portValue)
        );

        final ProgressDialog loadingConfigDialog = new ProgressDialog(getActivity());
        loadingConfigDialog.setTitle("Connection to the remote server");
        loadingConfigDialog.setMessage("Loading taskgroups associated to this device. Please wait....");
        loadingConfigDialog.setCancelable(true);
        loadingConfigDialog.setCanceledOnTouchOutside(true);
        loadingConfigDialog.setMax(10);
        loadingConfigDialog.show();

        configReader.getTaskgroups(
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject taskgroupsData) {
                        try {
                            boolean success = (taskgroupsData != null && taskgroupsData.getBoolean("success"));
                            if (!success) {
                                Log.e(TAG, "No valid taskgroups found for this device");
                                loadingConfigDialog.dismiss();

                                if (taskgroupsData.getJSONObject("error").getInt("code") == 501) {
                                    showAlertDialog(R.string.device_not_registered);
                                }
                                else {
                                    showAlertDialog(R.string.generic_taskgroup_error);
                                }
                                updateTaskGroupPreference();
                                return;
                            }
                            JSONArray jsonTaskgroups = taskgroupsData.getJSONObject("data").getJSONArray("task_groups");
                            CharSequence[] taskGroups = new CharSequence[jsonTaskgroups.length()];
                            CharSequence[] taskGroupsValues = new CharSequence[jsonTaskgroups.length()];
                            for (int i = 0; i < jsonTaskgroups.length(); i++) {
                                taskGroups[i] = jsonTaskgroups.getJSONObject(i).getString("name");
                                taskGroupsValues[i] = jsonTaskgroups.getJSONObject(i).getString("uuid");
                            }
                            mTaskGroupPreference.setEnabled(true);
                            mTaskGroupPreference.setTitle(getString(R.string.task_group));
                            mTaskGroupPreference.setEntries(taskGroups);
                            mTaskGroupPreference.setEntryValues(taskGroupsValues);
                            getPreferenceScreen().addPreference(mTaskGroupPreference);

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }
                        loadingConfigDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        Log.e(TAG, "Error contacting the configuration server");
                        loadingConfigDialog.dismiss();
                        showAlertDialog(R.string.error_contacting_the_server);
                        updateTaskGroupPreference();
                    }
                }
        );
    }

    private void showAlertDialog(int messageResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).
                setIconAttribute(android.R.attr.alertDialogIcon).
                setTitle(R.string.error).
                setMessage(messageResId).
                setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    public void updateTaskGroupPreference() {
        mTaskGroupPreference.setEnabled(false);
        mTaskGroupPreference.setEntries(null);
        mTaskGroupPreference.setEntryValues(null);
        mTaskGroupPreference.setValue(null);
//        QuerySettings.setTaskGroup(getActivity(), "");
    }


}

