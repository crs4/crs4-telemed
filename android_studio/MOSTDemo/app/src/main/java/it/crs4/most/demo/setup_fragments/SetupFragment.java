package it.crs4.most.demo.setup_fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.EventListener;

import it.crs4.most.demo.R;
import it.crs4.most.demo.SetupActivity;
import it.crs4.most.demo.TeleconsultationSetup;
import it.crs4.most.demo.TeleconsultationSetupActivity;

public abstract class SetupFragment extends Fragment {

    private static final String TAG = "SetupFragment";
    protected static final String TELECONSULTATION_SETUP_ARG =
        "it.crs4.most.demo.setup_fragment.teleconsultation_setup_arg";
    protected TeleconsultationSetup mTeleconsultationSetup;
    protected Response.ErrorListener mErrorListener;
    private ArrayList<StepEventListener> mEventListeners;

    public SetupFragment() {
        mEventListeners = new ArrayList<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err) {
                showError();
            }
        };
        setTeleconsultationSetup((TeleconsultationSetup) getArguments().getSerializable(TELECONSULTATION_SETUP_ARG));
        addEventListener((SetupActivity) getActivity());
        super.onCreate(savedInstanceState);
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

    public void setTeleconsultationSetup(TeleconsultationSetup teleconsultationSetup) {
        mTeleconsultationSetup = teleconsultationSetup;
    }

    protected void stepDone() {
        for (StepEventListener listener : mEventListeners) {
            listener.onStepDone();
        }
    }

    protected void skipStep() {
        for (StepEventListener listener : mEventListeners) {
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

    protected void showError() {
        new AlertDialog.Builder(getActivity())
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setTitle(R.string.error)
            .setMessage(R.string.server_connection_error)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    getActivity().finish();
                }
            })
            .create()
            .show();
    }

    public interface StepEventListener extends EventListener {
        void onStepDone();

        void onSkipStep();
    }
}
