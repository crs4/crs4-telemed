package most.demo.specapp;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import most.demo.specapp.TeleconsultationState;
import most.demo.specapp.models.Device;
import most.demo.specapp.models.Teleconsultation;
import most.demo.specapp.ui.TcStateTextView;
import most.voip.api.Utils;
import most.voip.api.VoipEventBundle;
import most.voip.api.VoipLib;
import most.voip.api.VoipLibBackend;
import most.voip.api.enums.CallState;
import most.voip.api.enums.VoipEvent;
import most.voip.api.enums.VoipEventType;

import org.crs4.most.streaming.IStream;
import org.crs4.most.streaming.StreamingEventBundle;
import org.crs4.most.streaming.StreamingLib;
import org.crs4.most.streaming.StreamingLibBackend;
import org.crs4.most.streaming.enums.PTZ_Direction;
import org.crs4.most.streaming.enums.PTZ_Zoom;
import org.crs4.most.streaming.enums.StreamProperty;
import org.crs4.most.streaming.enums.StreamState;
import org.crs4.most.streaming.enums.StreamingEvent;
import org.crs4.most.streaming.enums.StreamingEventType;
import org.crs4.most.streaming.ptz.PTZ_Manager;
import org.crs4.most.streaming.utils.ImageDownloader;
import org.crs4.most.streaming.utils.ImageDownloader.IBitmapReceiver;
import org.crs4.most.visualization.IPtzCommandReceiver;
import org.crs4.most.visualization.IStreamFragmentCommandListener;
import org.crs4.most.visualization.PTZ_ControllerFragment;
import org.crs4.most.visualization.PTZ_ControllerPopupWindowFactory;
import org.crs4.most.visualization.StreamViewerFragment;
import org.crs4.most.visualization.StreamInspectorFragment.IStreamProvider;
 


