package it.crs4.most.demo.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import it.crs4.most.demo.TeleconsultationException;

public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;
    private String mUid;
    private String mAccountNumber;
    private String mName;
    private String mSurname;

    public Patient(String uid, String name, String surname, String accountNumber) {
        mUid = uid;
        mAccountNumber = accountNumber;
        mName = name;
        mSurname = surname;
    }

    public String getUid() {
        return mUid;
    }

    public String getAccountNumber() {
        return mAccountNumber;
    }

    public String getName() {
        return mName;
    }

    public String getSurname() {
        return mSurname;
    }

    public static Patient fromJSON(JSONObject patientData) throws TeleconsultationException {
        try {
            String uid = patientData.getString("uid");
            String accountNumber = patientData.getString("account_number");
            String firstname = patientData.getString("firstname");
            String lastname =  patientData.getString("lastname");
            return new Patient(uid, firstname, lastname, accountNumber);
        }
        catch (JSONException e) {
            e.printStackTrace();
            throw new TeleconsultationException();
        }
    }

    @Override
    public String toString() {
        return getName() + " " + getSurname();
    }


}
