package it.crs4.most.demo.setup_fragments;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.Response;

import it.crs4.most.demo.ConfigFragment;
import it.crs4.most.demo.IConfigBuilder;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RemoteConfigReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class EnterCredentialsFragment extends ConfigFragment {

    private static String TAG = "EnterCredentialsFragment";
    private static final String CREDENTIALS_TYPE = "it.crs4.most.demo.credentials_type";
    private static int PASSCODE_LEN = 5;
    public static int PASSCODE_CREDENTIALS = 0;
    public static int PASSWORD_CREDENTIALS = 1;

    private EditText mEditPass;
    private TextView mUserText;
    private int mCredentialsType;

    // newInstance constructor for creating fragment with arguments
    public static EnterCredentialsFragment newInstance(IConfigBuilder config, int credentialsType) {
        EnterCredentialsFragment fragment = new EnterCredentialsFragment();
        Bundle args = new Bundle();
        args.putInt(CREDENTIALS_TYPE, credentialsType);
        fragment.setConfigBuilder(config);
        fragment.setArguments(args);
        return fragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCredentialsType = getArguments().getInt(CREDENTIALS_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_credentials, container, false);
        mEditPass = (EditText) view.findViewById(R.id.edit_passcode);
        mUserText = (TextView) view.findViewById(R.id.text_operator_username);
        Button sendPassword = (Button) view.findViewById(R.id.send_password);
        if (mCredentialsType == PASSCODE_CREDENTIALS) {
            mEditPass.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            mEditPass.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
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
        else {
            sendPassword.setVisibility(View.VISIBLE);
            sendPassword.setEnabled(true);
            sendPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String password = mEditPass.getText().toString();
                    retrieveAccessToken(password);
                }
            });
        }


        return view;
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

    @Override
    public int getTitle() {
        return R.string.enter_pass_code_title;
    }

    private void retrieveAccessToken(String pincode) {
        String username = getConfigBuilder().getUser().getUsername();
        String taskgroupId = getConfigBuilder().getUser().getTaskGroup().getId();
        String grantType = mCredentialsType == PASSCODE_CREDENTIALS ?
            RemoteConfigReader.GRANT_TYPE_PINCODE :
            RemoteConfigReader.GRANT_TYPE_PASSWORD;

        getConfigBuilder().getRemoteConfigReader().getAccessToken(username, taskgroupId,
            grantType, pincode,
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
                        }
                        else {
                            showLoginErrorAlert();
                            getConfigBuilder().getUser().setAccessToken(null);
                        }

                    }
                    catch (JSONException e) {
                        Log.e(TAG, "error parsing json response: " + e);
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    Log.e(TAG, "Invalid password");
                    e.printStackTrace();
                    showLoginErrorAlert();
                }
            });
    }

    private void showLoginErrorAlert() {
        new AlertDialog.Builder(getActivity())
            .setTitle(R.string.login_error)
            .setMessage(R.string.login_error_details)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .create()
            .show();
    }
}