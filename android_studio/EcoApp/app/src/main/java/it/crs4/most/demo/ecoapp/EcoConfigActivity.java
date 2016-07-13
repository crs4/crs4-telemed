package it.crs4.most.demo.ecoapp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import it.crs4.most.demo.ecoapp.config_fragments.ConfigFragment;
import it.crs4.most.demo.ecoapp.config_fragments.Fragment_EnterPasscode;
import it.crs4.most.demo.ecoapp.config_fragments.Fragment_PatientSelection;
import it.crs4.most.demo.ecoapp.config_fragments.Fragment_Summary;
import it.crs4.most.demo.ecoapp.config_fragments.Fragment_UserSelection;
import it.crs4.most.demo.ecoapp.models.Device;
import it.crs4.most.demo.ecoapp.models.EcoUser;
import it.crs4.most.demo.ecoapp.models.Patient;
import it.crs4.most.demo.ecoapp.models.Teleconsultation;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

public class EcoConfigActivity extends AppCompatActivity implements IConfigBuilder {

    private static String[] pages = {"User Selection",
            "Pass Code",
            "Emergency Patient Selection",
            "Summary",
    };

    private static String TAG = "MostViewPager";

    private ConfigFragment[] configFragments = null;

    private MostViewPager vpPager = null;
    private EcoUser ecoUser = null;
    private Patient patient = null;
    private Teleconsultation teleconsultation = null;
    private Device camera = null;

    private Properties configProps;

    private String configServerIP;
    private int configServerPort;
    private String clientId = null;
    private String clientSecret;
    private RemoteConfigReader rcr;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private SmartFragmentStatePagerAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.configProps = loadProperties("uri.properties.default");
        this.configServerIP = this.configProps.getProperty("configServerIp");
        this.configServerPort = Integer.valueOf(this.configProps.getProperty("configServerPort"));
        this.clientId = this.configProps.getProperty("clientId");
        this.clientSecret = this.configProps.getProperty("clientSecret");
        this.rcr = new RemoteConfigReader(this, this.configServerIP,
                configServerPort, this.clientId, this.clientSecret);

        setupConfigFragments();
        setContentView(R.layout.config_activity_main);

        vpPager = (MostViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new PagerAdapter(this, getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setOnPageListener(pages);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        String[] drawerItems = {
                getString(R.string.settings), getString(R.string.exit)
        };
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, drawerItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
//				getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//				getActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    private void setupConfigFragments() {

        this.configFragments = new ConfigFragment[this.pages.length];

        this.configFragments[0] = Fragment_UserSelection.newInstance(this, 1, "User Selection");
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
            Log.e("AssetsPropertyReader", e.toString());
        }
        return properties;
    }

    // Extend from SmartFragmentStatePagerAdapter now instead for more dynamic ViewPager items
    public static class PagerAdapter extends SmartFragmentStatePagerAdapter {

        private EcoConfigActivity activity = null;

        public PagerAdapter(EcoConfigActivity activity, FragmentManager fragmentManager) {
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
            Log.d(TAG, "Selected Page Item at pos:" + position);

            if (position >= 0 && position < pages.length)
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
        this.vpPager.setInternalCurrentItem(0, 0);
    }

    @Override
    public void setEcoUser(EcoUser user) {
        this.ecoUser = user;
        Log.d(TAG, "User selected:" + String.format("%s %s ", user.getFirstName(), user.getLastName()));
        if (this.ecoUser != null) {
            this.vpPager.setInternalCurrentItem(1, 0);
        }
    }

    @Override
    public EcoUser getEcoUser() {
        return this.ecoUser;
    }

    @Override
    public void listPatients() {
        this.vpPager.setInternalCurrentItem(2, 0);
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


    private void startTeleconsultationActivity() {
        Intent i = new Intent(this, EcoTeleconsultationActivity.class);
        Log.d(TAG, "STARTING ACTIVITY WITH ECO USER:" + this.ecoUser);
        i.putExtra("EcoUser", this.ecoUser);
        i.putExtra("Teleconsultation", this.teleconsultation);
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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        String TAG = "DrawerItemClickListener";

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            String value = (String) parent.getItemAtPosition(position);
            Log.d(TAG, String.format("position %d, value %s", position, value));

            switch (position) {
                case 0: //SETTINGS
                    Intent intent = new Intent(EcoConfigActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
                case 1: //EXIT
                    finish();
                    break;

            }
        }
    }

}