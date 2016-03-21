package it.crs4.most.demo.ecoapp.models;

import java.io.Serializable;

public class TaskGroup implements Serializable {
	
	
	private static final long serialVersionUID = 6314390526009668956L;
	private String id;
	private String description;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TaskGroup (String id, String description)
	{
		this.id = id;
		this.description = description;
		
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

}
