package it.crs4.most.demo.ecoapp.config_fragments;



 
import java.util.ArrayList;

import it.crs4.most.demo.ecoapp.IConfigBuilder;
import it.crs4.most.demo.ecoapp.R;
import it.crs4.most.demo.ecoapp.models.Patient;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PatientSelectionFragment extends ConfigFragment {
    // Store instance variables
   
	private ArrayList<Patient> patientsArray;
	private ArrayAdapter<Patient>  patientsArrayAdapter;

    public static PatientSelectionFragment newInstance(IConfigBuilder config) {
        PatientSelectionFragment fragmentPatientSel = new PatientSelectionFragment();
        fragmentPatientSel.setConfigBuilder(config);
        return fragmentPatientSel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patients_list, container, false);
        initializeGUI(view);
        return view;
    }
    
    private void initializeGUI(View view)
    {

    	Button butEmergency = (Button)view.findViewById(R.id.emergency_button);
    	
    	butEmergency.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getConfigBuilder().setPatient(null);
				
			}
		});
    	
        ListView listView = (ListView)view.findViewById(R.id.patients_list);
       
        this.patientsArray = new ArrayList<>();
        
        this.patientsArrayAdapter =
                new PatientArrayAdapter(this, R.layout.patient_row, this.patientsArray);
        listView.setAdapter(this.patientsArrayAdapter);
        
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Patient selectedPatient= patientsArray.get(position);
				getConfigBuilder().setPatient(selectedPatient);
				
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