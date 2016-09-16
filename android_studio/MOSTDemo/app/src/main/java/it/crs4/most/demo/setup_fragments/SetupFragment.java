package it.crs4.most.demo.setup_fragments;

import android.support.v4.app.Fragment;

import it.crs4.most.demo.TeleconsultationSetup;

public abstract class SetupFragment extends Fragment {

    protected TeleconsultationSetup mTeleconsultationSetup;
    public SetupFragment() {
    }

    public TeleconsultationSetup getTeleconsultationSetup() {
        return mTeleconsultationSetup;
    }

    public void setTeleconsultationSetup(TeleconsultationSetup teleconsultationSetup) {
        mTeleconsultationSetup = teleconsultationSetup;
    }

    public abstract void onShow();

}
