package most.demo.specapp;

import most.demo.specapp.models.SpecUser;
import most.demo.specapp.models.Teleconsultation;


public interface IConfigBuilder {

	public RemoteConfigReader getRemoteConfigReader();
	
	public void setSpecUser(SpecUser user);
	public SpecUser getSpecUser();
	
	public void setTeleconsultation(Teleconsultation selectedTc);
	public Teleconsultation getTeleconsultation();

}
