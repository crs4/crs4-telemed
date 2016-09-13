/*
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */


package it.crs4.most.demo;


import java.util.HashMap;
import java.util.Map;

import android.provider.Settings.Secure;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * curl -X POST -d "client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=password&username=YOUR_USERNAME&password=YOUR_PASSWORD" http://localhost:8000/oauth2/access_token/
 * <p/>
 * curl -X POST -d "client_id=1f2138b9c94c388503fb&client_secret=fda712b6456c498c4e826e2942e30175d9a3c682&grant_type=password&username=admin&password=12345" http://localhost:8001/oauth2/access_token/
 * 1f2138b9c94c388503fb               fda712b6456c498c4e826e2942e30175d9a3c682
 * curl -X POST -d "client_id=d67a0f2868956edece1a&client_secret=29df85c27354579d87f026cb33007f350398a491&grant_type=password&username=admin&password=admin" http://localhost:8001/oauth2/access_token/
 * <p/>
 * curl -X POST -d "client_id=d67a0f2868956edece1a&client_secret=29df85c27354579d87f026cb33007f350398a491&grant_type=pincode&username=admin&pincode=12345" http://localhost:8001/oauth2/access_token/
 * <p/>
 * client id d67a0f2868956edece1a
 * client secret: 29df85c27354579d87f026cb33007f350398a491
 */

public class RemoteConfigReader {

    private static final String TAG = "RemoteConfigReader";
    private static final String OAUTH_CLIENT_ID = "9db4f27b3d9c8e352b5c";
    private static final String OAUTH_CLIENT_SECRET = "00ea399c013349a716ea3e47d8f8002502e2e982";
    public static final String GRANT_TYPE_PINCODE = "pincode";
    public static final String GRANT_TYPE_PASSWORD = "password";

    private RequestQueue mRequestQueue;
    private String mDeviceID;
    private String mUrlPrefix;

