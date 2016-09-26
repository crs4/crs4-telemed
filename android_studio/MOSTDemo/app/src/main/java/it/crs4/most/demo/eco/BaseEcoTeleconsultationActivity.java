package it.crs4.most.demo.eco;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.RESTClient;
import it.crs4.most.demo.TeleconsultationState;
import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.streaming.StreamingEventBundle;
import it.crs4.most.voip.VoipEventBundle;
import it.crs4.most.voip.VoipLib;
import it.crs4.most.voip.VoipLibBackend;
import it.crs4.most.voip.enums.CallState;
import it.crs4.most.voip.enums.VoipEvent;
import it.crs4.most.voip.enums.VoipEventType;


@SuppressLint("InlinedApi")
public abstract class BaseEcoTeleconsultationActivity extends AppCompatActivity {

    private static final String TAG = "EcoTeleconsultActivity";
    public static final String TELECONSULTATION_ARG = "teleconsultation";
    protected TeleconsultationState mTcState = TeleconsultationState.IDLE;
    private String mSipServerIp;
    private String mSipServerPort;
    private VoipLib mVoipLib;
    private CallHandler voipHandler;
    protected StreamHandler mStreamHandler;
    private AudioManager mAudioManager;
    private int mOriginalAudioMode;
    protected HashMap<String, String> voipParams;

    protected boolean localHold = false;
    protected boolean remoteHold = false;
    protected boolean accountRegistered = false;

    protected Teleconsultation teleconsultation;
    protected RESTClient mRESTClient;

    protected abstract void notifyTeleconsultationStateChanged();

    protected void stopStream() {}

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mOriginalAudioMode = mAudioManager.getMode();
        Log.d(TAG, "Audio mode is: " + mOriginalAudioMode);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    protected void setTeleconsultationState(TeleconsultationState tcState) {
        mTcState = tcState;
        notifyTeleconsultationStateChanged();
    }

    protected void endTeleconsultation() {
        closeSession();
        if (mVoipLib.getCall().getState().equals(CallState.ACTIVE)) {
            hangupCall();
        }
        else {
            mVoipLib.destroyLib();
        }
    }

