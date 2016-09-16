package it.crs4.most.demo;

import android.content.Intent;
import android.support.v4.view.ViewPager;

import it.crs4.most.demo.models.Patient;
import it.crs4.most.demo.models.Teleconsultation;

public class TeleconsultationSetup {

    private TeleconsultationSetupActivity mActivity;
    private Patient mPatient;
    private Teleconsultation mTeleconsultation;

    public TeleconsultationSetup(TeleconsultationSetupActivity activity) {
        mActivity = activity;
    }

    public Patient getPatient() {
        return mPatient;
    }

    public void setPatient(Patient patient) {
        mPatient = patient;
        mActivity.nextStep();
    }

    public Teleconsultation getTeleconsultation() {
        return mTeleconsultation;
    }

    public void setTeleconsultation(Teleconsultation teleconsultation) {
        mTeleconsultation = teleconsultation;
        mActivity.startTeleconsultationActivity(teleconsultation);
    }
}
