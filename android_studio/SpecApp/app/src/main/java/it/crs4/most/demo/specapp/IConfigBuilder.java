package it.crs4.most.demo.specapp;

import it.crs4.most.demo.specapp.models.SpecUser;
import it.crs4.most.demo.specapp.models.Teleconsultation;


public interface IConfigBuilder {

	public RemoteConfigReader getRemoteConfigReader();
	
	public void setSpecUser(SpecUser user);
	public SpecUser getSpecUser();
	
	public void setTeleconsultation(Teleconsultation selectedTc);
	public Teleconsultation getTeleconsultation();

}
