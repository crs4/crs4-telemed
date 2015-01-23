package most.demo.ecoapp.models;

public class EcoUser {
	
	private String username = null;
	private String opUnit = null;
	private String userpwd;

	public EcoUser(String username, String userpwd, String opUnit)
	{
		this.username = username;
		this.opUnit = opUnit;
		this.userpwd = userpwd;
	}

	public String getUsername() {
		return username;
	}
	
	public String getUserPwd() {
		return userpwd;
	}

	public String getOpUnit() {
		return opUnit;
	}
}
