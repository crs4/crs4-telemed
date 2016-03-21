package it.crs4.most.demo.ecoapp.config_fragments;



 
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import it.crs4.most.demo.ecoapp.IConfigBuilder;
import it.crs4.most.demo.ecoapp.R;
import it.crs4.most.demo.ecoapp.RemoteConfigReader;
import it.crs4.most.demo.ecoapp.models.EcoUser;
import it.crs4.most.demo.ecoapp.models.TaskGroup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Fragment_UserSelection extends ConfigFragment {
    protected static final String TAG = "Fragment_UserSelectionFragment";
	// Store instance variables
	private ProgressDialog loadingConfigDialog;
	private ArrayList<EcoUser> ecoArray;
	private ArrayAdapter<EcoUser>  ecoArrayAdapter;
	private RemoteConfigReader rcr;
	private TaskGroup selectedTaskgroup = null;

    // newInstance constructor for creating fragment with arguments
    public static Fragment_UserSelection newInstance(IConfigBuilder config, int page, String title) {
        Fragment_UserSelection fragmentFirst = new Fragment_UserSelection();
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
        //page = getArguments().getInt("someInt", 0);
        //title = getArguments().getString("someTitle");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.eco_list, container, false);
        initializeGUI(view);
        return view;
    }
    
    private void initializeGUI(View view)
    {

        ListView listView = (ListView)view.findViewById(R.id.listEco);
       
        this.ecoArray = new ArrayList<EcoUser>();
        
        this.ecoArrayAdapter =
                new EcoUserArrayAdapter(this, R.layout.eco_row, this.ecoArray);
        listView.setAdapter(this.ecoArrayAdapter);
        
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				EcoUser selectedUser = ecoArray.get(position);
				config.setEcoUser(selectedUser);
				
			}});
        this.loadRemoteConfig();
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
    
    /**
     * Get the taskgroups associated to this device ID
     */
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
    
    /**
     * read the data of the taskgroup selected by the user
     * @param taskgroups_data the json data of the selected taskgroup
     */
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
                    	String taskgroupID = taskgroups.getJSONObject(which).getString("uuid");
                    	String taskgroudDesc = taskgroups.getJSONObject(which).getString("description");
						Fragment_UserSelection.this.selectedTaskgroup = new TaskGroup(taskgroupID, taskgroudDesc);
						retrieveUsers(taskgroupID);
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
    
    /**
     * retrieve the applicants associated to a specific taskgroup
     * @param taskgroupId
     */
    private void retrieveUsers(String taskgroupId){
    	this.rcr.getUsersByTaskgroup(taskgroupId, new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject users) {
				Log.d(TAG, "Received taskgroup applicants: " + users.toString());
				// {"data":{"applicants":[{"lastname":"admin","username":"admin","firstname":"admin"}]},"success":true}
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
   				return;
   				}
   			
   			final JSONArray users = users_data.getJSONObject("data").getJSONArray("applicants");
            
            for (int i=0;i<users.length();i++)
            	{
            	String username =  users.getJSONObject(i).getString("username");
            	String lastname =  users.getJSONObject(i).getString("lastname");
            	String firstname =  users.getJSONObject(i).getString("firstname");
            	this.ecoArray.add(new EcoUser(firstname,lastname,username, this.selectedTaskgroup));
        
            	}
            this.ecoArrayAdapter.notifyDataSetChanged();
   			
   			// -------------------------------------
   			
   		} catch (JSONException e) {
   			e.printStackTrace();
   			return;
   		}
       	
       }

  
	@Override
	public void updateConfigFields() {
	}
}