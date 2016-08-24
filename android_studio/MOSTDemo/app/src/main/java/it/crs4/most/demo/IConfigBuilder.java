package it.crs4.most.demo;

import it.crs4.most.demo.RemoteConfigReader;
import it.crs4.most.demo.models.Device;
import it.crs4.most.demo.models.User;
import it.crs4.most.demo.models.Patient;
import it.crs4.most.demo.models.Teleconsultation;

public interface IConfigBuilder {
	
	RemoteConfigReader getRemoteConfigReader();
	void listUsers();
	void listPatients();
	void setUser(User user);
	User getUser();
	void setPatient(Patient selectedUser);
	Patient getPatient();
	void setTeleconsultation(Teleconsultation teleconsultation);
}
