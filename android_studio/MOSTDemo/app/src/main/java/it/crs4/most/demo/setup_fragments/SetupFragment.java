package it.crs4.most.demo.setup_fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import it.crs4.most.demo.R;
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.setup_fragment, container, false);
        TextView titleText = (TextView) v.findViewById(R.id.setup_fragment_title);
        titleText.setText(getTitle());
        int layoutRes = getLayoutContent();
        if (layoutRes != -1) {
            FrameLayout placeHolder = (FrameLayout) v.findViewById(R.id.setup_fragment_container);
            inflater.inflate(getLayoutContent(), placeHolder);
        }
        return v;
    }

    @Override
    public Context getContext() {
        return super.getContext();
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

    protected abstract int getTitle();

    protected int getLayoutContent() {
        return -1;
    }
}
