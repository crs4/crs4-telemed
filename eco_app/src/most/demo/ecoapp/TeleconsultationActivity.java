package most.demo.ecoapp;

import java.util.HashMap;

import org.crs4.most.streaming.IStream;
import org.crs4.most.streaming.StreamingEventBundle;
import org.crs4.most.streaming.StreamingLib;
import org.crs4.most.streaming.StreamingLibBackend;
import org.crs4.most.visualization.IStreamFragmentCommandListener;
import org.crs4.most.visualization.StreamViewerFragment;

import most.demo.ecoapp.models.EcoUser;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;


import android.os.Handler;
import android.os.Message;

public class TeleconsultationActivity extends ActionBarActivity implements Handler.Callback, IStreamFragmentCommandListener {

	private static final String TAG = "TeleconsultationActivity";
	private EcoUser ecoUser = null;
	private StreamViewerFragment stream1Fragment = null;
	private IStream stream1 = null;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		this.ecoUser =  (EcoUser) i.getExtras().getSerializable("EcoUser");
		
		setContentView(R.layout.activity_teleconsultation);
		if (savedInstanceState == null) {
	        
			String streamName = "Teleconsultation Stream";
			String streamUri = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov";
			this.stream1Fragment = StreamViewerFragment.newInstance(streamName);
			
			 this.handler = new Handler(this);
		    	
		    	
		    // Instance and initialize the Streaming Library
		    	
		   
			// add the first fragment to the first container
	    	FragmentTransaction fragmentTransaction = getFragmentManager()
					.beginTransaction();
			fragmentTransaction.add(R.id.container_stream,
					stream1Fragment);
			fragmentTransaction.commit();
			
			this.prepareStream(streamName,streamUri);
		}
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		    
	
	}
	
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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		
		Log.d(TAG, "Stream Event Name:" + myEvent.getEvent().name());
		Log.d(TAG, "Stream Event Info:" + myEvent.getInfo());
		Log.d(TAG, "Stream State:" + this.stream1.getState());
		return false;
	}
	
	
	
//	private void waitForSpecialist()
//	{
//		//Toast.makeText(MainActivity.this, "Connecting to:" + deviceName + "(" + macAddress +")" , Toast.LENGTH_LONG).show();
//		progressWaitingSpec = new ProgressDialog(TeleconsultationActivity.this);
//		progressWaitingSpec.setTitle("Preparing Teleconsultation Session");
//		progressWaitingSpec.setMessage("Waiting for specialist...");
//		
//		progressWaitingSpec.show();
//		
//	}
	
}
