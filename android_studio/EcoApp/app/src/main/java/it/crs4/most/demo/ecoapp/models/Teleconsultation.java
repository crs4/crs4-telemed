package it.crs4.most.demo.ecoapp.models;

import java.io.Serializable;

public class Teleconsultation implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1408055529735190987L;
    private String mId;
    private String mInfo;
    private Room mRoom;
    private String mSeverity;
    private EcoUser mApplicant;
    private String mName;
    private TeleconsultationSession mSession;

    public Teleconsultation(String id, String name, String info, String severity, Room room, EcoUser applicant) {
        mId = id;
        mName = name;
        mInfo = info;
        mSeverity = severity;
        mRoom = room;
        mApplicant = applicant;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getInfo() {
        return mInfo;
    }


    public String getSeverity() {
        return mSeverity;
    }

    public EcoUser getApplicant() {
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


}
