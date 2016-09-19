package it.crs4.most.demo;

import java.io.Serializable;

import it.crs4.most.demo.models.Patient;
import it.crs4.most.demo.models.Room;
import it.crs4.most.demo.models.Teleconsultation;

public class TeleconsultationSetup implements Serializable{

    private Patient mPatient;
    private Teleconsultation mTeleconsultation;
    private String mUrgency;
    private Room mRoom;

    public TeleconsultationSetup() {
    }

    public Patient getPatient() {
        return mPatient;
    }

    public void setPatient(Patient patient) {
        mPatient = patient;
    }

    public Teleconsultation getTeleconsultation() {
        return mTeleconsultation;
    }

    public void setTeleconsultation(Teleconsultation teleconsultation) {
        mTeleconsultation = teleconsultation;
    }

    public String getUrgency() {
        return mUrgency;
    }

    public void setUrgency(String urgency) {
        mUrgency = urgency;
    }

    public Room getRoom() {
        return mRoom;
    }

    public void setRoom(Room room) {
        mRoom = room;
    }

    @Override
    public String toString() {
        return String.format("TeleconsultationSetup object with Patient: %1$s, Urgency %2$s, Room: ",
            getPatient(), getUrgency(), getRoom());
    }
}
