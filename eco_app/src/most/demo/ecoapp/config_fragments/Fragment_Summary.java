package most.demo.ecoapp.config_fragments;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import most.demo.ecoapp.IConfigBuilder;
import most.demo.ecoapp.R;
import most.demo.ecoapp.models.Patient;
import most.demo.ecoapp.models.Room;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


public class Fragment_Summary extends ConfigFragment {
	
	private EditText txtPatientFullName = null;
	private Button butStartEmergency = null;
	private Spinner severitySpinner;
	private Spinner roomSpinner;
			
	private static String TAG = "MostFragmentSummary";
    // newInstance constructor for creating fragment with arguments
    public static Fragment_Summary newInstance(IConfigBuilder config, int page, String title) {
        Fragment_Summary fragmentFirst = new Fragment_Summary();
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
        View view = inflater.inflate(R.layout.fragment_summary, container, false);
        initializeGUI(view);
        return view;
    }
    
    private void initializeGUI(View view)
    {
    	this.txtPatientFullName = (EditText) view.findViewById(R.id.textSummaryPatientFullName);
    	this.butStartEmergency = (Button) view.findViewById(R.id.buttonStartEmergency);
    	
    	this.setupTcSeveritySpinner(view);
    	this.setupTcRoomSpinner(view);
    	
    	this.butStartEmergency.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				//config.setTeleconsultation(null);
				createNewTeleconsultation();
			}
		});
   
    }
   
    private void createNewTeleconsultation()
    {
    	Log.d(TAG, "Trying to create a new teleconsultation...");
    	String description = "new Test Teleconsultation";
    	String severity =  severitySpinner.getSelectedItem().toString();
    	String roomId = ((Room)roomSpinner.getSelectedItem()).getId();
    	
    	Log.d(TAG, String.format("Creating teleconsultation with room: %s and desc:%s", roomId, description));
    	this.config.getRemoteConfigReader().createNewTeleconsultation(description, severity, roomId, this.config.getEcoUser().getAccessToken(), new Listener<String>() {

			@Override
			public void onResponse(String teleconsultationData) {
				Log.d(TAG, "Created teleconsultation: " + teleconsultationData);
				
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError err) {
				Log.e(TAG, "Error creating the new teleconsultation: " + err);
				
			}
		});
    }
    
    private void setupTcSeveritySpinner(View view) {
    	severitySpinner = (Spinner) view.findViewById(R.id.spinnerSeverity);
     
    	// Create an ArrayAdapter using the string array and a default spinner layout
    	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(),
    	        R.array.tc_severities, android.R.layout.simple_spinner_item);
    	// Specify the layout to use when the list of choices appears
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	// Apply the adapter to the spinner
    	severitySpinner.setAdapter(adapter);
    }
    
    private void setupTcRoomSpinner(View view) {
    	this.roomSpinner = (Spinner) view.findViewById(R.id.spinnerRoom);
    	this.retrieveRooms();
   
    }
    
    
    private void retrieveRooms() {
    	String accessToken = this.config.getEcoUser().getAccessToken();
    	String taskgroupId = this.config.getEcoUser().getTaskGroup().getId();
    	this.config.getRemoteConfigReader().getRooms(taskgroupId, accessToken, new Response.Listener<JSONObject>() {

			
			@Override
			public void onResponse(JSONObject rooms) {
				
				Log.d(TAG, String.format("Received rooms:\n\n%s\n\n" , rooms));
				populateTcRoomSpinner(rooms);
			}}, new Response.ErrorListener() {
				
				@Override
				public void onErrorResponse(VolleyError arg0) {
					Log.e(TAG, "Error retrieving rooms:" + arg0);
					
				}
			});
    }
 
    private void populateTcRoomSpinner(JSONObject roomsData)
    {
    	//{"data":{"rooms":[{"description":"Stanza di Test","name":"Ufficio Francesco","uuid":"yytgsihw4vtm5tpceunnoyclwvbnwi53"}]},"success":true}
    	try {
			JSONArray jrooms = roomsData.getJSONObject("data").getJSONArray("rooms");
			ArrayList<Room> rooms = new ArrayList<Room>();
	    	
			for (int i=0; i<jrooms.length(); i++)
			{
				JSONObject jroom = jrooms.getJSONObject(i);
			    rooms.add(new Room(jroom.getString("uuid"), jroom.getString("name"), jroom.getString("description")));
			}
			
	    	ArrayAdapter<Room> spinnerArrayAdapter = new ArrayAdapter<Room>(getActivity(),   android.R.layout.simple_spinner_item, rooms);
	    	spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
	    	roomSpinner.setAdapter(spinnerArrayAdapter);
	  
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
	@Override
	public void updateConfigFields() {
		Patient patient = this.config.getPatient();
    	if (patient!=null)
    	{
    		txtPatientFullName.setText(patient.getName() + " " + patient.getSurname());
    		txtPatientFullName.setFocusable(false);
    	}
    	else
    	{
    		txtPatientFullName.setFocusable(true);
    		txtPatientFullName.setText("INSERT NAME");
    		
    	}
		
	}
	
	
}