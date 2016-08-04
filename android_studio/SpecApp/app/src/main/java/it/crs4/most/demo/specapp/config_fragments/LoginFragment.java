package it.crs4.most.demo.specapp.config_fragments;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import it.crs4.most.demo.specapp.IConfigBuilder;
import it.crs4.most.demo.specapp.R;
import it.crs4.most.demo.specapp.RemoteConfigReader;
import it.crs4.most.demo.specapp.models.User;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;


public class LoginFragment extends ConfigFragment {

    private static String TAG = "LoginFragment";

    private RemoteConfigReader mConfigReader;
    private String mTaskgroupID;
    private String mUsername;
    private EditText mEditPass = null;
    private EditText mEditUsername;
    private ProgressDialog mLoadingConfigDialog;
    protected String mAccessToken;


    // newInstance constructor for creating fragment with arguments
    public static LoginFragment newInstance(IConfigBuilder config) {
        LoginFragment fragmentFirst = new LoginFragment();
        fragmentFirst.setConfigBuilder(config);
        return fragmentFirst;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);
        mEditPass = (EditText) view.findViewById(R.id.editPassword);
        mEditUsername = (EditText) view.findViewById(R.id.editUsername);
        Button butLogin = (Button) view.findViewById(R.id.butLogin);
        butLogin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                retrieveAccessToken();
            }
        });
        loadRemoteConfig();

        mEditPass.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditPass, 0);

        return view;
    }

    private void loadRemoteConfig() {
        //Toast.makeText(EcoConfigActivity.this, "Connecting to:" + deviceName + "(" + macAddress +")" , Toast.LENGTH_LONG).show();
        mLoadingConfigDialog = new ProgressDialog(getActivity());
        mLoadingConfigDialog.setTitle("Connection to the remote server");
        mLoadingConfigDialog.setMessage("Loading taskgroups associated to this device. Please wait....");
        mLoadingConfigDialog.setCancelable(false);
        mLoadingConfigDialog.setCanceledOnTouchOutside(false);
        mLoadingConfigDialog.show();

        mConfigReader = config.getRemoteConfigReader();
        retrieveTaskgroups();
    }


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

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
            builderSingle.setCancelable(false);
            builderSingle.setIcon(R.drawable.ic_launcher);
            builderSingle.setTitle("Select the taskgroup");
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    getActivity(),
                    android.R.layout.select_dialog_item);

            final JSONArray taskgroups = taskgroups_data.getJSONObject("data").getJSONArray("task_groups");

            for (int i = 0; i < taskgroups.length(); i++) {
                arrayAdapter.add(taskgroups.getJSONObject(i).getString("name"));
            }

            builderSingle.setNeutralButton("cancel",
                    new DialogInterface.OnClickListener() {

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
                        LoginFragment.this.mTaskgroupID = taskgroups.getJSONObject(which).getString("uuid");
                        retrieveUsers(LoginFragment.this.mTaskgroupID);
                        dialog.dismiss();
                    }
                    catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            });
            builderSingle.show();

        }
        catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    private void retrieveUsers(String taskgroupId) {
        mConfigReader.getUsersByTaskgroup(taskgroupId, new Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject users) {
                Log.d(TAG, "Received taskgroup applicants: " + users.toString());
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
        // {"data":{"applicants":[{"lastname":"admin","mUsername":"admin","firstname":"admin"}]},"success":true}
        // String mUsername = users.getJSONObject("data").getJSONArray("applicants").getJSONObject(0).getString("mUsername");
        //	mEditUsername.setText(mUsername);

        try {
            boolean success = (users_data != null && users_data.getBoolean("success"));
            if (!success) {
                Log.e(TAG, "No valid users found for this taskgroup");
            }

            // Alert Dialog for users selection

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    getActivity());
            builderSingle.setIcon(R.drawable.ic_launcher);
            builderSingle.setTitle("Select the user");
            builderSingle.setCancelable(false);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    android.R.layout.select_dialog_item);

            final JSONArray users = users_data.getJSONObject("data").getJSONArray("applicants");

            for (int i = 0; i < users.length(); i++) {
                arrayAdapter.add(String.format("%s %s", users.getJSONObject(i).getString("lastname"), users.getJSONObject(i).getString("firstname")));
            }

            builderSingle.setNeutralButton("cancel",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });


            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {

                        mUsername = users_data.getJSONObject("data").getJSONArray("applicants").getJSONObject(which).getString("username");
                        Log.d(TAG, "Selected user:" + mUsername);

                        mEditUsername.setText(mUsername);
                        mEditUsername.setEnabled(false);
                        dialog.dismiss();
                    }
                    catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            });

            builderSingle.show();

            // -------------------------------------

        }
        catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    private void retrieveAccessToken() {
        String password = mEditPass.getText().toString();
        mConfigReader.getAccessToken(mUsername, password, LoginFragment.this.mTaskgroupID, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Query Response:" + response);
                try {
                    JSONObject jsonresponse = new JSONObject(response);
                    Log.d(TAG, "ACCESS TOKEN: " + jsonresponse.getString("access_token"));
                    mAccessToken = jsonresponse.getString("access_token");

                    if (mAccessToken != null)
                        config.setUser(new User(mUsername, mTaskgroupID, mAccessToken));
                    else showWrongPasswordAlert();

                }
                catch (JSONException e) {
                    Log.e(TAG, "error parsing json response: " + e);
                    e.printStackTrace();
                }


            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error [" + error + "]");
                Log.e(TAG, "Network response: " + error.getMessage());
                mAccessToken = null;
                showWrongPasswordAlert();
            }
        });
    }

    private void showWrongPasswordAlert() {
        AlertDialog.Builder loginErrorAlert = new AlertDialog.Builder(getActivity());
        loginErrorAlert.setTitle("Login Error");
        loginErrorAlert.setMessage("Invalid password.\n Please retry.");
        AlertDialog alert = loginErrorAlert.create();
        alert.show();
    }


    @Override
    public void onShow() {
    }
}