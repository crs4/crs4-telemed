package most.demo.ecoapp.models;

import java.io.Serializable;

public class Teleconsultation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1408055529735190987L;
	private String id;
	private String info;
	private String roomId;
	private Device encoder;
	private Device camera;
	
	public String getId() {
		return id;
	}

    public String getInfo()
    {
    	return this.info;
    }
    
	public Teleconsultation(String id, String info, String roomId)
	{
		this.id = id;
		this.info = info;
	    this.roomId = roomId;
	}

	public String getRoomId() {
		return roomId;
	}

	public Device getEncoder() {
		return encoder;
	}
	
	
	public void setEncoder(Device encoder) {
		this.encoder = encoder;
	}
	
	public Device getCamera() {
		return camera;
	}
	
	public void setCamera(Device camera) {
		this.camera = camera;
	}
}
