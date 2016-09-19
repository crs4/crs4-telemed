package it.crs4.most.demo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.crs4.most.demo.models.User;


public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private static final String USERS = "users";
    private static final int PASSCODE_LEN = 5;

    private TextView mPasswordText;
    private Spinner mUsernameSpinner;
    private ArrayList<User> mUsers;
    private RESTClient mRemCfg;
    private String mAccessToken;
    private String mTaskGroup;
    private ArrayAdapter<User> mUsersAdapter;

    public LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String serverIP = QuerySettings.getConfigServerAddress(getActivity());
        Integer serverPort = Integer.valueOf(QuerySettings.getConfigServerPort(getActivity()));
        mTaskGroup = QuerySettings.getTaskGroup(getActivity());
        mAccessToken = QuerySettings.getAccessToken(getActivity());
        mRemCfg = new RESTClient(getActivity(), serverIP, serverPort);

        View v = inflater.inflate(R.layout.login_fragment, container, false);
        mUsernameSpinner = (Spinner) v.findViewById(R.id.username_spinner);
        mPasswordText = (TextView) v.findViewById(R.id.password_text);
        Button sendPassword = (Button) v.findViewById(R.id.password_button);

        if (savedInstanceState != null && savedInstanceState.containsKey(USERS)) {
            mUsers = (ArrayList<User>) savedInstanceState.getSerializable(USERS);
        }
        else {
            mUsers = new ArrayList<>();
            loadUsers();
        }
        mUsersAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown, mUsers);
        mUsersAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        mUsernameSpinner.setAdapter(mUsersAdapter);

        if (isEco()) {
            mPasswordText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            mPasswordText.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String passcode = mPasswordText.getText().toString();
                    if (passcode.length() == PASSCODE_LEN) {
                        retrieveAccessToken(passcode);
                        mPasswordText.setText("");
                    }
                }
            });
            sendPassword.setVisibility(View.INVISIBLE);
        }
        else {
            sendPassword.setVisibility(View.VISIBLE);
            sendPassword.setEnabled(true);
            sendPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String password = mPasswordText.getText().toString();
                    retrieveAccessToken(password);
                }
            });
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(USERS, mUsers);
    }

    private void loadUsers() {
        final ProgressDialog loadUserDialog = new ProgressDialog(getActivity());
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
                    users = usersData.getJSONObject("data").getJSONArray("applicants");
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
                mPasswordText.setEnabled(true);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadUserDialog.dismiss();
                showError(R.string.server_connection_error);
            }
        };

        mRemCfg.getUsersByTaskgroup(mTaskGroup, listener, errorListener);
    }

    private void retrieveAccessToken(String password) {
        User selectedUser = (User) mUsernameSpinner.getSelectedItem();
        String username = selectedUser.getUsername();
        String grantType = isEco() ? RESTClient.GRANT_TYPE_PINCODE : RESTClient.GRANT_TYPE_PASSWORD;

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Query Response:" + response);
                try {
                    JSONObject jsonresponse = new JSONObject(response);
                    Log.d(TAG, "ACCESS TOKEN: " + jsonresponse.getString("access_token"));
                    String accessToken = jsonresponse.getString("access_token");

                    if (accessToken != null) {
                        QuerySettings.setAccessToken(getActivity(), accessToken);
                        getActivity().finish();
                    }
                    else {
                        showError(R.string.login_error_details);
                    }
                }
                catch (JSONException e) {
                    Log.e(TAG, "error parsing json response: " + e);
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                e.printStackTrace();
                showError(R.string.server_connection_error);
            }
        };

        mRemCfg.getAccessToken(username, mTaskGroup, grantType, password, listener, errorListener);
    }

    private boolean isEco() {
        String role = QuerySettings.getRole(getActivity());
        String[] roles = getResources().getStringArray(R.array.roles_entries_values);
        return role.equals(roles[0]);
    }

    private void showError(int errorMsgId) {
        new AlertDialog.Builder(getActivity())
            .setTitle(R.string.error)
            .setMessage(errorMsgId)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                    dialog.dismiss();
                }
            })
            .create()
            .show();
    }
}
