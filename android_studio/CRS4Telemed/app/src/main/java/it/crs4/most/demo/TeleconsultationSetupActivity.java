package it.crs4.most.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;

import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.setup_fragments.SetupFragment;


public class TeleconsultationSetupActivity extends SetupActivity
    implements SetupFragment.StepEventListener {
    public static final String ACTION_ARG = "it.crs4.most.demo.action";
    public static final String ACTION_NEW_TELE = "it.crs4.most.demo.action_new_tele";
    public static final String ACTION_CONTINUE_TELE = "it.crs4.most.demo.action_continue_tele";

    private static final String TELECONSULTATION_SETUP = "it.crs4.most.demo.teleconsultation_setup";
    private static final String TAG = "TeleconsultSetupAct";

    private TeleconsultationController mTcController;
    private ViewPager mVpPager;
    private TeleconsultationSetup mTeleconsultationSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.teleconsultation_setup_activity);
        if (savedInstanceState != null) {
            mTeleconsultationSetup = (TeleconsultationSetup) savedInstanceState.getSerializable(TELECONSULTATION_SETUP);
        }
        else {
            mTeleconsultationSetup = new TeleconsultationSetup();
        }

        String action = getIntent().getExtras().getString(ACTION_ARG);

        mTcController = TeleconsultationControllerFactory.getTeleconsultationController(this,
            getSupportFragmentManager(), action, mTeleconsultationSetup);
        mVpPager = (ViewPager) findViewById(R.id.vp_pager);
        mVpPager.setAdapter(mTcController);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(TELECONSULTATION_SETUP, mTeleconsultationSetup);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        mNavDirection = 1;
        if (mVpPager.getCurrentItem() != 0) {
            previousStep();
            SetupFragment current = ((SetupFragment) mTcController
                .getFragment(mVpPager.getId(), mVpPager.getCurrentItem()));
            current.onShow();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void nextStep() {
        int newItem = mVpPager.getCurrentItem() + 1;
        mNavDirection = 0;
        if (newItem == mTcController.getCount()) {
            startTeleconsultationActivity(mTeleconsultationSetup.getTeleconsultation());
        }
        else {
            mVpPager.setCurrentItem(newItem);
            SetupFragment current = ((SetupFragment) mTcController
                .getFragment(mVpPager.getId(), mVpPager.getCurrentItem()));
            current.onShow();
        }
    }

    @Override
    public void previousStep() {
        int newItem = mVpPager.getCurrentItem() - 1;
        mVpPager.setCurrentItem(newItem);
    }

    public void startTeleconsultationActivity(Teleconsultation teleconsultation) {
        mTcController.startTeleconsultationActivity(this, teleconsultation);
    }

}