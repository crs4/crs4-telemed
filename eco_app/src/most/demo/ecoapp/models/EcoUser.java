package most.demo.ecoapp.models;

import java.io.Serializable;

public class EcoUser implements Serializable {
	
	 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6108801942060044140L;
	public String getUserpwd() {
		return userpwd;
	}

	public void setUserpwd(String userpwd) {
		this.userpwd = userpwd;
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

	private String username = null;
	private TaskGroup taskGroup = null;
	private String userpwd;

	public EcoUser(String username, String userpwd, TaskGroup taskGroup)
	{
		this.username = username;
		this.taskGroup = taskGroup;
		this.userpwd = userpwd;
	}

	public String getUsername() {
		return username;
	}
	
	public String getUserPwd() {
		return userpwd;
	}

	public TaskGroup getTaskGroup() {
		return taskGroup;
	}
}
