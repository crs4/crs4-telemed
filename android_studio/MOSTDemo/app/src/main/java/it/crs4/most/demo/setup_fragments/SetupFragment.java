package it.crs4.most.demo.setup_fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EventListener;

import it.crs4.most.demo.LoginActivity;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RESTClient;
import it.crs4.most.demo.TeleconsultationSetup;
import it.crs4.most.demo.TeleconsultationSetupActivity;

public abstract class SetupFragment extends Fragment {

    private static final String TAG = "SetupFragment";
    protected static final String TELECONSULTATION_SETUP = "TELECONSULTATION_SETUP";
    protected TeleconsultationSetup mTeleconsultationSetup;
    private ArrayList<StepEventListener> mEventListeners;

    public SetupFragment() {
        mEventListeners = new ArrayList<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTeleconsultationSetup((TeleconsultationSetup) getArguments().getSerializable(TELECONSULTATION_SETUP));
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

    public void onShow() {}

    public TeleconsultationSetup getTeleconsultationSetup() {
        return mTeleconsultationSetup;
    }

    public void setTeleconsultationSetup(TeleconsultationSetup teleconsultationSetup) {
        mTeleconsultationSetup = teleconsultationSetup;
    }

    protected void stepDone() {
        for (StepEventListener listener: mEventListeners) {
            listener.onStepDone();
        }
    }

    protected void skipStep() {
        for (StepEventListener listener: mEventListeners) {
            listener.onSkipStep();
        }
    }

    public void addEventListener(StepEventListener listener) {
        mEventListeners.add(listener);
    }

    protected abstract int getTitle();

    protected int getLayoutContent() {
        return -1;
    }

    public interface StepEventListener extends EventListener {
        void onStepDone();

        void onSkipStep();
    }
}
