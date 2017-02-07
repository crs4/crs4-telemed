package it.crs4.most.demo.spec;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.Map;

import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.RESTClient;
import it.crs4.most.demo.models.Room;

/**
 * Created by mauro on 07/02/17.
 */

public class RESTKeyboardCoordinatesStore implements VirtualKeyboard.KeyboardCoordinatesStore {
    private static final String TAG = "RESTCStore";
    private RESTClient restClient;
    private Room room;
    String accessToken;

    public RESTKeyboardCoordinatesStore(Room room, RESTClient restClient, String accessToken) {
        this.restClient = restClient;
        this.room = room;
        this.accessToken = accessToken;
    }

    @Override
    public Map<String, float[]> read() {
        return room.getARConfiguration().getKeymap();
    }

    @Override
    public void save(Room room, String key, float x, float y, float z) {
        restClient.setARKeyboardCoordinates(accessToken, room.getId(), key, x, y, z,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "DONE");
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
