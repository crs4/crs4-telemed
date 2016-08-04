package it.crs4.most.demo.eco_setup;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.Response;

import it.crs4.most.demo.IConfigBuilder;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RemoteConfigReader;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;


public class EnterPasscodeFragment extends ConfigFragment {

    private static String TAG = "MostViewPager";
    private static int PASSCODE_LEN = 5;

    private View mView;
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
        mView = inflater.inflate(R.layout.fragment_passcode, container, false);
        mEditPass = (EditText) mView.findViewById(R.id.edit_passcode);
        mUserText = (TextView) mView.findViewById(R.id.text_operator_username);

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
        return mView;
    }

    private void retrieveAccessToken(String pincode) {
        String username = getConfigBuilder().getUser().getUsername();
        String taskgroupId = getConfigBuilder().getUser().getTaskGroup().getId();
        Log.d(TAG, "Get access token with pin code: " + pincode);
        getConfigBuilder().getRemoteConfigReader().getAccessToken(username, taskgroupId,
                RemoteConfigReader.GRANT_TYPE_PINCODE, pincode,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Query Response:" + response);
                        try {
                            JSONObject jsonresponse = new JSONObject(response);
                            Log.d(TAG, "ACCESS TOKEN: " + jsonresponse.getString("access_token"));
                            String accessToken = jsonresponse.getString("access_token");

                            if (accessToken != null) {
                                getConfigBuilder().getUser().setAccessToken(accessToken);
                                getConfigBuilder().listPatients();
                            } else {
                                showPinCodeErrorAlert();
                                getConfigBuilder().getUser().setAccessToken(null);
                                getConfigBuilder().listUsers();
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
                        getConfigBuilder().listUsers();
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
    public void onShow() {
        Log.d(TAG, "onShow");
        mUserText.setText(
                String.format("%s %s",
                        getConfigBuilder().getUser().getFirstName(),
                        getConfigBuilder().getUser().getLastName())
        );
        mEditPass.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditPass, InputMethodManager.SHOW_IMPLICIT);
    }
}