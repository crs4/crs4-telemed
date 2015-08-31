package most.demo.ecoapp;



import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import most.demo.ecoapp.config_fragments.ConfigFragment;
import most.demo.ecoapp.config_fragments.Fragment_EnterPasscode;
import most.demo.ecoapp.config_fragments.Fragment_PatientSelection;
import most.demo.ecoapp.config_fragments.Fragment_Summary;
import most.demo.ecoapp.config_fragments.Fragment_UserSelection;
import most.demo.ecoapp.models.Device;
import most.demo.ecoapp.models.EcoUser;
import most.demo.ecoapp.models.Patient;
import most.demo.ecoapp.models.Teleconsultation;
import most.demo.ecoapp.RemoteConfigReader;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class EcoConfigActivity extends ActionBarActivity implements IConfigBuilder {
   
	private static String [] pages = { "User Selection",
									  "Pass Code",
									   "Emergency Patient Selection", 
									   "Summary",
									   };
	
	private static String TAG = "MostViewPager";
	
	private ConfigFragment [] configFragments = null;

	private MostViewPager vpPager  = null;
	private EcoUser ecoUser = null;
	private Patient patient = null;
	private Teleconsultation teleconsultation = null;
	private Device camera = null;

	private Properties configProps;

	private String configServerIP;
	private int configServerPort;
	private String clientId = null;
	private String clientSecret;
	
	private most.demo.ecoapp.RemoteConfigReader rcr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.configProps = loadProperties("uri.properties.default");
		this.configServerIP = this.configProps.getProperty("configServerIp");
		this.configServerPort = Integer.valueOf(this.configProps.getProperty("configServerPort")).intValue();
		this.clientId = this.configProps.getProperty("clientId");
		this.clientSecret = this.configProps.getProperty("clientSecret");
		this.rcr = new RemoteConfigReader(this, this.configServerIP, configServerPort, this.clientId,this.clientSecret);
		
		setupConfigFragments();
		setContentView(R.layout.config_activity_main);
		
		//setupActionBar();
		
		vpPager = (MostViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(this,getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setOnPageListener(pages);
        
        //android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.config, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_exit:
			finish();
			break;
		
		}
		return true;
	}
	

	private void setupActionBar()
	{
		ActionBar actionBar = getSupportActionBar();
	    // add the custom view to the action bar
	    actionBar.setCustomView(R.layout.config_actionbar_view);
	    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
		        | ActionBar.DISPLAY_SHOW_HOME);
	}
	
    private void setupConfigFragments() {
			
	   this.configFragments = new ConfigFragment[this.pages.length];
	   
	   this.configFragments[0] = Fragment_UserSelection.newInstance(this,1, "User Selection");	
	   this.configFragments[1] = Fragment_EnterPasscode.newInstance(this, 2, "Enter passcode");
	   this.configFragments[2] = Fragment_PatientSelection.newInstance(this, 3, "Patient Selection");
	   this.configFragments[3] = Fragment_Summary.newInstance(this, 4, "Summary  ");
	}
    
    private Properties loadProperties(String FileName) {
		Properties properties = new Properties();
        try {
               /**
                * getAssets() Return an AssetManager instance for your
                * application's package. AssetManager Provides access to an
                * application's raw asset files;
                */
               AssetManager assetManager = this.getAssets();
               /**
                * Open an asset using ACCESS_STREAMING mode. This
                */
               InputStream inputStream = assetManager.open(FileName);
               /**
                * Loads properties from the specified InputStream,
                */
               properties.load(inputStream);

        } catch (IOException e) {
               // TODO Auto-generated catch block
               Log.e("AssetsPropertyReader",e.toString());
        }
        return properties;
 }


	private SmartFragmentStatePagerAdapter adapterViewPager;

    // Extend from SmartFragmentStatePagerAdapter now instead for more dynamic ViewPager items
    public static class MyPagerAdapter extends SmartFragmentStatePagerAdapter {
       
    	private EcoConfigActivity activity = null;
       
        public MyPagerAdapter(EcoConfigActivity activity,FragmentManager fragmentManager) {
            super(fragmentManager);
            this.activity = activity; 
            
        }
        
        

        // Returns total number of pages
        @Override
        public int getCount() {
            return pages.length;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
        	Log.d(TAG,"Selected Page Item at pos:" + position);
        	
        	  Toast.makeText(this.activity, 
                      "getItem on position:::: " + position, Toast.LENGTH_SHORT).show();
              if (position>=0 && position < pages.length)
                return this.activity.configFragments[position];
              else
            	  return null;
            }
       

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return pages[position];
        }

    }

    @Override
	public void listEcoUsers() {
		this.vpPager.setInternalCurrentItem(0,0);
	}
    
	@Override
	public void setEcoUser(EcoUser user) {
		this.ecoUser = user;
		
		if (this.ecoUser!=null)
		{   
			this.vpPager.setInternalCurrentItem(1,0);
		}
	}


	@Override
	public EcoUser getEcoUser() {
		return this.ecoUser;
	}


	@Override
	public void listPatients() {
		this.vpPager.setInternalCurrentItem(2,0);
	}

	@Override
	public void setPatient(Patient selectedPatient) {
		
	this.patient = selectedPatient;
	this.vpPager.setInternalCurrentItem(3, 2);
	}


	@Override
	public void setTeleconsultation(Teleconsultation teleconsultation) {
	 this.teleconsultation = teleconsultation;
     this.startTeleconsultationActivity();
	}


	@Override
	public Patient getPatient() {
		return this.patient;
	}


	private void startTeleconsultationActivity()
	{
		Intent i = new Intent(this,EcoTeleconsultationActivity.class);
		Log.d(TAG,"STARTING ACTIVITY WITH ECO USER:" + this.ecoUser);
		i.putExtra("EcoUser", this.ecoUser);
		i.putExtra("Teleconsultation" , this.teleconsultation);
		startActivity(i);
	}

	@Override
	public RemoteConfigReader getRemoteConfigReader() {
		return this.rcr;
	}

	@Override
	public Device getCamera() {
		return this.camera;
	}

	@Override
	public void setCamera(Device camera) {
		this.camera = camera;
	}
	
}