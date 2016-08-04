package it.crs4.most.demo.models;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import it.crs4.most.voip.Utils;

public class TeleconsultationSession implements Serializable {
    private static final long serialVersionUID = -6277133365800493720L;
    private static final String TAG = "TeleconsultationSession";
    private String mId;
    private String mSpecAppAddress;
    private TeleconsultationSessionState mSessionState;
    private HashMap<String, String> voipParams;
    private Room mRoom;

    public TeleconsultationSession(String id, String specAppAddress, TeleconsultationSessionState state) {
        mId = id;
        mSpecAppAddress = specAppAddress;
        mSessionState = state;
    }

    public void setVoipParams(Context context, JSONObject sessionData, int role) {
        JSONObject localExtData;
        String localUser;
        String remoteUser;
        String paramName;

        if (role == 0) {
            localUser = "applicant";
            remoteUser = "specialist";
            paramName = "specExtension";
        }
        else {
            localUser = "specialist";
            remoteUser = "applicant";
            paramName = "ecoExtension";
        }
        Log.d(TAG, "Setting voip params for: " + localUser + " " + remoteUser + " " + paramName);
        Log.d(TAG, "DATA ARE: " + sessionData);
        try {
            localExtData = sessionData.getJSONObject("teleconsultation").getJSONObject(localUser).getJSONObject("voip_data");
            String sipServerIp = localExtData.getJSONObject("sip_server").getString("address");
            String sipServerPort = localExtData.getJSONObject("sip_server").getString("port");
            String sipServerTransport = localExtData.getString("sip_transport");
            String sipUserName = localExtData.getString("sip_user");
            String sipUserPwd = localExtData.getString("sip_password");
            String remoteExtData = sessionData.getJSONObject("teleconsultation").getJSONObject(remoteUser).getJSONObject("voip_data").getString("extension");
            String onHoldSoundPath = Utils.getResourcePathByAssetCopy(context, "", "test_hold.wav");
            String onIncomingCallRingTonePath = Utils.getResourcePathByAssetCopy(context, "", "ring_in_call.wav");
            String onOutcomingCallRingTonePath = Utils.getResourcePathByAssetCopy(context, "", "ring_out_call.wav");

            voipParams = new HashMap<>();
            voipParams.put("sipServerIp", sipServerIp);
            voipParams.put("sipServerPort", sipServerPort); // default 5060
            voipParams.put("sipServerTransport", sipServerTransport);

            // used by the app for calling the specified extension, not used directly by the VoipLib (TO CHECK)
            voipParams.put(paramName, remoteExtData);

            // specialista
            voipParams.put("sipUserPwd", sipUserPwd);
            voipParams.put("sipUserName", sipUserName);
            voipParams.put("onHoldSound", onHoldSoundPath);
            voipParams.put("onIncomingCallSound", onIncomingCallRingTonePath);
            voipParams.put("onOutcomingCallSound", onOutcomingCallRingTonePath);
            //params.put("turnServerIp",  sipServerIp);
            //params.put("turnServerUser",sipUserName);
            //params.put("turnServerPwd",sipUserPwd);

        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getVoipParams() {
        return voipParams;
    }

    public Room getRoom() {
        return mRoom;
    }

    public void setRoom(Room room) {
        mRoom = room;
    }

    private Device getDevice(JSONObject sessionData, String deviceName) {
        try {
            JSONObject jcamera = sessionData.getJSONObject("room").getJSONObject("devices").getJSONObject(deviceName);

            return new Device(jcamera.getString("name"), jcamera.getJSONObject("capabilities").optString("streaming"),
                    jcamera.getJSONObject("capabilities").optString("shot"),
                    jcamera.getJSONObject("capabilities").optString("web"),
                    jcamera.getJSONObject("capabilities").optString("ptz"),
                    jcamera.getString("user"),
                    jcamera.getString("password"));
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public Device getCamera() {
        return mRoom.getCamera();
    }

    public Device getEncoder() {
        return mRoom.getEncoder();
    }

    public String getId() {
        return mId;
    }

    public String getSpecAppAddress() {
        return mSpecAppAddress;
    }

    public void setSpecAppAddress(String specAppAddress) {
        mSpecAppAddress = specAppAddress;
    }

    public void setState(TeleconsultationSessionState state) {
        mSessionState = state;
    }

    public TeleconsultationSessionState getState() {
        return mSessionState;
    }

    public static TeleconsultationSession fromJSON(Context context, JSONObject sessionData, int role) {
        try {
            String id = sessionData.getString("uuid");
            String state = sessionData.getString("state");
            String specAppAddress = sessionData.getString("spec_app_address");
            JSONObject roomData = sessionData.getJSONObject("room");
            Room room = Room.fromJSON(roomData);

            TeleconsultationSession tcSession = new TeleconsultationSession(id, specAppAddress,
                    TeleconsultationSessionState.getState(state));
            tcSession.setRoom(room);
            tcSession.setVoipParams(context, sessionData, role);
            return tcSession;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }
}
