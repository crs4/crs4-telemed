package it.crs4.most.demo.ecoapp.models;

import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 4399813546588966888L;
    private String mId;
    private String mName;
    private String mDescription;
    private Device mEncoder;
    private Device mCamera;

    public Room(String id, String name, String description) {
        mId = id;
        mName = name;
        mDescription = description;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public Device getEncoder() {
        return mEncoder;
    }

    public void setEncoder(Device encoder) {
        mEncoder = encoder;
    }

    public Device getCamera() {
        return mCamera;
    }

    public void setCamera(Device camera) {
        mCamera = camera;
    }

    @Override
    public String toString() {
        return getName();
    }
}
