package it.crs4.most.demo.ecoapp.config_fragments;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import it.crs4.most.demo.ecoapp.IConfigBuilder;
import it.crs4.most.demo.ecoapp.R;
import it.crs4.most.demo.ecoapp.RemoteConfigReader;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


public class EnterPasscodeFragment extends ConfigFragment {

    private static String TAG = "MostViewPager";
    private static int PASSCODE_LEN = 5;

    private EditText mEditPass;
    private RemoteConfigReader mConfigReader;
    private TextView mUserText;

    // newInstance constructor for creating fragment with arguments
    public static EnterPasscodeFragment newInstance(IConfigBuilder config, int page, String title) {
        EnterPasscodeFragment fragmentFirst = new EnterPasscodeFragment();
        fragmentFirst.setConfigBuilder(config);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mConfigReader = config.getRemoteConfigReader();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passcode, container, false);
        initializeGUI(view);
        return view;
    }

    private void initializeGUI(View view) {
        this.mEditPass = (EditText) view.findViewById(R.id.editPasscode);
        this.mUserText = (TextView) view.findViewById(R.id.text_operator_username);

        mEditPass.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                String passcode = mEditPass.getText().toString();

                if (passcode.length() == PASSCODE_LEN) {
                    retrieveAccessToken(passcode);
                    mEditPass.setText("");
                }
            }
        });
    }

    private void retrieveAccessToken(String pincode) {
        String username = this.config.getEcoUser().getUsername();
        String taskgroupId = this.config.getEcoUser().getTaskGroup().getId();
        Log.d(TAG, "GET ACCESS TOKEN WITH PIN CODE: " + pincode);
        this.mConfigReader.getAccessToken(username, pincode, taskgroupId, new Listener<String>() {


            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Query Response:" + response);
                try {
                    JSONObject jsonresponse = new JSONObject(response);
                    Log.d(TAG, "ACCESS TOKEN: " + jsonresponse.getString("access_token"));
                    String accessToken = jsonresponse.getString("access_token");

                    if (accessToken != null) {
                        config.getEcoUser().setAccessToken(accessToken);
                        config.listPatients();
                    } else {
                        showPinCodeErrorAlert();
                        config.getEcoUser().setAccessToken(null);
                        config.listEcoUsers();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "error parsing json response: " + e);
                    e.printStackTrace();
                }


            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error [" + error + "]");
                showPinCodeErrorAlert();
                config.listEcoUsers();
            }
        });
    }

    private void showPinCodeErrorAlert() {
        AlertDialog.Builder loginErrorAlert = new AlertDialog.Builder(this.getActivity());
        loginErrorAlert.setTitle("Login Error");
        loginErrorAlert.setMessage("Invalid Pin code.\n Please retry.");
        AlertDialog alert = loginErrorAlert.create();
        alert.show();
    }

    @Override
    public void updateConfigFields() {
        Log.d(TAG, String.format("Config fields for %s %s", config.getEcoUser().getFirstName(), config.getEcoUser().getLastName()));
        this.mUserText.setText(String.format("%s %s", config.getEcoUser().getFirstName(), config.getEcoUser().getLastName()));
    }
}