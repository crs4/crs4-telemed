package it.crs4.most.demo.ecoapp;


import it.crs4.most.demo.ecoapp.models.Device;
import it.crs4.most.demo.ecoapp.models.EcoUser;
import it.crs4.most.demo.ecoapp.models.Patient;
import it.crs4.most.demo.ecoapp.models.Teleconsultation;
import it.crs4.most.demo.ecoapp.RemoteConfigReader;

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
