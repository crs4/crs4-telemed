package it.crs4.most.demo.setup_fragments;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RESTClient;
import it.crs4.most.demo.ResponseHandlerDecorator;
import it.crs4.most.demo.TeleconsultationSetup;
import it.crs4.most.demo.models.Patient;
import it.crs4.most.demo.models.Room;
import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.models.TeleconsultationSession;
import it.crs4.most.demo.models.TeleconsultationSessionState;


public class SummaryFragment extends SetupFragment {

    private static String TAG = "SummaryFragment";
    private static final String IS_WAITING = "it.crs4.most.demo.summary_fragment.is_waiting";
    private static final String TELECONSULTATION = "it.crs4.most.demo.summary_fragment.teleconsultation";

    private TextView mTxtPatientFullName;
    private TextView mPatientId;
    private TextView mUrgency;
    private TextView mRoom;
    private ProgressDialog mWaitForSpecialistDialog;
    private Runnable mWaitForSpecialistTask;
    private Handler mWaitForSpecialistHandler;
    private Teleconsultation mTeleconsultation;
    private RESTClient mRESTClient;
    private boolean mWaitingForSpecialist;

    public static SummaryFragment newInstance(TeleconsultationSetup teleconsultationSetup) {
        SummaryFragment fragment = new SummaryFragment();
        Bundle args = new Bundle();
        args.putSerializable(TELECONSULTATION_SETUP_ARG, teleconsultationSetup);
        fragment.setArguments(args);
        Log.d(TAG, "creating summary");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mTxtPatientFullName = (TextView) v.findViewById(R.id.text_summary_patient_full_name);
        mPatientId = (TextView) v.findViewById(R.id.text_summary_patient_id);
        mUrgency = (TextView) v.findViewById(R.id.text_summary_urgency);
        mRoom = (TextView) v.findViewById(R.id.text_summary_room);
        Button butStartEmergency = (Button) v.findViewById(R.id.button_summary_start_emergency);
        butStartEmergency.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // bug to be fixed on the remote server during teleconsultation creation
                createNewTeleconsultation();
            }
        });

        mWaitForSpecialistDialog = new ProgressDialog(getActivity());
        mWaitForSpecialistDialog.setTitle(getString(R.string.waiting_for_specialist));
        mWaitForSpecialistDialog.setMessage(getString(R.string.wait_for_specialist_message));
        mWaitForSpecialistDialog.setCancelable(false);
        mWaitForSpecialistDialog.setCanceledOnTouchOutside(false);
        if (savedInstanceState != null) {
            mTeleconsultation = (Teleconsultation) savedInstanceState.getSerializable(TELECONSULTATION);
            if (savedInstanceState.getBoolean(IS_WAITING)) {
                waitForSpecialist();
            }
        }
        String configServerIP = QuerySettings.getConfigServerAddress(getActivity());
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(getActivity()));
        mRESTClient = new RESTClient(getActivity(), configServerIP, configServerPort);
        return v;
    }

    @Override
    public void onPause() {
        Log.d(TAG, "Cancel requests");
        mRESTClient.cancelRequests();
        if (mWaitForSpecialistTask != null) {
            mWaitForSpecialistHandler.removeCallbacks(mWaitForSpecialistTask);
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mWaitForSpecialistDialog != null) {
            mWaitForSpecialistDialog.dismiss();
        }
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_WAITING, mWaitingForSpecialist);
        outState.putSerializable(TELECONSULTATION, mTeleconsultation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            Patient patient = mTeleconsultationSetup.getPatient();
            if (patient == null) {
                mTxtPatientFullName.setText(R.string.unknown);
            }
            else {
                mTxtPatientFullName.setText(String.format("%s %s", patient.getName(), patient.getSurname()));
                mPatientId.setText(patient.getAccountNumber());
            }
            mUrgency.setText(mTeleconsultationSetup.getUrgency());
            mRoom.setText(mTeleconsultationSetup.getRoom().toString());
        }
    }

    @Override
    protected int getTitle() {
        return R.string.summary_title;
    }

    @Override
    protected int getLayoutContent() {
        return R.layout.summary_fragment;
    }

    private void createNewTeleconsultation() {
        final String description = "Teleconsultation 0001";  //TODO: this should be editable in the summary
        final String severity = mTeleconsultationSetup.getUrgency();
        final Patient patient = mTeleconsultationSetup.getPatient();
        Response.Listener<String> listener = new ResponseHandlerDecorator<>(getActivity(),
            new Response.Listener<String>() {
                @Override
                public void onResponse(String teleconsultationData) {
                    try {
                        JSONObject tcData = new JSONObject(teleconsultationData);
                        String uuid = tcData.getJSONObject("data").
                            getJSONObject("teleconsultation").
                            getString("uuid");
                        mTeleconsultation = new Teleconsultation(uuid, description, severity, null, patient);
                        createTeleconsultationSession();
                    }
                    catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });

        String patientUid = patient != null ? patient.getUid() : null;
        mRESTClient.createNewTeleconsultation(description, severity, patientUid, getAccessToken(),
            listener, mErrorListener);
    }

    private void createTeleconsultationSession() {
        final Room room = mTeleconsultationSetup.getRoom();
        final String ipAddress = QuerySettings.isArEnabled(getActivity()) ? getIPAddress() : "";

        Response.Listener<String> listener = new ResponseHandlerDecorator<>(getActivity(),
            new Response.Listener<String>() {
                @Override
                public void onResponse(String sessionData) {
                    Log.d(TAG, "Created teleconsultation session: " + sessionData);
                    try {
                        JSONObject jsonData = new JSONObject(sessionData).
                            getJSONObject("data").
                            getJSONObject("session");
                        String sessionUUID = jsonData.getString("uuid");
                        TeleconsultationSession s = new TeleconsultationSession(sessionUUID,
                            null, ipAddress, TeleconsultationSessionState.NEW, room);
                        mTeleconsultation.setLastSession(s);
                        startSession();
                    }
                    catch (JSONException e) {
                        Log.e(TAG, "Error parsing the new teleconsultation session creation response: " + e);
                        e.printStackTrace();
                    }
                }
            });

        mRESTClient.createNewTeleconsultationSession(mTeleconsultation.getId(), room.getId(),
            ipAddress, getAccessToken(), listener, mErrorListener);
    }

    private void startSession() {
        Response.Listener<JSONObject> listener = new ResponseHandlerDecorator<>(getActivity(),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    waitForSpecialist();
                }
            });

        mRESTClient.startSession(mTeleconsultation.getLastSession().getId(), getAccessToken(),
            listener, mErrorListener);
    }

    private void waitForSpecialist() {
        mWaitForSpecialistDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
            getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    closeSessionAndTeleconsultation();
                }
            });
        mWaitingForSpecialist = true;
        mWaitForSpecialistDialog.show();

        final Response.Listener<JSONObject> listener = new ResponseHandlerDecorator<>(getActivity(),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject res) {
                    try {

                        String state = res.getJSONObject("data").getJSONObject("session").getString("state");
                        String specAppAddress = res.getJSONObject("data").getJSONObject("session").getString("spec_app_address");
                        Log.d(TAG, "NEW STATE: " + state);
                        if (state.equals(TeleconsultationSessionState.WAITING.name())) {
                            mWaitForSpecialistHandler.postDelayed(mWaitForSpecialistTask, 5000);
                        }
                        else if (state.equals(TeleconsultationSessionState.CLOSE.name())) {
                            mWaitForSpecialistDialog.dismiss();
                        }
                        else {
                            mTeleconsultation.getLastSession().setSpecAppAddress(specAppAddress);
                            runSession();
                        }
                    }
                    catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        );

        mWaitForSpecialistHandler = new Handler();
        mWaitForSpecialistTask = new Runnable() {
            @Override
            public void run() {
                mRESTClient.getSessionState(mTeleconsultation.getLastSession().getId(), getAccessToken(),
                    listener,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            Log.e(TAG, "Error reading Teleconsultation state response:" + arg0);
                            showError();
                            mWaitForSpecialistDialog.dismiss();
                        }
                    }
                );
            }
        };
        mWaitForSpecialistHandler.post(mWaitForSpecialistTask);
    }

    private void closeSessionAndTeleconsultation() {
        final String accessToken = getAccessToken();

        final Response.Listener<JSONObject> listener = new ResponseHandlerDecorator<>(getActivity(),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "Session closed");
                    mRESTClient.closeTeleconsultation(
                        mTeleconsultation.getId(),
                        accessToken,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, "Teleconsulation closed");
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "Error closing teleconsultation");
                            }
                        }
                    );
                }
            }

        );

        mRESTClient.closeSession(mTeleconsultation.getLastSession().getId(), accessToken, listener,
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error closing teleconsultation");
                }
            });
    }

    private void runSession() {
        final Response.Listener<JSONObject> listener = new ResponseHandlerDecorator<>(getActivity(),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject sessionData) {
                    String role = QuerySettings.getRole(getActivity());
                    try {
                        sessionData = sessionData.getJSONObject("data").getJSONObject("session");
                        Log.d(TAG, "Session running: " + sessionData);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mTeleconsultation.getLastSession().setVoipParams(getActivity(), sessionData, role);
                    mTeleconsultationSetup.setTeleconsultation(mTeleconsultation);
                    stepDone();
                }
            });

        mRESTClient.runSession(mTeleconsultation.getLastSession().getId(), getAccessToken(),
            listener, mErrorListener);
    }

    public String getAccessToken() {
        return QuerySettings.getAccessToken(getActivity());
    }
}