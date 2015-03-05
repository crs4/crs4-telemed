package most.demo.specapp.config_fragments;

import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import most.demo.specapp.IConfigBuilder;
import most.demo.specapp.R;
import most.demo.specapp.RemoteConfigReader;
import most.demo.specapp.models.SpecUser;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.provider.Settings.Secure;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class FragmentLogin extends ConfigFragment {
	
	RemoteConfigReader rcr = null;
	
	// params to be moved in the configuration file 
	
	private String clientId = "";
	private String clientSecret = "";
	private String taskgroupID = "hdhtoz6ef4vixu3gk4s62knhncz6tmww"; // CRS4 taskgroup ID
	//PYTHONPATH=.. python manage.py runserver 0.0.0.0:8001

	private String configServerIP="127.0.0.1"; 
	private int configServerPort = 8001;
	
	// --------------------------------------------------
	
	private EditText editPass = null;
	private EditText editUsername;
	private Button butLogin;
	private ProgressDialog loadingConfigDialog;
		
	private static String TAG = "FragmentLogin";
    // newInstance constructor for creating fragment with arguments
    public static FragmentLogin newInstance(IConfigBuilder config, int page, String title) {
    	FragmentLogin fragmentFirst = new FragmentLogin();
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
     
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);
        initializeGUI(view);
        loadRemoteConfig();
        return view;
    }
    
    private void initializeGUI(View view)
    {
     this.editPass = (EditText) view.findViewById(R.id.editPassword);
     this.editUsername = (EditText) view.findViewById(R.id.editUsername);
     
    
     this.butLogin = (Button) view.findViewById(R.id.butLogin);
     //Log.d(TAG,"Call to getAccessToken()");
     //this.getAccessToken();
     this.butLogin.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
		     doLogin();
			
		}
	});
     
    }
    
    private void loadRemoteConfig()
	{
		//Toast.makeText(EcoConfigActivity.this, "Connecting to:" + deviceName + "(" + macAddress +")" , Toast.LENGTH_LONG).show();
		loadingConfigDialog = new ProgressDialog(getActivity());
		loadingConfigDialog.setTitle("Connection to the remote server");
		loadingConfigDialog.setMessage("Loading taskgroups associated to this device. Please wait....");
		loadingConfigDialog.setCancelable(false);
		loadingConfigDialog.setCanceledOnTouchOutside(false);
		loadingConfigDialog.show();
		
		this.rcr = new RemoteConfigReader(this.getActivity(), this.configServerIP, configServerPort);
		this.getTaskgroups();
	}
    
    
    private void getTaskgroups()
    {
    	this.rcr.getTaskgroups(new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject taskgroups) {
				loadingConfigDialog.setMessage("Taskgroups found for this device. Recovering Taskgroup applicants...");
				Log.d(TAG, "Received taskgroups: " + taskgroups.toString());
				getUsers(taskgroupID);
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				Log.e(TAG,"Error retrieving the taskgroup: " + arg0);
				loadingConfigDialog.setMessage("No taskgroups found for the current device: " + arg0);
				// [TODO] Handle the error
			}
		});
    }
    
    private void getUsers(String taskgroupId){
    	this.rcr.getUsersByTaskgroup(taskgroupId, new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject users) {
				loadingConfigDialog.setMessage("Taskgroup applicants found");
				Log.d(TAG, "Received taskgroup applicants: " + users.toString());
				try {
					String username = users.getJSONObject("data").getJSONArray("applicants").getJSONObject(0).getString("username");
					editUsername.setText(username);
					loadingConfigDialog.cancel();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				Log.e(TAG,"Error retrieving the taskgroup users: " + arg0);
				loadingConfigDialog.setMessage("No users found for the selected taskgroup: " + arg0);
				// [TODO] Handle the error
			}
		});
    }
    
    private void getAccessToken()
    {
    	
    	//rcr.getAccessToken("1f2138b9c94c388503fb", "fda712b6456c498c4e826e2942e30175d9a3c682", "admin", "12345");
    	rcr.getAccessToken("d67a0f2868956edece1a", "29df85c27354579d87f026cb33007f350398a491", "admin", "12345");                                        
    }
    
    
    private void doLogin()
    {
    	if (checkForPassword())
    	{
    	}
    	else
    	{
    		this.editUsername.setText("");
    		this.editPass.setText("");
    		
    		AlertDialog.Builder loginErrorAlert = new AlertDialog.Builder(this.getActivity());
    		loginErrorAlert.setTitle("Login Error");
    		loginErrorAlert.setMessage("Invalid Username or password.\n Please retry.");
			AlertDialog alert = loginErrorAlert.create();
			alert.show();
    	}
    }

    private boolean checkForPassword()
    {
    	String username =  this.editPass.getText().toString();
    	String pwd = this.editPass.getText().toString();
    	
    	if (isValidUser(username, pwd))
    	{
    		config.setSpecUser(new SpecUser(username, pwd));
    		return true;
    	}
    	
    	return false;
    
    }
 
    private boolean isValidUser(String username, String password)
    {
    	return (username.equalsIgnoreCase("most") && password.equalsIgnoreCase("most"));
    }
    
	@Override
	public void updateConfigFields() {
	}
}