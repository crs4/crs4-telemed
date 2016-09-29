package it.crs4.most.demo.setup_fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RESTClient;
import it.crs4.most.demo.ResponseHandlerDecorator;
import it.crs4.most.demo.TeleconsultationSetup;
import it.crs4.most.demo.models.Patient;

public class PatientSearchFragment extends SetupFragment {

    private static final String TAG = "PatientSearchFragment";
    private TextView mPatientIDText;
    private TextView mPatientNameText;
    private TextView mPatientSurnameText;

    public static PatientSearchFragment newInstance(TeleconsultationSetup teleconsultationSetup) {
        PatientSearchFragment fragment = new PatientSearchFragment();
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
        String serverIP = QuerySettings.getConfigServerAddress(getActivity());
        int serverPort = Integer.valueOf(QuerySettings.getConfigServerPort(getActivity()));

        final RESTClient restClient = new RESTClient(getActivity(), serverIP, serverPort);

        mPatientIDText = (TextView) v.findViewById(R.id.patient_id_text);
        mPatientNameText = (TextView) v.findViewById(R.id.patient_name_text);
        mPatientSurnameText = (TextView) v.findViewById(R.id.patient_surname_text);
        Button searchPatientButton = (Button) v.findViewById(R.id.search_patient_button);
        searchPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientID = mPatientIDText.getText() != null ? mPatientIDText.getText().toString() : null;
                String patientName = mPatientNameText.getText() != null ? mPatientNameText.getText().toString() : null;
                String patientSurname = mPatientSurnameText.getText() != null ? mPatientSurnameText.getText().toString() : null;

                if (!(patientID == null && patientName == null && patientSurname == null)) {
                    Response.Listener<JSONObject> listener = new ResponseHandlerDecorator<>(getActivity(),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                try {
                                    JSONArray patientsData = response.getJSONArray("data");
                                    if (patientsData.length() == 0) {
                                        Toast.makeText(getActivity(), "No patient found", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        ArrayList<Patient> patients = new ArrayList<>();
                                        for (int i = 0; i < patientsData.length(); i++) {
                                            JSONObject patientData = (JSONObject) patientsData.get(i);
                                            String uid = patientData.getString("uid");
                                            String accountNumber = patientData.getString("account_number");
                                            String firstName = patientData.getString("first_name");
                                            String lastName = patientData.getString("last_name");
                                            patients.add(new Patient(uid, firstName, lastName, accountNumber));
                                        }
                                        mTeleconsultationSetup.setPatients(patients);
                                        stepDone();
                                    }
                                }
                                catch (JSONException e) {
                                    Log.e(TAG, "Error in json structure");
                                }

                            }
                        });
                    Response.ErrorListener errorListener = new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    };

                    restClient.searchPatient(null, patientID, patientName, patientSurname, listener, errorListener);
                }
            }
        });

        Button anonymousButton = (Button) v.findViewById(R.id.anonymous_patient_button);
        anonymousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTeleconsultationSetup.setPatient(null);
                mTeleconsultationSetup.setPatients(null);
                stepDone();
            }
        });

        return v;
    }

    @Override
    protected int getTitle() {
        return R.string.search_patient;
    }

    @Override
    protected int getLayoutContent() {
        return R.layout.patient_search_fragment;
    }
}
