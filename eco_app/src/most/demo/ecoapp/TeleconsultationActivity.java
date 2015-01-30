package most.demo.ecoapp;

import java.util.HashMap;

import org.crs4.most.streaming.IStream;
import org.crs4.most.streaming.StreamingEventBundle;
import org.crs4.most.streaming.StreamingLib;
import org.crs4.most.streaming.StreamingLibBackend;
import org.crs4.most.visualization.IStreamFragmentCommandListener;
import org.crs4.most.visualization.StreamViewerFragment;

import most.demo.ecoapp.models.EcoUser;
import most.demo.ecoapp.ui.TcStateTextView;
import most.voip.api.Utils;
import most.voip.api.VoipEventBundle;
import most.voip.api.VoipLib;
import most.voip.api.VoipLibBackend;
import most.voip.api.enums.VoipEvent;
import most.voip.api.enums.VoipEventType;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


import android.os.Handler;
import android.os.Message;

@SuppressLint("InlinedApi")
public class TeleconsultationActivity extends ActionBarActivity implements Handler.Callback, IStreamFragmentCommandListener {

	private static final String TAG = "TeleconsultationActivity";
	private EcoUser ecoUser = null;
	private StreamViewerFragment stream1Fragment = null;
	private IStream stream1 = null;
	private Handler handler;
	
	
	private ProgressDialog progressWaitingSpec;
	
	private TeleconsultationState tcState = TeleconsultationState.IDLE;
	private TcStateTextView txtTcState = null;
	private ImageButton butCall;
	private String sipServerIp;
	private String sipServerPort;
	private String accountName;
	private VoipLib myVoip;
	private CallHandler voipHandler;
	private PopupWindow popupWindow;
	private Button popupCancelButton;
	private Button popupHangupButton;
	private Button popupHoldButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Intent i = getIntent();
		//this.ecoUser =  (EcoUser) i.getExtras().getSerializable("EcoUser");
		
		
		    setContentView(R.layout.activity_teleconsultation);
		    txtTcState = (TcStateTextView) findViewById(R.id.txtTcState);
	       
		   
			this.handler = new Handler(this);
			
			this.setupActionBar();
			this.setupCallPopupWindow();
			this.setTeleconsultationState(TeleconsultationState.IDLE);
			
