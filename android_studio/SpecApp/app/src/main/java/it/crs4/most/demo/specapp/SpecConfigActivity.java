package it.crs4.most.demo.specapp;


import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;


import it.crs4.most.demo.specapp.config_fragments.ConfigFragment;
import it.crs4.most.demo.specapp.config_fragments.LoginFragment;
import it.crs4.most.demo.specapp.config_fragments.TeleconsultationSelectionFragment;
import it.crs4.most.demo.specapp.models.SpecUser;
import it.crs4.most.demo.specapp.models.Teleconsultation;
import it.crs4.most.demo.specapp.models.TeleconsultationSessionState;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class SpecConfigActivity extends AppCompatActivity implements IConfigBuilder {

    private static final String OAUTH_CLIENT_ID = "9db4f27b3d9c8e352b5c";
    private static final String OAUTH_CLIENT_SECRET = "00ea399c013349a716ea3e47d8f8002502e2e982";
    private static String TAG = "MostViewPager";
    private static String[] mPages = {"Login", "Teleconsultations"};

    private ConfigFragment[] mConfigFragments = null;
    private SpecUser mSpecUser = null;
    private Teleconsultation mTeleconsultation = null;
    private MostViewPager mPager = null;
    private RemoteConfigReader mConfigReader;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String configServerIP = QuerySettings.getConfigServerAddress(this);
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(this));

        mConfigReader = new RemoteConfigReader(this, configServerIP,
                configServerPort, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET);

        setupConfigFragments();
        setContentView(R.layout.config_activity_main);
        mPager = (MostViewPager) findViewById(R.id.vp_pager);
        FragmentStatePagerAdapter pagerAdapter = new PagerAdapter(this, getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        mPager.setOnPageListener();

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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.config, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_exit:
//                finish();
//                break;
//            case R.id.action_settings:
//                Intent i = new Intent(this, SettingsActivity.class);
//                startActivity(i);
//                break;
//        }
//        return true;
//    }

    private void setupConfigFragments() {
        mConfigFragments = new ConfigFragment[mPages.length];
        mConfigFragments[0] = LoginFragment.newInstance(this);
        mConfigFragments[1] = TeleconsultationSelectionFragment.newInstance(this);
    }

    private void startTeleconsultationActivity() {
        Intent i = new Intent(this, SpecTeleconsultationActivity.class);
        Log.d(TAG, "STARTING ACTIVITY WITH TELECONSULTATION: " + mTeleconsultation.getInfo());
        i.putExtra("Teleconsultation", mTeleconsultation);
        startActivity(i);

        finish();
    }

    @Override
    public void setSpecUser(SpecUser user) {
        mSpecUser = user;
        mPager.setInternalCurrentItem(1, 0);
    }

    @Override
    public SpecUser getSpecUser() {
        return mSpecUser;
    }

    @Override
    public void setTeleconsultation(Teleconsultation selectedTc) {
        mTeleconsultation = selectedTc;
        mTeleconsultation.setSpecialist(getSpecUser());
        joinTeleconsultationSession(mTeleconsultation);
    }

    private void joinTeleconsultationSession(final Teleconsultation selectedTc) {
        Log.d(TAG, "joining mTeleconsultation session...");
        if (selectedTc.getLastSession().getState() == TeleconsultationSessionState.WAITING) {
            mConfigReader.joinSession(selectedTc.getLastSession().getId(), getSpecUser().getAccessToken(), new Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "Session Join Response:" + response);
                    selectedTc.getLastSession().setupSessionData(response);
                    startTeleconsultationActivity();
                }
            }, new ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError err) {
                    Log.d(TAG, "Error in Session Join Response: " + err);
                }
            });
        }
    }

    @Override
    public Teleconsultation getTeleconsultation() {
        return mTeleconsultation;
    }

    @Override
    public RemoteConfigReader getRemoteConfigReader() {
        return mConfigReader;
    }

    // Extend from SmartFragmentStatePagerAdapter now instead for more dynamic ViewPager items
    public static class PagerAdapter extends SmartFragmentStatePagerAdapter {

        private SpecConfigActivity mActivity;

        public PagerAdapter(SpecConfigActivity activity, FragmentManager fragmentManager) {
            super(fragmentManager);
            mActivity = activity;
        }

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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        String TAG = "DrawerItemClickListener";

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            String value = (String) parent.getItemAtPosition(position);
            Log.d(TAG, String.format("position %d, value %s", position, value));

            switch (position) {
                case 0: //SETTINGS
                    Intent intent = new Intent(SpecConfigActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
                case 1: //EXIT
                    finish();
                    break;

            }
        }
    }

}