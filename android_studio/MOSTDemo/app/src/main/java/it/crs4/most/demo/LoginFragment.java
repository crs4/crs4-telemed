package it.crs4.most.demo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.crs4.most.demo.models.User;


public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private TextView mPasswordText;
    private Spinner mUsernameSpinner;
    private ArrayList<User> mUsers; //TODO: change to User and implement an Adapter
    private RemoteConfigReader mRemCfg;
    private String mAccessToken;
    private String mServerIP;
    private Integer mServerPort;
    private String mTaskGroup;
    private ArrayAdapter<User> mUsersAdapter;

    public LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.login_fragment, container, false);
        mUsernameSpinner = (Spinner) v.findViewById(R.id.username_spinner);
        mPasswordText = (TextView) v.findViewById(R.id.passcode_text);

        mUsers = new ArrayList<>();
        mUsersAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown, mUsers);
        mUsersAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        mUsernameSpinner.setAdapter(mUsersAdapter);

        mServerIP = QuerySettings.getConfigServerAddress(getActivity());
        mServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(getActivity()));
        mTaskGroup = QuerySettings.getTaskGroup(getActivity());
        mAccessToken = QuerySettings.getAccessToken(getActivity());

        mRemCfg = new RemoteConfigReader(getActivity(), mServerIP, mServerPort);
        loadUsers();
        return v;
    }

    private void loadUsers() {
        final ProgressDialog loadUserDialog = new ProgressDialog(getActivity());
//        loadingConfigDialog.setTitle("Connecting to the remote server");
        loadUserDialog.setMessage(getString(R.string.loading_users));
        loadUserDialog.setCancelable(false);
        loadUserDialog.setCanceledOnTouchOutside(false);
        loadUserDialog.setMax(10);
        loadUserDialog.show();

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject usersData) {
                final JSONArray users;
                try {
                    boolean success = usersData != null && usersData.getBoolean("success");
                    if (!success) {
                        Log.e(TAG, "No valid users found for this taskgroup");
                        return;
                    }
                    users = usersData.getJSONObject("data")
                        .getJSONArray("applicants");
                }
                catch (JSONException e) {
                    Log.e(TAG, "Error loading user information");
                    e.printStackTrace();
                    return;
                }

                for (int i = 0; i < users.length(); i++) {
                    User u;
                    try {
                        u = User.fromJSON(users.getJSONObject(i));
                        u.setTaskGroup(mTaskGroup);
                        mUsers.add(u);
                    }
                    catch (TeleconsultationException | JSONException e) {
                        Log.e(TAG, "Error loading user information");
                    }
                }
                mUsersAdapter.notifyDataSetChanged();
                loadUserDialog.dismiss();
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error ");
                loadUserDialog.dismiss();
            }
        };

        mRemCfg.getUsersByTaskgroup(mTaskGroup, listener, errorListener);
    }
}
