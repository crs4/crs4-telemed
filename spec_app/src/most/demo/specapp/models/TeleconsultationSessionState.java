package most.demo.specapp.models;


public enum TeleconsultationSessionState {
	
	NEW("NEW", "new session"),	
	WAITING("WAITING","Session waiting for specialist"),
	READY("READY","Session ready to start"),
    RUN("RUN","Session in progress"),
    CLOSE("CLOSE","Session is closed"),
    CANCELED("CANCELED","Session is canceled");
	
	private String state;
    private String description;
    
    private TeleconsultationSessionState(String state, String description) {
        this.state = state;
        this.description = description;
    }
    
    public String getDescription() {
    	return description;
    }
    
    @Override
    public String toString() {
        return state;
    }
    
    public static TeleconsultationSessionState  getState(String state)
    {
    	for (TeleconsultationSessionState st : TeleconsultationSessionState.values())
    	{
    		if (st.state.equalsIgnoreCase(state))
    			return st;
    	}
    	return null;
    }
}