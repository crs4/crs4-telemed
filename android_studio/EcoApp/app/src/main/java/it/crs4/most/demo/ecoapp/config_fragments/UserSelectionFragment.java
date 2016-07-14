package it.crs4.most.demo.ecoapp.config_fragments;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import it.crs4.most.demo.ecoapp.IConfigBuilder;
import it.crs4.most.demo.ecoapp.R;
import it.crs4.most.demo.ecoapp.RemoteConfigReader;
import it.crs4.most.demo.ecoapp.models.EcoUser;
import it.crs4.most.demo.ecoapp.models.TaskGroup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class UserSelectionFragment extends ConfigFragment {
    protected static final String TAG = "UserSelectionFragment";
    // Store instance variables
    private ProgressDialog mLoadingConfigDialog;
    private ArrayList<EcoUser> mEcoArray;
    private ArrayAdapter<EcoUser> mEcoArrayAdapter;
    private RemoteConfigReader mConfigReader;
    private TaskGroup selectedTaskgroup = null;

    // newInstance constructor for creating fragment with arguments
    public static UserSelectionFragment newInstance(IConfigBuilder config, int page, String title) {
        UserSelectionFragment fragment = new UserSelectionFragment();
        fragment.setConfigBuilder(config);
        return fragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.eco_list, container, false);
        mEcoArray = new ArrayList<>();
        mEcoArrayAdapter = new EcoUserArrayAdapter(this, R.layout.eco_row, mEcoArray);

        ListView listView = (ListView) view.findViewById(R.id.operator_list);
        listView.setAdapter(mEcoArrayAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EcoUser selectedUser = mEcoArray.get(position);
                config.setEcoUser(selectedUser);
            }
        });
        loadRemoteConfig();
        return view;
    }

    private void loadRemoteConfig() {
        mLoadingConfigDialog = new ProgressDialog(getActivity());
        mLoadingConfigDialog.setTitle("Connection to the remote server");
        mLoadingConfigDialog.setMessage("Loading taskgroups associated to this device. Please wait....");
        mLoadingConfigDialog.setCancelable(false);
        mLoadingConfigDialog.setCanceledOnTouchOutside(false);
        mLoadingConfigDialog.show();

        mConfigReader = config.getRemoteConfigReader();
        retrieveTaskgroups();
    }

    /**
     * Get the taskgroups associated to this device ID
     */
    private void retrieveTaskgroups() {
        mConfigReader.getTaskgroups(new Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject taskgroups) {
                mLoadingConfigDialog.setMessage("Taskgroups found for this device. Recovering Taskgroup applicants...");
                Log.d(TAG, "Received taskgroups: " + taskgroups.toString());
                mLoadingConfigDialog.dismiss();
                retrieveSelectedTaskgroup(taskgroups);
            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                Log.e(TAG, "Error retrieving the taskgroup: " + arg0);
                mLoadingConfigDialog.setMessage("No taskgroups found for the current device: " + arg0);
                mLoadingConfigDialog.dismiss();
                // [TODO] Handle the error
            }
        });
    }

    /**
     * read the data of the taskgroup selected by the user
     *
     * @param taskgroups_data the json data of the selected taskgroup
     */
    private void retrieveSelectedTaskgroup(JSONObject taskgroups_data) {
        /*{"data":{"task_groups":[
         *         {"description":"CRS4","name":"CRS4","uuid":"hdhtoz6ef4vixu3gk4s62knhncz6tmww"}
    	 *         ]},
    	 *         
    	 *         "success":true}
    	 */
        try {
            boolean success = (taskgroups_data != null && taskgroups_data.getBoolean("success"));
            if (!success) {
                Log.e(TAG, "No valid taskgroups found for this device");
            }

            // Alert Dialog for taskgroup selection
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    getActivity());
            builderSingle.setCancelable(false);
//            builderSingle.setIcon(R.drawable.ic_launcher);
            builderSingle.setTitle(R.string.select_task_group_dialog);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.select_dialog_item);

            final JSONArray taskgroups = taskgroups_data.getJSONObject("data").getJSONArray("task_groups");

            for (int i = 0; i < taskgroups.length(); i++) {
                arrayAdapter.add(taskgroups.getJSONObject(i).getString("name"));
            }

            builderSingle.setNeutralButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        // call the getUsers for the selected taskgroup
                        String taskgroupID = taskgroups.getJSONObject(which).getString("uuid");
                        String taskgroudDesc = taskgroups.getJSONObject(which).getString("description");
                        UserSelectionFragment.this.selectedTaskgroup = new TaskGroup(taskgroupID, taskgroudDesc);
                        retrieveUsers(taskgroupID);
                        dialog.dismiss();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            });

            builderSingle.show();

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * retrieve the applicants associated to a specific taskgroup
     *
     * @param taskgroupId
     */
    private void retrieveUsers(String taskgroupId) {
        mConfigReader.getUsersByTaskgroup(taskgroupId, new Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject users) {
                Log.d(TAG, "Received taskgroup applicants: " + users.toString());
                // {"data":{"applicants":[{"lastname":"admin","username":"admin","firstname":"admin"}]},"success":true}
                retrieveSelectedUser(users);
            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                Log.e(TAG, "Error retrieving the taskgroup users: " + arg0);
                //mLoadingConfigDialog.setMessage("No users found for the selected taskgroup: " + arg0);
                // [TODO] Handle the error
            }
        });
    }

    private void retrieveSelectedUser(final JSONObject users_data) {
        // {"data":{"applicants":[{"lastname":"admin","username":"admin","firstname":"admin"}]},"success":true}
        // String username = users.getJSONObject("data").getJSONArray("applicants").getJSONObject(0).getString("username");
        // editUsername.setText(username);

        try {
            boolean success = (users_data != null && users_data.getBoolean("success"));
            if (!success) {
                Log.e(TAG, "No valid users found for this taskgroup");
                return;
            }

            final JSONArray users = users_data.getJSONObject("data").getJSONArray("applicants");

            for (int i = 0; i < users.length(); i++) {
                String username = users.getJSONObject(i).getString("username");
                String lastname = users.getJSONObject(i).getString("lastname");
                String firstname = users.getJSONObject(i).getString("firstname");
                mEcoArray.add(new EcoUser(firstname, lastname, username, selectedTaskgroup));

            }
            mEcoArrayAdapter.notifyDataSetChanged();

            // -------------------------------------

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

    }

    @Override
    public void updateConfigFields() {

    }

    private class EcoUserArrayAdapter extends ArrayAdapter<EcoUser> {

        public EcoUserArrayAdapter(UserSelectionFragment userSelectionFragment, int textViewResourceId,
                                   List<EcoUser> objects) {

            super(userSelectionFragment.getActivity(), textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getViewOptimize(position, convertView, parent);
        }

        public View getViewOptimize(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.eco_row, null);
                viewHolder = new ViewHolder();
                viewHolder.username = (TextView) convertView.findViewById(R.id.text_operator_username);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            EcoUser ecoUser = getItem(position);
            viewHolder.username.setText(String.format("%s %s", ecoUser.getLastName(), ecoUser.getFirstName()));
            return convertView;
        }

        private class ViewHolder {
            public TextView username;
        }
    }
}