package it.crs4.most.demo.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import it.crs4.most.demo.TeleconsultationException;

public class Mesh implements Serializable {
    private String cls;
    private float sizeX;
    private float sizeY;
    private float sizeZ;
    private String name;

    public static Mesh fromJSON(JSONObject obj) throws TeleconsultationException {
        try {
            return new Mesh(
                    obj.getString("cls"),
                    (float) obj.getDouble("sizeX"),
                    (float) obj.getDouble("sizeY"),
                    (float) obj.getDouble("sizeZ"),
                    obj.getString("name")
            );
        } catch (JSONException e) {
            throw new TeleconsultationException();
        }
    }

    public  Mesh(String cls, float sizeX, float sizeY, float sizeZ, String name){
        this.cls = cls;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.name = name;
    }

    public String getCls() {
        return cls;
    }

    public float getSizeX() {
        return sizeX;
    }

    public float getSizeY() {
        return sizeY;
    }

    public float getSizeZ() {
        return sizeZ;
    }

    public String getName() {
        return name;
    }
}
