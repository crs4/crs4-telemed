/*!
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014-15, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */

package it.crs4.most.ehrlib.example;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import it.crs4.ehrlib.example.R;
import it.crs4.most.ehrlib.example.models.MedicalRecord;
import it.crs4.most.ehrlib.example.models.Patient;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class PatientMedicalRecordFragment extends Fragment{
	private RemotePyEHRConnector rc = null;
	private ListView listPatients = null;
	private ListView listMedicalRecords = null;
	private RemoteArchetypeViewerActivityExample av = null;
	private static String TAG = "PatientMedicalRecordF";
	
	public PatientMedicalRecordFragment(RemoteArchetypeViewerActivityExample av,  RemotePyEHRConnector rc )
	{
	    this.av = av;
		this.rc = rc;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.patient_medical_record_view, container, false);
		listPatients = (ListView) view.findViewById(R.id.listPatients);
		listMedicalRecords =   (ListView) view.findViewById(R.id.listMedicalRecords);
		
		Log.d(TAG, String.format("View:%s List:%s", view, listPatients));
	    //listView.setAdapter(accountsArrayAdapter);
	    //listView.setOnItemClickListener(new OnItemClickListener()
		this.loadPatients(listPatients);
		
		return view;
	}

	private void loadPatientMedicalRecords(final Patient patient)
	{
		
		rc.getPatientMedicalRecords(patient.getUuid(), new Listener<JSONObject>(){
             private List<MedicalRecord> medicalRecords = null;
     		
			@Override
			public void onResponse(JSONObject jres) {
				try {
					
					Log.d(TAG, "MEDICAL RECORDS:" + jres.toString());
					
					JSONArray jmrecords= jres.getJSONObject("RECORD").getJSONArray("ehr_records");
					
					medicalRecords = new ArrayList<MedicalRecord>();
					for (int i=0; i<jmrecords.length();i++)
					{
						JSONObject jmr = jmrecords.getJSONObject(i);
						
						MedicalRecord mr = new MedicalRecord(patient, jmr);
						medicalRecords.add(mr);
					}
					listMedicalRecords.setAdapter(new MedicalRecordArrayAdapter(getActivity(), R.layout.medical_record_row, medicalRecords));
					listMedicalRecords.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							MedicalRecord mr = medicalRecords.get(position);
							Log.d(TAG , "Archetype instance to load:" + mr.getMedicalRecord().toString());
							loadPatientMedicalRecord(mr);
						}});
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}}, new ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError arg0) {
					// TODO Auto-generated method stub
					
				}});
	}
	
	private void loadPatientMedicalRecord(final MedicalRecord mr)
	{
		rc.getPatientMedicalRecord(mr.getPatient().getUuid(), mr.getRecordId(),  new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject res) {
				Log.d(TAG, "loadPatientMedicalRecord Response:" + res);
				try {
					String instances = res.getJSONObject("RECORD").getJSONObject("ehr_data").toString();
					//String instances = null;
					av.loadArchetypeFragment(mr, instances);
				} 
				
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}}, new ErrorListener(){

				@Override
				public void onErrorResponse(VolleyError arg0) {
					// TODO Auto-generated method stub
					
				}});
	}
	
	private void loadPatients(final ListView listPatients)
	{
		rc.getPatients(new Listener<JSONObject>() {

			private ArrayList<Patient> patients;

			@Override
			public void onResponse(JSONObject jres) {
				try {
					JSONArray jpatients = jres.getJSONArray("patients");
					
					patients = new ArrayList<Patient>();
					
					for (int i=0; i<jpatients.length();i++)
					{
						JSONObject jp = jpatients.getJSONObject(i);
						
						Patient p = new Patient(jp.getString("uuid"), jp.getString("demographic_uuid"),jp.getString("ehr_uuid"));
						patients.add(p);
					}
					
					Log.d(TAG, String.format("patients:%s on %s",listPatients, R.layout.patient_row));
					listPatients.setAdapter(new PatientsArrayAdapter(getActivity(), R.layout.patient_row, patients));
					
					listPatients.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							if (patients!=null)
								loadPatientMedicalRecords(patients.get(position));
							
						}
					});
				} catch (JSONException e) {
					Toast.makeText(getActivity(), "Error loading remote patients", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
				
			}
		},   new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				// TODO Auto-generated method stub
				
			}});
	}
}
