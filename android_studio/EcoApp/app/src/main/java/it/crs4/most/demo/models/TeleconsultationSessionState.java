package it.crs4.most.demo.models;


public enum TeleconsultationSessionState {
    NEW("NEW", "new session"),
    WAITING("WAITING", "Session waiting for specialist"),
    READY("READY", "Session ready to start"),
    RUN("RUN", "Session in progress"),
    CLOSE("CLOSE", "Session is closed"),
    CANCELED("CANCELED", "Session is canceled");

    private String mState;
    private String mDescription;

    TeleconsultationSessionState(String state, String description) {
        mState = state;
        mDescription = description;
    }

    public String getDescription() {
        return mDescription;
    }

    @Override
    public String toString() {
        return mState;
    }

    public static TeleconsultationSessionState getState(String state) {
        for (TeleconsultationSessionState st : TeleconsultationSessionState.values()) {
            if (st.mState.equalsIgnoreCase(state))
                return st;
        }
        return null;
    }
}