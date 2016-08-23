package it.crs4.most.demo.setup_fragments;


import java.util.ArrayList;
import java.util.List;

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
import android.widget.TextView;

public class PatientSelectionFragment extends SetupFragment {
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
        mView = inflater.inflate(R.layout.fragment_patient_selection, container, false);
        FloatingActionButton addPatient = (FloatingActionButton) mView.findViewById(R.id.button_patients_add);
        addPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getConfigBuilder().setPatient(null);
            }
        });

        ListView listView = (ListView) mView.findViewById(R.id.patients_list);
        mPatients = new ArrayList<>();
        mPatientArrayAdapter = new PatientAdapter(this, R.layout.fragment_patient_selection_item, mPatients);
        listView.setAdapter(mPatientArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Patient selected = mPatients.get(position);
                getConfigBuilder().setPatient(selected);
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

    @Override
    public int getTitle() {
        return R.string.patient_selection_title;
    }

    public class PatientAdapter extends ArrayAdapter<Patient> {

        public PatientAdapter(PatientSelectionFragment fragment, int textViewId, List<Patient> objects) {
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
                convertView = inflater.inflate(R.layout.fragment_patient_selection_item, null);
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
            viewHolder.id.setText(patient.getId());
            return convertView;
        }

        private class ViewHolder {
            public TextView fullName;
            public TextView id;
        }
    }
}