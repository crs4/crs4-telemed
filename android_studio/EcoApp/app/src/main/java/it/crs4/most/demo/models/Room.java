package it.crs4.most.demo.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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

    public static Room fromJSON(JSONObject roomData) {
        Log.d("ROOM", "ROOM_DATA: " + roomData);

        try {
            String id = roomData.getString("uuid");
            String name = roomData.getString("name");
            String description = roomData.getString("description");
            JSONObject cameraData = roomData.getJSONObject("devices").getJSONObject("camera");
            JSONObject encoderData = roomData.getJSONObject("devices").getJSONObject("encoder");
            Device camera = Device.fromJSON(cameraData);
            Device encoder = Device.fromJSON(encoderData);
            Room r = new Room(id, name, description);
            r.setCamera(camera);
            r.setEncoder(encoder);
            return r;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }
}
