package most.demo.specapp.config_fragments;


import java.util.ArrayList;

import most.demo.specapp.IConfigBuilder;
import most.demo.specapp.R;
import most.demo.specapp.models.Teleconsultation;



import android.os.Bundle;
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
        this.retrieveTeleconsultations();
    }

    private void retrieveTeleconsultations()
    {
    	this.tcArray.add(new Teleconsultation("0001", "Teleconsultation 1"));
    	this.tcArray.add(new Teleconsultation("0002", "Teleconsultation 2"));
    }
    
	@Override
	public void updateConfigFields() {
	}
}