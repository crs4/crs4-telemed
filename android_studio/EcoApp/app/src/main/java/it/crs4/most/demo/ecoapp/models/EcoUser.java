package it.crs4.most.demo.ecoapp.models;

import java.io.Serializable;

public class EcoUser implements Serializable {

    private static final long serialVersionUID = 6108801942060044140L;

    private String mUsername = null;
    private TaskGroup mTaskGroup = null;
    private String mFirstName = null;
    private String mLastName = null;
    private String mAccessToken = null;

    public EcoUser(String firstName, String lastName, String username, TaskGroup taskGroup) {
        this.mFirstName = firstName;
        this.mLastName = lastName;
        this.mUsername = username;
        this.mTaskGroup = taskGroup;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    public void setTaskGroup(TaskGroup taskGroup) {
        this.mTaskGroup = taskGroup;
    }

    public String getUsername() {
        return mUsername;
    }

    public TaskGroup getTaskGroup() {
        return mTaskGroup;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public void setAccessToken(String accessToken) {
        this.mAccessToken = accessToken;
    }

    @Override
    public String toString() {
        return getUsername();
    }
}
