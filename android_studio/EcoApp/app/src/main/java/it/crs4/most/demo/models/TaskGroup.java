package it.crs4.most.demo.models;

import java.io.Serializable;

public class TaskGroup implements Serializable {

    private static final long serialVersionUID = 1L;
    private String mId;
    private String mDescription;

    public TaskGroup(String id, String description) {
        mId = id;
        mDescription = description;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }
}
