package most.demo.specapp.config_fragments;


import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import most.demo.specapp.IConfigBuilder;
import most.demo.specapp.R;
import most.demo.specapp.RemoteConfigReader;
import most.demo.specapp.models.SpecUser;
import most.demo.specapp.models.Teleconsultation;



import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Fragment_TeleconsultationSelection extends ConfigFragment {
    // Store instance variables
   
	private ArrayList<Teleconsultation> tcArray;
	private ArrayAdapter<Teleconsultation>  tcArrayAdapter;
	private RemoteConfigReader rcr = null;
	private static String TAG = "FragmentTeleconsultation";

    // newInstance constructor for creating fragment with arguments
    public static Fragment_TeleconsultationSelection newInstance(IConfigBuilder config, int page, String title) {
        Fragment_TeleconsultationSelection fragmentTeleconsultationSel = new Fragment_TeleconsultationSelection();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentTeleconsultationSel.setArguments(args);
        fragmentTeleconsultationSel.setConfigBuilder(config);
        return fragmentTeleconsultationSel;
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
        View view = inflater.inflate(R.layout.tc_list, container, false);
        initializeGUI(view);
        return view;
    }
    
    private void initializeGUI(View view)
    {
        ListView listView = (ListView)view.findViewById(R.id.listTeleconsultation);  
        this.tcArray = new ArrayList<Teleconsultation>();
        this.tcArrayAdapter =
                new TcArrayAdapter(this, R.layout.tc_row, this.tcArray);
        listView.setAdapter(this.tcArrayAdapter);
        
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Teleconsultation selectedTc= tcArray.get(position);
				config.setTeleconsultation(selectedTc);
				
			}});
    }

    private void retrieveTeleconsultations()
    {
    	Log.d(TAG, "called retrieveTeleconsultations()");
    	tcArrayAdapter.clear();
    	
    	this.rcr = config.getRemoteConfigReader();
    	SpecUser specUser = config.getSpecUser();
    	
    	this.rcr.getTeleconsultationsByTaskgroup(specUser.getTaskgroupId(), specUser.getAccessToken(), new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				try {
					Log.d(TAG, "Teleconsultation list response: " + response);
					final JSONArray teleconsultations = response.getJSONObject("data").getJSONArray("teleconsultations");
	             
		            for (int i=0;i<teleconsultations.length();i++)
		            	{ 
		            	
		            	String tcId = teleconsultations.getJSONObject(i).getString("uuid");
		            	String tcInfo = teleconsultations.getJSONObject(i).getString("description");
		            	
		            	Log.d(TAG, "Adding teleconsultation " + i + " -> ID: " + tcId );
		            			 
		            	tcArrayAdapter.add(new Teleconsultation(tcId, tcInfo));
		            	}
					} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							}
		       }
		    	
		}, new ErrorListener(){

			@Override
			public void onErrorResponse(VolleyError response) {
				response.printStackTrace();
				
			}});
    	
    	
    }
    
	@Override
	public void updateConfigFields() {
		this.retrieveTeleconsultations();
	}
}