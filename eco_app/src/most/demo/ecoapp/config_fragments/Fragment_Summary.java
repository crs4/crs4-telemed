package most.demo.ecoapp.config_fragments;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import most.demo.ecoapp.IConfigBuilder;
import most.demo.ecoapp.R;
import most.demo.ecoapp.models.EcoUser;
import most.demo.ecoapp.models.Patient;
import most.demo.ecoapp.models.Room;
import most.demo.ecoapp.models.Device;
import most.demo.ecoapp.models.Teleconsultation;
import most.demo.ecoapp.models.TeleconsultationSession;
import most.demo.ecoapp.models.TeleconsultationSessionState;

import android.app.ProgressDialog;
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
				// bug to be fixed on the remote server during teleconsultation creation
				createNewTeleconsultation();
			
			}
		});
   
    }
   
    private void createNewTeleconsultation()
    {
    	Log.d(TAG, "Trying to create a new teleconsultation...");
    	final String description = "Teleconsultation 0001";
    	final String severity =  severitySpinner.getSelectedItem().toString();
    	final Room room = (Room)roomSpinner.getSelectedItem();
    	retrieveRoomDevices(room);
    	
    	 
    	Log.d(TAG, String.format("Creating teleconsultation with room: %s and desc:%s", room.getId(), description));
    	this.config.getRemoteConfigReader().createNewTeleconsultation(description, severity, room.getId(), this.config.getEcoUser().getAccessToken(), new Listener<String>() {

			@Override
			public void onResponse(String teleconsultationData) {
				Log.d(TAG, "Created teleconsultation: " + teleconsultationData);
				try {
					JSONObject tcData = new JSONObject(teleconsultationData);
					String uuid = tcData.getJSONObject("data").getJSONObject("teleconsultation").getString("uuid");
					String roomUUIO = room.getId();
					Teleconsultation tc = new Teleconsultation(uuid,"", description, severity, room,config.getEcoUser());
			    	
					createTeleconsultationSession(tc);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError err) {
				Log.e(TAG, "Error creating the new teleconsultation: " + err);
				
			}
		});
    }
    
    private void createTeleconsultationSession(final Teleconsultation teleconsultation)
    {
    	EcoUser ecoUser = config.getEcoUser();
    	config.getRemoteConfigReader().createNewTeleconsultationSession(teleconsultation.getId(), teleconsultation.getRoom().getId(), ecoUser.getAccessToken(), new Listener<String>()  {

			@Override
			public void onResponse(String tcSessionData) {
				Log.d(TAG, "Created teleconsultation session: " + tcSessionData);
				try {
					String sessionUUID = new JSONObject(tcSessionData).getJSONObject("data").getJSONObject("session").getString("uuid");
					
					TeleconsultationSession ts = new TeleconsultationSession(sessionUUID, TeleconsultationSessionState.NEW);
					teleconsultation.setLastSession(ts);
					startSession(teleconsultation);
					
				} catch (JSONException e) {
					Log.e(TAG, "Error parsing the new teleconsultation session creation response: " + e);
					e.printStackTrace();
				}
				
			}},
    			new Response.ErrorListener(){

					@Override
					public void onErrorResponse(VolleyError err) {
						Log.e(TAG, "Error creating the new teleconsultation session: " + err);
						
					}});
    }
    
    
    private void startSession(final Teleconsultation tc)
    {
    	EcoUser ecoUser = config.getEcoUser();
    	config.getRemoteConfigReader().startSession(tc.getLastSession().getId(), ecoUser.getAccessToken(), new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject arg0) {
				Log.d(TAG, "Session started: " + arg0);
				 waitForSpecialist(tc);
			}},new ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError arg0) {
					Log.e(TAG, "Error startung session: " + arg0);
					
				}});
    }
    
    private void retrieveRoomDevices(final Room	room)
    {
    	EcoUser ecoUser = config.getEcoUser();
    	Log.d(TAG, "using access token: " + ecoUser.getAccessToken());
    	config.getRemoteConfigReader().getRoom(room.getId(),ecoUser.getAccessToken(), new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject jroom) {
				Log.d(TAG, "Room data: " + jroom);
				room.setEncoder(getDevice(jroom, "encoder"));
				room.setCamera(getDevice(jroom, "camera"));
				Log.d(TAG, "TC Encoder: " + room.getEncoder());
				Log.d(TAG, "TC Camera: " + room.getCamera());
				//config.setTeleconsultation(selectedTc);
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError e) {
				Log.e(TAG, "Error retrieving device json data: " + e);
				
			}
		});
    }
    
    
    private void waitForSpecialist(Teleconsultation tc)
	{
		//Toast.makeText(EcoConfigActivity.this, "Connecting to:" + deviceName + "(" + macAddress +")" , Toast.LENGTH_LONG).show();
    	 ProgressDialog waitForSpecialistDialog = new ProgressDialog(getActivity());
		waitForSpecialistDialog.setTitle("waiting for specialist...");
		waitForSpecialistDialog.setMessage("Waiting for specialist...");
		waitForSpecialistDialog.setCancelable(false);
		waitForSpecialistDialog.setCanceledOnTouchOutside(false);
		waitForSpecialistDialog.show();
		
		 
		this.pollForSpecialist(waitForSpecialistDialog , tc);
	}
    
    private void pollForSpecialist(final ProgressDialog wfsd, final Teleconsultation tc)
    {
    	final Timer t = new Timer();
    	
    	t.schedule(new TimerTask() {
			
			@Override
			public void run() {
				
				config.getRemoteConfigReader().getSessionState(tc.getLastSession().getId(), tc.getApplicant().getAccessToken(), new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject res) {
						Log.d(TAG, "Teleconsultation state response:" + res);
						try {
							String state = res.getJSONObject("data").getJSONObject("session").getString("state");
							if (state.equals(TeleconsultationSessionState.WAITING.name()))
									return;
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						t.cancel();
						wfsd.dismiss();
						runSession(tc);
						
					}

					
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "Error reading Teleconsultation state response:" + arg0);
						t.cancel();
						wfsd.dismiss();
					}
				});
				// config.setTeleconsultation(selectedTc);
			}
		}, 0, 10000);
    }
    
    
    private void runSession(final Teleconsultation tc)
    {
    	EcoUser ecoUser = config.getEcoUser();
    	config.getRemoteConfigReader().runSession(tc.getLastSession().getId(), ecoUser.getAccessToken(), new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject sessionData) {
				Log.d(TAG, "Session running: " + sessionData);
			    tc.getLastSession().setVoipParams(sessionData);
				config.setTeleconsultation(tc);
			}},new ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError arg0) {
					Log.e(TAG, "Error running the session: " + arg0);
					
				}});
    }
    
    private Device getDevice(JSONObject room, String deviceName) {
    	JSONObject jsonDevice;
		try {
			jsonDevice = room.getJSONObject("data").getJSONObject("room").getJSONObject("devices").getJSONObject(deviceName);
			Device device = new Device(jsonDevice.getString("name"),
									   jsonDevice.getJSONObject("capabilities").getString("streaming"),
					                   jsonDevice.getJSONObject("capabilities").getString("shot"),
					                   jsonDevice.getJSONObject("capabilities").getString("web"),
					                   jsonDevice.getJSONObject("capabilities").getString("ptz"),
					                   jsonDevice.getString("user"),
					                   jsonDevice.getString("password")
					                   );
		    return device;
		} catch (JSONException e) {
			Log.e(TAG, "Error retrieving device data: " + e);
			e.printStackTrace();
		}
    	
    	
    	return null;
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
    	
    	this.config.getRemoteConfigReader().getRooms(accessToken, new Response.Listener<JSONObject>() {

			
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