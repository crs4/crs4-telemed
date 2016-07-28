package it.crs4.most.demo.specapp;

import android.content.Context;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
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
import java.util.Timer;
import java.util.TimerTask;

import it.crs4.most.demo.specapp.models.Device;
import it.crs4.most.demo.specapp.models.Teleconsultation;
import it.crs4.most.demo.specapp.models.TeleconsultationSessionState;
import it.crs4.most.demo.specapp.ui.TcStateTextView;
import it.crs4.most.visualization.augmentedreality.ARFragment;
import it.crs4.most.visualization.augmentedreality.mesh.Arrow;
import it.crs4.most.visualization.augmentedreality.mesh.Mesh;
import it.crs4.most.visualization.augmentedreality.renderer.PubSubARRenderer;
import it.crs4.most.visualization.utils.zmq.ZMQPublisher;
import it.crs4.most.voip.Utils;
import it.crs4.most.voip.VoipEventBundle;
import it.crs4.most.voip.VoipLib;
import it.crs4.most.voip.VoipLibBackend;
import it.crs4.most.voip.enums.CallState;
import it.crs4.most.voip.enums.VoipEvent;
import it.crs4.most.voip.enums.VoipEventType;

import it.crs4.most.streaming.IStream;
import it.crs4.most.streaming.StreamingEventBundle;
import it.crs4.most.streaming.StreamingLib;
import it.crs4.most.streaming.StreamingLibBackend;
import it.crs4.most.streaming.enums.PTZ_Direction;
import it.crs4.most.streaming.enums.PTZ_Zoom;
import it.crs4.most.streaming.enums.StreamProperty;
import it.crs4.most.streaming.enums.StreamState;
import it.crs4.most.streaming.enums.StreamingEvent;
import it.crs4.most.streaming.enums.StreamingEventType;
import it.crs4.most.streaming.ptz.PTZ_Manager;
import it.crs4.most.streaming.utils.ImageDownloader;
import it.crs4.most.streaming.utils.ImageDownloader.IBitmapReceiver;
import it.crs4.most.visualization.IPtzCommandReceiver;
import it.crs4.most.visualization.IStreamFragmentCommandListener;
import it.crs4.most.visualization.PTZ_ControllerPopupWindowFactory;
import it.crs4.most.visualization.StreamViewerFragment;
import it.crs4.most.visualization.StreamInspectorFragment.IStreamProvider;

import org.artoolkit.ar.base.assets.AssetHelper;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;


