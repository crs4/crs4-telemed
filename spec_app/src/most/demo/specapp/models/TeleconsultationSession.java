package most.demo.specapp.models;

public class TeleconsultationSession {

	private String id;
	private TeleconsultationSessionState tss;


	public TeleconsultationSession(String id , TeleconsultationSessionState tss)
	{
		this.id = id;
		this.tss = tss;
	}
	
	
	public String getId() {
		return id;
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
