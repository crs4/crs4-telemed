package it.crs4.most.demo.eco_setup;


import java.util.ArrayList;

import it.crs4.most.demo.IConfigBuilder;
import it.crs4.most.demo.R;
import it.crs4.most.demo.models.Patient;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PatientSelectionFragment extends ConfigFragment {
    private ArrayList<Patient> mPatients;
    private ArrayAdapter<Patient> mPatientArrayAdapter;
    private View mView;

    public static PatientSelectionFragment newInstance(IConfigBuilder config) {
        PatientSelectionFragment fragment = new PatientSelectionFragment();
        fragment.setConfigBuilder(config);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_patients, container, false);
        FloatingActionButton addPatient = (FloatingActionButton) mView.findViewById(R.id.button_patients_add);
        addPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getConfigBuilder().setPatient(null);
            }
        });

        ListView listView = (ListView) mView.findViewById(R.id.patients_list);
        mPatients = new ArrayList<>();
        mPatientArrayAdapter = new PatientArrayAdapter(this, R.layout.patient_row, mPatients);
        listView.setAdapter(mPatientArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Patient selectedPatient = mPatients.get(position);
                getConfigBuilder().setPatient(selectedPatient);
            }
        });
        retrievePatients();

        return mView;
    }

    private void retrievePatients() {
        mPatients.add(new Patient("Mario", "Rossi", "MRSI1234636R243R"));
        mPatients.add(new Patient("Carlo", "Verdi", "VRLI1334636R243P"));
        mPatients.add(new Patient("Gianni", "Bianchi", "BHGI3334636R243V"));
        mPatientArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onShow() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
    }
}