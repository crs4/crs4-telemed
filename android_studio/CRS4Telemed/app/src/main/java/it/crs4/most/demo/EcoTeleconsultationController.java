package it.crs4.most.demo;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import it.crs4.most.demo.eco.AREcoTeleconsultationActivity;
import it.crs4.most.demo.eco.BaseEcoTeleconsultationActivity;
import it.crs4.most.demo.eco.EcoTeleconsultationActivity;
import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.setup_fragments.PatientSearchFragment;
import it.crs4.most.demo.setup_fragments.PatientSelectionFragment;
import it.crs4.most.demo.setup_fragments.SummaryFragment;
import it.crs4.most.demo.setup_fragments.TeleconsultationSelectionFragment;
import it.crs4.most.demo.setup_fragments.UrgencyRoomFragment;

class EcoTeleconsultationController extends TeleconsultationController {

    private static final String TAG = "EcoTeleconsultationController";
    private String mAction;
    private TeleconsultationSetup mTeleconsultationSetup;

    EcoTeleconsultationController(FragmentManager fm, String action,
                                  TeleconsultationSetup teleconsultationSetup) {
        super(fm);
        mAction = action;
        mTeleconsultationSetup = teleconsultationSetup;
    }

    @Override
    public Fragment getItem(int position) {
        if (mAction.equals(TeleconsultationSetupActivity.ACTION_NEW_TELE)) {
            switch (position) {
                case 0:
                    return PatientSearchFragment.newInstance(mTeleconsultationSetup);
                case 1:
                    return PatientSelectionFragment.newInstance(mTeleconsultationSetup);
                case 2:
                    return UrgencyRoomFragment.newInstance(mTeleconsultationSetup);
                case 3:
                    return SummaryFragment.newInstance(mTeleconsultationSetup);
                default:
                    return null;
            }
        }
        else {
            if (position == 0) {
                return TeleconsultationSelectionFragment.newInstance(mTeleconsultationSetup);
            }
            return null;
        }
    }

    @Override
    public int getCount() {
        return mAction.equals(TeleconsultationSetupActivity.ACTION_NEW_TELE) ? 4 : 1;
    }

    @Override
    public void startTeleconsultationActivity(Activity activity, Teleconsultation teleconsultation) {
        Intent i;
        boolean isArEnabled = QuerySettings.isArEnabled(activity);
        if (isArEnabled) {
            i = new Intent(activity, AREcoTeleconsultationActivity.class);
        }
        else {
            i = new Intent(activity, EcoTeleconsultationActivity.class);
        }
        i.putExtra(BaseEcoTeleconsultationActivity.TELECONSULTATION_ARG, teleconsultation);
        activity.startActivityForResult(i, EcoTeleconsultationActivity.TELECONSULT_ENDED_REQUEST);
    }
}
