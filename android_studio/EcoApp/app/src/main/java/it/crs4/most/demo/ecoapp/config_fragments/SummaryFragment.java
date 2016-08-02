package it.crs4.most.demo.ecoapp.config_fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import it.crs4.most.demo.ecoapp.IConfigBuilder;
import it.crs4.most.demo.ecoapp.R;
import it.crs4.most.demo.ecoapp.RemoteConfigReader;
import it.crs4.most.demo.ecoapp.models.EcoUser;
import it.crs4.most.demo.ecoapp.models.Patient;
import it.crs4.most.demo.ecoapp.models.Room;
import it.crs4.most.demo.ecoapp.models.Device;
import it.crs4.most.demo.ecoapp.models.Teleconsultation;
import it.crs4.most.demo.ecoapp.models.TeleconsultationSession;
import it.crs4.most.demo.ecoapp.models.TeleconsultationSessionState;

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
        SummaryFragment fragmentFirst = new SummaryFragment();
        fragmentFirst.setConfigBuilder(config);
        return fragmentFirst;
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
        Log.d(TAG, "Trying to create a new teleconsultation...");
        final String description = "Teleconsultation 0001";
        final String severity = mSeveritySpinner.getSelectedItem().toString();
        final Room room = (Room) mRoomSpinner.getSelectedItem();
        retrieveRoomDevices(room);

        Log.d(TAG, String.format("Creating teleconsultation with room: %s and desc:%s", room.getId(), description));
        getConfigBuilder().getRemoteConfigReader().
                createNewTeleconsultation(
                        description,
                        severity,
                        room.getId(),
                        getConfigBuilder().getEcoUser().getAccessToken(),
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String teleconsultationData) {
                                Log.d(TAG, "Created teleconsultation: " + teleconsultationData);
                                try {
                                    JSONObject tcData = new JSONObject(teleconsultationData);
                                    String uuid = tcData.getJSONObject("data").getJSONObject("teleconsultation").getString("uuid");
                                    Teleconsultation tc = new Teleconsultation(uuid, "", description, severity,
                                            room, getConfigBuilder().getEcoUser());

                                    createTeleconsultationSession(tc);
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
        EcoUser ecoUser = getConfigBuilder().getEcoUser();
        getConfigBuilder().getRemoteConfigReader().createNewTeleconsultationSession(
                teleconsultation.getId(),
                teleconsultation.getRoom().getId(),
                ecoUser.getAccessToken(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String tcSessionData) {
                        Log.d(TAG, "Created teleconsultation session: " + tcSessionData);
                        try {
                            String sessionUUID = new JSONObject(tcSessionData).
                                    getJSONObject("data").
                                    getJSONObject("session").
                                    getString("uuid");
                            TeleconsultationSession ts = new TeleconsultationSession(sessionUUID,
                                    TeleconsultationSessionState.NEW);
                            teleconsultation.setLastSession(ts);
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
        EcoUser ecoUser = getConfigBuilder().getEcoUser();
        getConfigBuilder().getRemoteConfigReader().startSession(
                tc.getLastSession().getId(),
                ecoUser.getAccessToken(),
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

    private void retrieveRoomDevices(final Room room) {
        EcoUser ecoUser = getConfigBuilder().getEcoUser();
        getConfigBuilder().getRemoteConfigReader().getRoom(
                room.getId(),
                ecoUser.getAccessToken(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jroom) {
                        room.setEncoder(getDevice(jroom, "encoder"));
                        room.setCamera(getDevice(jroom, "camera"));
                        Log.d(TAG, "TC Encoder: " + room.getEncoder());
                        Log.d(TAG, "TC Camera: " + room.getCamera());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Log.e(TAG, "Error retrieving device json data: " + e);

                    }
                });
    }


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
        final String accessToken = getConfigBuilder().getEcoUser().getAccessToken();
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
                        tc.getApplicant().getAccessToken(),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject res) {
                                Log.d(TAG, "Teleconsultation state response:" + res);
                                try {
                                    String state = res.getJSONObject("data").getJSONObject("session").getString("state");
                                    if (state.equals(TeleconsultationSessionState.WAITING.name())) {
                                        mWaitForSpecialistHandler.postDelayed(mWaitForSpecialistTask, 10000);
                                        return;
                                    }
                                    else if (state.equals(TeleconsultationSessionState.CLOSE.name())) {
                                        wfsd.dismiss();
                                        return;
                                    }
                                }
                                catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                wfsd.dismiss();
                                runSession(tc);
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
        EcoUser ecoUser = getConfigBuilder().getEcoUser();
        getConfigBuilder().getRemoteConfigReader().runSession(tc.getLastSession().getId(),
                ecoUser.getAccessToken(),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject sessionData) {
                        Log.d(TAG, "Session running: " + sessionData);
                        tc.getLastSession().setVoipParams(getActivity(), sessionData);
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

    private Device getDevice(JSONObject room, String deviceName) {
        JSONObject jsonDevice;
        try {
            jsonDevice = room.getJSONObject("data").getJSONObject("room").
                    getJSONObject("devices").getJSONObject(deviceName);
            return new Device(jsonDevice.getString("name"),
                    jsonDevice.getJSONObject("capabilities").getString("streaming"),
                    jsonDevice.getJSONObject("capabilities").getString("shot"),
                    jsonDevice.getJSONObject("capabilities").getString("web"),
                    jsonDevice.getJSONObject("capabilities").getString("ptz"),
                    jsonDevice.getString("user"),
                    jsonDevice.getString("password")
            );
        }
        catch (JSONException e) {
            Log.e(TAG, "Error retrieving device data: " + e);
            e.printStackTrace();
        }
        return null;
    }

    private void setupTcSeveritySpinner(View view) {
        mSeveritySpinner = (Spinner) view.findViewById(R.id.spinner_summary_severity);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.tc_severities, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSeveritySpinner.setAdapter(adapter);
    }

    private void setupTcRoomSpinner(View view) {
        mRoomSpinner = (Spinner) view.findViewById(R.id.spinner_summary_room);
        retrieveRooms();
    }


    private void retrieveRooms() {
        if (getConfigBuilder() != null) {
            Log.d(TAG, String.format("getConfigBuilder() == null %b", getConfigBuilder() == null));
            Log.d(TAG, String.format("getConfigBuilder().getEcoUser() == null %b", getConfigBuilder().getEcoUser() == null));

            String accessToken = getConfigBuilder().getEcoUser().getAccessToken();

            getConfigBuilder().getRemoteConfigReader().getRooms(accessToken, new Response.Listener<JSONObject>() {


                @Override
                public void onResponse(JSONObject rooms) {

                    Log.d(TAG, String.format("Received rooms:\n\n%s\n\n", rooms));
                    populateTcRoomSpinner(rooms);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError arg0) {
                    Log.e(TAG, "Error retrieving rooms:" + arg0);

                }
            });
        }
    }

    private void populateTcRoomSpinner(JSONObject roomsData) {
        //{"data":{"rooms":[{"description":"Stanza di Test","name":"Ufficio Francesco","uuid":"yytgsihw4vtm5tpceunnoyclwvbnwi53"}]},"success":true}
        try {
            JSONArray jrooms = roomsData.getJSONObject("data").getJSONArray("rooms");
            ArrayList<Room> rooms = new ArrayList<Room>();

            for (int i = 0; i < jrooms.length(); i++) {
                JSONObject jroom = jrooms.getJSONObject(i);
                rooms.add(new Room(jroom.getString("uuid"), jroom.getString("name"), jroom.getString("description")));
            }

            ArrayAdapter<Room> spinnerArrayAdapter = new ArrayAdapter<Room>(getActivity(), android.R.layout.simple_spinner_item, rooms);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
            mRoomSpinner.setAdapter(spinnerArrayAdapter);

        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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


}