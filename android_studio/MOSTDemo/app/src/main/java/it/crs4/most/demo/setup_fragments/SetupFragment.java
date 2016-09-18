package it.crs4.most.demo.setup_fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.crs4.most.demo.TeleconsultationSetup;
import it.crs4.most.demo.TeleconsultationSetupActivity;

public abstract class SetupFragment extends Fragment {

    protected static final String TELECONSULTATION_SETUP = "TELECONSULTATION_SETUP";
    private static final String TAG = "SetupFragment";
    protected TeleconsultationSetup mTeleconsultationSetup;

    public SetupFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTeleconsultationSetup = (TeleconsultationSetup) getArguments().getSerializable(TELECONSULTATION_SETUP);
    }

    public TeleconsultationSetup getTeleconsultationSetup() {
        return mTeleconsultationSetup;
    }

    public void setTeleconsultationSetup(TeleconsultationSetup teleconsultationSetup) {
        mTeleconsultationSetup = teleconsultationSetup;
        Log.d(TAG, "Received TC SETUP " + teleconsultationSetup);
    }

    protected void nextStep() {
        ((TeleconsultationSetupActivity) getActivity()).nextStep();
    }

}
