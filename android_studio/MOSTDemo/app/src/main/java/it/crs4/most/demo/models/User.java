package it.crs4.most.demo.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import it.crs4.most.demo.TeleconsultationException;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String mUsername = null;
    private String mFirstName = null;
    private String mLastName = null;
    private String mAccessToken = null;
    private String mTaskGroup = null;

    public User(String firstName, String lastName, String username, String taskGroup) {
        mFirstName = firstName;
        mLastName = lastName;
        mUsername = username;
        mTaskGroup = taskGroup;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setTaskGroup(String taskGroup) {
        mTaskGroup = taskGroup;
    }

    public String getTaskGroup() {
        return mTaskGroup;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }

    @Override
    public String toString() {
        return  String.format("%s %s (%s)", getFirstName(), getLastName(), getUsername());

    }

    public static User fromJSON(JSONObject userData) throws TeleconsultationException {
        try {
            String firstname = userData.getString("firstname");
            String lastname = userData.getString("lastname");
            String username = userData.getString("username");
            return new User(firstname, lastname, username, null);
        }
        catch (JSONException e) {
            throw new TeleconsultationException();
        }
    }
}
