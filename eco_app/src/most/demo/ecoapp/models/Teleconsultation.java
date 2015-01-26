package most.demo.ecoapp.models;

import java.io.Serializable;

public class Teleconsultation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1408055529735190987L;
	private String id;
	
	public String getId() {
		return id;
	}


	public Teleconsultation(String id)
	{
		this.id = id;
	}
	
}
