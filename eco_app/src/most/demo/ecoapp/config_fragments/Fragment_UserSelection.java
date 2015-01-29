package most.demo.ecoapp.config_fragments;



 
import java.util.ArrayList;

import most.demo.ecoapp.IConfigBuilder;
import most.demo.ecoapp.R;
import most.demo.ecoapp.models.EcoUser;
import most.demo.ecoapp.models.TaskGroup;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Fragment_UserSelection extends ConfigFragment {
    // Store instance variables
   
	private ArrayList<EcoUser> ecoArray;
	private ArrayAdapter<EcoUser>  ecoArrayAdapter;

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
        this.retrieveEcoUsers();
    }

    private void retrieveEcoUsers()
    {
    	this.ecoArray.add(new EcoUser("Eco 1", "1234", new TaskGroup("1234","Lanusei")));
    	this.ecoArray.add(new EcoUser("Eco 2","5678", new TaskGroup("1234","Lanusei")));
    	this.ecoArray.add(new EcoUser("Eco 3", "0000" , new TaskGroup("1234","Cagliari")));
    	this.ecoArrayAdapter.notifyDataSetChanged();
    }
    
	@Override
	public void updateConfigFields() {
	}
}