public class SpecTeleconsultationActivity extends ActionBarActivity implements Handler.Callback, 
															   IPtzCommandReceiver, 
															   IStreamFragmentCommandListener,
															   IStreamProvider	
																		{

	private static String TAG = "SpecMainActivity";
	
	//ID for the menu exit option
    private final int ID_MENU_EXIT = 1;
	private boolean exitFromAppRequest = false;
	
	private Handler handler;
	private IStream stream1 = null;
	private IStream streamEcho = null;
	
	private StreamViewerFragment stream1Fragment = null;
	private StreamViewerFragment streamEchoFragment = null;
	
	private TeleconsultationState tcState = TeleconsultationState.IDLE;
	private TcStateTextView txtTcState = null;
	
	private PTZ_ControllerFragment ptzControllerFragment = null;
	private PTZ_Manager ptzManager =  null;

	private String streamingUri;
	private String streamingEchoUri;
	
	//private Properties uriProps = null;
	
	// VOIP
	
	private String sipServerIp;
	private String sipServerPort;
	
	private VoipLib myVoip;
	private CallHandler voipHandler;

    private Teleconsultation teleconsultation = null;

	private PTZ_ControllerPopupWindowFactory ptzPopupWindowController;

	private String MAIN_STREAM="MAIN_STREAM";
	private String ECHO_STREAM="ECHO_STREAM";

	private String ecoExtension;

	private Button butMakeCall;
	private ToggleButton butHoldCall;

	private HashMap<String, String> voipParams;

	private boolean streaming_ready=false;
	
	private boolean localHold = false;
	private boolean remoteHold = false;
	private boolean accountRegistered = false;

	private boolean streamMainDestroyed=false;
	private boolean streamEchoDestroyed=false;
	private boolean voipDestroyed=false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    
        this.handler = new Handler(this);
        setContentView(R.layout.activity_main);
        this.setupActionBar();
        this.setupTeleconsultationInfo();
        this.setupVoipGUI();
        
        
        this.setTeleconsultationState(TeleconsultationState.IDLE);
        
        
		this.setupStreamLib();
		this.setupPtzPopupWindow();
        this.setupVoipLib();
        
    }
    
    private void setupTeleconsultationInfo()
    {
    	  Intent i = getIntent();
          this.teleconsultation =  (Teleconsultation) i.getExtras().getSerializable("Teleconsultation");
          TextView txtTeleconsultation = (TextView) findViewById(R.id.txtTeleconsultation);
          txtTeleconsultation.setText(this.teleconsultation.getInfo());
         
    }
    
    private void setupStreamLib()
    {
    	try {
            
        	//this.uriProps = getUriProperties("uri.properties.default");
        	
    		Device camera = teleconsultation.getLastSession().getCamera();
        	
        	// Instance and initialize the Streaming Library
        	StreamingLib streamingLib = new StreamingLibBackend();
    	  	// First of all, initialize the library 
			streamingLib.initLib(this.getApplicationContext());
			
			this.ptzControllerFragment = PTZ_ControllerFragment.newInstance(true,true,true);
            this.ptzManager = new PTZ_Manager(this, 
            		camera.getPtzUri(), //     uriProps.getProperty("uri_ptz") , 
            		camera.getUser(), //  uriProps.getProperty("username_ptz"), 
            		camera.getPwd() //  uriProps.getProperty("password_ptz")
            		);
            
            // Instance the first stream
	    	HashMap<String,String> stream1_params = new HashMap<String,String>();
	    	stream1_params.put("name", MAIN_STREAM);
	    	
	    	
       	    this.streamingUri =  camera.getStreamUri(); //    uriProps.getProperty("uri_stream");  
	    	stream1_params.put("uri", this.streamingUri);
	    	    	 
	    	this.stream1 = streamingLib.createStream(stream1_params, this.handler);
	    	Log.d(TAG,"STREAM 1 INSTANCE");
	    	
	    	// Instance the first StreamViewer fragment where to render the first stream by passing the stream name as its ID.
	    	this.stream1Fragment = StreamViewerFragment.newInstance(stream1.getName());
	    	
	    	
	    	
	    	// Instance the Echo Stream
	    	
	    	Device encoder = teleconsultation.getLastSession().getEncoder();
	    	
	    	HashMap<String,String> stream_echo_params = new HashMap<String,String>();
	    	stream_echo_params.put("name", ECHO_STREAM);
	    	
	    	
       	    this.streamingEchoUri = encoder.getStreamUri(); //     uriProps.getProperty("uri_echo");  
       	    stream_echo_params.put("uri", this.streamingEchoUri);
	    	    	 
	    	this.streamEcho = streamingLib.createStream(stream_echo_params, this.handler);
	    	Log.d(TAG,"STREAM ECHO INSTANCE");
	    	
	    	// Instance the echo StreamViewer fragment where to render the echo stream by passing the stream name as its ID.
	    	this.streamEchoFragment = StreamViewerFragment.newInstance(streamEcho.getName());
	    	
	    	 
		} catch (Exception e) {
		    streaming_ready = false;
			e.printStackTrace();
		}
    	
    	// add the first fragment to the first container
    	FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction.add(R.id.container_stream_1, stream1Fragment);
		fragmentTransaction.add(R.id.container_stream_echo, streamEchoFragment);
		fragmentTransaction.commit();
		
		streaming_ready = true;
    }
    
    private void setupVoipLib()
	{
		// Voip Lib Initialization Params

				this.voipParams = getVoipSetupParams();
				
				this.ecoExtension = voipParams.get("ecoExtension");
				
				Log.d(TAG, "Initializing the lib...");
				if (myVoip==null)
				{
					Log.d(TAG,"Voip null... Initialization.....");
					myVoip = new  VoipLibBackend();
					this.voipHandler = new CallHandler(this, myVoip);
					
					// Initialize the library providing custom initialization params and an handler where
					// to receive event notifications. Following Voip methods are called from the handleMassage() callback method
					//boolean result = myVoip.initLib(params, new RegistrationHandler(this, myVoip));
					myVoip.initLib(this.getApplicationContext(), voipParams, this.voipHandler);
				}
				else 
					{
					Log.d(TAG,"Voip is not null... Destroying the lib before reinitializing.....");
					// Reinitialization will be done after deinitialization event callback
					this.voipHandler.reinitRequest  = true;
					myVoip.destroyLib();
					}
	}
	
    private void setupActionBar()
	{
		ActionBar actionBar = getSupportActionBar();
	    // add the custom view to the action bar
	    actionBar.setCustomView(R.layout.actionbar_view);
	    Button butPTZ = (Button) actionBar.getCustomView().findViewById(R.id.butPTZActionBar);
	    butPTZ.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			 showPTZPopupWindow();
				
			}
		});
	    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
	        | ActionBar.DISPLAY_SHOW_HOME);
		
	}
    
    
    private void setupPtzPopupWindow()
    {
    	 this.ptzPopupWindowController = new PTZ_ControllerPopupWindowFactory(this,  this, true,true,true,100,100);
    	 //PopupWindow ptzPopupWindow = this.ptzPopupWindowController.getPopupWindow();
    }
    
    
    private void setupVoipGUI()
    {
    	this.butMakeCall = (Button) findViewById(R.id.but_make_call);
    	
    	this.butMakeCall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				handleButMakeCallClicked();
			}
		});
    	
    	this.butHoldCall = (ToggleButton) findViewById(R.id.but_hold_call);
    	
    	this.butHoldCall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				handleButHoldClicked();
			}
		});
    }
    
    private void showPTZPopupWindow()
    {
    	this.ptzPopupWindowController.show();
    }

    
    private void setTeleconsultationState(TeleconsultationState tcState)
	{
		this.tcState = tcState; 
		notifyTeleconsultationStateChanged();
		
	}
    
