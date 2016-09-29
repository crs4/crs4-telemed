package it.crs4.most.demo.setup_fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.crs4.most.demo.R;
import it.crs4.most.demo.TeleconsultationSetup;
import it.crs4.most.demo.models.Patient;

public class PatientSelectionFragment extends SetupFragment {
    private static final String TAG = "PatientsSelectionFrag";
    private ArrayList<Patient> mPatients;
    private ArrayAdapter<Patient> mPatientArrayAdapter;

    public static PatientSelectionFragment newInstance(TeleconsultationSetup teleconsultationSetup) {
        PatientSelectionFragment fragment = new PatientSelectionFragment();
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
//        FloatingActionButton addPatient = (FloatingActionButton) v.findViewById(R.id.button_patients_add);

        ListView listView = (ListView) v.findViewById(R.id.patients_list);
        mPatients = new ArrayList<>();
        mPatientArrayAdapter = new PatientAdapter(this, R.layout.patient_selection_fragment_item, mPatients);
        listView.setAdapter(mPatientArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Patient selected = mPatientArrayAdapter.getItem(position);
                mTeleconsultationSetup.setPatient(selected);
                stepDone();
            }
        });
        return v;
    }

    @Override
    public void onShow() {
//            Log.d(TAG, "patients = " + mTeleconsultationSetup.getPatients());
        if (mTeleconsultationSetup.getPatients() == null) {
            skipStep();
        }
        else {
            mPatientArrayAdapter.clear();
            mPatientArrayAdapter.addAll(mTeleconsultationSetup.getPatients());
            mPatientArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected int getTitle() {
        return R.string.patient_selection_title;
    }

    @Override
    protected int getLayoutContent() {
        return R.layout.patient_selection_fragment;
    }

    private class PatientAdapter extends ArrayAdapter<Patient> {

        PatientAdapter(PatientSelectionFragment fragment, int textViewId, List<Patient> objects) {
            super(fragment.getActivity(), textViewId, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return getViewOptimize(position, convertView, parent);
        }

        View getViewOptimize(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.patient_selection_fragment_item, null);
                viewHolder = new ViewHolder();
                viewHolder.fullName = (TextView) convertView.findViewById(R.id.patient_full_name_text);
                viewHolder.id = (TextView) convertView.findViewById(R.id.patient_id_text);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Patient patient = getItem(position);
            viewHolder.fullName.setText(String.format("%s %s", patient.getName(), patient.getSurname()));
            viewHolder.id.setText(patient.getAccountNumber());
            return convertView;
        }

        private class ViewHolder {
            TextView fullName;
            public TextView id;
        }
    }
}