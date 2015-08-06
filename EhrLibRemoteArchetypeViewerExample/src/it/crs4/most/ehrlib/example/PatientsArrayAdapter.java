/*!
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014-15, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */

package it.crs4.most.ehrlib.example;

import it.crs4.ehrlib.example.R;
import it.crs4.most.ehrlib.example.models.Patient;

import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PatientsArrayAdapter extends ArrayAdapter<Patient> {

    public PatientsArrayAdapter(Context context, int textViewResourceId,
                 List<Patient> objects) {
        super(context, textViewResourceId, objects);
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
            viewHolder.uuid = (TextView)convertView.findViewById(R.id.textPatientUUID);
         
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Patient patient = getItem(position);
        viewHolder.uuid.setText(patient.getUuid());
        return convertView;
    }

    private class ViewHolder {
        public TextView uuid;
    }
}