package it.crs4.most.demo.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.crs4.most.demo.TeleconsultationException;
import it.crs4.most.visualization.augmentedreality.MarkerFactory;

public class ARConfiguration implements Serializable {

    private List<ARMarker> markers = new ArrayList<>();
    private float screenHeight;
    private float screenWidth;

    public ARConfiguration(
            List<ARMarker> markers,
            float screenHeight,
            float screenWidth) {

        this.markers = markers;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
    }

    public static ARConfiguration fromJSON(JSONObject obj) throws TeleconsultationException {
        try {
            JSONArray jsonMarkers = obj.getJSONArray("markers");
            List<ARMarker> markers = new ArrayList<>();
            for(int i=0; i < jsonMarkers.length(); i++){
                markers.add(ARMarker.fromJSON((JSONObject) jsonMarkers.get(i)));
            }
            float screenHeight = (float) obj.getDouble("screen_height");
            float screenWidth = (float) obj.getDouble("screen_width");

            return new ARConfiguration(markers, screenHeight, screenWidth);
        } catch (JSONException e) {
            throw new TeleconsultationException();
        }
    }

    public List<ARMarker> getMarkers() {
        return markers;
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
