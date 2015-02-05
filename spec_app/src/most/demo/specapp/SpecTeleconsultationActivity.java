package most.demo.specapp;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;



import most.demo.specapp.TeleconsultationState;
import most.demo.specapp.ui.TcStateTextView;

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
 


 

import android.app.FragmentTransaction;
 
import android.content.res.AssetManager;
import android.graphics.Bitmap;
 
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;


import android.widget.Button;


import android.widget.Toast;



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
	
	private Properties uriProps = null;

 

	private PTZ_ControllerPopupWindowFactory ptzPopupWindowController;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtTcState = (TcStateTextView) findViewById(R.id.txtTcState);
        
        this.handler = new Handler(this);
        this.setTeleconsultationState(TeleconsultationState.IDLE);
        
        try {
         
        	// Instance and initialize the Streaming Library
        	StreamingLib streamingLib = new StreamingLibBackend();
            
            
            this.ptzControllerFragment = PTZ_ControllerFragment.newInstance(true,true,true);
            
    	  	// First of all, initialize the library 
			streamingLib.initLib(this.getApplicationContext());
			
			
	    	this.uriProps = getUriProperties("uri.properties.default");
       	 	
            this.ptzManager = new PTZ_Manager(this, uriProps.getProperty("uri_ptz") , uriProps.getProperty("username_ptz"), uriProps.getProperty("password_ptz"));
            
            // Instance the first stream
	    	HashMap<String,String> stream1_params = new HashMap<String,String>();
	    	stream1_params.put("name", "Stream_1");
	    	
	    	
       	    this.streamingUri =  uriProps.getProperty("uri_stream");  
	    	stream1_params.put("uri", this.streamingUri);
	    	    	 
	    	this.stream1 = streamingLib.createStream(stream1_params, this.handler);
	    	Log.d(TAG,"STREAM 1 INSTANCE");
	    	
	    	// Instance the first StreamViewer fragment where to render the first stream by passing the stream name as its ID.
	    	this.stream1Fragment = StreamViewerFragment.newInstance(stream1.getName());
	    	
	    	
	    	
	    	// Instance the Echo Stream
	    	
	    	
	    	HashMap<String,String> stream_echo_params = new HashMap<String,String>();
	    	stream_echo_params.put("name", "Stream_Echo");
	    	
	    	
       	    this.streamingEchoUri =  uriProps.getProperty("uri_echo");  
       	    stream_echo_params.put("uri", this.streamingEchoUri);
	    	    	 
	    	this.streamEcho = streamingLib.createStream(stream_echo_params, this.handler);
	    	Log.d(TAG,"STREAM ECHO INSTANCE");
	    	
	    	// Instance the echo StreamViewer fragment where to render the echo stream by passing the stream name as its ID.
	    	this.streamEchoFragment = StreamViewerFragment.newInstance(streamEcho.getName());
	    	
	    	 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// add the first fragment to the first container
    	FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction.add(R.id.container_stream_1, stream1Fragment);
		fragmentTransaction.add(R.id.container_stream_echo, streamEchoFragment);
		fragmentTransaction.commit();
		
		this.setupPtzPopupWindow();
        this.setupActionBar();
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
    
    private void showPTZPopupWindow()
    {
    	this.ptzPopupWindowController.show();
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
			
//			butCall.setEnabled(false);
//			popupCancelButton.setEnabled(true);
//			popupHoldButton.setEnabled(false);
//			popupHangupButton.setEnabled(false);
		}
		else if (this.tcState==TeleconsultationState.CALLING)
		{
//			butCall.setEnabled(true);
//			popupCancelButton.setEnabled(true);
//			popupHoldButton.setEnabled(true);
//			popupHangupButton.setEnabled(true);
		}
		
		else if (this.tcState==TeleconsultationState.HOLDING)
		{
//			butCall.setEnabled(true);
//			popupCancelButton.setEnabled(true);
//			popupHoldButton.setEnabled(true);
//			popupHoldButton.setText("Unhold"); // to be fixed
//			popupHangupButton.setEnabled(true);
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
		String homePreset = this.uriProps.getProperty("home_preset_ptz");
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
		ImageDownloader imageDownloader = new ImageDownloader(receiver, this, uriProps.getProperty("username_ptz"), uriProps.getProperty("password_ptz"));
		imageDownloader.downloadImage(uriProps.getProperty("uri_still_image"));
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
						if (this.stream1.getState()==StreamState.DEINITIALIZED && this.exitFromAppRequest)
						{
							Log.d(TAG,"Stream deinitialized. Exiting from app.");
							this.finish();
						}
						
					    // All events of type STREAM_EVENT provide a reference to the stream that triggered it.
					    // In this case we are handling two streams, so we need to check what stream triggered the event.
					    // Note that we are only interested to the new state of the stream
						IStream stream  =  (IStream) myEvent.getData();
					   
						 
					}
				return false;
	}
	
	 private void exitFromApp() {
			this.exitFromAppRequest = true;
			if (this.stream1!=null)
			{
				this.stream1.destroy();
			}
			else
				this.finish();
		}

	@Override
	public void onPlay(String streamId) {
		//showPTZDialog();
		setupPtzPopupWindow();
		//this.stream1.play();
	}

	@Override
	public void onPause(String streamId) {
		this.stream1.pause();
		
	}

	@Override
	public void onSurfaceViewCreated(String streamId, SurfaceView surfaceView) {
		this.stream1.prepare(surfaceView);
	}

	@Override
	public void onSurfaceViewDestroyed(String streamId) {
		this.stream1.destroy();
	}

	@Override
	public List<IStream> getStreams() {
		 List<IStream> streams = new ArrayList<IStream>();
		 streams.add(this.stream1);
		return streams;
	}

	@Override
	public List<StreamProperty> getStreamProperties() {
		ArrayList<StreamProperty> streamProps = new ArrayList<StreamProperty>();
		streamProps.add(StreamProperty.NAME);
		streamProps.add(StreamProperty.STATE);
		return streamProps;
	}

	
}
