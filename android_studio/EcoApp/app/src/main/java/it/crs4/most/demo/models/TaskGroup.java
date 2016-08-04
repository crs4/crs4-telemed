package it.crs4.most.demo.models;

import java.io.Serializable;

public class TaskGroup implements Serializable {

    private static final long serialVersionUID = 6314390526009668956L;
    private String mId;
    private String mDescription;

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public TaskGroup(String id, String description) {
        mId = id;
        mDescription = description;
    }

    public String getId() {
        return mId;
    }

    public String getDescription() {
        return mDescription;
    }

}
