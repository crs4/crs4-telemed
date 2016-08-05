package it.crs4.most.demo.setup_fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import it.crs4.most.demo.ConfigFragment;
import it.crs4.most.demo.IConfigBuilder;
import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RemoteConfigReader;
import it.crs4.most.demo.TeleconsultationException;
import it.crs4.most.demo.models.User;
import it.crs4.most.demo.models.Patient;
import it.crs4.most.demo.models.Room;
import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.models.TeleconsultationSession;
import it.crs4.most.demo.models.TeleconsultationSessionState;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


public class SummaryFragment extends ConfigFragment {

    private static String TAG = "MostFragmentSummary";

    private EditText mTxtPatientFullName;
    private EditText mPatientId;
    private Spinner mSeveritySpinner;
    private Spinner mRoomSpinner;
    private Runnable mWaitForSpecialistTask;
    private Handler mWaitForSpecialistHandler;

    public static SummaryFragment newInstance(IConfigBuilder config) {
        SummaryFragment fragment = new SummaryFragment();
        fragment.setConfigBuilder(config);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);
        mTxtPatientFullName = (EditText) view.findViewById(R.id.text_summary_patient_full_name);
        mPatientId = (EditText) view.findViewById(R.id.text_summary_patient_id);
        Button butStartEmergency = (Button) view.findViewById(R.id.button_summary_start_emergency);
        butStartEmergency.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // bug to be fixed on the remote server during teleconsultation creation
                createNewTeleconsultation();
            }
        });
        setupTcSeveritySpinner(view);
        setupTcRoomSpinner(view);

        return view;
    }

    private void createNewTeleconsultation() {
        final String description = "Teleconsultation 0001";
        final String severity = mSeveritySpinner.getSelectedItem().toString();
        final Room room = (Room) mRoomSpinner.getSelectedItem();
//        retrieveRoomDevices(room);  This is not required anymore since getRooms now returns als info about the streaming devices

        Log.d(TAG, String.format("Creating teleconsultation with room: %s and desc:%s", room.getId(), description));
        getConfigBuilder().getRemoteConfigReader().
            createNewTeleconsultation(
                description,
                severity,
                room.getId(),
                getConfigBuilder().getUser().getAccessToken(),
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
                            Teleconsultation t = new Teleconsultation(uuid, description, severity,
                                room, getConfigBuilder().getUser());

                            createTeleconsultationSession(t);
                        }
                        catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError err) {
                        Log.e(TAG, "Error creating the new teleconsultation: " + err);
                    }
                });
    }

    private void createTeleconsultationSession(final Teleconsultation teleconsultation) {
        User user = getConfigBuilder().getUser();
        getConfigBuilder().getRemoteConfigReader().createNewTeleconsultationSession(
            teleconsultation.getId(),
            teleconsultation.getRoom().getId(),
            user.getAccessToken(),
            new Response.Listener<String>() {

                @Override
                public void onResponse(String tcSessionData) {
                    Log.d(TAG, "Created teleconsultation session: " + tcSessionData);
                    try {
                        JSONObject sessionData = new JSONObject(tcSessionData).
                            getJSONObject("data").
                            getJSONObject("session");
                        String sessionUUID = sessionData.getString("uuid");
                        TeleconsultationSession s = new TeleconsultationSession(sessionUUID,
                            null, TeleconsultationSessionState.NEW, teleconsultation.getRoom());
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
        User user = getConfigBuilder().getUser();
        getConfigBuilder().getRemoteConfigReader().startSession(
            tc.getLastSession().getId(),
            user.getAccessToken(),
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

//    private void retrieveRoomDevices(final Room room) {
//        User user = getConfigBuilder().getUser();
//        getConfigBuilder().getRemoteConfigReader().getRoom(
//            room.getId(),
//            user.getAccessToken(),
//            new Response.Listener<JSONObject>() {
//                @Override
//                public void onResponse(JSONObject jroom) {
//                    room.setEncoder(getDevice(jroom, "encoder"));
//                    room.setCamera(getDevice(jroom, "camera"));
//                    Log.d(TAG, "TC Encoder: " + room.getEncoder());
//                    Log.d(TAG, "TC Camera: " + room.getCamera());
//                }
//            },
//            new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError e) {
//                    Log.e(TAG, "Error retrieving device json data: " + e);
//
//                }
//            });
//    }


    private void waitForSpecialist(final Teleconsultation tc) {
        ProgressDialog waitForSpecialistDialog = new ProgressDialog(getActivity());
        waitForSpecialistDialog.setTitle(getString(R.string.waiting_for_specialist));
        waitForSpecialistDialog.setMessage("A consultation request has been sent\nWait for the specialist to respond");
        waitForSpecialistDialog.setCancelable(false);
        waitForSpecialistDialog.setCanceledOnTouchOutside(false);
        waitForSpecialistDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
            getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    closeSessionAndTeleconsultation(tc);
                }
            });
        waitForSpecialistDialog.show();
        pollForSpecialist(waitForSpecialistDialog, tc);
    }

    private void closeSessionAndTeleconsultation(final Teleconsultation tc) {
        final RemoteConfigReader mConfigReader = getConfigBuilder().getRemoteConfigReader();
        final String accessToken = getConfigBuilder().getUser().getAccessToken();
        mConfigReader.closeSession(
            tc.getLastSession().getId(),
            accessToken,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "Sessione closed");
                    mConfigReader.closeTeleconsultation(
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

    private void pollForSpecialist(final ProgressDialog wfsd, final Teleconsultation tc) {
        mWaitForSpecialistHandler = new Handler();
        mWaitForSpecialistTask = new Runnable() {
            @Override
            public void run() {
                getConfigBuilder().getRemoteConfigReader().getSessionState(
                    tc.getLastSession().getId(),
                    tc.getUser().getAccessToken(),
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
                                    wfsd.dismiss();
                                }
                                else {
                                    tc.getLastSession().setSpecAppAddress(specAppAddress);
                                    wfsd.dismiss();
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
                            wfsd.dismiss();
                        }
                    });
            }
        };
        mWaitForSpecialistHandler.post(mWaitForSpecialistTask);
    }

    private void runSession(final Teleconsultation tc) {
        User user = getConfigBuilder().getUser();
        getConfigBuilder().getRemoteConfigReader().runSession(tc.getLastSession().getId(),
            user.getAccessToken(),
            new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject sessionData) {
                    Log.d(TAG, "Session running: " + sessionData);
                    String role = QuerySettings.getRole(getActivity());
                    try {
                        sessionData = sessionData.getJSONObject("data").getJSONObject("session");
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    tc.getLastSession().setVoipParams(getActivity(), sessionData, role);
                    getConfigBuilder().setTeleconsultation(tc);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError arg0) {
                    Log.e(TAG, "Error running the session: " + arg0);
                }
            });
    }

//    private Device getDevice(JSONObject room, String deviceName) {
//        JSONObject jsonDevice;
//        try {
//            jsonDevice = room.getJSONObject("data").getJSONObject("room").
//                getJSONObject("devices").getJSONObject(deviceName);
//            return new Device(jsonDevice.getString("name"),
//                jsonDevice.getJSONObject("capabilities").getString("streaming"),
//                jsonDevice.getJSONObject("capabilities").getString("shot"),
//                jsonDevice.getJSONObject("capabilities").getString("web"),
//                jsonDevice.getJSONObject("capabilities").getString("ptz"),
//                jsonDevice.getString("user"),
//                jsonDevice.getString("password")
//            );
//        }
//        catch (JSONException e) {
//            Log.e(TAG, "Error retrieving device data: " + e);
//            e.printStackTrace();
//        }
//        return null;
//    }

    private void setupTcSeveritySpinner(View view) {
        mSeveritySpinner = (Spinner) view.findViewById(R.id.spinner_summary_severity);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
            R.array.tc_severities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSeveritySpinner.setAdapter(adapter);
    }

    private void setupTcRoomSpinner(View view) {
        mRoomSpinner = (Spinner) view.findViewById(R.id.spinner_summary_room);
        if (getConfigBuilder() != null) {
            String accessToken = getConfigBuilder().getUser().getAccessToken();
            getConfigBuilder().getRemoteConfigReader().getRooms(accessToken,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject roomsData) {
                        try {
                            JSONArray jrooms = roomsData.getJSONObject("data").getJSONArray("rooms");
                            ArrayList<Room> rooms = new ArrayList<>();

                            for (int i = 0; i < jrooms.length(); i++) {
                                JSONObject roomData = jrooms.getJSONObject(i);
                                Room r = null;
                                try {
                                    r = Room.fromJSON(roomData);
                                }
                                catch (TeleconsultationException e) {
                                    e.printStackTrace();
                                }
                                rooms.add(r);
                            }

                            ArrayAdapter<Room> adapter = new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_spinner_item, rooms);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                            mRoomSpinner.setAdapter(adapter);

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
                        Log.e(TAG, "Error retrieving rooms:" + arg0);
                    }
                });
        }
    }

    @Override
    public void onShow() {
        Patient patient = getConfigBuilder().getPatient();
        if (patient != null) {
            mTxtPatientFullName.setText(patient.getName() + " " + patient.getSurname());
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

    @Override
    public int getTitle() {
        return R.string.summary_title;
    }
}