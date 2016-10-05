package it.crs4.most.demo.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import it.crs4.most.demo.TeleconsultationException;

public class ARConfiguration implements Serializable {

    private ARMarker ecoMarker;
    private ARMarker keyboardMarker;
    private ARMarker patientMarker;
    private float screenHeight;
    private float screenWidth;

    public ARConfiguration(
            ARMarker ecoMarker,
            ARMarker keyboardMarker,
            ARMarker patientMarker,
            float screenHeight,
            float screenWidth) {

        this.ecoMarker = ecoMarker;
        this.keyboardMarker = keyboardMarker;
        this.patientMarker = patientMarker;

        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
    }

    public static ARConfiguration fromJSON(JSONObject obj) throws TeleconsultationException {
        try {
            ARMarker ecoMarker = obj.isNull("eco_marker")?
                    null: ARMarker.fromJSON(obj.getJSONObject("eco_marker"));
            ARMarker keyboardMarker = obj.isNull("keyboard_marker")?
                    null: ARMarker.fromJSON(obj.getJSONObject("keyboard_marker"));
            ARMarker patienMarker = obj.isNull("patient_marker")?
                    null: ARMarker.fromJSON(obj.getJSONObject("patient_marker"));

            float screenHeight = (float) obj.getDouble("screen_height");
            float screenWidth = (float) obj.getDouble("screen_width");

            return new ARConfiguration(ecoMarker, keyboardMarker, patienMarker, screenHeight, screenWidth);
        } catch (JSONException e) {
            throw new TeleconsultationException();
        }
    }

    public ARMarker getEcoMarker() {
        return ecoMarker;
    }

    public void setEcoMarker(ARMarker ecoMarker) {
        this.ecoMarker = ecoMarker;
    }

    public ARMarker getKeyboardMarker() {
        return keyboardMarker;
    }

    public void setKeyboardMarker(ARMarker keyboardMarker) {
        this.keyboardMarker = keyboardMarker;
    }

    public ARMarker getPatientMarker() {
        return patientMarker;
    }

    public void setPatientMarker(ARMarker patientMarker) {
        this.patientMarker = patientMarker;
    }

    public float getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(float screenHeight) {
        this.screenHeight = screenHeight;
    }

    public float getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(float screenWidth) {
        this.screenWidth = screenWidth;
    }
}
