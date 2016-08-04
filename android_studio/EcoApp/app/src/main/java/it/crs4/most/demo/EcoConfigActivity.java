package it.crs4.most.demo;

import it.crs4.most.demo.config_fragments.ConfigFragment;
import it.crs4.most.demo.config_fragments.EnterPasscodeFragment;
import it.crs4.most.demo.config_fragments.PatientSelectionFragment;
import it.crs4.most.demo.config_fragments.SummaryFragment;
import it.crs4.most.demo.config_fragments.UserSelectionFragment;
import it.crs4.most.demo.models.Device;
import it.crs4.most.demo.models.User;
import it.crs4.most.demo.models.Patient;
import it.crs4.most.demo.models.Teleconsultation;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.widget.ListView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

public class EcoConfigActivity extends AppCompatActivity implements IConfigBuilder {
    private static final String TAG = "MostViewPager";

    private static String[] mPages = {"User Selection",
            "Pass Code",
            "Emergency Patient Selection",
            "Summary",
    };

    private ConfigFragment[] mConfigFragments;
    private MostViewPager mVpPager;
    private User mUser;
    private Patient mPatient;
    private Teleconsultation mTeleconsultation;
    private Device mCamera;
    private RemoteConfigReader mConfigReader;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private SmartFragmentStatePagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String configServerIP = QuerySettings.getConfigServerAddress(this);
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(this));

        mConfigReader = new RemoteConfigReader(this, configServerIP, configServerPort);

        setupConfigFragments();
        setContentView(R.layout.config_activity_main);

        mVpPager = (MostViewPager) findViewById(R.id.vp_pager);
        mPagerAdapter = new PagerAdapter(this, getSupportFragmentManager());
        mVpPager.setAdapter(mPagerAdapter);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        String[] drawerItems = {
                getString(R.string.settings), getString(R.string.exit)
        };
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, drawerItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open,
                R.string.drawer_close);

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        catch (NullPointerException ex) {
            Log.d(TAG, "There's no actionbar");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    private void setupConfigFragments() {
        mConfigFragments = new ConfigFragment[mPages.length];
        mConfigFragments[0] = UserSelectionFragment.newInstance(this);
        mConfigFragments[1] = EnterPasscodeFragment.newInstance(this);
        mConfigFragments[2] = PatientSelectionFragment.newInstance(this);
        mConfigFragments[3] = SummaryFragment.newInstance(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public RemoteConfigReader getRemoteConfigReader() {
        return mConfigReader;
    }

    @Override
    public void listUsers() {
        mVpPager.setInternalCurrentItem(0, 0);
    }

    public void setUser(User user) {
        mUser = user;
        if (mUser != null) {
            mVpPager.setInternalCurrentItem(1, 0);
        }
    }

    public User getUser() {
        return mUser;
    }

    @Override
    public void listPatients() {
        mVpPager.setInternalCurrentItem(2, 0);
    }

    @Override
    public void setPatient(Patient selectedPatient) {
        mPatient = selectedPatient;
        if (mPatient != null) {
            mVpPager.setInternalCurrentItem(3, 2);
        }
    }

    @Override
    public Patient getPatient() {
        return mPatient;
    }

    @Override
    public void setTeleconsultation(Teleconsultation teleconsultation) {
        mTeleconsultation = teleconsultation;
        if (mTeleconsultation != null) {
            startTeleconsultationActivity();
        }
    }

    @Override
    public Device getCamera() {
        return mCamera;
    }

    @Override
    public void setCamera(Device camera) {
        mCamera = camera;
    }

    private void startTeleconsultationActivity() {
        Intent i;
        if (Build.MANUFACTURER.equals("EPSON") && Build.MODEL.equals("embt2")) {
            i = new Intent(this, AREcoTeleconsultationActivity.class);
        }
        else {
            i = new Intent(this, EcoTeleconsultationActivity.class);
        }
        Log.d(TAG, "Starting activity with eco user: " + mUser);
        i.putExtra("User", mUser);
        i.putExtra("Teleconsultation", mTeleconsultation);
        startActivityForResult(i, EcoTeleconsultationActivity.TELECONSULT_ENDED_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EcoTeleconsultationActivity.TELECONSULT_ENDED_REQUEST) {
            if (resultCode == RESULT_OK) {
                setUser(null);
                setPatient(null);
                setCamera(null);
                setTeleconsultation(null);
                mVpPager.setInternalCurrentItem(0, 0);
            }
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        String TAG = "DrawerItemClickListener";

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            String value = (String) parent.getItemAtPosition(position);
            Log.d(TAG, String.format("position %d, value %s", position, value));

            switch (position) {
                case 0: // SETTINGS
                    Intent settingsIntent = new Intent(EcoConfigActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
                case 1: // EXIT
                    finish();
                    break;
            }
        }
    }

    // Extend from SmartFragmentStatePagerAdapter now instead for more dynamic ViewPager items
    public static class PagerAdapter extends SmartFragmentStatePagerAdapter {

        private EcoConfigActivity mActivity = null;

        public PagerAdapter(EcoConfigActivity activity, FragmentManager fragmentManager) {
            super(fragmentManager);
            mActivity = activity;
        }

        // Returns total number of mPages
        @Override
        public int getCount() {
            return mPages.length;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "Selected Page Item at pos:" + position);

            if (position >= 0 && position < mPages.length) {
                return mActivity.mConfigFragments[position];
            }
            else {
                return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return mPages[position];
        }
    }
}