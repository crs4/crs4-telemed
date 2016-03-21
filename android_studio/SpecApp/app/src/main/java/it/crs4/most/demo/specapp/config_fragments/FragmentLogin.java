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
import it.crs4.most.demo.specapp.models.SpecUser;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;


public class FragmentLogin extends ConfigFragment {
	
	RemoteConfigReader rcr = null;
	
	// params to be moved in the configuration file 
	
	private String clientId = "9db4f27b3d9c8e352b5c" ; //"d67a0f2868956edece1a";
	private String clientSecret = "00ea399c013349a716ea3e47d8f8002502e2e982"; //"29df85c27354579d87f026cb33007f350398a491";
	//private String taskgroupID = "hdhtoz6ef4vixu3gk4s62knhncz6tmww"; // CRS4 taskgroup ID
	private String taskgroupID = null;
	private String username = null;
	//PYTHONPATH=.. python manage.py runserver 0.0.0.0:8001

	// --------------------------------------------------
	
	private EditText editPass = null;
	private EditText editUsername;
	private Button butLogin;
	private ProgressDialog loadingConfigDialog;

	protected String accessToken;
		
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
		     retrieveAccessToken();
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
		
		this.rcr = config.getRemoteConfigReader();
		this.retrieveTaskgroups();
	}
    
    
    private void retrieveTaskgroups()
    {
    	this.rcr.getTaskgroups(new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject taskgroups) {
				loadingConfigDialog.setMessage("Taskgroups found for this device. Recovering Taskgroup applicants...");
				Log.d(TAG, "Received taskgroups: " + taskgroups.toString());
				loadingConfigDialog.dismiss();
				retrieveSelectedTaskgroup(taskgroups);
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				Log.e(TAG,"Error retrieving the taskgroup: " + arg0);
				loadingConfigDialog.setMessage("No taskgroups found for the current device: " + arg0);
				loadingConfigDialog.dismiss();
				// [TODO] Handle the error
			}
		});
    }
    
    private void retrieveSelectedTaskgroup(JSONObject taskgroups_data)
    {
    	/*{"data":{"task_groups":[
    	 *         {"description":"CRS4","name":"CRS4","uuid":"hdhtoz6ef4vixu3gk4s62knhncz6tmww"}
    	 *         ]},
    	 *         
    	 *         "success":true}
    	 */
    
 
    	try {
			boolean success = (taskgroups_data!=null && taskgroups_data.getBoolean("success"));
			if (!success) {
				Log.e(TAG, "No valid taskgroups found for this device");
				}
			
			// Alert Dialog for taskgroup selection
			
			AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    getActivity());
			builderSingle.setCancelable(false);
            builderSingle.setIcon(R.drawable.ic_launcher);
            builderSingle.setTitle("Select the taskgroup");
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    android.R.layout.select_dialog_item);
            
            final JSONArray taskgroups = taskgroups_data.getJSONObject("data").getJSONArray("task_groups");
             
            for (int i=0;i<taskgroups.length();i++)
            	{ arrayAdapter.add(taskgroups.getJSONObject(i).getString("name"));}
            
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
						FragmentLogin.this.taskgroupID = taskgroups.getJSONObject(which).getString("uuid");
						retrieveUsers(FragmentLogin.this.taskgroupID);
						dialog.dismiss();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			});
            
            builderSingle.show();
			
			// -------------------------------------
			
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
    }
    
    private void retrieveUsers(String taskgroupId){
    	this.rcr.getUsersByTaskgroup(taskgroupId, new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject users) {
				Log.d(TAG, "Received taskgroup applicants: " + users.toString());
			    retrieveSelectedUser(users); 
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				Log.e(TAG,"Error retrieving the taskgroup users: " + arg0);
				//loadingConfigDialog.setMessage("No users found for the selected taskgroup: " + arg0);
				// [TODO] Handle the error
			}
		});
    }
    
    private void retrieveSelectedUser(final JSONObject users_data){
   	 // {"data":{"applicants":[{"lastname":"admin","username":"admin","firstname":"admin"}]},"success":true}
     // String username = users.getJSONObject("data").getJSONArray("applicants").getJSONObject(0).getString("username");
     //	editUsername.setText(username);
    	
    	try {
			boolean success = (users_data!=null && users_data.getBoolean("success"));
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
             
            for (int i=0;i<users.length();i++)
            	{ arrayAdapter.add(String.format("%s %s", users.getJSONObject(i).getString("lastname"),users.getJSONObject(i).getString("firstname")));}
            
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
				       
                    	username = users_data.getJSONObject("data").getJSONArray("applicants").getJSONObject(which).getString("username");
                    	Log.d(TAG, "Selected user:" + username);
                    	
                    	editUsername.setText(username);
                    	editUsername.setEnabled(false);
						dialog.dismiss();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			});
            
            builderSingle.show();
			
			// -------------------------------------
			
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
    	
    }
    
    private void retrieveAccessToken()
    {
    	String password = this.editPass.getText().toString();
    	rcr.getAccessToken(username, password, FragmentLogin.this.taskgroupID,  new Listener<String>() {

			@Override
			public void onResponse(String response) {
		    	Log.d(TAG, "Query Response:" + response);
		    	try {
					JSONObject  jsonresponse = new JSONObject(response);
					Log.d(TAG,"ACCESS TOKEN: " + jsonresponse.getString("access_token"));
					accessToken =  jsonresponse.getString("access_token");
					
					if (accessToken!=null)
						config.setSpecUser(new SpecUser(username, taskgroupID, accessToken));
					else showWrongPasswordAlert();
					
				} catch (JSONException e) {
					Log.e(TAG, "error parsing json response: " + e);
					e.printStackTrace();
				}
		    
				
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "Error ["+error+"]");
				Log.e(TAG, "Network response: " + error.getMessage());
				accessToken = null;
				showWrongPasswordAlert();
			}
		});                                        
    }
    
    private void showWrongPasswordAlert(){
    	AlertDialog.Builder loginErrorAlert = new AlertDialog.Builder(this.getActivity());
		loginErrorAlert.setTitle("Login Error");
		loginErrorAlert.setMessage("Invalid password.\n Please retry.");
		AlertDialog alert = loginErrorAlert.create();
		alert.show();
    }
    
 
	@Override
	public void updateConfigFields() {
	}
}