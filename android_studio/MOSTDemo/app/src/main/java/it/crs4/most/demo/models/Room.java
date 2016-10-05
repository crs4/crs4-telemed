package it.crs4.most.demo.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import it.crs4.most.demo.TeleconsultationException;

public class Room implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String TAG = "Room";
    private String mId;
    private String mName;
    private String mDescription;
    private Device mEncoder;
    private Device mCamera;
    private ARConfiguration arConfiguration;

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

    public ARConfiguration getARConfiguration() {
        return arConfiguration;
    }

    public void setARConfiguration(ARConfiguration arConfiguration) {
        this.arConfiguration = arConfiguration;
    }

    public static Room fromJSON(JSONObject roomData) throws TeleconsultationException {
        String id;
        String name;
        String description;
        JSONObject cameraData;
        JSONObject encoderData;


        try {
            id = roomData.getString("uuid");
            name = roomData.getString("name");
            description = roomData.getString("description");
        }
        catch (JSONException e) {
            throw new TeleconsultationException();
        }

        Room r = new Room(id, name, description);

        try {
            cameraData = roomData.getJSONObject("devices").getJSONObject("camera");
            Device camera = Device.fromJSON(cameraData);
            r.setCamera(camera);
        }
        catch (JSONException e) {
            Log.d(TAG, "Camera data not found for the room");
        }
        try {
            encoderData = roomData.getJSONObject("devices").getJSONObject("encoder");
            Device encoder = Device.fromJSON(encoderData);
            r.setEncoder(encoder);
        }
        catch (JSONException e) {
            Log.d(TAG, "Encoder data not found for the room");
        }

        try {
            ARConfiguration arConfiguration = ARConfiguration.
                    fromJSON(roomData.getJSONObject("ar_conf"));

            r.setARConfiguration(arConfiguration);
        }
        catch (JSONException e) {
            Log.d(TAG, "Encoder data not found for the room");
        }


        return r;
    }

    @Override
    public String toString() {
        return getName();
    }
}
