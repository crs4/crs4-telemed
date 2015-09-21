package most.demo.specapp.models;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;


public class TeleconsultationSession implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2115275250797165484L;
	private String id;
	private TeleconsultationSessionState tss;
    //private JSONObject sessionData = null;

    private Device camera = null;
    private Device encoder = null;
    private HashMap <String,String> voipParams = null; 
    
	public HashMap<String, String> getVoipParams() {
		return voipParams;
	}

	public void setupSessionData(JSONObject sessionData)
	{
		try {
			JSONObject sd = sessionData.getJSONObject("data").getJSONObject("session");
			this.camera = getDevice(sd, "camera");
			this.encoder = getDevice(sd, "encoder");
			this.voipParams = getVoipSetupParams(sd);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	private Device getDevice(JSONObject sessionData, String deviceName)
	{
		try {
			JSONObject jcamera = sessionData.getJSONObject("room").getJSONObject("devices").getJSONObject(deviceName);
			
			return new Device(jcamera.getString("name"), jcamera.getJSONObject("capabilities").optString("streaming"),
														 jcamera.getJSONObject("capabilities").optString("shot"),
														 jcamera.getJSONObject("capabilities").optString("web"),
														 jcamera.getJSONObject("capabilities").optString("ptz"),
									 jcamera.getString("user"),
									 jcamera.getString("password"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	public TeleconsultationSession(String id , TeleconsultationSessionState tss)
	{
		this.id = id;
		this.tss = tss;
	}
	
	
	private HashMap<String,String> getVoipSetupParams(JSONObject sessionData) 
    { 
		JSONObject specialistVoipData;
		try {
			specialistVoipData = sessionData.getJSONObject("teleconsultation").getJSONObject("specialist").getJSONObject("voip_data");
			String sipServerIp = specialistVoipData.getJSONObject("sip_server").getString("address");
		    String sipServerPort=specialistVoipData.getJSONObject("sip_server").getString("port");
		    String sipServerTransport=specialistVoipData.getString("sip_transport");
		    String sipUserName = specialistVoipData.getString("sip_user");
		    String sipUserPwd = specialistVoipData.getString("sip_password");
		    String ecoExtension = sessionData.getJSONObject("teleconsultation").getJSONObject("applicant").getJSONObject("voip_data").getString("extension");
		    
		    HashMap <String,String> params = new HashMap<String,String>();
			
	    	params.put("sipServerIp",sipServerIp); 
			params.put("sipServerPort",sipServerPort); // default 5060
			params.put("sipServerTransport",sipServerTransport); 
			
			
			// used by the app for calling the specified extension, not used directly by the VoipLib
			params.put("ecoExtension",ecoExtension); 

			// specialista	
			params.put("sipUserPwd", sipUserPwd); // 
			params.put("sipUserName",sipUserName); // specialista
			
			//params.put("turnServerIp",  sipServerIp);
			//params.put("turnServerUser",sipUserName);   
			//params.put("turnServerPwd",sipUserPwd); 
			
			return params;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	   
    }
	 
	public String getId() {
		return id;
	}

	public Device getCamera()
	{
		return this.camera;
	}
	
	public Device getEncoder()
	{
		return this.encoder;
	}
	
	
	public void setState(TeleconsultationSessionState tss)
	{
		this.tss = tss;
	}
	
	public TeleconsultationSessionState getState()
	{
		return this.tss;
	}
}
