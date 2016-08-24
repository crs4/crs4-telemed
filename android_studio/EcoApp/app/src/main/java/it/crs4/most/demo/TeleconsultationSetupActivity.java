package it.crs4.most.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.ref.WeakReference;

import it.crs4.most.demo.eco.EcoTeleconsultationActivity;
import it.crs4.most.demo.models.Device;
import it.crs4.most.demo.models.Patient;
import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.models.User;
import it.crs4.most.demo.setup_fragments.SetupFragment;


public class TeleconsultationSetupActivity extends AppCompatActivity implements IConfigBuilder {
    private static final String TAG = "TeleconsultSetupAct";

    private SetupFragment[] mSetupFragments;
    private TeleconsultationController mTcController;
    private MostViewPager mVpPager;
    private User mUser;
    private Patient mPatient;
    private Teleconsultation mTeleconsultation;
    private Device mCamera;
    private RemoteConfigReader mConfigReader;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String configServerIP = QuerySettings.getConfigServerAddress(this);
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(this));
        mConfigReader = new RemoteConfigReader(this, configServerIP, configServerPort);
        setContentView(R.layout.teleconsultation_setup_activity);
        mTcController = TeleconsultationControllerFactory.getTeleconsultationController(this);
        mSetupFragments = mTcController.getFragments(this);

        mVpPager = (MostViewPager) findViewById(R.id.vp_pager);
        SmartFragmentStatePagerAdapter pagerAdapter = new PagerAdapter(this, getSupportFragmentManager());
        mVpPager.setAdapter(pagerAdapter);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        String[] drawerItems = {
            getString(R.string.settings), getString(R.string.exit)
        };
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, drawerItems));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open,
            R.string.drawer_close);

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(mDrawerToggle);
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

    @Override
    public void setUser(User user) {
        mUser = user;
        if (mUser != null) {
            mVpPager.setInternalCurrentItem(1, 0);
        }
    }

    @Override
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
        mTcController.startTeleconsultationActivity(this, mTeleconsultation);
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
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            String value = (String) parent.getItemAtPosition(position);
            if (value.equals(getString(R.string.settings))) {
                Intent settingsIntent = new Intent(TeleconsultationSetupActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
            else if (value.equals(getString(R.string.exit))) {
                finish();
            }
        }
    }

    public static class PagerAdapter extends SmartFragmentStatePagerAdapter {

        private TeleconsultationSetupActivity mainActivity;

        public PagerAdapter(TeleconsultationSetupActivity outerRef, FragmentManager fragmentManager) {
            super(fragmentManager);
            mainActivity = new WeakReference<>(outerRef).get();
        }

        @Override
        public int getCount() {
            return mainActivity.mSetupFragments.length;
        }

        @Override
        public Fragment getItem(int position) {
            if (position >= 0 && position < getCount()) {
                return mainActivity.mSetupFragments[position];
            }
            else {
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int resId = mainActivity.mSetupFragments[position].getTitle();
            return mainActivity.getString(resId);
        }
    }
}