package most.demo.ecoapp.config_fragments;

import most.demo.ecoapp.IConfigBuilder;
import most.demo.ecoapp.R;
import most.demo.ecoapp.models.EcoUser;
import most.demo.ecoapp.models.Patient;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
			
				config.setTeleconsultation(null);
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
    	roomSpinner = (Spinner) view.findViewById(R.id.spinnerRoom);
     
    	// Create an ArrayAdapter using the string array and a default spinner layout
    	//ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(),
    	 //       R.array.tc_severities, android.R.layout.simple_spinner_item);
    	// Specify the layout to use when the list of choices appears
    	//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	// Apply the adapter to the spinner
    	//severitySpinner.setAdapter(adapter);
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