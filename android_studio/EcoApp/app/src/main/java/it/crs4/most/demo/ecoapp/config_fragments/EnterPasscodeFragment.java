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
	
	private EditText editPass = null;
	private RemoteConfigReader rcr;
	private TextView txtUser;
	private static int PASSCODE_LEN = 5;

	private static String TAG = "MostViewPager";
    // newInstance constructor for creating fragment with arguments
    public static EnterPasscodeFragment newInstance(IConfigBuilder config, int page, String title) {
        EnterPasscodeFragment fragmentFirst = new EnterPasscodeFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        fragmentFirst.setConfigBuilder(config);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.rcr = config.getRemoteConfigReader();
        Log.d(TAG, "Config: " + config);
        Log.d(TAG, "Eco Config: " + config.getEcoUser());
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passcode, container, false);
        initializeGUI(view);
        return view;
    }
    
    
  
    private void initializeGUI(View view)
    {
     this.editPass = (EditText) view.findViewById(R.id.editPasscode);
     this.txtUser = (TextView) view.findViewById(R.id.text_operator_username);
     Log.d(TAG, "INIT VIEW FOR TEXT VIEW " + this.txtUser);
     Log.d(TAG, "Config:::: " + config);
  
     editPass.addTextChangedListener(new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
		   String passcode = editPass.getText().toString();
		   
		   Log.d(TAG, "PASSCODE:" + passcode + " LEN:" + passcode.length());
			if (passcode.length()==PASSCODE_LEN)
				{
				retrieveAccessToken(passcode);
				editPass.setText("");
				}
		}
	});
  
    }

    
    private void retrieveAccessToken(String pincode)
    {
    	String username = this.config.getEcoUser().getUsername();
    	String taskgroupId = this.config.getEcoUser().getTaskGroup().getId();
    	Log.d(TAG, "GET ACCESS TOKEN WITH PIN CODE: " + pincode); 
    	this.rcr.getAccessToken(username, pincode, taskgroupId, new Listener<String>() {

			

			@Override
			public void onResponse(String response) {
		    	Log.d(TAG, "Query Response:" + response);
		    	try {
					JSONObject  jsonresponse = new JSONObject(response);
					Log.d(TAG,"ACCESS TOKEN: " + jsonresponse.getString("access_token"));
					String accessToken =  jsonresponse.getString("access_token");
					
					if (accessToken!=null)
					{
						config.getEcoUser().setAccessToken(accessToken);
						config.listPatients();
					}
					else 
					{
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
				Log.e(TAG, "Error ["+error+"]");
				showPinCodeErrorAlert();
				config.listEcoUsers();
			}
		});                                        
    }
    
    private void showPinCodeErrorAlert(){
    	AlertDialog.Builder loginErrorAlert = new AlertDialog.Builder(this.getActivity());
		loginErrorAlert.setTitle("Login Error");
		loginErrorAlert.setMessage("Invalid Pin code.\n Please retry.");
		AlertDialog alert = loginErrorAlert.create();
		alert.show();
    }
  
	@Override
	public void updateConfigFields() {
		Log.d(TAG, String.format("Config fields for %s %s" , config.getEcoUser().getFirstName() , config.getEcoUser().getLastName()));
	     this.txtUser.setText(String.format("%s %s" , config.getEcoUser().getFirstName() , config.getEcoUser().getLastName()));
	}
}