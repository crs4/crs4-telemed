package most.demo.ecoapp.models;

public class Room {
	
	private String id;
	private String name;
	private String description;
	
	private Device encoder;
	private Device camera;
	
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
	
	public Device getEncoder() {
		return encoder;
	}
	
	
	public void setEncoder(Device encoder) {
		this.encoder = encoder;
	}
	
	public Device getCamera() {
		return camera;
	}
	
	public void setCamera(Device camera) {
		this.camera = camera;
	}

	@Override
	public String toString(){
		return this.name;
	}
}
