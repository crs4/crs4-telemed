package it.crs4.most.demo.ecoapp.config_fragments;

import java.util.List;

import it.crs4.most.demo.ecoapp.R;

import it.crs4.most.demo.ecoapp.models.Patient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PatientArrayAdapter extends ArrayAdapter<Patient> {

    public PatientArrayAdapter(PatientSelectionFragment patientSelectionFragment, int textViewResourceId,
                               List<Patient> objects) {
    	
        super(patientSelectionFragment.getActivity(), textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getViewOptimize(position, convertView, parent);
    }

    public View getViewOptimize(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                      .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.patient_row, null);
            viewHolder = new ViewHolder();
            viewHolder.fullName = (TextView)convertView.findViewById(R.id.patient_full_name_text);
            viewHolder.id = (TextView)convertView.findViewById(R.id.patient_id_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Patient patient = getItem(position);
        viewHolder.fullName.setText(patient.getName()+" " + patient.getSurname());
        viewHolder.id.setText(patient.getId());
        return convertView;
    }

    private class ViewHolder {
        public TextView fullName;
        public TextView id;
    }
}