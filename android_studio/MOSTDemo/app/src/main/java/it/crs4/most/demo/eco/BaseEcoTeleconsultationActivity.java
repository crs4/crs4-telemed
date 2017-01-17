package it.crs4.most.demo.eco;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.media.AudioManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import it.crs4.most.demo.BaseTeleconsultationActivity;
import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.TeleconsultationState;
import it.crs4.most.demo.models.ARConfiguration;
import it.crs4.most.demo.models.ARMarker;
import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.streaming.StreamingEventBundle;
import it.crs4.most.visualization.augmentedreality.MarkerFactory;
import it.crs4.most.visualization.augmentedreality.mesh.Mesh;
import it.crs4.most.visualization.augmentedreality.mesh.MeshManager;
import it.crs4.most.voip.VoipEventBundle;
import it.crs4.most.voip.enums.CallState;
import it.crs4.most.voip.enums.VoipEvent;
import it.crs4.most.voip.enums.VoipEventType;


@SuppressLint("InlinedApi")
public abstract class BaseEcoTeleconsultationActivity extends BaseTeleconsultationActivity {

    private static final String TAG = "EcoTeleconsultActivity";
    protected StreamHandler mStreamHandler;
    protected boolean localHold = false;
    protected boolean remoteHold = false;
    protected boolean accountRegistered = false;

    protected void stopStream() {}

    protected Handler getVoipHandler(){
        return new CallHandler(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
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



    protected void subscribeBuddies() {
        String buddyExtension = mVoipParams.get("specExtension");
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
                act.stopStream();
                act.setResult(RESULT_OK);
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

    protected void createARMeshes(Teleconsultation teleconsultation, MeshManager meshManager){

        float [] redColor = new float []{
                0, 0, 0, 1f,
                1, 0, 0, 1f,
                1, 0, 0, 1f,
                1, 0, 0, 1f,
                1, 0, 0, 1f
        };

        Map<String, Mesh> meshes = new HashMap<>();
        ARConfiguration arConf= teleconsultation.getLastSession().getRoom().getARConfiguration();
        if (arConf != null){
            for (ARMarker markerModel: arConf.getMarkers()){
                MarkerFactory.Marker marker = MarkerFactory.getMarker(markerModel.getConf());
                float [] trans = new float[16];
                Matrix.setIdentityM(trans, 0);
                trans[12] = markerModel.getTransX();
                trans[13] = markerModel.getTransY();
                marker.setModelMatrix(trans);

                it.crs4.most.demo.models.Mesh meshModel = markerModel.getMesh();
                Mesh mesh;
                if (meshes.containsKey(meshModel.getName())) {
                    mesh = meshes.get(meshModel.getName());
                }
                else {
                    try {

                        Class clsMesh = Class.forName(meshModel.getCls());
                        Class[] cArg = new Class[] {
                                float.class, float.class, float.class, String.class
                        };
                        mesh = (Mesh) clsMesh.getDeclaredConstructor(cArg).newInstance(
                                meshModel.getSizeX(),
                                meshModel.getSizeY(),
                                meshModel.getSizeZ(),
                                meshModel.getName()
                        );
                        meshes.put(meshModel.getName(), mesh);
                        mesh.setColors(redColor);
                        meshManager.addMesh(mesh);

                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                mesh.addMarker(marker);
            }
        }


    }
}
