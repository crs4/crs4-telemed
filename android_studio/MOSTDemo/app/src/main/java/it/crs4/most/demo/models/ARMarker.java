package it.crs4.most.demo.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import it.crs4.most.demo.TeleconsultationException;


public class ARMarker implements Serializable {
    private String conf;
    private float transX;
    private float transY;
    private int pk;
    private String group;
    private Mesh mesh;

    public ARMarker(int pk, String conf, float transX, float transY, String group, Mesh mesh){
        this.conf = conf;
        this.transX = transX;
        this.transY = transY;
        this.pk = pk;
        this.group = group;
        this.mesh = mesh;
    }

    public static ARMarker fromJSON(JSONObject obj) throws TeleconsultationException {
        try {
            String conf = obj.getString("conf");
            float transX = (float) obj.getDouble("trans_x");
            float transY = (float) obj.getDouble("trans_y");
            int pk = obj.getInt("pk");
            String group = obj.getString("group");
            Mesh mesh = Mesh.fromJSON(obj.getJSONObject("mesh"));
            return new ARMarker(pk, conf, transX, transY, group, mesh);
        }
        catch (JSONException e) {
            throw new TeleconsultationException();
        }
    }

    public String getConf() {
        return conf;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }

    public float getTransX() {
        return transX;
    }

    public void setTransX(float transX) {
        this.transX = transX;
    }

    public float getTransY() {
        return transY;
    }

    public void setTransY(float transY) {
        this.transY = transY;
    }

    public String getGroup() {
        return group;
    }

    public int getPk() {
        return pk;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public String toString(){
        return String.format("%s-%s", group, conf);
    }
}

