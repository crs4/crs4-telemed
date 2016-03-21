package it.crs4.most.demo.ecoapp.models;

import java.io.Serializable;

public class EcoUser implements Serializable {
	
	private static final long serialVersionUID = 6108801942060044140L;
	
	private String username = null;
	private TaskGroup taskGroup = null;
	private String firstName = null;
	private String lastName = null;
	private String accessToken = null;
	
	public EcoUser(String firstName, String lastName, String username, TaskGroup taskGroup)
	{   
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.taskGroup = taskGroup;
	}
	

	public String getFirstName() {
		return firstName;
	}


	public String getLastName() {
		return lastName;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setTaskGroup(TaskGroup taskGroup) {
		this.taskGroup = taskGroup;
	}

	
	


	public String getUsername() {
		return username;
	}
	

	public TaskGroup getTaskGroup() {
		return taskGroup;
	}


	public String getAccessToken() {
		return accessToken;
	}


	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
}
