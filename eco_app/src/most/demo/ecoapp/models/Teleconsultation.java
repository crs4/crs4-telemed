package most.demo.ecoapp.models;

import java.io.Serializable;

public class Teleconsultation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1408055529735190987L;
	private String id;
	private String info;
	private Room room;
	
	private String severity;
	private EcoUser applicant;
	private String name;
	
	
    
	public Teleconsultation(String id, String name, String info, String severity, Room room, EcoUser applicant)
	{
		this.id = id;
		this.name = name;
		this.info = info;
		this.severity = severity;
	    this.room = room;
	    this.applicant = applicant;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

    public String getInfo()
    {
    	return this.info;
    }
    

	public String getSeverity() {
		return severity;
	}

	public EcoUser getApplicant() {
		return applicant;
	}

	public Room getRoom() {
		return room;
	}

	
}
