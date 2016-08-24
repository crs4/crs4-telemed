package it.crs4.most.demo;

import android.app.Activity;

import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.setup_fragments.SetupFragment;

public abstract class TeleconsultationController {

    public abstract SetupFragment[] getFragments(IConfigBuilder builder);

    public abstract void startTeleconsultationActivity(Activity activity, Teleconsultation teleconsultation);
}
