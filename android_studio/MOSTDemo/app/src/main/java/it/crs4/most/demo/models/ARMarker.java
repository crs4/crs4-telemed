package it.crs4.most.demo.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import it.crs4.most.demo.TeleconsultationException;


public class ARMarker implements Serializable {
    private String conf;
    private float transX;
    private float transY;

    public ARMarker(String conf, float transX, float transY){
        this.conf = conf;
        this.transX = transX;
        this.transY = transY;
    }

    public static ARMarker fromJSON(JSONObject obj) throws TeleconsultationException {
        try {
            String conf = obj.getString("conf");
            float transX = (float) obj.getDouble("trans_x");
            float transY = (float) obj.getDouble("trans_y");
            return new ARMarker(conf, transX, transY);
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
}
