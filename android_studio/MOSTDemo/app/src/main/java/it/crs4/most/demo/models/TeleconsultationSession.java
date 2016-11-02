package it.crs4.most.demo.models;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.TeleconsultationException;
import it.crs4.most.voip.Utils;

public class TeleconsultationSession implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String TAG = "TeleconsultationSession";
    private String mId;
    private String mSpecAppAddress;
    private TeleconsultationSessionState mSessionState;
    private HashMap<String, String> voipParams;
    private Room mRoom;
    private Context mContext;

    public TeleconsultationSession(String id, String specAppAddress, TeleconsultationSessionState state, Room room) {
        mId = id;
        mSpecAppAddress = specAppAddress;
        mSessionState = state;
        mRoom = room;
    }

    public void setVoipParams(Context context, JSONObject sessionData, String role) {
        JSONObject localExtData;
        String localUser;
        String remoteUser;
        String paramName;
        String sipServerIp;
        String sipServerPort;
        String sipServerTransport;
        String turnServerIp = null;
        String turnServerPort = null;
        String turnServerUser = null;
        String turnServerPwd = null;
        String sipUserName;
        String sipUserPwd;
        String remoteExtData;
        String onHoldSoundPath;
        String incomingCallPath;
        String onOutcomingCallPath;

        if (QuerySettings.isEcographist(context)) {
            localUser = "applicant";
            remoteUser = "specialist";
            paramName = "specExtension";
        }
        else {
            localUser = "specialist";
            remoteUser = "applicant";
            paramName = "ecoExtension";
        }
        try {
            localExtData = sessionData.getJSONObject("teleconsultation").getJSONObject(localUser).getJSONObject("voip_data");
            sipServerIp = localExtData.getJSONObject("sip_server").getString("address");
            sipServerPort = localExtData.getJSONObject("sip_server").getString("port");
            sipServerTransport = localExtData.getString("sip_transport");
            sipUserName = localExtData.getString("sip_user");
            sipUserPwd = localExtData.getString("sip_password");
            Log.d(TAG, localExtData.toString());
            if (!localExtData.isNull("turn_server")) {
                turnServerIp = localExtData.getJSONObject("turn_server").getString("address");
                turnServerPort = localExtData.getJSONObject("turn_server").getString("port");
                turnServerUser = localExtData.getString("turn_user");
                turnServerPwd = localExtData.getString("turn_password");
            }
            remoteExtData = sessionData.getJSONObject("teleconsultation").getJSONObject(remoteUser).getJSONObject("voip_data").getString("extension");
            onHoldSoundPath = Utils.getResourcePathByAssetCopy(context, "", "test_hold.wav");
            incomingCallPath = Utils.getResourcePathByAssetCopy(context, "", "ring_in_call.wav");
            onOutcomingCallPath = Utils.getResourcePathByAssetCopy(context, "", "ring_out_call.wav");
        }
        catch (JSONException e) {
            Log.e(TAG, "Error loading voip data");
            e.printStackTrace();
            return;
        }

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
        voipParams.put("onIncomingCallSound", incomingCallPath);
        voipParams.put("onOutcomingCallSound", onOutcomingCallPath);
        if (turnServerIp != null && turnServerPort != null &&
            turnServerUser != null && turnServerPwd != null) {
            voipParams.put("turnServerIp", turnServerIp);
            voipParams.put("turnServerPort", turnServerPort);
            voipParams.put("turnServerUser", turnServerUser);
            voipParams.put("turnServerPwd", turnServerPwd);
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

    public static TeleconsultationSession fromJSON(Context context, JSONObject sessionData, String role) throws TeleconsultationException {
        String id;
        String state;
        String specAppAddress;
        JSONObject roomData;

        try {
            id = sessionData.getString("uuid");
            state = sessionData.getString("state");
            roomData = sessionData.getJSONObject("room");
        }
        catch (JSONException e) {
            throw new TeleconsultationException();
        }

        try {
            specAppAddress = sessionData.getString("spec_app_address");
        }
        catch (JSONException e) {
            specAppAddress = null;
        }

        Room room = Room.fromJSON(roomData);
        TeleconsultationSession tcSession = new TeleconsultationSession(id, specAppAddress,
            TeleconsultationSessionState.getState(state), room);

        tcSession.setVoipParams(context, sessionData, role);
        return tcSession;
    }
}
