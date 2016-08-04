package it.crs4.most.demo.models;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Teleconsultation implements Serializable {

    private static final long serialVersionUID = -1408055529735190987L;
    private String mId;
    private String mDescription;
    private Room mRoom;
    private String mSeverity;
    private User mApplicant;
    private String mName;
    private TeleconsultationSession mSession;

    public Teleconsultation(String id, String description,
                            String severity, Room room, User applicant) {
        mId = id;
        mDescription = description;
        mSeverity = severity;
        mRoom = room;
        mApplicant = applicant;
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

    public User getApplicant() {
        return mApplicant;
    }

    public Room getRoom() {
        return mRoom;
    }

    public TeleconsultationSession getLastSession() {
        return mSession;
    }

    public void setLastSession(TeleconsultationSession session) {
        mSession = session;
    }

    public static Teleconsultation fromJSON(Context context, JSONObject teleconsultationData, int role) {
        try {
            String id = teleconsultationData.getString("uuid");
            String description = teleconsultationData.getString("description");
            String severity = teleconsultationData.getString("severity");
            Teleconsultation t = new Teleconsultation(id, description, severity, null, null);
            JSONObject lastSessionData = teleconsultationData.getJSONObject("last_session");
            TeleconsultationSession lastSession = TeleconsultationSession.fromJSON(context, lastSessionData, role);
            t.setLastSession(lastSession);
            return t;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
