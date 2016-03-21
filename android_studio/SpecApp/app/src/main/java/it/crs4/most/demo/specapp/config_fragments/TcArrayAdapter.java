package it.crs4.most.demo.specapp.config_fragments;

import java.util.List;

import it.crs4.most.demo.specapp.R;
import it.crs4.most.demo.specapp.models.Teleconsultation;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TcArrayAdapter extends ArrayAdapter<Teleconsultation> {

    public TcArrayAdapter(Fragment_TeleconsultationSelection fragment_PatientSelection, int textViewResourceId,
                 List<Teleconsultation> objects) {
    	
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
            convertView = inflater.inflate(R.layout.tc_row, null);
            viewHolder = new ViewHolder();
            viewHolder.fullName = (TextView)convertView.findViewById(R.id.textTcTitle);
            viewHolder.id = (TextView)convertView.findViewById(R.id.textTcID);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Teleconsultation tc = getItem(position);
        viewHolder.fullName.setText(tc.getInfo());
        viewHolder.id.setText(tc.getId());
        return convertView;
    }

    private class ViewHolder {
        public TextView fullName;
        public TextView id;
    }
}