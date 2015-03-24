package most.demo.ecoapp.models;

public class Room {
	
	private String id;
	private String name;
	private String description;
	
	
	public String getId() {
		return id;
	}


	public String getName() {
		return name;
	}


	public String getDescription() {
		return description;
	}


	public Room(String id, String name,  String description){
		this.id = id;
		this.name = name;
		this.description = description;
	}

	@Override
	public String toString(){
		return this.name;
	}
}
