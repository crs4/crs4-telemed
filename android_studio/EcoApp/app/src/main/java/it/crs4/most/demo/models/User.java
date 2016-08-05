package it.crs4.most.demo.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 6108801942060044140L;

    private String mUsername = null;
    private String mFirstName = null;
    private String mLastName = null;
    private String mAccessToken = null;
    private TaskGroup mTaskGroup = null;

    public User(String firstName, String lastName, String username, TaskGroup taskGroup) {
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

    public void setTaskGroup(TaskGroup taskGroup) {
        mTaskGroup = taskGroup;
    }

    public TaskGroup getTaskGroup() {
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
        return getUsername();
    }

    public static User fromJSON(JSONObject userData) {
        try {
            String firstname = userData.getString("firstname");
            String lastname = userData.getString("lastname");
            String username = userData.getString("username");
            return new User(firstname, lastname, username, null);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