    protected void closeSession() {
        //TODO: think of putting this in the TeleconsultationSetupActivity
        final String accessToken = QuerySettings.getAccessToken(this);
        mRESTClient.closeSession(teleconsultation.getLastSession().getId(),
            accessToken,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject sessionData) {
                    mRESTClient.closeTeleconsultation(
                        teleconsultation.getId(),
                        accessToken,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                    Log.d(TAG, "Session closed: " + sessionData);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError err) {
                    Log.e(TAG, "Error closing the session: " + err);
                }
            });
    }

    protected void answerCall() {
        mVoipLib.answerCall();
    }

    protected void registerAccount() {
        mVoipLib.registerAccount();
    }

    protected CallState getCallState() {
        return mVoipLib.getCall().getState();
    }

    protected void toggleHoldCall(boolean holding) {
        if (holding) {
            mVoipLib.holdCall();
        }
        else {
            mVoipLib.unholdCall();
        }
    }

    protected void hangupCall() {
        mVoipLib.hangupCall();
    }

    protected void setupVoipLib() {
        // Voip Lib Initialization Params
        voipParams = teleconsultation.getLastSession().getVoipParams();
        mSipServerIp = voipParams.get("sipServerIp");
        mSipServerPort = voipParams.get("sipServerPort");
        mVoipLib = new VoipLibBackend();
        voipHandler = new CallHandler(this);
        mVoipLib.initLib(getApplicationContext(), voipParams, voipHandler);
    }

    protected void subscribeBuddies() {
        String buddyExtension = voipParams.get("specExtension");
        Log.d(TAG, "Subscribing buddy " + buddyExtension);
        mVoipLib.getAccount().addBuddy(getBuddyUri(buddyExtension));
    }

    protected String getBuddyUri(String extension) {
        return "sip:" + extension + "@" + mSipServerIp + ":" + mSipServerPort;
    }

    // VOIP METHODS AND LOGIC
    protected static class CallHandler extends Handler {

        private final WeakReference<BaseEcoTeleconsultationActivity> mOuterRef;

        private CallHandler(BaseEcoTeleconsultationActivity outerRef) {
            mOuterRef = new WeakReference<>(outerRef);
        }

        @Override
        public void handleMessage(Message voipMessage) {
            BaseEcoTeleconsultationActivity act = mOuterRef.get();
            VoipEventBundle eventBundle = (VoipEventBundle) voipMessage.obj;
            Log.d(TAG, "Event type:" + eventBundle.getEventType() + " Event: " + eventBundle.getEvent());

            VoipEvent event = eventBundle.getEvent();
            if (event == VoipEvent.LIB_INITIALIZED) {
                // Register the account after the Lib Initialization
                act.registerAccount();
            }
            else if (event == VoipEvent.ACCOUNT_REGISTERED) {
                // The first time it is called we subscribe the buddy
                if (!act.accountRegistered) {  // Next times we don't need to do this
                    act.subscribeBuddies();
                    act.accountRegistered = true;
                }
            }
            else if (event == VoipEvent.ACCOUNT_UNREGISTERED) {
                act.setTeleconsultationState(TeleconsultationState.IDLE);
            }
            else if (eventBundle.getEventType() == VoipEventType.BUDDY_EVENT) {
                // There is only one subscribed buddy in this mMainActivity, so we don't need to get IBuddy informations
                if (event == VoipEvent.BUDDY_CONNECTED) {
                    // the remote buddy is no longer on Hold State
                    if (act.mTcState == TeleconsultationState.REMOTE_HOLDING ||
                        act.mTcState == TeleconsultationState.HOLDING) {
                        if (act.localHold) {
                            act.setTeleconsultationState(TeleconsultationState.HOLDING);
                        }
                        else {
                            act.setTeleconsultationState(TeleconsultationState.CALLING);
                        }
                    }
                    else if (act.mTcState == TeleconsultationState.IDLE) {
                        act.setTeleconsultationState(TeleconsultationState.READY);
                    }
                }
                else if (event == VoipEvent.BUDDY_HOLDING) {
                    CallState callState = act.getCallState();
                    if (callState == CallState.ACTIVE || callState == CallState.HOLDING) {
                        act.setTeleconsultationState(TeleconsultationState.REMOTE_HOLDING);
                    }
                }
                else if (event == VoipEvent.BUDDY_DISCONNECTED) {
                    act.setTeleconsultationState(TeleconsultationState.IDLE);
                }
            }
            else if (event == VoipEvent.CALL_INCOMING) {
                // answer call as soon as we get the call
                act.answerCall();
            }
            else if (event == VoipEvent.CALL_ACTIVE) {
                act.mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                if (!act.mAudioManager.isWiredHeadsetOn()) {
                    act.mAudioManager.setSpeakerphoneOn(true);
                }
                else {
                    act.mAudioManager.setSpeakerphoneOn(false);
                }
                act.setTeleconsultationState(TeleconsultationState.CALLING);
            }
            else if (event == VoipEvent.CALL_HOLDING) {
                act.setTeleconsultationState(TeleconsultationState.HOLDING);
            }
            else if (event == VoipEvent.CALL_HANGUP || event == VoipEvent.CALL_REMOTE_HANGUP) {
                act.endTeleconsultation();
                act.mAudioManager.setMode(act.mOriginalAudioMode);
            }
            // Deinitialize the Voip Lib and release all allocated resources
            else if (event == VoipEvent.LIB_DEINITIALIZED) {
                act.setResult(RESULT_OK);
                act.stopStream();
                act.finish();
            }
            else if (event == VoipEvent.LIB_DEINITIALIZATION_FAILED) {
            }
            else if (event == VoipEvent.LIB_INITIALIZATION_FAILED ||
                event == VoipEvent.ACCOUNT_REGISTRATION_FAILED ||
                event == VoipEvent.LIB_CONNECTION_FAILED ||
                event == VoipEvent.BUDDY_SUBSCRIPTION_FAILED)
                showErrorEventAlert(eventBundle);
        }

        private void showErrorEventAlert(VoipEventBundle myEventBundle) {
            BaseEcoTeleconsultationActivity mainActivity = mOuterRef.get();
            AlertDialog.Builder miaAlert = new AlertDialog.Builder(mainActivity);
            miaAlert.setTitle(myEventBundle.getEventType() + ":" + myEventBundle.getEvent());
            miaAlert.setMessage(myEventBundle.getInfo());
            AlertDialog alert = miaAlert.create();
            alert.show();
        }
    }

    protected static class StreamHandler extends Handler {
        @Override
        public void handleMessage(Message streamingMessage) {
            StreamingEventBundle event = (StreamingEventBundle) streamingMessage.obj;
            String infoMsg = "Event Type: " + event.getEventType() + ", " + event.getEvent() + ":" + event.getInfo();
            Log.d(TAG, "Stream Message Arrived: Current Event:" + infoMsg);

        }
    }
}
