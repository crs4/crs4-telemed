package most.demo.ecoapp;



import most.demo.ecoapp.config_fragments.ConfigFragment;
import most.demo.ecoapp.config_fragments.Fragment_EnterPasscode;
import most.demo.ecoapp.config_fragments.Fragment_PatientSelection;
import most.demo.ecoapp.config_fragments.Fragment_Summary;
import most.demo.ecoapp.config_fragments.Fragment_UserSelection;
import most.demo.ecoapp.models.EcoUser;
import most.demo.ecoapp.models.Patient;
import most.demo.ecoapp.models.Teleconsultation;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupConfigFragments();
		setContentView(R.layout.activity_main);
		vpPager = (MostViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(this,getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setOnPageListener(pages);
        
        //android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);


	}

	
    private void setupConfigFragments() {
			
	   this.configFragments = new ConfigFragment[this.pages.length];
	   
	   this.configFragments[0] = Fragment_UserSelection.newInstance(this,1, "User Selection");	
	   this.configFragments[1] = Fragment_EnterPasscode.newInstance(this, 2, "Enter passcode");
	   this.configFragments[2] = Fragment_PatientSelection.newInstance(this, 3, "Patient Selection");
	   this.configFragments[3] = Fragment_Summary.newInstance(this, 4, "Summary  ");
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
		Intent i = new Intent(this,TeleconsultationActivity.class);
		Log.d(TAG,"STARTING ACTIVITY WITH ECO USER:" + this.ecoUser);
		i.putExtra("EcoUser", this.ecoUser);
		i.putExtra("Teleconsultation" , this.teleconsultation);
		startActivity(i);
	}
	
}