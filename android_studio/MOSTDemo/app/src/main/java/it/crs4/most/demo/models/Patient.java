package it.crs4.most.demo.models;

import java.io.Serializable;

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

}
