package it.crs4.most.demo.specapp.config_fragments;


import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import it.crs4.most.demo.specapp.IConfigBuilder;
import it.crs4.most.demo.specapp.R;
import it.crs4.most.demo.specapp.RemoteConfigReader;
import it.crs4.most.demo.specapp.models.SpecUser;
import it.crs4.most.demo.specapp.models.Teleconsultation;
import it.crs4.most.demo.specapp.models.TeleconsultationSession;
import it.crs4.most.demo.specapp.models.TeleconsultationSessionState;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TeleconsultationSelectionFragment extends ConfigFragment {

    private ArrayList<Teleconsultation> tcArray;
    private ArrayAdapter<Teleconsultation> tcArrayAdapter;
    private RemoteConfigReader mConfigReader;
    private Runnable mGetTeleconsultationsTask;
    private Handler mGetTeleconsultationsHandler;
    private static String TAG = "TeleconsultationSelectionFragment";

    public static TeleconsultationSelectionFragment newInstance(IConfigBuilder config) {
        TeleconsultationSelectionFragment fragmentTeleconsultationSel = new TeleconsultationSelectionFragment();
        fragmentTeleconsultationSel.setConfigBuilder(config);
        return fragmentTeleconsultationSel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mGetTeleconsultationsHandler = new Handler();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tc_list, container, false);
        ListView listView = (ListView) view.findViewById(R.id.teleconsultation_list);
        tcArray = new ArrayList<>();
        tcArrayAdapter = new TcArrayAdapter(this, R.layout.tc_row, tcArray);

        listView.setAdapter(tcArrayAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Teleconsultation selectedTc = tcArray.get(position);
                config.setTeleconsultation(selectedTc);
            }
        });
//        retrieveTeleconsultations();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "Called onAttach");
        super.onAttach(context);
//        retrieveTeleconsultations();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "Called on start");
        super.onStart();
    }

    @Override
    public void onPause() {
//        mGetTeleconsultationsHandler.removeCallbacks(mGetTeleconsultationsTask);
        Log.d(TAG, "Called onPause");
        super.onPause();
    }

    private void retrieveTeleconsultations() {
        Log.d(TAG, "called retrieveTeleconsultations()");
        mConfigReader = config.getRemoteConfigReader();
        final SpecUser specUser = config.getSpecUser();

        mGetTeleconsultationsTask = new Runnable() {
            @Override
            public void run() {
                tcArrayAdapter.clear();
                mGetTeleconsultationsHandler.postDelayed(
                        mGetTeleconsultationsTask, 5000);
                Log.d(TAG, "Running again");

                mConfigReader.getTeleconsultationsByTaskgroup(
                        specUser.getTaskgroupId(),
                        specUser.getAccessToken(),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {

                                    /**
                                     * {"data":
                                     {"teleconsultations":
                                     [{"created":1441369186,"description":"new Test Teleconsultation",
                                     "last_session":{"state":"WAITING","created":1441369186,"updated":1441369186,"uuid":"bbcmerfivua2mnqzymji2gpy6kaiwytc"},
                                     "uuid":"4afnydvlc5qb7rf4r7psldtlbckgnoqm"}
                                     ]},"success":true}
                                     */
                                    Log.d(TAG, "Teleconsultation list response: " + response);
                                    final JSONArray teleconsultations = response.
                                            getJSONObject("data").
                                            getJSONArray("teleconsultations");

                                    for (int i = 0; i < teleconsultations.length(); i++) {
                                        JSONObject item = teleconsultations.getJSONObject(i);
                                        String tcId = item.getString("uuid");
                                        String tcInfo = item.getString("description");
                                        JSONObject lastSession = item.getJSONObject("last_session");
                                        String lastSessionId = lastSession.getString("uuid");
                                        String lastSessionState = lastSession.getString("state");

                                        TeleconsultationSession tcs = new TeleconsultationSession(
                                                lastSessionId,
                                                TeleconsultationSessionState.getState(lastSessionState)
                                        );
                                        Log.d(TAG, "Adding teleconsultation " + i + " -> ID: " + tcId);
                                        tcArrayAdapter.add(new Teleconsultation(tcId, tcInfo, tcs));
                                    }
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
//                                handler.postDelayed(mGetTeleconsultationsTask, 5000);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError response) {
                                response.printStackTrace();
                            }
                        }
                );
            }
        };

        mGetTeleconsultationsHandler.post(mGetTeleconsultationsTask);
    }

    @Override
    public void onShow() {
        retrieveTeleconsultations();
    }
}