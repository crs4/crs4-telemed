package most.demo.ecoapp;

import most.demo.ecoapp.models.EcoUser;
import most.demo.ecoapp.models.Patient;

public interface IConfigBuilder {
	
	public void listEcoUsers();
	public void listPatients();
	public void setEcoUser(EcoUser user);
	public EcoUser getEcoUser();
	
	public void setPatient(Patient selectedUser);
}
