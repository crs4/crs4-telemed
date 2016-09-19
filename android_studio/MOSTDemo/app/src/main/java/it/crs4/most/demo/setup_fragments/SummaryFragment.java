package it.crs4.most.demo.setup_fragments;

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
import it.crs4.most.demo.TeleconsultationSetup;
import it.crs4.most.demo.models.Patient;
import it.crs4.most.demo.models.Room;
import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.models.TeleconsultationSession;
import it.crs4.most.demo.models.TeleconsultationSessionState;


public class SummaryFragment extends SetupFragment {

    private static String TAG = "SummaryFragment";

    private TextView mTxtPatientFullName;
    private TextView mPatientId;
    private Runnable mWaitForSpecialistTask;
    private Handler mWaitForSpecialistHandler;
    private RESTClient mRESTClient;

    public static SummaryFragment newInstance(TeleconsultationSetup teleconsultationSetup) {
        SummaryFragment fragment = new SummaryFragment();
        Bundle args = new Bundle();
        args.putSerializable(TELECONSULTATION_SETUP, teleconsultationSetup);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
//        View view = inflater.inflate(R.layout.summary_fragment, container, false);
        mTxtPatientFullName = (TextView) v.findViewById(R.id.text_summary_patient_full_name);
        mPatientId = (TextView) v.findViewById(R.id.text_summary_patient_id);
        Button butStartEmergency = (Button) v.findViewById(R.id.button_summary_start_emergency);
        butStartEmergency.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // bug to be fixed on the remote server during teleconsultation creation
                createNewTeleconsultation();
            }
        });

        String configServerIP = QuerySettings.getConfigServerAddress(getActivity());
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(getActivity()));
        mRESTClient = new RESTClient(getActivity(), configServerIP, configServerPort);
        return v;
    }

    @Override
    public void onPause() {
        mRESTClient.cancelRequests();
        super.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            Patient patient = mTeleconsultationSetup.getPatient();
            if (patient != null) {
                mTxtPatientFullName.setText(String.format("%s %s", patient.getName(), patient.getSurname()));
                mPatientId.setText(patient.getId());
                mTxtPatientFullName.setFocusable(false);
                mPatientId.setFocusable(false);
                mPatientId.setFocusable(false);
            }
            else {
                mTxtPatientFullName.setFocusable(true);
                mPatientId.setFocusable(true);
            }
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
        final Room room = mTeleconsultationSetup.getRoom();

        Log.d(TAG, String.format("Creating teleconsultation with room: %s and desc:%s", room.getId(), description));
        mRESTClient
            .createNewTeleconsultation(
                description,
                severity,
                room.getId(),
                getAccessToken(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String teleconsultationData) {
                        Log.d(TAG, "Created teleconsultation: " + teleconsultationData);
                        try {
                            //TODO_ Why the request is not a jsonpost request?
                            JSONObject tcData = new JSONObject(teleconsultationData);
                            String uuid = tcData.getJSONObject("data").
                                getJSONObject("teleconsultation").
                                getString("uuid");
                            Teleconsultation t = new Teleconsultation(uuid, description, severity);

                            createTeleconsultationSession(t);
                        }
                        catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError err) {
                        Log.e(TAG, "Error creating the new teleconsultation: " + err);
                    }
                });
    }

    private void createTeleconsultationSession(final Teleconsultation teleconsultation) {
        final Room room = mTeleconsultationSetup.getRoom();
        mRESTClient.createNewTeleconsultationSession(
            teleconsultation.getId(),
            room.getId(),
            getAccessToken(),
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
                            null, TeleconsultationSessionState.NEW, room);
                        teleconsultation.setLastSession(s);
                        startSession(teleconsultation);
                    }
                    catch (JSONException e) {
                        Log.e(TAG, "Error parsing the new teleconsultation session creation response: " + e);
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError err) {
                    Log.e(TAG, "Error creating the new teleconsultation session: " + err);
                }
            });
    }

    private void startSession(final Teleconsultation tc) {
        mRESTClient.startSession(
            tc.getLastSession().getId(),
            getAccessToken(),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject arg0) {
                    Log.d(TAG, "Session started: " + arg0);
                    waitForSpecialist(tc);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError arg0) {
                    Log.e(TAG, "Error startung session: " + arg0);
                }
            });
    }

    private void waitForSpecialist(final Teleconsultation tc) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(getString(R.string.waiting_for_specialist));
        dialog.setMessage(getString(R.string.wait_for_specialist_message));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
            getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    closeSessionAndTeleconsultation(tc);
                }
            });
        dialog.show();
        pollForSpecialist(dialog, tc);
    }

    private void closeSessionAndTeleconsultation(final Teleconsultation tc) {
        final String accessToken = getAccessToken();
        mRESTClient.closeSession(
            tc.getLastSession().getId(),
            accessToken,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "Sessione closed");
                    mRESTClient.closeTeleconsultation(
                        tc.getId(),
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
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error closing teleconsultation");
                }
            });
    }

    private void pollForSpecialist(final ProgressDialog dialog, final Teleconsultation tc) {
        mWaitForSpecialistHandler = new Handler();
        mWaitForSpecialistTask = new Runnable() {
            @Override
            public void run() {
                mRESTClient.getSessionState(
                    tc.getLastSession().getId(),
                    getAccessToken(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject res) {
                            Log.d(TAG, "Teleconsultation state response:" + res);
                            try {
                                String state = res.getJSONObject("data").getJSONObject("session").getString("state");
                                String specAppAddress = res.getJSONObject("data").getJSONObject("session").getString("spec_app_address");
                                if (state.equals(TeleconsultationSessionState.WAITING.name())) {
                                    mWaitForSpecialistHandler.postDelayed(mWaitForSpecialistTask, 10000);
                                }
                                else if (state.equals(TeleconsultationSessionState.CLOSE.name())) {
                                    dialog.dismiss();
                                }
                                else {
                                    tc.getLastSession().setSpecAppAddress(specAppAddress);
                                    dialog.dismiss();
                                    runSession(tc);
                                }
                            }
                            catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            Log.e(TAG, "Error reading Teleconsultation state response:" + arg0);
                            dialog.dismiss();
                        }
                    });
            }
        };
        mWaitForSpecialistHandler.post(mWaitForSpecialistTask);
    }

    private void runSession(final Teleconsultation tc) {
        mRESTClient.runSession(
            tc.getLastSession().getId(),
            getAccessToken(),
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
                    tc.getLastSession().setVoipParams(getActivity(), sessionData, role);
                    mTeleconsultationSetup.setTeleconsultation(tc);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error running the session: " + error);
                }
            });
    }

    public String getAccessToken() {
        return QuerySettings.getAccessToken(getActivity());
    }
}