			this.setupStreamLib();
			this.setupVoipLib();
			
			
			 //this.waitForSpecialist();
	}
    
	
	private void setTeleconsultationState(TeleconsultationState tcState)
	{
		this.tcState = tcState; 
		notifyTeleconsultationStateChanched();
		
	}
	
	private void notifyTeleconsultationStateChanched() {
		
		txtTcState.setTeleconsultationState(this.tcState);
		if (this.tcState==TeleconsultationState.IDLE || tcState==TeleconsultationState.READY)
		{
			butCall.setEnabled(false);
			popupCancelButton.setEnabled(true);
			popupHoldButton.setEnabled(false);
			popupHangupButton.setEnabled(false);
		}
		else if (this.tcState==TeleconsultationState.CALLING)
		{
			butCall.setEnabled(true);
			popupCancelButton.setEnabled(true);
			popupHoldButton.setEnabled(true);
			popupHangupButton.setEnabled(true);
		}
		
		else if (this.tcState==TeleconsultationState.HOLDING)
		{
			butCall.setEnabled(true);
			popupCancelButton.setEnabled(true);
			popupHoldButton.setEnabled(true);
			popupHoldButton.setText("Unhold"); // to be fixed
			popupHangupButton.setEnabled(true);
		}
		 
	}
	
	private void setupStreamLib()
	{
		String streamName = "Teleconsultation Stream"; 
		String streamUri =  "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov";
		
		this.prepareStream(streamName,streamUri);
		
		this.stream1Fragment = StreamViewerFragment.newInstance(streamName);
		
		// add the first fragment to the first container
    	FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction.add(R.id.container_stream,
				stream1Fragment);
		fragmentTransaction.commit();
	}
	
	private void prepareStream(String name, String uri)
	{
		 StreamingLib streamingLib = new StreamingLibBackend();
		 HashMap<String,String> stream1_params = new HashMap<String,String>();
	    	stream1_params.put("name", name);
	    	stream1_params.put("uri", uri);
	    	
		 try {
			
			// First of all, initialize the library 
			streamingLib.initLib(this.getApplicationContext());
				
			this.stream1 = streamingLib.createStream(stream1_params, this.handler);
		} catch (Exception e) {
			Log.e(TAG,"ERROR INITIALIZING STREAM:" + e);
			e.printStackTrace();
		}
		    
	}
	
	
	
	
	private void setupActionBar()
	{
		ActionBar actionBar = getSupportActionBar();
	    // add the custom view to the action bar
	    actionBar.setCustomView(R.layout.actionbar_view);
	    butCall = (ImageButton) actionBar.getCustomView().findViewById(R.id.butCallActionBar);
	    butCall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			 showCallPopupWindow();
				
			}
		});
	    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
	        | ActionBar.DISPLAY_SHOW_HOME);
		
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

	private void showCallPopupWindow()
	{
		 LayoutInflater inflater = (LayoutInflater)  TeleconsultationActivity.this
				 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				 
		 View popupView = inflater.inflate(R.layout.popup_call_selection,
				 null);
		this.popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
	}
	
	private void setupCallPopupWindow()
	{
		
		 LayoutInflater inflater = (LayoutInflater)  TeleconsultationActivity.this
				 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				 
		 View popupView = inflater.inflate(R.layout.popup_call_selection,
				 null);	 
       
		if (popupWindow==null)
		{
	        popupWindow = new PopupWindow(popupView,
	                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
	        
	    
	        popupCancelButton = (Button) popupView.findViewById(R.id.butCallCancel);
	        
	        popupCancelButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					popupWindow.dismiss();
					
				}
			});
	        
	        popupHoldButton = (Button) popupView.findViewById(R.id.butCallHold);
	        
	        popupHoldButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// to be implemented
					popupWindow.dismiss();
				}
			});
	        
	        popupHangupButton = (Button) popupView.findViewById(R.id.butCallHangup);
	        
	        popupHangupButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					hangupCall();
					popupWindow.dismiss();
				}
			});
	        
	        popupWindow.setTouchable(true);
	        popupWindow.setFocusable(true);
		}
		
        
       
	}
	
	@Override
	public void onPlay(String streamId) {
		this.stream1.play();
		
	}

	@Override
	public void onPause(String streamId) {
	   this.stream1.pause();
	}

	@Override
	public void onSurfaceViewCreated(String streamId, SurfaceView surfaceView) {
		Log.d(TAG, "Surface View created: preparing surface for stream" + streamId);
		this.stream1.prepare(surfaceView);
		this.stream1Fragment.setPlayerButtonsVisible(true);
		
	}

	@Override
	public void onSurfaceViewDestroyed(String streamId) {
		this.stream1.destroy();
	}

	@Override
	public boolean handleMessage(Message streamingMessage) {
		StreamingEventBundle myEvent = (StreamingEventBundle) streamingMessage.obj;
		
		String infoMsg ="Event Type:" +  myEvent.getEventType() + " ->" +  myEvent.getEvent() + ":" + myEvent.getInfo();
		Log.d(TAG, "handleMessage: Current Event:" + infoMsg);
		Log.d(TAG, "Stream State:" + this.stream1.getState());
		return false;
	}
	
	
	
	private void waitForSpecialist()
	{
		//Toast.makeText(MainActivity.this, "Connecting to:" + deviceName + "(" + macAddress +")" , Toast.LENGTH_LONG).show();
		progressWaitingSpec = new ProgressDialog(TeleconsultationActivity.this);
		progressWaitingSpec.setTitle("Preparing Teleconsultation Session");
		progressWaitingSpec.setMessage("Waiting for specialist...");
		progressWaitingSpec.setCancelable(false);
		progressWaitingSpec.setCanceledOnTouchOutside(false);
		progressWaitingSpec.show();
		
	}
	
