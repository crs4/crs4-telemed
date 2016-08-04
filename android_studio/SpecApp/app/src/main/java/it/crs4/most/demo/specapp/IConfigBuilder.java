package it.crs4.most.demo.specapp;

import it.crs4.most.demo.specapp.models.User;
import it.crs4.most.demo.specapp.models.Teleconsultation;


public interface IConfigBuilder {

	public RemoteConfigReader getRemoteConfigReader();
	
	public void setUser(User user);
	public User getUser();
	
	public void setTeleconsultation(Teleconsultation selectedTc);
	public Teleconsultation getTeleconsultation();

}