    /**
     * @param context    a Context object
     * @param serverIp   the ip of the remote configuration server
     * @param serverPort the port of the remote configuration server
     */
    public RemoteConfigReader(Context context, String serverIp, int serverPort) {
        mDeviceID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        mUrlPrefix = "http://" + serverIp + ":" + String.valueOf(serverPort) + "/";
        mRequestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Get the task groups associated to this specific device (i.e by its internal device id).
     * Note that the used device must be registered on the remote server
     *
     * @param listener      the listener where to receive the list of taskgroups associated to this device
     * @param errorListener the listener used for error responses
     */
    public void getTaskgroups(Response.Listener<JSONObject> listener,
                              Response.ErrorListener errorListener) {
        String uri = String.format("%steleconsultation/taskgroups/%s/", mUrlPrefix, mDeviceID);
        JsonObjectRequest req = new JsonObjectRequest(uri, null, listener, errorListener);
        mRequestQueue.add(req);
    }

    public void getTeleconsultationsByTaskgroup(String taskgroupId, String accessToken,
                                                Response.Listener<JSONObject> listener,
                                                Response.ErrorListener errorListener) {
        String uri = String.format("%steleconsultation/today/open/?access_token=%s", mUrlPrefix, accessToken);
        JsonObjectRequest postReq = new JsonObjectRequest(uri, null, listener, errorListener);
        mRequestQueue.add(postReq);
    }

    public void startSession(String sessionId,
                             String accessToken,
                             Response.Listener<JSONObject> listener,
                             Response.ErrorListener errorListener) {
        String uri = String.format("%steleconsultation/session/%s/start?access_token=%s",
            mUrlPrefix, sessionId, accessToken);
        JsonObjectRequest req = new JsonObjectRequest(uri, null, listener, errorListener);
        mRequestQueue.add(req);
    }

    public void runSession(String sessionId,
                           String accessToken,
                           Response.Listener<JSONObject> listener,
                           Response.ErrorListener errorListener) {
        String uri = String.format("%steleconsultation/session/%s/run?access_token=%s",
            mUrlPrefix, sessionId, accessToken);
        JsonObjectRequest req = new JsonObjectRequest(uri, null, listener, errorListener);
        mRequestQueue.add(req);
    }

    public void closeSession(String sessionId,
                             String accessToken,
                             Response.Listener<JSONObject> listener,
                             Response.ErrorListener errorListener) {
        String uri = String.format("%steleconsultation/session/%s/close?access_token=%s",
            mUrlPrefix, sessionId, accessToken);
        JsonObjectRequest req = new JsonObjectRequest(uri, null, listener, errorListener);
        mRequestQueue.add(req);
    }

    public void closeTeleconsultation(String teleconsultationId,
                                      String accessToken,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener) {
        String uri = String.format("%steleconsultation/close/%s/?access_token=%s",
            mUrlPrefix, teleconsultationId, accessToken);
        JsonObjectRequest req = new JsonObjectRequest(uri, null, listener, errorListener);
        mRequestQueue.add(req);
    }

    public void getSessionState(String sessionId,
                                String accessToken,
                                Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener) {
        String uri = String.format("%steleconsultation/session/%s/?access_token=%s",
            mUrlPrefix, sessionId, accessToken);
        JsonObjectRequest req = new JsonObjectRequest(uri, null, listener, errorListener);
        mRequestQueue.add(req);
    }

    /**
     * Retrieve the users associated to the specified String ID
     *
     * @param taskgroupId   the id of the taskgroup
     * @param listener      the listener where to receive the Taskgroup user(s)
     * @param errorListener the listener used for error responses
     */
    public void getUsersByTaskgroup(String taskgroupId,
                                    Response.Listener<JSONObject> listener,
                                    Response.ErrorListener errorListener) {
        String uri = String.format("%steleconsultation/applicants/%s/", mUrlPrefix, taskgroupId);
        JsonObjectRequest postReq = new JsonObjectRequest(uri, null, listener, errorListener);
        mRequestQueue.add(postReq);
    }

    /**
     * Get the room json object by its id
     *
     * @param roomId        the room id
     * @param accessToken   tbe access token
     * @param listener      the listener where to receive the json room data
     * @param errorListener the listener used for error responses
     */
    public void getRoom(String roomId,
                        String accessToken,
                        Response.Listener<JSONObject> listener,
                        Response.ErrorListener errorListener) {
        String uri = String.format("%steleconsultation/room/%s/?access_token=%s", mUrlPrefix, roomId, accessToken);
        Log.d(TAG, "getRoomDataUri: " + uri);
        JsonObjectRequest postReq = new JsonObjectRequest(uri, null, listener, errorListener);
        mRequestQueue.add(postReq);
    }

    /**
     * Get the rooms json list by the taskgroup id
     *
     * @param accessToken   tbe access token
     * @param listener      the listener where to receive the json room data
     * @param errorListener the listener used for error responses
     */
    public void getRooms(String accessToken,
                         Response.Listener<JSONObject> listener,
                         Response.ErrorListener errorListener) {
        String uri = String.format("%steleconsultation/rooms/?access_token=%s", mUrlPrefix, accessToken);
        JsonObjectRequest postReq = new JsonObjectRequest(uri, null, listener, errorListener);
        mRequestQueue.add(postReq);
    }

    /**
     * Get the access token needed for teleconsultation rest calls
     *
     * @param username      the user name
     * @param grantValue    the pin code of the user
     * @param taskgroup     the taskgroup id
     * @param listener      the listener where to receive the access token
     * @param errorListener the listener where to receive remote server error responses
     */
    public void getAccessToken(final String username, final String taskgroup,
                               final String grantType, final String grantValue,
                               Response.Listener<String> listener,
                               Response.ErrorListener errorListener) {
        String uri = mUrlPrefix + "oauth2/access_token/";
        Log.d(TAG, "Called getAccessToken() on uri: " + uri);
        Log.d(TAG, "client id: " + OAUTH_CLIENT_ID);
        Log.d(TAG, "client secret: " + OAUTH_CLIENT_SECRET);
        Log.d(TAG, "device ID: " + mDeviceID);
        Log.d(TAG, "username: " + username);
        Log.d(TAG, "grant_type: " + grantType);
        Log.d(TAG, "pincode: " + grantValue);
        Log.d(TAG, "taskgroup: " + taskgroup);

        StringRequest postReq = new StringRequest(Request.Method.POST, uri, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("client_id", OAUTH_CLIENT_ID);
                params.put("client_secret", OAUTH_CLIENT_SECRET);
                params.put("grant_type", grantType);
                params.put("username", username);
                params.put(grantType, grantValue);
                params.put("taskgroup", taskgroup); // "h52job6mlgqpym5f57djdtbsw3u5kfyf"
                return params;
            }
        };

        mRequestQueue.add(postReq);
        Log.d(TAG, "Request added to the queue");
    }

