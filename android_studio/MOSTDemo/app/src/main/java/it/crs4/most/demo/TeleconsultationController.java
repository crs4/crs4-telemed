package it.crs4.most.demo;

import android.app.Activity;

import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.setup_fragments.SetupFragment;

public abstract class TeleconsultationController {

    public abstract SetupFragment[] getFragments(TeleconsultationSetup teleconsultationSetup, String action);

    public abstract void startTeleconsultationActivity(Activity activity, Teleconsultation teleconsultation);
}
