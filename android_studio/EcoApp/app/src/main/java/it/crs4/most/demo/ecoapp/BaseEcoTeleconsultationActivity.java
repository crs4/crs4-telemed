package it.crs4.most.demo.ecoapp;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.JSONObject;
import java.util.HashMap;
import it.crs4.most.demo.ecoapp.models.EcoUser;
import it.crs4.most.demo.ecoapp.models.Teleconsultation;
import it.crs4.most.demo.ecoapp.ui.TcStateTextView;
import it.crs4.most.streaming.IStream;
import it.crs4.most.streaming.StreamingEventBundle;
import it.crs4.most.streaming.StreamingLib;
import it.crs4.most.streaming.StreamingLibBackend;
import it.crs4.most.streaming.enums.StreamState;
import it.crs4.most.visualization.IStreamFragmentCommandListener;
import it.crs4.most.visualization.StreamViewerFragment;
import it.crs4.most.voip.Utils;
import it.crs4.most.voip.VoipEventBundle;
import it.crs4.most.voip.VoipLib;
import it.crs4.most.voip.VoipLibBackend;
import it.crs4.most.voip.enums.CallState;
import it.crs4.most.voip.enums.VoipEvent;
import it.crs4.most.voip.enums.VoipEventType;


@SuppressLint("InlinedApi")
public abstract class BaseEcoTeleconsultationActivity extends AppCompatActivity{

    private static final String TAG = "EcoTeleconsultActivity";

    private ProgressDialog progressWaitingSpec;

    private TeleconsultationState tcState = TeleconsultationState.IDLE;

    private String sipServerIp;
    private String sipServerPort;

    private VoipLib myVoip;
    private CallHandler voipHandler;
    private PopupWindow popupWindow;

    protected HashMap<String, String> voipParams;

    private boolean localHold = false;
    private boolean remoteHold = false;

    private boolean accountRegistered = false;
    private boolean exitFromAppRequest = false;
    protected Handler handler;
    protected Teleconsultation teleconsultation;
    protected RemoteConfigReader rcr;

    protected void init() {
        String configServerIP = QuerySettings.getConfigServerAddress(this);
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(this));
        this.rcr = new RemoteConfigReader(this, configServerIP, configServerPort);
//        this.setTeleconsultationState(TeleconsultationState.IDLE);
//        this.setupTeleconsultationInfo();
//        this.setupVoipLib();

    }

    private void setupTeleconsultationInfo() {
        Intent i = getIntent();
//        this.teleconsultation = (Teleconsultation) i.getExtras().getSerializable("Teleconsultation");

        TextView txtEcoUser = (TextView) findViewById(R.id.txtEcoUser);
        txtEcoUser.setText(String.format("%s %s", this.teleconsultation.getApplicant().getFirstName(),
                this.teleconsultation.getApplicant().getLastName()));
    }

    protected void setTeleconsultationState(TeleconsultationState tcState) {
        this.tcState = tcState;
        notifyTeleconsultationStateChanged();
    }

    protected abstract void notifyTeleconsultationStateChanged();

    protected void exitFromApp() {

        Log.d(TAG, "Called exitFromApp()");

        this.exitFromAppRequest = true;

        if (this.myVoip != null) {
            this.myVoip.destroyLib();
        }
        else {
            Log.d(TAG, "Voip Library deinitialized. Exiting the app");
            this.finish();
        }
    }


	/*
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.teleconsultation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_call) {
			showCallPopupWindow();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	*/


    protected void closeSession() {
        EcoUser ecoUser = this.teleconsultation.getApplicant();
        this.rcr.closeSession(this.teleconsultation.getLastSession().getId(), ecoUser.getAccessToken(), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject sessionData) {
                Log.d(TAG, "Session closed: " + sessionData);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err) {
                Log.e(TAG, "Error closing the session: " + err);

            }
        });
    }

    private void showCallPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.popup_call_selection,
                null);
        this.popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }


    private void waitForSpecialist() {
        //Toast.makeText(EcoConfigActivity.this, "Connecting to:" + deviceName + "(" + macAddress +")" , Toast.LENGTH_LONG).show();
        progressWaitingSpec = new ProgressDialog(this);
        progressWaitingSpec.setTitle("Preparing Teleconsultation Session");
        progressWaitingSpec.setMessage("Waiting for specialist...");
        progressWaitingSpec.setCancelable(false);
        progressWaitingSpec.setCanceledOnTouchOutside(false);
        progressWaitingSpec.show();

    }

