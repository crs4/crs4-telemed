package it.crs4.most.demo.models;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import it.crs4.most.demo.TeleconsultationException;

public class Teleconsultation implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String TAG = "Teleconsultation";
    private String mId;
    private String mDescription;
    private String mSeverity;
    private User mApplicant;
    private Patient mPatient;
    private TeleconsultationSession mSession;

    public Teleconsultation(String id, String description, String severity, User applicant, Patient patient) {
        mId = id;
        mDescription = description;
        mSeverity = severity;
        mApplicant = applicant;
        mPatient = patient;
    }

    public String getId() {
        return mId;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getSeverity() {
        return mSeverity;
    }

    public TeleconsultationSession getLastSession() {
        return mSession;
    }

    public void setLastSession(TeleconsultationSession session) {
        mSession = session;
    }

    public User getApplicant() {
        return mApplicant;
    }

    public void setApplicant(User applicant) {
        mApplicant = applicant;
    }

    public Patient getPatient() {
        return mPatient;
    }

    public static Teleconsultation fromJSON(Context context, JSONObject teleconsultationData,
                                            String role) throws TeleconsultationException {
        String id;
        String description;
        String severity;
        User applicant;
        Patient patient;

        try {
            id = teleconsultationData.getString("uuid");
            severity = teleconsultationData.getString("severity");
            description = teleconsultationData.getString("description");
            applicant = User.fromJSON(teleconsultationData.getJSONObject("applicant"));
            patient = Patient.fromJSON(teleconsultationData.getJSONObject("patient"));
        }
        catch (JSONException e) {
            e.printStackTrace();
            throw new TeleconsultationException();
        }

        Teleconsultation t = new Teleconsultation(id, description, severity, applicant, patient);
        try {
            JSONObject lastSessionData = teleconsultationData.getJSONObject("last_session");
            TeleconsultationSession lastSession = TeleconsultationSession.fromJSON(context, lastSessionData, role);
            t.setLastSession(lastSession);
        }
        catch (JSONException e) {
            Log.d(TAG, "No session found");
        }

        return t;
    }
}
