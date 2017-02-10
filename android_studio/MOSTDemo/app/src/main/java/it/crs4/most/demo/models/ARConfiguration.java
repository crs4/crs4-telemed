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


    public ARConfiguration(
            List<ARMarker> markers,
            float screenHeight,
            float screenWidth,
            Map<String, float []> keymap) {

        this.markers = markers;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.keymap = keymap;
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


            return new ARConfiguration(markers, screenHeight, screenWidth, keymap);
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
}
