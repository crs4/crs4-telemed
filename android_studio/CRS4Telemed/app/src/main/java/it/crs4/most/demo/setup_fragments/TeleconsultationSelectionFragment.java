package it.crs4.most.demo.setup_fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RESTClient;
import it.crs4.most.demo.ResponseHandlerDecorator;
import it.crs4.most.demo.TeleconsultationException;
import it.crs4.most.demo.TeleconsultationSetup;
import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.models.TeleconsultationSessionState;
import it.crs4.most.demo.spec.SpecTeleconsultationActivity;

public class TeleconsultationSelectionFragment extends SetupFragment {

    private static final String TAG = "TeleconsultSelFragment";
    private static final String DIALOG_SHOWN = "it.crs4.most.demo.teleconsultation_selection_fragment.dialog_shown";
    private ArrayList<Teleconsultation> mTeleconsultations;
    private ArrayAdapter<Teleconsultation> mAdapter;
    private RESTClient mRESTClient;
    private Runnable mGetTeleconsultationsTask;
    private Handler mGetTeleconsultationsHandler;
    private ListView mListView;
    private TextView mEmptyView;
    private ProgressDialog mLaunchingTcDialog;

    public static TeleconsultationSelectionFragment newInstance(TeleconsultationSetup teleconsultationSetup) {
        TeleconsultationSelectionFragment fragment = new TeleconsultationSelectionFragment();
        Bundle args = new Bundle();
        args.putSerializable(TELECONSULTATION_SETUP_ARG, teleconsultationSetup);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGetTeleconsultationsHandler = new Handler();
        String configServerIP = QuerySettings.getConfigServerAddress(getActivity());
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(getActivity()));
        mRESTClient = new RESTClient(getActivity(), configServerIP, configServerPort);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mListView = (ListView) view.findViewById(R.id.teleconsultation_list);
        mEmptyView = (TextView) view.findViewById(R.id.empty_view);
        mTeleconsultations = new ArrayList<>();
        mAdapter = new TeleconsultationAdapter(this, R.layout.teleconsultation_selection_fragment_item, mTeleconsultations);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Teleconsultation selectedTc = mTeleconsultations.get(position);
                mTeleconsultationSetup.setTeleconsultation(selectedTc);
                mLaunchingTcDialog.show();
                joinSession(selectedTc);
            }
        });

        mLaunchingTcDialog = new ProgressDialog(getActivity());
        mLaunchingTcDialog.setTitle(getString(R.string.wait));
        mLaunchingTcDialog.setMessage(getString(R.string.launghing_teleconsultation));
        mLaunchingTcDialog.setCancelable(false);
        mLaunchingTcDialog.setCanceledOnTouchOutside(false);
        if (savedInstanceState != null && savedInstanceState.getBoolean(DIALOG_SHOWN)) {
            mLaunchingTcDialog.show();
        }

        PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("select_task_group_preference", null);
        retrieveTeleconsultations();
        return view;
    }

    private void joinSession(final Teleconsultation teleconsultation) {
        String ipAddress = getIPAddress();
        Log.d(TAG, "IP address: " + ipAddress);
        String uri = "";
        if (ipAddress != null) {
            uri = ipAddress + ":" + SpecTeleconsultationActivity.ZMQ_LISTENING_PORT;
        }
        String configServerIP = QuerySettings.getConfigServerAddress(getActivity());
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(getActivity()));
        RESTClient mConfigReader = new RESTClient(getActivity(), configServerIP, configServerPort);
        String accessToken = QuerySettings.getAccessToken(getActivity());
        if (teleconsultation.getLastSession().getState() == TeleconsultationSessionState.WAITING) {
            mConfigReader.joinSession(teleconsultation.getLastSession().getId(),
                accessToken,
                uri,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject sessionData;
                        try {
                            sessionData = response.getJSONObject("data").getJSONObject("session");
                            String role = QuerySettings.getRole(getActivity());
                            // Only in this mooment the voipParam are complete with specialist information, so we set them
                            teleconsultation.getLastSession().setVoipParams(getActivity(), sessionData, role);
                            stepDone();
                        }
                        catch (JSONException e) {
                            Log.e(TAG, "Something wrong happened with the JSON structure");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError err) {
                        Log.d(TAG, "Error in Session Join Response: " + err);
                    }
                });
        }
    }

    @Override
    public void onPause() {
        mGetTeleconsultationsHandler.removeCallbacks(mGetTeleconsultationsTask);
        mRESTClient.cancelRequests();
        super.onPause();
    }

    @Override
    public void onStop() {
        mLaunchingTcDialog.dismiss();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(DIALOG_SHOWN, mLaunchingTcDialog.isShowing());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected int getTitle() {
        return R.string.teleconsultation_title;
    }

    @Override
    protected int getLayoutContent() {
        return R.layout.teleconsultation_selection_fragment;
    }

    private void retrieveTeleconsultations() {
        final String accessToken = QuerySettings.getAccessToken(getActivity());
        final String taskGroup = QuerySettings.getTaskGroup(getActivity());
        mGetTeleconsultationsTask = new Runnable() {
            @Override
            public void run() {
                mGetTeleconsultationsHandler.postDelayed(mGetTeleconsultationsTask, 10000);
                ResponseHandlerDecorator<JSONObject> listener = new ResponseHandlerDecorator<>(getActivity(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                final JSONArray teleconsultations = response
                                    .getJSONObject("data")
                                    .getJSONArray("teleconsultations");
                                mTeleconsultations = new ArrayList<>();
                                for (int i = 0; i < teleconsultations.length(); i++) {
                                    JSONObject item = teleconsultations.getJSONObject(i);

                                    Teleconsultation t = null;
                                    try {
                                        String role = QuerySettings.getRole(getActivity());
                                        t = Teleconsultation.fromJSON(getActivity(), item, role);
                                    }
                                    catch (TeleconsultationException e) {
                                        Log.e(TAG, "There's something wrong with the JSON structure returned by the server");
                                    }
                                    mTeleconsultations.add(t);
                                }
                                mAdapter.clear();
                                mAdapter.addAll(mTeleconsultations);
                                mAdapter.notifyDataSetChanged();
                                if (mTeleconsultations.size() == 0) {
                                    mListView.setEmptyView(mEmptyView);
                                }
                            }
                            catch (JSONException e) {
                                Log.e(TAG, "There's something wrong with the JSON structure returned by the server");
                            }
                        }
                    }
                );
                mRESTClient.getWaitingTeleconsultationsByTaskgroup(taskGroup, accessToken, listener,
                    mErrorListener);
            }
        };

        mGetTeleconsultationsHandler.post(mGetTeleconsultationsTask);
    }

    private static class TeleconsultationAdapter extends ArrayAdapter<Teleconsultation> {

        TeleconsultationAdapter(TeleconsultationSelectionFragment fragment,
                                int textViewId, List<Teleconsultation> objects) {
            super(fragment.getActivity(), textViewId, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.teleconsultation_selection_fragment_item, null);
                viewHolder = new ViewHolder();
                viewHolder.patient = (TextView) convertView.findViewById(R.id.text_tc_patient);
                viewHolder.description = (TextView) convertView.findViewById(R.id.text_tc_description);
                viewHolder.applicant = (TextView) convertView.findViewById(R.id.text_tc_applicant);
                viewHolder.urgency = (TextView) convertView.findViewById(R.id.text_tc_urgency);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Teleconsultation tc = getItem(position);
            if (tc.getPatient() == null) {
                viewHolder.patient.setText(R.string.anonymous);
            }
            else {
                viewHolder.patient.setText(tc.getPatient().toString());
            }
            viewHolder.description.setText(tc.getDescription());
            viewHolder.applicant.setText(tc.getApplicant().getFirstName() + " " + tc.getApplicant().getLastName());
            viewHolder.urgency.setText(tc.getSeverity());
            return convertView;
        }

        private class ViewHolder {
            TextView patient;
            TextView description;
            TextView applicant;
            TextView urgency;
        }
    }
}