package it.crs4.most.demo.ecoapp.config_fragments;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.Response;

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
    private TextView mUserText;

    // newInstance constructor for creating fragment with arguments
    public static EnterPasscodeFragment newInstance(IConfigBuilder config) {
        EnterPasscodeFragment fragmentFirst = new EnterPasscodeFragment();
        fragmentFirst.setConfigBuilder(config);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passcode, container, false);
        mEditPass = (EditText) view.findViewById(R.id.editPasscode);
        mUserText = (TextView) view.findViewById(R.id.text_operator_username);

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
        return view;
    }

    private void retrieveAccessToken(String pincode) {
        String username = getConfigBuilder().getEcoUser().getUsername();
        String taskgroupId = getConfigBuilder().getEcoUser().getTaskGroup().getId();
        Log.d(TAG, "Get access token with pin code: " + pincode);
        getConfigBuilder().getRemoteConfigReader().getAccessToken(username, pincode, taskgroupId,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Query Response:" + response);
                        try {
                            JSONObject jsonresponse = new JSONObject(response);
                            Log.d(TAG, "ACCESS TOKEN: " + jsonresponse.getString("access_token"));
                            String accessToken = jsonresponse.getString("access_token");

                            if (accessToken != null) {
                                getConfigBuilder().getEcoUser().setAccessToken(accessToken);
                                getConfigBuilder().listPatients();
                            } else {
                                showPinCodeErrorAlert();
                                getConfigBuilder().getEcoUser().setAccessToken(null);
                                getConfigBuilder().listEcoUsers();
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "error parsing json response: " + e);
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error [" + error + "]");
                        showPinCodeErrorAlert();
                        getConfigBuilder().listEcoUsers();
                    }
                });
    }

    private void showPinCodeErrorAlert() {
        AlertDialog.Builder loginErrorAlert = new AlertDialog.Builder(getActivity());
        loginErrorAlert.setTitle(R.string.login_error);
        loginErrorAlert.setMessage(R.string.login_error_details);
        AlertDialog alert = loginErrorAlert.create();
        alert.show();
    }

    @Override
    public void updateConfigFields() {
        mUserText.setText(String.format("%s %s", getConfigBuilder().getEcoUser().getFirstName(),
                getConfigBuilder().getEcoUser().getLastName()));
    }
}