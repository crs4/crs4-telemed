package most.demo.ecoapp.config_fragments;



 
import java.util.ArrayList;

import most.demo.ecoapp.IConfigBuilder;
import most.demo.ecoapp.R;
import most.demo.ecoapp.models.Patient;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Fragment_PatientSelection extends ConfigFragment {
    // Store instance variables
   
	private ArrayList<Patient> patientsArray;
	private ArrayAdapter<Patient>  patientsArrayAdapter;

    // newInstance constructor for creating fragment with arguments
    public static Fragment_PatientSelection newInstance(IConfigBuilder config, int page, String title) {
        Fragment_PatientSelection fragmentPatientSel = new Fragment_PatientSelection();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentPatientSel.setArguments(args);
        fragmentPatientSel.setConfigBuilder(config);
        return fragmentPatientSel;
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
        View view = inflater.inflate(R.layout.patients_list, container, false);
        initializeGUI(view);
        return view;
    }
    
    private void initializeGUI(View view)
    {

    	Button butEmergency = (Button)view.findViewById(R.id.buttonEmergency);
    	
    	butEmergency.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				config.setPatient(null);
				
			}
		});
    	
        ListView listView = (ListView)view.findViewById(R.id.listPatients);
       
        this.patientsArray = new ArrayList<Patient>();
        
        this.patientsArrayAdapter =
                new PatientArrayAdapter(this, R.layout.patient_row, this.patientsArray);
        listView.setAdapter(this.patientsArrayAdapter);
        
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Patient selectedPatient= patientsArray.get(position);
				config.setPatient(selectedPatient);
				
			}});
        this.retrievePatients();
    }

    private void retrievePatients()
    {
    	this.patientsArray.add(new Patient("Mario", "Rossi", "MRSI1234636R243R"));
    	this.patientsArray.add(new Patient("Carlo", "Verdi","VRLI1334636R243P"));
    	this.patientsArray.add(new Patient("Gianni", "Bianchi","BHGI3334636R243V"));
    	this.patientsArrayAdapter.notifyDataSetChanged();
    }
    
	@Override
	public void updateConfigFields() {
	}
}