public class SpecTeleconsultationActivity extends AppCompatActivity implements Handler.Callback,
        IPtzCommandReceiver,
        IStreamFragmentCommandListener,
        IStreamProvider,
        ARFragment.OnCompleteListener, SurfaceHolder.Callback {

    private static String TAG = "SpecTeleconsultationActivity";
    private String MAIN_STREAM = "MAIN_STREAM";
    private String ECO_STREAM = "ECO_STREAM";

    //ID for the menu exit option
    private boolean exitFromAppRequest = false;

    private Handler mStreamHandler;
    private IStream mStreamCamera;
    private IStream mStreamEco;
    private ARFragment mStreamCameraFragment;
    private StreamViewerFragment mStreamEcoFragment;
    private TeleconsultationState mTcState = TeleconsultationState.IDLE;
    private TcStateTextView mTextTcState;
    private PTZ_Manager mPTZManager;

    // VOIP
    private String mSipServerIp;
    private String mSipServerPort;
    private VoipLib mVoipLib;
    private Teleconsultation mTeleconsultation;
    private PTZ_ControllerPopupWindowFactory mPTZPopupWindowController;
    private String mEcoExtension;

    private MenuItem mButtonMakeCall;
    private MenuItem mButtonHoldCall;

    private HashMap<String, String> mVoipParams;
    private RemoteConfigReader mConfigReader;
    private boolean mLocalHold = false;
    private boolean mRemoteHold = false;
    private boolean mAccountRegistered = false;
    private boolean mFirstCallStarted = false;
    private boolean mStreamPrepared = false;
    private HashMap<String, Mesh> mMeshes = new HashMap<>();
    private PubSubARRenderer mRenderer;
    private Handler mHandlerAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        String configServerIP = QuerySettings.getConfigServerAddress(this);
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(this));
        mConfigReader = new RemoteConfigReader(this, configServerIP, configServerPort);

        mStreamHandler = new Handler(this);
        mHandlerAR = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message streamingMessage) {
                StreamingEventBundle event = (StreamingEventBundle) streamingMessage.obj;
                String infoMsg = "Event Type:" + event.getEventType() + " ->" + event.getEvent() + ":" + event.getInfo();
                Log.d(TAG, "handleMessage: Current Event:" + infoMsg);

                StreamState streamState = ((IStream) event.getData()).getState();
                Log.d(TAG, "event.getData().streamState " + streamState);
                if (event.getEventType() == StreamingEventType.STREAM_EVENT &&
                        event.getEvent() == StreamingEvent.STREAM_STATE_CHANGED

                        ) {
                    if (streamState == StreamState.PLAYING) {

                        Log.d(TAG, "event.getData().streamState " + streamState);
                        Log.d(TAG, "ready to call cameraPreviewStarted");

                        //FIXME should be dynamically set
                        int width = 704;
                        int height = 576;
                        Log.d(TAG, "width " + width);
                        Log.d(TAG, "height " + height);
                        mStreamCameraFragment.cameraPreviewStarted(width, height, 25, 0, false);
                    }
                }
            }
        };

        setContentView(R.layout.spec_activity_main);
        setupTeleconsultationInfo();
        setTeleconsultationState(TeleconsultationState.IDLE);

        AssetHelper assetHelper = new AssetHelper(getAssets());
        assetHelper.cacheAssetFolder(this, "Data");

        ZMQPublisher publisher = new ZMQPublisher(5556);
        Thread pubThread = new Thread(publisher);
        pubThread.start();

        Arrow arrow = new Arrow("arrow");
        mMeshes.put(arrow.getId(), arrow);
        arrow.publisher = publisher;
        mRenderer = new PubSubARRenderer(this, publisher);
        mRenderer.setMeshes(mMeshes);

        setupStreamLib();
        setupPtzPopupWindow();
        setupVoipLib();
    }

    private void setupTeleconsultationInfo() {
        Intent i = getIntent();
        mTeleconsultation = (Teleconsultation) i.getExtras().getSerializable("Teleconsultation");
//        TextView txtTeleconsultation = (TextView) findViewById(R.id.txtTeleconsultation);
//        txtTeleconsultation.setText(mTeleconsultation.getInfo());

    }

    private void setupStreamLib() {
        Log.d(TAG, "setupStreamLib");
        try {
            Device camera = mTeleconsultation.getLastSession().getCamera();

            // Instance and initialize the Streaming Library
            StreamingLib streamingLib = new StreamingLibBackend();
            // First of all, initialize the library
            streamingLib.initLib(getApplicationContext());

//            PTZ_ControllerFragment ptzControllerFragment = PTZ_ControllerFragment.newInstance(true, true, true);
            mPTZManager = new PTZ_Manager(this,
                    camera.getPtzUri(), //     uriProps.getProperty("uri_ptz") ,
                    camera.getUser(), //  uriProps.getProperty("username_ptz"),
                    camera.getPwd() //  uriProps.getProperty("password_ptz")
            );

            // Instance the first stream
            HashMap<String, String> stream_camera = new HashMap<>();
            stream_camera.put("name", MAIN_STREAM);


            String streamingUri = camera.getStreamUri();
            stream_camera.put("uri", streamingUri);

            mStreamCamera = streamingLib.createStream(stream_camera, mHandlerAR);
            Log.d(TAG, "STREAM 1 INSTANCE");

            // Instance the first StreamViewer fragment where to render the first stream by passing the stream name as its ID.
            mStreamCameraFragment = ARFragment.newInstance(mStreamCamera.getName());
            mStreamCameraFragment.setPlayerButtonsVisible(false);
            Log.d(TAG, String.format("mRenderer != null %b", mRenderer != null));
            mStreamCameraFragment.setRenderer(mRenderer);
            mStreamCameraFragment.setStreamAR(mStreamCamera);

            // Instance the Eco Stream

            Device encoder = mTeleconsultation.getLastSession().getEncoder();
            HashMap<String, String> stream_eco_params = new HashMap<>();
            stream_eco_params.put("name", ECO_STREAM);


            String streamingEcoUri = encoder.getStreamUri();
            stream_eco_params.put("uri", streamingEcoUri);

            mStreamEco = streamingLib.createStream(stream_eco_params, mStreamHandler);
            Log.d(TAG, "STREAM ECHO INSTANCE");

            // Instance the eco StreamViewer fragment where to render the eco stream by passing the stream name as its ID.
            mStreamEcoFragment = StreamViewerFragment.newInstance(mStreamEco.getName());
            mStreamEcoFragment.setPlayerButtonsVisible(false);


        }
        catch (Exception e) {
            e.printStackTrace();
        }
        mStreamCameraFragment.setGlSurfaceViewCallback(this);

        // add the first fragment to the first container
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container_stream_camera, mStreamCameraFragment);
        fragmentTransaction.add(R.id.container_stream_eco, mStreamEcoFragment);
        fragmentTransaction.commit();
    }

    private void setupVoipLib() {
        // Voip Lib Initialization Params

        mVoipParams = getVoipSetupParams();
        mEcoExtension = mVoipParams.get("ecoExtension");

        Log.d(TAG, "Initializing the lib...");
        CallHandler voipHandler = null;
        if (mVoipLib == null) {
            Log.d(TAG, "Voip null... Initialization.....");
            mVoipLib = new VoipLibBackend();
            voipHandler = new CallHandler(this, mVoipLib);

            // Initialize the library providing custom initialization params and an handler where
            // to receive event notifications. Following Voip methods are called from the handleMassage() callback method
            //boolean result = mVoipLib.initLib(params, new RegistrationHandler(this, mVoipLib));
            mVoipLib.initLib(getApplicationContext(), mVoipParams, voipHandler);
        }
        else {
            Log.d(TAG, "Voip is not null... Destroying the lib before reinitializing.....");
            // Reinitialization will be done after deinitialization event callback
            voipHandler.reinitRequest = true;
            mVoipLib.destroyLib();
        }
    }

    private void setupPtzPopupWindow() {
        mPTZPopupWindowController = new PTZ_ControllerPopupWindowFactory(this, this, true, true, true, 100, 100);
        //PopupWindow ptzPopupWindow = mPTZPopupWindowController.getPopupWindow();
    }

    private void showPTZPopupWindow() {
        mPTZPopupWindowController.show();
    }

    private void setTeleconsultationState(TeleconsultationState tcState) {
        mTcState = tcState;
        notifyTeleconsultationStateChanged();

    }

    private void notifyTeleconsultationStateChanged() {

//        if (mTextTcState == null) {
//            mTextTcState = (TcStateTextView) findViewById(R.id.txtTcState);
//        }
//
//        mTextTcState.setTeleconsultationState(mTcState);
        try {
            if (mTcState == TeleconsultationState.IDLE) {
                mButtonMakeCall.setTitle("Call");
                mButtonMakeCall.setEnabled(false);
                mButtonHoldCall.setEnabled(false);
                mLocalHold = false;
                mRemoteHold = false;
                mAccountRegistered = false;
                pauseStreams();
            }
            else if (mTcState == TeleconsultationState.READY) {
                mButtonMakeCall.setTitle("Call");
                mButtonMakeCall.setEnabled(true);
                mButtonHoldCall.setEnabled(false);
                mLocalHold = false;
                mRemoteHold = false;
                mAccountRegistered = true;
                pauseStreams();

                if (mFirstCallStarted)
                    checkForSessionClosed();
            }
            else if (mTcState == TeleconsultationState.CALLING) {
                mButtonMakeCall.setEnabled(true);
                mButtonMakeCall.setTitle("Hangup");
                mButtonHoldCall.setEnabled(true);
                mLocalHold = false;
                mRemoteHold = false;
                mFirstCallStarted = true;
                playStreams();
            }
            else if (mTcState == TeleconsultationState.HOLDING) {
                mButtonMakeCall.setEnabled(true);
                mButtonMakeCall.setTitle("Hangup");
                mButtonHoldCall.setEnabled(true);
                mLocalHold = true;
                pauseStreams();
            }
            else if (mTcState == TeleconsultationState.REMOTE_HOLDING) {
                mButtonMakeCall.setEnabled(true);
                mButtonMakeCall.setTitle("Hangup");
                mButtonHoldCall.setEnabled(true);
                mRemoteHold = true;
                pauseStreams();
            }
            else if (mTcState == TeleconsultationState.SESSION_CLOSED) {
                startReportActivity();
            }
        }
        catch (NullPointerException ne) {

        }
    }

    private void checkForSessionClosed() {
        final Timer t = new Timer();

        t.schedule(new TimerTask() {

            @Override
            public void run() {

                mConfigReader.getSessionState(mTeleconsultation.getLastSession().getId(), mTeleconsultation.getSpecialist().getAccessToken(), new Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject res) {
                        Log.d(TAG, "Teleconsultation state response:" + res);
                        try {
                            String state = res.getJSONObject("data").getJSONObject("session").getString("state");
                            Log.d(TAG, "Teleconsultation state found:" + state);
                            if (state.equals(TeleconsultationSessionState.CLOSE.name())) {
                                Log.d(TAG, "Closing session");
                                t.cancel();
                                setTeleconsultationState(TeleconsultationState.SESSION_CLOSED);
                            }

                        }
                        catch (JSONException e) {
                            Log.e(TAG, "Error retrieving session state:" + e);
                            e.printStackTrace();
                        }


                    }


                }, new ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        Log.e(TAG, "Error reading Teleconsultation state response:" + arg0);
                    }
                });
                // config.setTeleconsultation(selectedTc);
            }
        }, 0, 5000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.teleconsultation_menu, menu);
        boolean res = super.onCreateOptionsMenu(menu);
        mButtonMakeCall = menu.findItem(R.id.button_call);
        return res;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //check selected menu item

        switch (item.getItemId()) {
            case R.id.button_ptz:
                showPTZPopupWindow();
                break;
            case R.id.button_exit:
                exitFromApp();
                break;
            case R.id.button_call:
                handleButMakeCallClicked();
                break;
            case R.id.button_hold:
                handleButHoldClicked();
            break;
        }
        return false;
    }


    private void startReportActivity() {
        Log.d(TAG, "Do report... Call activity!");
        Intent i = new Intent(this, InnerArchetypeViewerActivity.class);
        Log.d(TAG, "STARTING ACTIVITY InnerArchetypeViewerActivity");
        startActivity(i);

        //finish();
    }

    @Override
    public void onPTZstartMove(PTZ_Direction dir) {
        Log.d(TAG, "Called onPTZstartMove for direction:" + dir);
        //Toast.makeText(this, "Start Moving to ->" + dir, Toast.LENGTH_LONG).show();
        mPTZManager.startMove(dir);
    }


    @Override
    public void onPTZstopMove(PTZ_Direction dir) {
        Log.d(TAG, "Called onPTZstoptMove for direction:" + dir);
        //Toast.makeText(this, "Stop Moving from ->" + dir, Toast.LENGTH_LONG).show();
        mPTZManager.stopMove();
    }


    @Override
    public void onPTZstartZoom(PTZ_Zoom dir) {
        mPTZManager.startZoom(dir);
    }

    @Override
    public void onPTZstopZoom(PTZ_Zoom dir) {
        mPTZManager.stopZoom();
    }

    @Override
    public void onGoHome() {
        String homePreset = "home";// configProps.getProperty("home_preset_ptz");
        mPTZManager.goTo(homePreset);

    }


    @Override
    public void onSnaphot() {

        Log.d(TAG, "on snapshot called");

        IBitmapReceiver receiver = new IBitmapReceiver() {
            @Override
            public void onBitmapSaved(ImageDownloader imageDownloader, String filename) {
                Log.d(TAG, "Saved Image:" + filename);
                Toast.makeText(SpecTeleconsultationActivity.this, "Image saved:" + filename, Toast.LENGTH_LONG).show();
                imageDownloader.logAppFileNames();
            }

            @Override
            public void onBitmapDownloaded(ImageDownloader imageDownloader, Bitmap image) {
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

        Device camera = mTeleconsultation.getLastSession().getCamera();
        ImageDownloader imageDownloader = new ImageDownloader(receiver, this,
                camera.getUser(), //   uriProps.getProperty("username_ptz"),
                camera.getPwd()); // uriProps.getProperty("password_ptz"));

        imageDownloader.downloadImage(camera.getShotUri()); //    uriProps.getProperty("uri_still_image"));
    }

    @Override
    public boolean handleMessage(Message streamingMessage) {
        // The bundle containing all available informations and resources about the incoming event
        StreamingEventBundle myEvent = (StreamingEventBundle) streamingMessage.obj;

        String infoMsg = "Event Type:" + myEvent.getEventType() + " ->" + myEvent.getEvent() + ":" + myEvent.getInfo();
        Log.d(TAG, "handleMessage: Current Event:" + infoMsg);


        // for simplicity, in this example we only handle events of type STREAM_EVENT
        if (myEvent.getEventType() == StreamingEventType.STREAM_EVENT)
            if (myEvent.getEvent() == StreamingEvent.STREAM_STATE_CHANGED || myEvent.getEvent() == StreamingEvent.STREAM_ERROR) {

                // All events of type STREAM_EVENT provide a reference to the stream that triggered it.
                // In this case we are handling two streams, so we need to check what stream triggered the event.
                // Note that we are only interested to the new state of the stream
                IStream stream = (IStream) myEvent.getData();
                String streamName = stream.getName();

                if (mStreamCamera.getState() == StreamState.DEINITIALIZED && exitFromAppRequest) {
                    boolean streamMainDestroyed = false;
                    boolean streamEcoDestroyed = false;
                    if (streamName.equalsIgnoreCase(MAIN_STREAM))
                        streamMainDestroyed = true;
                    else if (streamName.equalsIgnoreCase(ECO_STREAM))
                        streamEcoDestroyed = true;

                    Log.d(TAG, "Stream " + streamName + " deinitialized..");
                    exitFromApp();
                }
            }
        return false;
    }

    private void exitFromApp() {

        Log.d(TAG, "Called exitFromApp()");

        exitFromAppRequest = true;


        boolean voipDestroyed = false;
        if (mVoipLib != null && !voipDestroyed) {
            mVoipLib.destroyLib();
        }
        else {
            Log.d(TAG, "Voip Library deinitialized. Exiting the app");
            finish();
        }
    }


    private void playStreams() {
        if (mStreamCamera != null && mStreamCamera.getState() != StreamState.PLAYING) {
            mStreamCameraFragment.setStreamVisible();
            mStreamCamera.play();

        }

        if (mStreamEco != null && mStreamEco.getState() != StreamState.PLAYING) {
            mStreamEcoFragment.setStreamVisible();
            mStreamEco.play();

        }

    }

    private void pauseStreams() {


        if (mStreamCamera != null && mStreamCamera.getState() == StreamState.PLAYING) {
            mStreamCamera.pause();
            mStreamCameraFragment.setStreamInvisible("PAUSED");
        }

        if (mStreamEco != null && mStreamEco.getState() == StreamState.PLAYING) {
            mStreamEco.pause();
            mStreamEcoFragment.setStreamInvisible("PAUSED");

        }
    }

    @Override
    public void onPlay(String streamId) {
        if (streamId.equals(MAIN_STREAM))
            mStreamCamera.play();
        else if (streamId.equals(ECO_STREAM))
            mStreamEco.play();
    }

    @Override
    public void onPause(String streamId) {
        if (streamId.equals(MAIN_STREAM))
            mStreamCamera.pause();
        else if (streamId.equals(ECO_STREAM))
            mStreamEco.pause();

    }

    @Override
    public void onSurfaceViewCreated(String streamId, SurfaceView surfaceView) {
        Log.d("TAG", "onSurfaceViewCreated");
        if (surfaceView != null) {
            if (streamId.equals(MAIN_STREAM))
                if (!mStreamPrepared) {
                    mStreamCamera.prepare(surfaceView, true);
//                mStreamCameraFragment.setStreamAR(mStreamCamera);
//                mStreamCameraFragment.setRenderer(mRenderer);
                    mStreamCameraFragment.prepareRemoteAR();
                    mStreamPrepared = true;
                }

                else if (streamId.equals(ECO_STREAM))
                    mStreamEco.prepare(surfaceView);
        }
    }

    @Override
    public void onSurfaceViewDestroyed(String streamId) {
        if (streamId.equals(MAIN_STREAM))
            mStreamCamera.destroy();
        else if (streamId.equals(ECO_STREAM))
            mStreamEco.destroy();
    }

    @Override
    public List<IStream> getStreams() {
        List<IStream> streams = new ArrayList<IStream>();
        streams.add(mStreamCamera);
        streams.add(mStreamEco);
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

    private void handleButMakeCallClicked() {
        if (mTcState == TeleconsultationState.READY) {
            makeCall();
        }
        else if (mTcState == TeleconsultationState.CALLING ||
                mTcState == TeleconsultationState.HOLDING ||
                mTcState == TeleconsultationState.REMOTE_HOLDING) {
            hangupCall();
        }
    }

    private void makeCall() {
        if (mVoipLib != null && mVoipLib.getCall().getState() == CallState.IDLE)
            mVoipLib.makeCall(mEcoExtension);
    }

    private void handleButHoldClicked() {
//			if (mTcState==TeleconsultationState.CALLING)
//				toggleHoldCall(true);
//			else if (mTcState==TeleconsultationState.HOLDING)
//				toggleHoldCall(false);

        if (mTcState != TeleconsultationState.READY && mTcState != TeleconsultationState.IDLE)
            toggleHoldCall(mButtonHoldCall.isChecked());
    }

    private void toggleHoldCall(boolean holding) {
        if (holding) {
            mVoipLib.holdCall();
        }
        else {
            mVoipLib.unholdCall();
        }
    }

    private void hangupCall() {
        mVoipLib.hangupCall();
    }

    private void subscribeBuddies() {
        String buddyExtension = mVoipParams.get("ecoExtension");
        Log.d(TAG, "adding buddies...");
        mVoipLib.getAccount().addBuddy(getBuddyUri(buddyExtension));
    }

    private String getBuddyUri(String extension) {
        return "sip:" + extension + "@" + mSipServerIp + ":" + mSipServerPort;
    }

    private HashMap<String, String> getVoipSetupParams() {

        HashMap<String, String> params = mTeleconsultation.getLastSession().getVoipParams();

        mSipServerIp = params.get("sipServerIp");
        mSipServerPort = params.get("sipServerPort");

        String onHoldSoundPath = Utils.getResourcePathByAssetCopy(getApplicationContext(), "", "test_hold.wav");
        String onIncomingCallRingTonePath = Utils.getResourcePathByAssetCopy(getApplicationContext(), "", "ring_in_call.wav");
        String onOutcomingCallRingTonePath = Utils.getResourcePathByAssetCopy(getApplicationContext(), "", "ring_out_call.wav");

        params.put("onHoldSound", onHoldSoundPath);
        params.put("onIncomingCallSound", onIncomingCallRingTonePath); // onIncomingCallRingTonePath
        params.put("onOutcomingCallSound", onOutcomingCallRingTonePath); // onOutcomingCallRingTonePath

        Log.d(TAG, "OnHoldSoundPath:" + onHoldSoundPath);

        return params;

    }

    @Override
    public void onFragmentCreate() {

    }

    @Override
    public void onFragmentResume() {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mStreamCameraFragment.getGlView().setMeshes(mMeshes);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    private class CallHandler extends Handler {

        private SpecTeleconsultationActivity app;
        private VoipLib myVoip;
        public boolean reinitRequest = false;
        private boolean incoming_call_request;


        public CallHandler(SpecTeleconsultationActivity teleconsultationActivity,
                           VoipLib myVoip) {
            app = teleconsultationActivity;
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
                if (!mAccountRegistered) {
                    app.subscribeBuddies();
                }
                else mAccountRegistered = true;
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
                    mRemoteHold = false;

                    if (mTcState == TeleconsultationState.REMOTE_HOLDING || mTcState == TeleconsultationState.HOLDING) {
                        if (mLocalHold) {
                            setTeleconsultationState(TeleconsultationState.HOLDING);
                        }
                        else
                            setTeleconsultationState(TeleconsultationState.CALLING);
                    }
                    else if (mTcState == TeleconsultationState.IDLE) {
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

            //else if (myEventBundle.getEvent()==VoipEvent.CALL_INCOMING)

            else if (myEventBundle.getEvent() == VoipEvent.CALL_READY) {

            }
            else if (myEventBundle.getEvent() == VoipEvent.CALL_ACTIVE) {
                if (mRemoteHold) {
                    app.setTeleconsultationState(TeleconsultationState.REMOTE_HOLDING);
                }
                else {
                    app.setTeleconsultationState(TeleconsultationState.CALLING);
                }

            }
            else if (myEventBundle.getEvent() == VoipEvent.CALL_HOLDING) {
                app.setTeleconsultationState(TeleconsultationState.HOLDING);
            }
            else if (myEventBundle.getEvent() == VoipEvent.CALL_HANGUP || myEventBundle.getEvent() == VoipEvent.CALL_REMOTE_HANGUP) {

                if (app.mTcState != TeleconsultationState.IDLE)
                    app.setTeleconsultationState(TeleconsultationState.READY);
            }
            // Deinitialize the Voip Lib and release all allocated resources
            else if (myEventBundle.getEvent() == VoipEvent.LIB_DEINITIALIZED || myEventBundle.getEvent() == VoipEvent.LIB_DEINITIALIZATION_FAILED) {
                Log.d(TAG, "Setting to null MyVoipLib");
                app.mVoipLib = null;
                app.setTeleconsultationState(TeleconsultationState.IDLE);

                if (reinitRequest) {
                    reinitRequest = false;
                    app.setupVoipLib();
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

            AlertDialog.Builder miaAlert = new AlertDialog.Builder(app);
            miaAlert.setTitle(myEventBundle.getEventType() + ":" + myEventBundle.getEvent());
            miaAlert.setMessage(myEventBundle.getInfo());
            AlertDialog alert = miaAlert.create();
            alert.show();
        }

    }
}
