package it.crs4.most.demo.ecoapp.models;

import android.content.Context;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import it.crs4.most.voip.Utils;

public class TeleconsultationSession implements Serializable {
    private static final long serialVersionUID = -6277133365800493720L;
    private String mId;
    private TeleconsultationSessionState mSessionState;
    private HashMap<String, String> voipParams;

    public TeleconsultationSession(String id, TeleconsultationSessionState teleconsultationSessionState) {
        mId = id;
        mSessionState = teleconsultationSessionState;
    }

    public void setVoipParams(Context context, JSONObject sd) {
        JSONObject applicantVoipData;
        try {
            JSONObject sessionData = sd.getJSONObject("data").getJSONObject("session");

            applicantVoipData = sessionData.getJSONObject("teleconsultation").getJSONObject("applicant").getJSONObject("voip_data");

            String sipServerIp = applicantVoipData.getJSONObject("sip_server").getString("address");
            String sipServerPort = applicantVoipData.getJSONObject("sip_server").getString("port");
            String sipServerTransport = applicantVoipData.getString("sip_transport");
            String sipUserName = applicantVoipData.getString("sip_user");
            String sipUserPwd = applicantVoipData.getString("sip_password");
            String specExtension = sessionData.getJSONObject("teleconsultation").getJSONObject("specialist").getJSONObject("voip_data").getString("extension");
            String onHoldSoundPath = Utils.getResourcePathByAssetCopy(context, "", "test_hold.wav");
            String onIncomingCallRingTonePath = Utils.getResourcePathByAssetCopy(context, "", "ring_in_call.wav");
            String onOutcomingCallRingTonePath = Utils.getResourcePathByAssetCopy(context, "", "ring_out_call.wav");


            voipParams = new HashMap<>();

            voipParams.put("sipServerIp", sipServerIp);
            voipParams.put("sipServerPort", sipServerPort); // default 5060
            voipParams.put("sipServerTransport", sipServerTransport);

            // used by the app for calling the specified extension, not used directly by the VoipLib (TO CHECK)
            voipParams.put("specExtension", specExtension);

            // specialista
            voipParams.put("sipUserPwd", sipUserPwd); //
            voipParams.put("sipUserName", sipUserName); // specialista

            voipParams.put("onHoldSound", onHoldSoundPath);
            voipParams.put("onIncomingCallSound", onIncomingCallRingTonePath);
            voipParams.put("onOutcomingCallSound", onOutcomingCallRingTonePath);
            //params.put("turnServerIp",  sipServerIp);
            //params.put("turnServerUser",sipUserName);
            //params.put("turnServerPwd",sipUserPwd);


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getVoipParams() {
        return voipParams;
    }

    public String getId() {
        return mId;
    }

    public void setState(TeleconsultationSessionState tss) {
        mSessionState = tss;
    }

    public TeleconsultationSessionState getState() {
        return mSessionState;
    }
}
