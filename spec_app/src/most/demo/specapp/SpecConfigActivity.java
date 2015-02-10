package most.demo.specapp;


import most.demo.specapp.config_fragments.ConfigFragment;
import most.demo.specapp.config_fragments.FragmentLogin;
import most.demo.specapp.config_fragments.Fragment_TeleconsultationSelection;
import most.demo.specapp.models.SpecUser;
import most.demo.specapp.models.Teleconsultation;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;


public class SpecConfigActivity extends ActionBarActivity implements IConfigBuilder {
   
	private static String [] pages = { "Login",
		                               "Teleconsultations"
									   };
	
	private static String TAG = "MostViewPager";
	
	private ConfigFragment [] configFragments = null;

	private SpecUser specUser = null;
	private Teleconsultation teleconsultation = null;
	
	private MostViewPager vpPager  = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupConfigFragments();
		setContentView(R.layout.config_activity_main);
		vpPager = (MostViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(this,getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setOnPageListener(pages);
    
	}

	
    private void setupConfigFragments() {
			
	   this.configFragments = new ConfigFragment[this.pages.length];
	   
	   this.configFragments[0] = FragmentLogin.newInstance(this,1, "Login");	
	   this.configFragments[1] = Fragment_TeleconsultationSelection.newInstance(this,2, "Teleconsultations");	
	}


	private SmartFragmentStatePagerAdapter adapterViewPager;

    // Extend from SmartFragmentStatePagerAdapter now instead for more dynamic ViewPager items
    public static class MyPagerAdapter extends SmartFragmentStatePagerAdapter {
       
    	private SpecConfigActivity activity = null;
       
        public MyPagerAdapter(SpecConfigActivity activity,FragmentManager fragmentManager) {
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



	private void startTeleconsultationActivity()
	{
		Intent i = new Intent(this,SpecTeleconsultationActivity.class);
		Log.d(TAG,"STARTING ACTIVITY WITH TELECONSULTATION:" + this.teleconsultation.getInfo());
		i.putExtra("Teleconsultation", this.teleconsultation);
		startActivity(i);
	}


	@Override
	public void setSpecUser(SpecUser user) {
	   this.specUser = user;
	   this.vpPager.setInternalCurrentItem(1, 0);
	}


	@Override
	public SpecUser getSpecUser() {
		 
		return this.specUser;
	}


	@Override
	public void setTeleconsultation(Teleconsultation selectedTc) {
		this.teleconsultation = selectedTc;
		this.startTeleconsultationActivity();
	}


	@Override
	public Teleconsultation getTeleconsultation() {
		return this.teleconsultation;
	}
	
}