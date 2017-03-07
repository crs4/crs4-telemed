package it.crs4.most.demo.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.crs4.most.demo.TeleconsultationException;
import it.crs4.most.visualization.augmentedreality.MarkerFactory;

public class ARConfiguration implements Serializable {

    private List<ARMarker> markers = new ArrayList<>();
    private float screenHeight;
    private float screenWidth;
    private Map<String, float []> keymap;
    private Map<String, float []> calibrations;
    private String eye;


    public ARConfiguration(
            List<ARMarker> markers,
            float screenHeight,
            float screenWidth,
            Map<String, float []> keymap,
            Map<String, float []> calibrations,
            String eye
    ) {

        this.markers = markers;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.keymap = keymap;
        this.calibrations = calibrations;
        this.eye = eye;
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

            JSONArray  keymapList = obj.getJSONArray("keymap");
            Map<String, float []> keymap = new HashMap<>();
            for (int i = 0; i < keymapList.length(); i++) {
                JSONArray keymapEntry = keymapList.getJSONArray(i);
                String key = keymapEntry.getString(0);
                float [] coords = new float[] {
                        Float.valueOf(keymapEntry.getString(1)),
                        Float.valueOf(keymapEntry.getString(2)),
                        Float.valueOf(keymapEntry.getString(3)),
                };
                keymap.put(key, coords);
            }

            JSONArray calibrationsList = obj.getJSONArray("calibrations");
            Map<String, float []> calibrations = new HashMap<>();
            for (int i = 0; i < calibrationsList.length(); i++) {
                JSONObject calibrationEntry = calibrationsList.getJSONObject(i);
                String group = calibrationEntry .getString("group");
                float [] coords = new float[] {
                        Float.valueOf(calibrationEntry.getString("x")),
                        Float.valueOf(calibrationEntry.getString("y")),
                        Float.valueOf(calibrationEntry.getString("z")),
                };
                calibrations.put(group, coords);
            }
            String eye = "";
            if (!obj.isNull("eye"))
                eye = obj.getString("eye");
            return new ARConfiguration(markers, screenHeight, screenWidth, keymap, calibrations, eye);
        } catch (JSONException e) {
            e.printStackTrace();
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

    public Map<String, float[]> getKeymap() {
        return keymap;
    }

    public void setKeymap(Map<String, float[]> keymap) {
        this.keymap = keymap;
    }

    public Map<String, float[]> getCalibrations() {
        return calibrations;
    }

    public String getEye() {
        return eye;
    }

    public List<ARMarker> getMarkers(String group) {
        List<ARMarker> result = new ArrayList<>();
        for (ARMarker marker: markers) {
            if (marker.getGroup().equals(group)) {
                result.add(marker);
            }
        }
        return result;
    }
}
