package most.demo.specapp;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;



import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;


import most.demo.specapp.RemoteConfigReader;
import most.demo.specapp.config_fragments.ConfigFragment;
import most.demo.specapp.config_fragments.FragmentLogin;
import most.demo.specapp.config_fragments.Fragment_TeleconsultationSelection;
import most.demo.specapp.models.SpecUser;
import most.demo.specapp.models.Teleconsultation;
import most.demo.specapp.models.TeleconsultationSessionState;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


public class SpecConfigActivity extends ActionBarActivity implements IConfigBuilder {
   
	private static String [] pages = { "Login",
		                               "Teleconsultations"
									   };
	
	private String configServerIP="156.148.132.223"; 
	private int configServerPort = 8000;
	
	private static String TAG = "MostViewPager";
	
	private ConfigFragment [] configFragments = null;

	private SpecUser specUser = null;
	private Teleconsultation teleconsultation = null;
	
	private MostViewPager vpPager  = null;
	
	private Properties configProps;

	private RemoteConfigReader rcr;

	private String clientId;

	private String clientSecret;

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
		vpPager = (MostViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(this,getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setOnPageListener(pages);
    
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
		//this.startTeleconsultationActivity();
		this.joinTeleconsultationSession(selectedTc);
	}
	
	private void joinTeleconsultationSession(final Teleconsultation selectedTc)
	{
		Log.d(TAG, "joining teleconsultation session...");
	 if (selectedTc.getLastSession().getState()== TeleconsultationSessionState.WAITING)
		 this.rcr.joinSession(selectedTc.getLastSession().getId(), getSpecUser().getAccessToken(), new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				Log.d(TAG, "Session Join Response:" + response);
				selectedTc.getLastSession().setupSessionData(response);
				startTeleconsultationActivity();
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError err) {
				Log.d(TAG, "Error in Session Join Response:" + err);
				
			}
		});
	}


	@Override
	public Teleconsultation getTeleconsultation() {
		return this.teleconsultation;
	}


	@Override
	public RemoteConfigReader getRemoteConfigReader() {
		return this.rcr;
	}
	
}