// VOIP METHODS AND LOGIC

    protected class CallHandler extends Handler {

        private BaseEcoTeleconsultationActivity app;
        private VoipLib myVoip;
        public boolean reinitRequest = false;
        private boolean incoming_call_request;

        public CallHandler(BaseEcoTeleconsultationActivity teleconsultationActivity,
                           VoipLib myVoip) {
            this.app = teleconsultationActivity;
            this.myVoip = myVoip;
        }

        protected VoipEventBundle getEventBundle(Message voipMessage) {
            //int msg_type = voipMessage.what;
            VoipEventBundle myState = (VoipEventBundle) voipMessage.obj;
            String infoMsg = "Event:" + myState.getEvent() + ": Type:" + myState.getEventType() + " : " + myState.getInfo();
            Log.d(TAG, "Called handleMessage with event info:" + infoMsg);
            return myState;
        }

        @Override
        public void handleMessage(Message voipMessage) {
            VoipEventBundle myEventBundle = getEventBundle(voipMessage);
            Log.d(TAG, "HANDLE EVENT TYPE:" + myEventBundle.getEventType() + " EVENT:" + myEventBundle.getEvent());


            // Register the account after the Lib Initialization
            if (myEventBundle.getEvent() == VoipEvent.LIB_INITIALIZED) {
                myVoip.registerAccount();
            }
            else if (myEventBundle.getEvent() == VoipEvent.ACCOUNT_REGISTERED) {
                if (!accountRegistered) {
                    this.app.subscribeBuddies();
                }
                else accountRegistered = true;
            }
            else if (myEventBundle.getEvent() == VoipEvent.ACCOUNT_UNREGISTERED) {
                setTeleconsultationState(TeleconsultationState.IDLE);
            }
            else if (myEventBundle.getEventType() == VoipEventType.BUDDY_EVENT) {

                Log.d(TAG, "In handle Message for BUDDY EVENT");
                //IBuddy myBuddy = (IBuddy) myEventBundle.getData();

                // There is only one subscribed buddy in this app, so we don't need to get IBuddy informations
                if (myEventBundle.getEvent() == VoipEvent.BUDDY_CONNECTED) {
                    // the remote buddy is no longer on Hold State
                    remoteHold = false;

                    if (tcState == TeleconsultationState.REMOTE_HOLDING || tcState == TeleconsultationState.HOLDING) {
                        if (localHold) {
                            setTeleconsultationState(TeleconsultationState.HOLDING);
                        }
                        else
                            setTeleconsultationState(TeleconsultationState.CALLING);
                    }
                    else if (tcState == TeleconsultationState.IDLE) {
                        setTeleconsultationState(TeleconsultationState.READY);
                    }

                }
                else if (myEventBundle.getEvent() == VoipEvent.BUDDY_HOLDING) {
                    if (myVoip.getCall().getState() == CallState.ACTIVE || myVoip.getCall().getState() == CallState.HOLDING)
                        setTeleconsultationState(TeleconsultationState.REMOTE_HOLDING);
                }
                else if (myEventBundle.getEvent() == VoipEvent.BUDDY_DISCONNECTED) {
                    setTeleconsultationState(TeleconsultationState.IDLE);
                }

            }
            else if (myEventBundle.getEvent() == VoipEvent.CALL_INCOMING)
                answerCall(); //handleIncomingCallRequest();
            /*
            else if (myEventBundle.getEvent()==VoipEvent.CALL_READY)
            {
                    if (incoming_call_request)

                    {
                            answerCall();
                    }
            }
            */
            else if (myEventBundle.getEvent() == VoipEvent.CALL_ACTIVE) {
                if (remoteHold) {
                    this.app.setTeleconsultationState(TeleconsultationState.REMOTE_HOLDING);
                }
                else {
                    this.app.setTeleconsultationState(TeleconsultationState.CALLING);
                }

            }
            else if (myEventBundle.getEvent() == VoipEvent.CALL_HOLDING) {
                this.app.setTeleconsultationState(TeleconsultationState.HOLDING);
            }
            else if (myEventBundle.getEvent() == VoipEvent.CALL_HANGUP || myEventBundle.getEvent() == VoipEvent.CALL_REMOTE_HANGUP) {

                if (this.app.tcState != TeleconsultationState.IDLE)
                    this.app.setTeleconsultationState(TeleconsultationState.READY);
            }
            // Deinitialize the Voip Lib and release all allocated resources
            else if (myEventBundle.getEvent() == VoipEvent.LIB_DEINITIALIZED || myEventBundle.getEvent() == VoipEvent.LIB_DEINITIALIZATION_FAILED) {
                Log.d(TAG, "Setting to null MyVoipLib");
                this.app.myVoip = null;
                this.app.setTeleconsultationState(TeleconsultationState.IDLE);

                if (this.reinitRequest) {
                    this.reinitRequest = false;
                    this.app.setupVoipLib();
                }
                else if (exitFromAppRequest) {
                    exitFromApp();
                }
            }
            else if (myEventBundle.getEvent() == VoipEvent.LIB_INITIALIZATION_FAILED || myEventBundle.getEvent() == VoipEvent.ACCOUNT_REGISTRATION_FAILED ||
                    myEventBundle.getEvent() == VoipEvent.LIB_CONNECTION_FAILED || myEventBundle.getEvent() == VoipEvent.BUDDY_SUBSCRIPTION_FAILED)
                showErrorEventAlert(myEventBundle);


        } // end of handleMessage()

        private void showErrorEventAlert(VoipEventBundle myEventBundle) {

            AlertDialog.Builder miaAlert = new AlertDialog.Builder(this.app);
            miaAlert.setTitle(myEventBundle.getEventType() + ":" + myEventBundle.getEvent());
            miaAlert.setMessage(myEventBundle.getInfo());
            AlertDialog alert = miaAlert.create();
            alert.show();
        }

        private void handleIncomingCallRequest() {

            incoming_call_request = true;

        }
    }

    protected void answerCall() {
        myVoip.answerCall();

		/*
		Log.d(TAG, "Answering the call after 2 seconds");

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		  @Override
		  public void run() {
			  Log.d(TAG, "Answering the call now...");
			  myVoip.answerCall();
		  }
		}, 2000);
		*/
    }

    protected void toggleHoldCall(boolean holding) {
        if (holding) {
            myVoip.holdCall();
        }
        else {
            myVoip.unholdCall();
        }
    }

    protected void hangupCall() {
        myVoip.hangupCall();
    }

    protected void setupVoipLib() {
        // Voip Lib Initialization Params

        this.voipParams = getVoipSetupParams();

        Log.d(TAG, "Initializing the lib...");
        if (myVoip == null) {
            Log.d(TAG, "Voip null... Initialization.....");
            myVoip = new VoipLibBackend();
            this.voipHandler = new CallHandler(this, myVoip);

            // Initialize the library providing custom initialization params and an handler where
            // to receive event notifications. Following Voip methods are called from the handleMassage() callback method
            //boolean result = myVoip.initLib(params, new RegistrationHandler(this, myVoip));
            myVoip.initLib(this.getApplicationContext(), this.voipParams, this.voipHandler);
        }
        else {
            Log.d(TAG, "Voip is not null... Destroying the lib before reinitializing.....");
            // Reinitialization will be done after deinitialization event callback
            this.voipHandler.reinitRequest = true;
            myVoip.destroyLib();
        }
    }


    protected HashMap<String, String> getVoipSetupParams() {
        HashMap<String, String> params = teleconsultation.getLastSession().getVoipParams();

        this.sipServerIp = params.get("sipServerIp");
        this.sipServerPort = params.get("sipServerPort");

        /**
         this.sipServerIp = "192.168.1.100";
         this.sipServerPort="5060";
         HashMap<String,String> params = new HashMap<String,String>();
         params.put("sipServerIp",sipServerIp);
         params.put("sipServerPort",sipServerPort); // default 5060
         params.put("turnServerIp",  sipServerIp);
         params.put("sipServerTransport","tcp");

         // used by the app for calling the specified extension, not used directly by the VoipLib
         params.put("specExtension","MOST0001");

         */

		/* ecografista 	*/
        //accountName = params.get("sipUserName");

        String onHoldSoundPath = Utils.getResourcePathByAssetCopy(this.getApplicationContext(), "", "test_hold.wav");
        String onIncomingCallRingTonePath = Utils.getResourcePathByAssetCopy(this.getApplicationContext(), "", "ring_in_call.wav");
        String onOutcomingCallRingTonePath = Utils.getResourcePathByAssetCopy(this.getApplicationContext(), "", "ring_out_call.wav");


        params.put("onHoldSound", onHoldSoundPath);
        params.put("onIncomingCallSound", onIncomingCallRingTonePath); // onIncomingCallRingTonePath
        params.put("onOutcomingCallSound", onOutcomingCallRingTonePath); // onOutcomingCallRingTonePath

        Log.d(TAG, "OnHoldSoundPath:" + onHoldSoundPath);

        return params;
    }

    protected void subscribeBuddies() {
        String buddyExtension = this.voipParams.get("specExtension");
        Log.d(TAG, "adding buddies...");
        myVoip.getAccount().addBuddy(getBuddyUri(buddyExtension));
    }

    protected String getBuddyUri(String extension) {
        return "sip:" + extension + "@" + this.sipServerIp + ":" + this.sipServerPort;
    }

}
