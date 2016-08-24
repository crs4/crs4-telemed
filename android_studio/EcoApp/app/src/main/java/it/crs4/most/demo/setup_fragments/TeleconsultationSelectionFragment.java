package it.crs4.most.demo.setup_fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

import it.crs4.most.demo.IConfigBuilder;
import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RemoteConfigReader;
import it.crs4.most.demo.TeleconsultationException;
import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.models.User;

public class TeleconsultationSelectionFragment extends SetupFragment {

    private static String TAG = "TeleconsultationSelectionFragment";
    private ArrayList<Teleconsultation> mTeleconsultations;
    private ArrayAdapter<Teleconsultation> mTcsArrayAdapter;
    private RemoteConfigReader mConfigReader;
    private Runnable mGetTeleconsultationsTask;
    private Handler mGetTeleconsultationsHandler;
    private View mView;

    public static TeleconsultationSelectionFragment newInstance(IConfigBuilder config) {
        TeleconsultationSelectionFragment fragment = new TeleconsultationSelectionFragment();
        fragment.setConfigBuilder(config);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mGetTeleconsultationsHandler = new Handler();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.teleconsultation_list, container, false);
        ListView listView = (ListView) mView.findViewById(R.id.teleconsultation_list);
        mTeleconsultations = new ArrayList<>();
        mTcsArrayAdapter = new TeleconsultationAdapter(this, R.layout.teleconsultation_row, mTeleconsultations);

        listView.setAdapter(mTcsArrayAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Teleconsultation selectedTc = mTeleconsultations.get(position);
                getConfigBuilder().setTeleconsultation(selectedTc);
            }
        });
        return mView;
    }

    @Override
    public void onPause() {
        mGetTeleconsultationsHandler.removeCallbacks(mGetTeleconsultationsTask);
        super.onPause();
    }

    private void retrieveTeleconsultations() {
        mConfigReader = getConfigBuilder().getRemoteConfigReader();
        final User user = getConfigBuilder().getUser();

        mGetTeleconsultationsTask = new Runnable() {
            @Override
            public void run() {
                mTeleconsultations.clear();
                mGetTeleconsultationsHandler.postDelayed(mGetTeleconsultationsTask, 5000);
                mConfigReader.getTeleconsultationsByTaskgroup(
                    user.getTaskGroup().getId(),
                    user.getAccessToken(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(TAG, "Teleconsultation list response: " + response);
                                final JSONArray teleconsultations = response
                                    .getJSONObject("data")
                                    .getJSONArray("teleconsultations");

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
                                mTcsArrayAdapter.notifyDataSetChanged();
                            }
                            catch (JSONException e) {
                                Log.e(TAG, "There's something wrong with the JSON structure returned by the server");
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
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
        retrieveTeleconsultations();
    }

    @Override
    public int getTitle() {
        return R.string.teleconsultation_title;
    }

    private static class TeleconsultationAdapter extends ArrayAdapter<Teleconsultation> {

        public TeleconsultationAdapter(TeleconsultationSelectionFragment fragment,
                                       int textViewId, List<Teleconsultation> objects) {
            super(fragment.getActivity(), textViewId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getViewOptimize(position, convertView, parent);
        }

        public View getViewOptimize(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.teleconsultation_row, null);
                viewHolder = new ViewHolder();
                viewHolder.fullName = (TextView) convertView.findViewById(R.id.text_tc_name);
                viewHolder.id = (TextView) convertView.findViewById(R.id.text_tc_id);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Teleconsultation tc = getItem(position);
            viewHolder.fullName.setText(tc.getDescription());
            viewHolder.id.setText(tc.getId());
            return convertView;
        }

        private class ViewHolder {
            public TextView fullName;
            public TextView id;
        }
    }
}