// VOIP METHODS AND LOGIC
	
	private class CallHandler extends Handler {

		private TeleconsultationActivity app;
		private VoipLib myVoip;
		public boolean reinitRequest = false;
		private boolean incoming_call_request;

		public CallHandler(TeleconsultationActivity teleconsultationActivity,
				VoipLib myVoip) {
			this.app = teleconsultationActivity;
			this.myVoip = myVoip;
		}
		
		protected VoipEventBundle getEventBundle(Message voipMessage)
 		{
 			//int msg_type = voipMessage.what;
			VoipEventBundle myState = (VoipEventBundle) voipMessage.obj;
			String infoMsg = "Event:" + myState.getEvent() + ": Type:"  + myState.getEventType() + " : " + myState.getInfo();
			Log.d(TAG, "Called handleMessage with event info:" + infoMsg);
			return myState;
 		}
		
		@Override
		public void handleMessage(Message voipMessage) {
		
			
			VoipEventBundle myEventBundle = getEventBundle(voipMessage);
			Log.d(TAG, "HANDLE EVENT TYPE:" + myEventBundle.getEventType() + " EVENT:" + myEventBundle.getEvent());
			
			
	
			//if (myEventBundle.getEventType()==VoipEventType.BUDDY_EVENT){}
			
			// Register the account after the Lib Initialization
			if (myEventBundle.getEvent()==VoipEvent.LIB_INITIALIZED)   {myVoip.registerAccount();
																			}	
			else if (myEventBundle.getEvent()==VoipEvent.ACCOUNT_REGISTERED)    {
																     this.app.setTeleconsultationState(TeleconsultationState.READY);
			                                                      }	
			
			
			else if (myEventBundle.getEvent()==VoipEvent.CALL_INCOMING)  handleIncomingCallRequest();
			
			else if (myEventBundle.getEvent()==VoipEvent.CALL_READY)
			{
				if (incoming_call_request)
				{
					answerCall();
				}
			}
			
			else if  (myEventBundle.getEvent()==VoipEvent.CALL_ACTIVE)    {
				this.app.setTeleconsultationState(TeleconsultationState.CALLING);
			}
			
		 
			
		
			else if (myEventBundle.getEvent()==VoipEvent.CALL_HANGUP)    {
				this.app.setTeleconsultationState(TeleconsultationState.READY);
			}
			// Deinitialize the Voip Lib and release all allocated resources
			else if (myEventBundle.getEvent()==VoipEvent.LIB_DEINITIALIZED || myEventBundle.getEvent()==VoipEvent.LIB_DEINITIALIZATION_FAILED) 
			{
				Log.d(TAG,"Setting to null MyVoipLib");
				this.app.myVoip = null;
				this.app.setTeleconsultationState(TeleconsultationState.IDLE);
				
				if (this.reinitRequest)
				{	this.reinitRequest = false;
					this.app.setupVoipLib();
				}
			}
			else showUnhandledEventAlert(myEventBundle);
			     
		} // end of handleMessage()

		
		private void showUnhandledEventAlert(VoipEventBundle myEventBundle) {
		
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
	
	private void answerCall() {
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
	
	private void toggleHoldCall(boolean holding)
	{
		if (holding)
			myVoip.holdCall();
		else
			myVoip.unholdCall();
	}
	
	private void hangupCall()
	{
		myVoip.hangupCall();
	}
	
	private void setupVoipLib()
	{
		// Voip Lib Initialization Params

				HashMap<String,String> params = getVoipSetupParams();
				
				Log.d(TAG, "Initializing the lib...");
				if (myVoip==null)
				{
					Log.d(TAG,"Voip null... Initialization.....");
					myVoip = new  VoipLibBackend();
					this.voipHandler = new CallHandler(this, myVoip);
					
					// Initialize the library providing custom initialization params and an handler where
					// to receive event notifications. Following Voip methods are called from the handleMassage() callback method
					//boolean result = myVoip.initLib(params, new RegistrationHandler(this, myVoip));
					myVoip.initLib(this.getApplicationContext(), params, this.voipHandler);
				}
				else 
					{
					Log.d(TAG,"Voip is not null... Destroying the lib before reinitializing.....");
					// Reinitialization will be done after deinitialization event callback
					this.voipHandler.reinitRequest  = true;
					myVoip.destroyLib();
					}
	}
	
	


	private HashMap<String,String> getVoipSetupParams()
	    { 
		    this.sipServerIp = "192.168.1.1";
		    this.sipServerPort="5060";
	    	HashMap<String,String> params = new HashMap<String,String>();
			params.put("sipServerIp",sipServerIp); 
			params.put("sipServerPort",sipServerPort); // default 5060
			params.put("turnServerIp",  sipServerIp);
			params.put("sipServerTransport","tcp"); 
			
			/* ecografista 	*/
			accountName = "ecografista";
			params.put("userPwd","sha1$fdcad$659da6841c6d8538b7a10ca12aae");

			
			/* specialista	
			accountName = "specialista";
			params.put("userPwd","sha1$40fcf$4718177db1b6966f64d2d436f212"); // 
		*/
			
			params.put("userName",accountName); // specialista
			params.put("turnServerUser",accountName);  // specialista
			params.put("turnServerPwd",accountName);  // specialista
		 
			
			String onHoldSoundPath = Utils.getResourcePathByAssetCopy(this.getApplicationContext(), "", "test_hold.wav");
			String onIncomingCallRingTonePath = Utils.getResourcePathByAssetCopy(this.getApplicationContext(), "", "ring_in_call.wav");
			String onOutcomingCallRingTonePath = Utils.getResourcePathByAssetCopy(this.getApplicationContext(), "", "ring_out_call.wav");
			
			
			params.put("onHoldSound", onHoldSoundPath);
			params.put("onIncomingCallSound",onIncomingCallRingTonePath ); // onIncomingCallRingTonePath
			params.put("onOutcomingCallSound",onOutcomingCallRingTonePath); // onOutcomingCallRingTonePath
			
			Log.d(TAG,"OnHoldSoundPath:" + onHoldSoundPath);
			 
			return params;
	    	
	    }
	
}
