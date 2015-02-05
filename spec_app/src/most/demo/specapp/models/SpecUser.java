package most.demo.specapp.models;

import java.io.Serializable;

public class SpecUser implements Serializable {
	
	private static final long serialVersionUID = -1438332811570855646L;

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

	
	private String username = null;
	private String userpwd;

	public SpecUser(String username, String userpwd)
	{
		this.username = username;
		this.userpwd = userpwd;
	}

	public String getUsername() {
		return username;
	}
	
	public String getUserPwd() {
		return userpwd;
	}

}
