package it.crs4.most.demo.models;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import it.crs4.most.demo.TeleconsultationException;
import it.crs4.most.voip.Utils;

public class TeleconsultationSession implements Serializable {
    private static final long serialVersionUID = -6277133365800493720L;
    private static final String TAG = "TeleconsultationSession";
    private String mId;
    private String mSpecAppAddress;
    private TeleconsultationSessionState mSessionState;
    private HashMap<String, String> voipParams;
    private Room mRoom;

    //TODO: Should have also the room in the constructor
    public TeleconsultationSession(String id, String specAppAddress,
                                   TeleconsultationSessionState state, Room room) {
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

        if (role.equals("eco")) {   //TODO: should not be hardwired
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
