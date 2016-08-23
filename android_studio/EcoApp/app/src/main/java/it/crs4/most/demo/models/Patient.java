package it.crs4.most.demo.models;

import java.io.Serializable;

public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;
    private String mId;
    private String mName;
    private String mSurname;

    public Patient(String name, String surname, String id) {
        mId = id;
        mName = name;
        mSurname = surname;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getSurname() {
        return mSurname;
    }

}
