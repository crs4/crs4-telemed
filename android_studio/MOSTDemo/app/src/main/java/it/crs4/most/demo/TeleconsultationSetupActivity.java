package it.crs4.most.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.lang.ref.WeakReference;

import it.crs4.most.demo.eco.EcoTeleconsultationActivity;
import it.crs4.most.demo.models.Device;
import it.crs4.most.demo.models.Patient;
import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.setup_fragments.SetupFragment;
import it.crs4.most.demo.spec.SpecTeleconsultationActivity;


public class TeleconsultationSetupActivity extends AppCompatActivity {
    private static final String TAG = "TeleconsultSetupAct";

    private SetupFragment[] mSetupFragments;
    private TeleconsultationController mTcController;
    private ViewPager mVpPager;
    private Device mCamera;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teleconsultation_setup_activity);

        TeleconsultationSetup teleconsultationSetup = new TeleconsultationSetup(this);
        mTcController = TeleconsultationControllerFactory.getTeleconsultationController(this);
        mSetupFragments = mTcController.getFragments(teleconsultationSetup);

        mVpPager = (ViewPager) findViewById(R.id.vp_pager);
        FragmentStatePagerAdapter pagerAdapter = new CustomPagerAdapter(this, getSupportFragmentManager());
        mVpPager.setAdapter(pagerAdapter);
    }

    public void nextStep() {
        int newItem = mVpPager.getCurrentItem() + 1;
        mVpPager.setCurrentItem(newItem);
        mSetupFragments[newItem].onShow();
    }

    public void startTeleconsultationActivity(Teleconsultation teleconsultation) {
        mTcController.startTeleconsultationActivity(this, teleconsultation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EcoTeleconsultationActivity.TELECONSULT_ENDED_REQUEST ||
            requestCode == SpecTeleconsultationActivity.TELECONSULT_ENDED_REQUEST) {
            if (resultCode == RESULT_OK) {
//                setPatient(null);
//                setTeleconsultation(null);
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

    public static class CustomPagerAdapter extends FragmentStatePagerAdapter {
        private TeleconsultationSetupActivity act;

        public CustomPagerAdapter(TeleconsultationSetupActivity outerRef, FragmentManager fragmentManager) {
            super(fragmentManager);
            act = new WeakReference<>(outerRef).get();
        }

        @Override
        public int getCount() {
            return act.mSetupFragments.length;
        }

        @Override
        public Fragment getItem(int position) {
            if (position >= 0 && position < getCount()) {
                return act.mSetupFragments[position];
            }
            else {
                return null;
            }
        }
    }
}