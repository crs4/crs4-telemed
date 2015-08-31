package most.demo.specapp.models;

import java.io.Serializable;

public class SpecUser implements Serializable {
	
	private static final long serialVersionUID = -1438332811570855646L;

	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	private String username = null;
    private String accessToken = null;
    private String taskgroupId = null;
 
 

	public SpecUser(String username, String taskgroupId, String accessToken)
	{
		this.username = username;
		this.taskgroupId = taskgroupId;
		this.accessToken = accessToken;	
	}

	public String getTaskgroupId() {
		return taskgroupId;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getUsername() {
		return username;
	}
	
	public String getAccessToken() {
		return this.accessToken;
	}
	
}
