package most.demo.specapp.models;

import java.io.Serializable;

public class Teleconsultation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1408055529735190987L;
	private String id;
	private String info;
	
	public String getId() {
		return id;
	}

    public String getInfo()
    {
    	return this.info;
    }
    
	public Teleconsultation(String id, String info)
	{
		this.id = id;
		this.info = info;
	}
	
}
