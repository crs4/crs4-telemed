package it.crs4.most.demo.spec;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.Map;

import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.RESTClient;
import it.crs4.most.demo.models.Room;


public class RESTKeyboardCoordinatesStore implements VirtualKeyboard.KeyboardCoordinatesStore {
    private static final String TAG = "RESTCStore";
    private RESTClient restClient;
    private Room room;
    String accessToken;
    Map<String, float []> keymap;

    public RESTKeyboardCoordinatesStore(Room room, RESTClient restClient, String accessToken) {
        this.restClient = restClient;
        this.room = room;
        this.accessToken = accessToken;
    }

    @Override
    public Map<String, float[]> read() {
        if (keymap == null)
            keymap = room.getARConfiguration().getKeymap();

        return keymap;
    }

    @Override
    public void save(final String key, final float x, final float y, final float z) {
        restClient.setARKeyboardCoordinates(accessToken, room.getId(), key, x, y, z,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "DONE");
                        keymap.put(key, new float[] {x, y, z});
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "FAIL");
                    }
                }

                );
    }

    public RESTClient getRestClient() {
        return restClient;
    }

    public void setRestClient(RESTClient restClient) {
        this.restClient = restClient;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
