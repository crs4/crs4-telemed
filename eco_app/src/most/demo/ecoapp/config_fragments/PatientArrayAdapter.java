package most.demo.ecoapp.config_fragments;

import java.util.List;

import most.demo.ecoapp.R;

import most.demo.ecoapp.models.Patient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PatientArrayAdapter extends ArrayAdapter<Patient> {

    public PatientArrayAdapter(Fragment_PatientSelection fragment_PatientSelection, int textViewResourceId,
                 List<Patient> objects) {
    	
        super(fragment_PatientSelection.getActivity(), textViewResourceId, objects);
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
            viewHolder.fullName = (TextView)convertView.findViewById(R.id.textPatientFullName);
            viewHolder.id = (TextView)convertView.findViewById(R.id.textPatientID);
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