private void notifyTeleconsultationStateChanged() {
		
	if (txtTcState==null)
		txtTcState = (TcStateTextView) findViewById(R.id.txtTcState);
       
		txtTcState.setTeleconsultationState(this.tcState);
		if (this.tcState==TeleconsultationState.IDLE)
		{  
			butMakeCall.setText("Make");
			butMakeCall.setEnabled(false);
			butHoldCall.setEnabled(false);
			localHold = false;
			remoteHold = false;
			accountRegistered = false;

		}
		else if (this.tcState==TeleconsultationState.READY)
		{  
			butMakeCall.setText("Make");
			butMakeCall.setEnabled(true);
			butHoldCall.setEnabled(false);
			localHold = false;
			remoteHold = false;
			accountRegistered = true;
		}
		
		else if (this.tcState==TeleconsultationState.CALLING)
		{
			butMakeCall.setEnabled(true);
			butMakeCall.setText("Hangup");
			butHoldCall.setEnabled(true);
			localHold = false;
			remoteHold = false;
 
		}
		
		else if (this.tcState==TeleconsultationState.HOLDING)
		{
			butMakeCall.setEnabled(true);
			butMakeCall.setText("Hangup");
			butHoldCall.setEnabled(true);
			localHold = true;
		}
		else if (this.tcState==TeleconsultationState.REMOTE_HOLDING)
		{
			butMakeCall.setEnabled(true);
			butMakeCall.setText("Hangup");
			butHoldCall.setEnabled(true);
			remoteHold = true;
		}
		 
		 
	}
    
    private Properties getUriProperties(String FileName) {
		Properties properties = new Properties();
        try {
               /**
                * getAssets() Return an AssetManager instance for your
                * application's package. AssetManager Provides access to an
                * application's raw asset files;
                */
               AssetManager assetManager = this.getAssets();
               /**
                * Open an asset using ACCESS_STREAMING mode. This
                */
               InputStream inputStream = assetManager.open(FileName);
               /**
                * Loads properties from the specified InputStream,
                */
               properties.load(inputStream);

        } catch (IOException e) {
               // TODO Auto-generated catch block
               Log.e("AssetsPropertyReader",e.toString());
        }
        return properties;

 }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	 	//get the MenuItem reference
	 MenuItem item = 
	    	menu.add(Menu.NONE,ID_MENU_EXIT,Menu.NONE,R.string.mnu_exit);
	 return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	//check selected menu item
    	if(item.getItemId() == ID_MENU_EXIT)
    	{
    		exitFromApp();
    		return true;
    	}
    	return false;
    }


	@Override
	public void onPTZstartMove(PTZ_Direction dir) {
		Log.d(TAG, "Called onPTZstartMove for direction:" + dir);
		//Toast.makeText(this, "Start Moving to ->" + dir, Toast.LENGTH_LONG).show();
		this.ptzManager.startMove(dir);
	}


	@Override
	public void onPTZstopMove(PTZ_Direction dir) {
		Log.d(TAG, "Called onPTZstoptMove for direction:" + dir);
		//Toast.makeText(this, "Stop Moving from ->" + dir, Toast.LENGTH_LONG).show();
		this.ptzManager.stopMove();
	}
	
	
	@Override
	public void onPTZstartZoom(PTZ_Zoom dir) {
		this.ptzManager.startZoom(dir);	
	}

	@Override
	public void onPTZstopZoom(PTZ_Zoom dir) {
		this.ptzManager.stopZoom();
	}

	@Override
	public void onGoHome() {
		String homePreset = "home"; // this.uriProps.getProperty("home_preset_ptz");
		this.ptzManager.goTo(homePreset);
		
	}

	
	
	@Override
	public void onSnaphot() {
		
		Log.d(TAG, "on snapshot called");
		
		IBitmapReceiver receiver = new IBitmapReceiver() {	
			@Override
			public void onBitmapSaved(ImageDownloader imageDownloader, String filename) {
				Log.d(TAG, "Saved Image:" + filename);
				Toast.makeText(SpecTeleconsultationActivity.this, "Image saved:" + filename , Toast.LENGTH_LONG).show();
				imageDownloader.logAppFileNames();
			}
			
			@Override
			public void onBitmapDownloaded(ImageDownloader imageDownloader,Bitmap image) {
				imageDownloader.saveImageToInternalStorage(image, "test_image__" + String.valueOf(System.currentTimeMillis()));
			}

			@Override
			public void onBitmapDownloadingError(
					ImageDownloader imageDownloader, Exception ex) {
					Toast.makeText(SpecTeleconsultationActivity.this, "Error downloading Image:" + ex.getMessage(), Toast.LENGTH_LONG).show();
				
			}

			@Override
			public void onBitmapSavingError(ImageDownloader imageDownloader,
					Exception ex) {
				Toast.makeText(SpecTeleconsultationActivity.this, "Error saving Image:" + ex.getMessage(), Toast.LENGTH_LONG).show();
				
			}
		};
		
	    Device camera = teleconsultation.getLastSession().getCamera();
		ImageDownloader imageDownloader = new ImageDownloader(receiver, this, 
				camera.getUser(), //   uriProps.getProperty("username_ptz"), 
				camera.getPwd()); // uriProps.getProperty("password_ptz"));
		
		imageDownloader.downloadImage(camera.getShotUri()); //    uriProps.getProperty("uri_still_image"));
	}

	@Override
	public boolean handleMessage(Message streamingMessage) {
		// The bundle containing all available informations and resources about the incoming event
				StreamingEventBundle myEvent = (StreamingEventBundle) streamingMessage.obj;
				
				String infoMsg ="Event Type:" +  myEvent.getEventType() + " ->" +  myEvent.getEvent() + ":" + myEvent.getInfo();
				Log.d(TAG, "handleMessage: Current Event:" + infoMsg);
				
				
				// for simplicity, in this example we only handle events of type STREAM_EVENT
				if (myEvent.getEventType()==StreamingEventType.STREAM_EVENT)
					if (myEvent.getEvent()==StreamingEvent.STREAM_STATE_CHANGED || myEvent.getEvent()== StreamingEvent.STREAM_ERROR)
					{
						
						// All events of type STREAM_EVENT provide a reference to the stream that triggered it.
					    // In this case we are handling two streams, so we need to check what stream triggered the event.
					    // Note that we are only interested to the new state of the stream
						IStream stream  =  (IStream) myEvent.getData();
					    String streamName = stream.getName();
						
						if (this.stream1.getState()==StreamState.DEINITIALIZED && this.exitFromAppRequest)
						{ 
							if (streamName.equalsIgnoreCase(MAIN_STREAM))
							streamMainDestroyed=true;
							else if (streamName.equalsIgnoreCase(ECHO_STREAM))
								streamEchoDestroyed = true;
							
							Log.d(TAG,"Stream " + streamName + " deinitialized..");
							exitFromApp();
						}
					}
				return false;
	}
	
	 private void exitFromApp() {
		    
		Log.d(TAG,"Called exitFromApp()");
	
		this.exitFromAppRequest = true;
		
		
			if (this.myVoip!=null &&  !this.voipDestroyed)
			{   
				this.myVoip.destroyLib();
			}
			else 
			{
				Log.d(TAG, "Voip Library deinitialized. Exiting the app");
				this.finish();
			}
		}
		    


	@Override
	public void onPlay(String streamId) {
		if (streamId.equals(MAIN_STREAM))
		this.stream1.play();
		else if (streamId.equals(ECHO_STREAM))
			this.streamEcho.play();
	}

	@Override
	public void onPause(String streamId) {
		if (streamId.equals(MAIN_STREAM))
			this.stream1.pause();
			else if (streamId.equals(ECHO_STREAM))
				this.streamEcho.pause();
		
	}

	@Override
	public void onSurfaceViewCreated(String streamId, SurfaceView surfaceView) {
		if (streamId.equals(MAIN_STREAM))
			this.stream1.prepare(surfaceView);
		else if (streamId.equals(ECHO_STREAM))
			this.streamEcho.prepare(surfaceView);
	}

	@Override
	public void onSurfaceViewDestroyed(String streamId) {
		if (streamId.equals(MAIN_STREAM))
			this.stream1.destroy();
		else if (streamId.equals(ECHO_STREAM))
			this.streamEcho.destroy();
	}

	@Override
	public List<IStream> getStreams() {
		 List<IStream> streams = new ArrayList<IStream>();
		 streams.add(this.stream1);
		 streams.add(this.streamEcho);
		return streams;
	}

	@Override
	public List<StreamProperty> getStreamProperties() {
		ArrayList<StreamProperty> streamProps = new ArrayList<StreamProperty>();
		streamProps.add(StreamProperty.NAME);
		streamProps.add(StreamProperty.STATE);
		return streamProps;
	}

	
	// VOIP METHODS AND LOGIC
	
	 private void handleButMakeCallClicked()
	 {
		 if (this.tcState==TeleconsultationState.READY)
			 makeCall();
		 else if (this.tcState==TeleconsultationState.CALLING || this.tcState==TeleconsultationState.HOLDING || this.tcState==TeleconsultationState.REMOTE_HOLDING)
			 hangupCall();
	 }
	 
	  private void makeCall()
	  {
		  if (myVoip!=null && myVoip.getCall().getState()==CallState.IDLE)
			  myVoip.makeCall(this.ecoExtension);
	  }
	  
	  private void handleButHoldClicked()
		{
//			if (this.tcState==TeleconsultationState.CALLING)
//				toggleHoldCall(true);
//			else if (this.tcState==TeleconsultationState.HOLDING)
//				toggleHoldCall(false);
			
			if (this.tcState!=TeleconsultationState.READY && this.tcState!=TeleconsultationState.IDLE)
				toggleHoldCall(butHoldCall.isChecked());
		}
		
		private void toggleHoldCall(boolean holding)
		{
			if (holding)
			{
				myVoip.holdCall();
			}
			else
			{
				myVoip.unholdCall();
			}
		}
		
	  private void hangupCall()
	  {
		  myVoip.hangupCall();
	  }
	
	  private Properties getProperties(String FileName) {
			Properties properties = new Properties();
	        try {
	               /**
	                * getAssets() Return an AssetManager instance for your
	                * application's package. AssetManager Provides access to an
	                * application's raw asset files;
	                */
	               AssetManager assetManager = this.getAssets();
	               /**
	                * Open an asset using ACCESS_STREAMING mode. This
	                */
	               InputStream inputStream = assetManager.open(FileName);
	               /**
	                * Loads properties from the specified InputStream,
	                */
	               properties.load(inputStream);

	        } catch (IOException e) {
	               // TODO Auto-generated catch block
	               Log.e("AssetsPropertyReader",e.toString());
	        }
	        return properties;

	 }

	  private void subscribeBuddies()
		{
			 String buddyExtension = this.voipParams.get("ecoExtension");
			 Log.d(TAG, "adding buddies...");
			 myVoip.getAccount().addBuddy(getBuddyUri(buddyExtension));
		}
	  
	  private String getBuddyUri(String extension)
		{
			return "sip:" + extension + "@" + this.sipServerIp + ":" + this.sipServerPort ;
		}

	private HashMap<String,String> getVoipSetupParams()
	    { 
		 
		    HashMap<String,String> params = teleconsultation.getLastSession().getVoipParams();
			
		    this.sipServerIp = params.get("sipServerIp");
		    this.sipServerPort = params.get("sipServerPort");
		   
		    		
			// ------------------------------------------------------------------------------------------------------------------
			
			String onHoldSoundPath = Utils.getResourcePathByAssetCopy(this.getApplicationContext(), "", "test_hold.wav");
			String onIncomingCallRingTonePath = Utils.getResourcePathByAssetCopy(this.getApplicationContext(), "", "ring_in_call.wav");
			String onOutcomingCallRingTonePath = Utils.getResourcePathByAssetCopy(this.getApplicationContext(), "", "ring_out_call.wav");
			
			
			params.put("onHoldSound", onHoldSoundPath);
			params.put("onIncomingCallSound",onIncomingCallRingTonePath ); // onIncomingCallRingTonePath
			params.put("onOutcomingCallSound",onOutcomingCallRingTonePath); // onOutcomingCallRingTonePath
			
			Log.d(TAG,"OnHoldSoundPath:" + onHoldSoundPath);
			 
			return params;
	    	
	    }
	
	
		private class CallHandler extends Handler {

			private SpecTeleconsultationActivity app;
			private VoipLib myVoip;
			public boolean reinitRequest = false;
			private boolean incoming_call_request;
		

			public CallHandler(SpecTeleconsultationActivity teleconsultationActivity,
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
				
				
		
			
				// Register the account after the Lib Initialization
				if (myEventBundle.getEvent()==VoipEvent.LIB_INITIALIZED)   {myVoip.registerAccount();
																				}	
				else if (myEventBundle.getEvent()==VoipEvent.ACCOUNT_REGISTERED)    {
																					if (!accountRegistered)
																						{
																						 this.app.subscribeBuddies(); 
																						}
																					else accountRegistered = true;
																					}	
				
				else if (myEventBundle.getEvent()==VoipEvent.ACCOUNT_UNREGISTERED)
				{
					 setTeleconsultationState(TeleconsultationState.IDLE);
				}
				
				else if (myEventBundle.getEventType()==VoipEventType.BUDDY_EVENT)
				{
					
					Log.d(TAG, "In handle Message for BUDDY EVENT");
					//IBuddy myBuddy = (IBuddy) myEventBundle.getData();
					
					// There is only one subscribed buddy in this app, so we don't need to get IBuddy informations
					if (myEventBundle.getEvent()==VoipEvent.BUDDY_CONNECTED)
					{
						// the remote buddy is no longer on Hold State
                        remoteHold = false;
                        
						if (tcState==TeleconsultationState.REMOTE_HOLDING || tcState==TeleconsultationState.HOLDING)
						{
							if (localHold)
							{
								setTeleconsultationState(TeleconsultationState.HOLDING);
							}
							else
								setTeleconsultationState(TeleconsultationState.CALLING);
						}
						else if (tcState==TeleconsultationState.IDLE)
						{
							setTeleconsultationState(TeleconsultationState.READY);
						}
					 
					}
					else if(myEventBundle.getEvent()==VoipEvent.BUDDY_HOLDING)
					{
						if (myVoip.getCall().getState()== CallState.ACTIVE || myVoip.getCall().getState()== CallState.HOLDING)
							setTeleconsultationState(TeleconsultationState.REMOTE_HOLDING);
					}
					else if(myEventBundle.getEvent()==VoipEvent.BUDDY_DISCONNECTED)
					{
						setTeleconsultationState(TeleconsultationState.IDLE);
					}
					 
				}
				
				//else if (myEventBundle.getEvent()==VoipEvent.CALL_INCOMING)  
				
				else if (myEventBundle.getEvent()==VoipEvent.CALL_READY)
				{
					
				}
				
				else if  (myEventBundle.getEvent()==VoipEvent.CALL_ACTIVE)    {
					if (remoteHold)
					{
						this.app.setTeleconsultationState(TeleconsultationState.REMOTE_HOLDING);
					}
					else
					{
						this.app.setTeleconsultationState(TeleconsultationState.CALLING);
					}
					
				}
			
				else if  (myEventBundle.getEvent()==VoipEvent.CALL_HOLDING)    {
					this.app.setTeleconsultationState(TeleconsultationState.HOLDING);
				}

				else if (myEventBundle.getEvent()==VoipEvent.CALL_HANGUP || myEventBundle.getEvent()==VoipEvent.CALL_REMOTE_HANGUP)    {
					
					if (this.app.tcState!=TeleconsultationState.IDLE)
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
					else if(exitFromAppRequest)
					{
						exitFromApp();
					}
				}
				else if  (myEventBundle.getEvent()==VoipEvent.LIB_INITIALIZATION_FAILED || myEventBundle.getEvent()==VoipEvent.ACCOUNT_REGISTRATION_FAILED ||
						myEventBundle.getEvent()==VoipEvent.LIB_CONNECTION_FAILED || myEventBundle.getEvent()==VoipEvent.BUDDY_SUBSCRIPTION_FAILED)
					    showErrorEventAlert(myEventBundle);
				
				     
			} // end of handleMessage()

			
			private void showErrorEventAlert(VoipEventBundle myEventBundle) {
			
				AlertDialog.Builder miaAlert = new AlertDialog.Builder(this.app);
				miaAlert.setTitle(myEventBundle.getEventType() + ":" + myEventBundle.getEvent());
				miaAlert.setMessage(myEventBundle.getInfo());
				AlertDialog alert = miaAlert.create();
				alert.show();
			}
	
}
																		}
