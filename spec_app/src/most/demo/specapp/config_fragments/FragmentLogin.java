package most.demo.specapp.config_fragments;



 
import most.demo.specapp.IConfigBuilder;
import most.demo.specapp.R;
import most.demo.specapp.models.SpecUser;


import android.app.AlertDialog;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class FragmentLogin extends ConfigFragment {
	
	private EditText editPass = null;

	private EditText editUsername;

	private Button butLogin;
			
	private static String TAG = "MostViewPager";
    // newInstance constructor for creating fragment with arguments
    public static FragmentLogin newInstance(IConfigBuilder config, int page, String title) {
    	FragmentLogin fragmentFirst = new FragmentLogin();
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
        View view = inflater.inflate(R.layout.login, container, false);
        initializeGUI(view);
        return view;
    }
    
    private void initializeGUI(View view)
    {
     this.editPass = (EditText) view.findViewById(R.id.editPassword);
     this.editUsername = (EditText) view.findViewById(R.id.editUsername);
     
     this.butLogin = (Button) view.findViewById(R.id.butLogin);
     
     this.butLogin.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
		     doLogin();
			
		}
	});
     
    }
    
    private void doLogin()
    {
    	if (checkForPassword())
    	{
    	}
    	else
    	{
    		this.editUsername.setText("");
    		this.editPass.setText("");
    		
    		AlertDialog.Builder loginErrorAlert = new AlertDialog.Builder(this.getActivity());
    		loginErrorAlert.setTitle("Login Error");
    		loginErrorAlert.setMessage("Invalid Username or password.\n Please retry.");
			AlertDialog alert = loginErrorAlert.create();
			alert.show();
    	}
    }

    private boolean checkForPassword()
    {
    	String username =  this.editPass.getText().toString();
    	String pwd = this.editPass.getText().toString();
    	
    	if (isValidUser(username, pwd))
    	{
    		config.setSpecUser(new SpecUser(username, pwd));
    		return true;
    	}
    	
    	return false;
    
    }
 
    private boolean isValidUser(String username, String password)
    {
    	return (username.equalsIgnoreCase("most") && password.equalsIgnoreCase("most"));
    }
    
	@Override
	public void updateConfigFields() {
	}
}