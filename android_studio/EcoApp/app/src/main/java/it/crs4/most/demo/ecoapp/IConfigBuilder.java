package it.crs4.most.demo.ecoapp;


import it.crs4.most.demo.ecoapp.models.Device;
import it.crs4.most.demo.ecoapp.models.EcoUser;
import it.crs4.most.demo.ecoapp.models.Patient;
import it.crs4.most.demo.ecoapp.models.Teleconsultation;
import it.crs4.most.demo.ecoapp.RemoteConfigReader;

public interface IConfigBuilder {
	
	RemoteConfigReader getRemoteConfigReader();
	void listEcoUsers();
	void listPatients();
	void setEcoUser(EcoUser user);
	EcoUser getEcoUser();
	Device getCamera();
	void setCamera(Device device);
	void setPatient(Patient selectedUser);
	Patient getPatient();
	void setTeleconsultation(Teleconsultation teleconsultation);
}
