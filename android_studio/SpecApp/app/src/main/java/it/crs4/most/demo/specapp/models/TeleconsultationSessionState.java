package it.crs4.most.demo.specapp.models;


public enum TeleconsultationSessionState {
	
	NEW("new session"),	
	WAITING("Session waiting for specialist"),
	READY("Session ready to start"),
    RUN("Session in progress"),
    CLOSE("Session is closed"),
    CANCELED("Session is canceled");
	
    private String description;
    
    private TeleconsultationSessionState(String description) {
       
        this.description = description;
    }
    
    public String getDescription() {
    	return description;
    }
    
    @Override
    public String toString() {
        return this.name();
    }
    
    public static TeleconsultationSessionState getState(String state)
    {
    	for (TeleconsultationSessionState st : TeleconsultationSessionState.values())
    	{
    		if (st.name().equalsIgnoreCase(state))
    			return st;
    	}
    	return null;
    }
}