package it.crs4.most.demo.models;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import it.crs4.most.demo.TeleconsultationException;

public class Teleconsultation implements Serializable {

    private static final long serialVersionUID = -1408055529735190987L;
    private static final String TAG = "Teleconsultation";
    private String mId;
    private String mDescription;
    private Room mRoom;
    private String mSeverity;
    private User mUser;
    private TeleconsultationSession mSession;

    public Teleconsultation(String id, String description,
                            String severity, Room room, User user) {
        mId = id;
        mDescription = description;
        mSeverity = severity;
        mRoom = room;
        mUser = user;
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

    public void setUser(User user) {
        mUser = user;
    }

    public User getUser() {
        return mUser;
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

    public static Teleconsultation fromJSON(Context context, JSONObject teleconsultationData, String role) throws TeleconsultationException {
        String id;
        String description;
        String severity;

        try {
            id = teleconsultationData.getString("uuid");
            severity = teleconsultationData.getString("severity");
            description = teleconsultationData.getString("description");
        }
        catch (JSONException e) {
            throw new TeleconsultationException();
        }

        Teleconsultation t = new Teleconsultation(id, description, severity, null, null);
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
