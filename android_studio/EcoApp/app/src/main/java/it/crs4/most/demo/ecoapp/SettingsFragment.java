package it.crs4.most.demo.ecoapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
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
        addPreferencesFromResource(R.xml.preferences);
        EditTextPreference configServerAddr = (EditTextPreference)
                findPreference("config_server_address");
        configServerAddr.setSummary(configServerAddr.getText());
        EditTextPreference configServerPort = (EditTextPreference)
                findPreference("config_server_port");
        configServerPort.setSummary(configServerPort.getText());
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
        loadingConfigDialog.setCancelable(false);
        loadingConfigDialog.setCanceledOnTouchOutside(false);
        loadingConfigDialog.show();

        configReader.getTaskgroups(
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject taskgroupsData) {
                        try {
                            boolean success = (taskgroupsData != null && taskgroupsData.getBoolean("success"));
                            if (!success) {
                                Log.e(TAG, "No valid taskgroups found for this device");
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

                        } catch (JSONException e) {
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
                        updateTaskGroupPreference();
                    }
                }
        );
    }

    public void updateTaskGroupPreference() {
        mTaskGroupPreference.setEnabled(false);
        mTaskGroupPreference.setEntries(null);
        mTaskGroupPreference.setEntryValues(null);
        mTaskGroupPreference.setValue(null);
//        QuerySettings.setTaskGroup(getActivity(), "");
    }
}

