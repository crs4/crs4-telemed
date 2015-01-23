package most.demo.ecoapp.config_fragments;



 


import most.demo.ecoapp.IConfigBuilder;
import most.demo.ecoapp.R;
import most.demo.ecoapp.models.EcoUser;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


public class Fragment_EnterPasscode extends ConfigFragment {
	
	private EditText editPass = null;
	private static int PASSCODE_LEN = 4;
			
	private static String TAG = "MostViewPager";
    // newInstance constructor for creating fragment with arguments
    public static Fragment_EnterPasscode newInstance(IConfigBuilder config, int page, String title) {
        Fragment_EnterPasscode fragmentFirst = new Fragment_EnterPasscode();
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
        View view = inflater.inflate(R.layout.fragment_passcode, container, false);
        initializeGUI(view);
        return view;
    }
    
    private void initializeGUI(View view)
    {
     this.editPass = (EditText) view.findViewById(R.id.editPasscode);
     editPass.addTextChangedListener(new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
		   String passcode = editPass.getText().toString();
		   
		   Log.d(TAG, "PASSCODE:" + passcode + " LEN:" + passcode.length());
			if (passcode.length()==PASSCODE_LEN)
				{
				editPass.setText("");
				checkPasscode(passcode);
				}
		}
	});
  
    }

  
    private void checkPasscode(String passCode) {
    	if (!passCode.equals(config.getEcoUser().getUserPwd()))
    		config.listEcoUsers();
    	else config.listPatients();
    }
    
    
	@Override
	public void updateConfigFields() {
	}
}