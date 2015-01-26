package most.demo.ecoapp.models;

import java.io.Serializable;

public class Patient implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8133793084935247284L;
	private String id;
	private String name;
	private String surname;

	public Patient(String name, String surname, String id)
	{
		this.id = id;
		this.name = name;
		this.surname = surname;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSurname() {
		return surname;
	}

}
