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
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
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
	
	private TeleconsultationState tcState = TeleconsultationState.READY;
	private TcStateTextView txtTcState = null;
	private ImageButton butCall;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Intent i = getIntent();
		//this.ecoUser =  (EcoUser) i.getExtras().getSerializable("EcoUser");
		
		    setContentView(R.layout.activity_teleconsultation);
	        txtTcState = (TcStateTextView) findViewById(R.id.txtTcState);
	        txtTcState.setTeleconsultationState(this.tcState);
		    //this.waitForSpecialist();

		    
			this.handler = new Handler(this);
			 
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
			
			this.setupActionBar();
			
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
				 
       
        final PopupWindow popupWindow = new PopupWindow(popupView,
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
        
    
        Button cancelButton = (Button) popupView.findViewById(R.id.butCallCancel);
        
        cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popupWindow.dismiss();
				
			}
		});
        
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);

        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);
       
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
	
}
