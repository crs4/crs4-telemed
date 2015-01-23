package most.demo.ecoapp.models;

public class Patient {
	
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
