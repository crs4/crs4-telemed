package it.crs4.most.demo.specapp.models;

import java.io.Serializable;

public class Device implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7284527758558743253L;
	private String streamUri = null;
	private String shotUri = null;
	private String webUri = null;
	private String ptzUri;
	private String name;
	String user;
	String pwd;

	public Device(String name, String streamUri, String shotUri, String webUri, String ptzUri, String user, String pwd)
	{   
		this.name = name;
		this.streamUri = streamUri;
		this.shotUri = shotUri;
		this.webUri = webUri;
		this.ptzUri = ptzUri;
		this.user = user;
		this.pwd = pwd;
	}

	
	public String getName() {
		return name;
	}
	
	public String getStreamUri() {
		return streamUri;
	}

	public String getShotUri() {
		return shotUri;
	}

	public String getWebUri() {
		return webUri;
	}
	
	public String getPtzUri() {
		return ptzUri;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPwd() {
		return pwd;
	}
	
	public String toString() {
		
		return String.format("[Device:%s\nStream: %s\nShot: %s\n Web: %s\n PTZ: %s]", this.name, this.streamUri, this.shotUri, this.webUri, this.ptzUri);
	}
}
