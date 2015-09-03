package most.demo.ecoapp.models;

public class TeleconsultationSession {

	private String id;
	private Teleconsultation tc;
	private TeleconsultationSessionState tss;


	public TeleconsultationSession(String id , Teleconsultation tc, TeleconsultationSessionState tss)
	{
		this.id = id;
		this.tc = tc;
		this.tss = tss;
	}
	
	

	public String getId() {
		return id;
	}

	public Teleconsultation getTeleconsultation() {
		return tc;
	}
	
	
	public void setState(TeleconsultationSessionState tss)
	{
		this.tss = tss;
	}
	
	public TeleconsultationSessionState getState()
	{
		return this.tss;
	}
}
