package most.demo.ecoapp;


import most.demo.ecoapp.models.Device;
import most.demo.ecoapp.models.EcoUser;
import most.demo.ecoapp.models.Patient;
import most.demo.ecoapp.models.Teleconsultation;
import most.demo.ecoapp.RemoteConfigReader;

public interface IConfigBuilder {
	
	public RemoteConfigReader getRemoteConfigReader();
	public void listEcoUsers();
	public void listPatients();
	public void setEcoUser(EcoUser user);
	public EcoUser getEcoUser();
	public Device getCamera();
	public void setCamera(Device device);
	public void setPatient(Patient selectedUser);
	public Patient getPatient();
	public void setTeleconsultation(Teleconsultation teleconsultation);
}