    /**
     * Create a new Teleconsultation
     *
     * @param description   A description of the Teleconsultation
     * @param accessToken   the oauth access token
     * @param listener      the listener to handle the response
     * @param errorListener the listener that handle the response in case of error
     */
    public void createNewTeleconsultation(final String description,
                                          final String severity,
                                          final String roomId,
                                          String accessToken,
                                          Response.Listener<String> listener,
                                          Response.ErrorListener errorListener) {

        String uri = String.format("%steleconsultation/create/?access_token=%s", mUrlPrefix, accessToken);
        StringRequest postReq = new StringRequest(Request.Method.POST, uri, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("description", description);
                params.put("severity", severity);
                params.put("room_uuid", roomId);

                return params;
            }
        };

        mRequestQueue.add(postReq);
    }

    /**
     * Create a new Teleconsultation Session
     *
     * @param teleconsultationUUID the teleconsultation ID
     * @param roomId               the room id
     * @param accessToken          the oauth access token
     * @param listener             the listener to handle the response
     * @param errorListener        the listener to handle the response in case of errors
     */
    public void createNewTeleconsultationSession(final String teleconsultationUUID,
                                                 final String roomId, String accessToken,
                                                 Response.Listener<String> listener,
                                                 Response.ErrorListener errorListener) {

        String uri = String.format("%steleconsultation/%s/session/create/?access_token=%s",
            mUrlPrefix, teleconsultationUUID, accessToken);
        StringRequest postReq = new StringRequest(Request.Method.POST, uri, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("room_uuid", roomId);

                return params;
            }
        };

        mRequestQueue.add(postReq);
    }

    public void joinSession(String sessionId, String accessToken, String ipAddress,
                            Response.Listener<JSONObject> listener,
                            Response.ErrorListener errorListener) {
        String uri = String.format("%steleconsultation/session/%s/%s/join?access_token=%s",
            mUrlPrefix, sessionId, ipAddress, accessToken);
        JsonObjectRequest req = new JsonObjectRequest(uri, null, listener, errorListener);
        mRequestQueue.add(req);
    }
    /*
    public void  getAccounts(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
		JsonObjectRequest postReq = new JsonObjectRequest( mUrlPrefix + "accounts/?access_token=" + accessToken, null, listener, errorListener);
		mRequestQueue.add(postReq);
		Log.d("most_example", "getAccountsRequest Sent");
	}


	public void getAccount(int accountId , Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
		JsonObjectRequest postReq = new JsonObjectRequest( mUrlPrefix + "accounts/" + String.valueOf(accountId)+"/?access_token=" + accessToken, null, listener, errorListener);
		mRequestQueue.add(postReq);
	}

	
	public void  getBuddies(int accountId , Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
		JsonObjectRequest postReq = new JsonObjectRequest( mUrlPrefix + "buddies/" + String.valueOf(accountId)+"/?access_token=" + accessToken, null, listener, errorListener);
		mRequestQueue.add(postReq);
	}
  */
}
