package it.crs4.most.demo.spec;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import org.artoolkit.ar.base.assets.AssetHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.crs4.most.demo.ReportActivity;
import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RESTClient;
import it.crs4.most.demo.TeleconsultationState;
import it.crs4.most.demo.models.ARConfiguration;
import it.crs4.most.demo.models.Device;
import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.models.TeleconsultationSessionState;
import it.crs4.most.demo.ui.TcStateTextView;
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
import it.crs4.most.streaming.utils.Size;
import it.crs4.most.visualization.IPtzCommandReceiver;
import it.crs4.most.visualization.IStreamFragmentCommandListener;
import it.crs4.most.visualization.PTZ_ControllerPopupWindowFactory;
import it.crs4.most.visualization.StreamInspectorFragment.IStreamProvider;
import it.crs4.most.visualization.augmentedreality.ARFragment;
import it.crs4.most.visualization.augmentedreality.MarkerFactory;
import it.crs4.most.visualization.augmentedreality.TouchGLSurfaceView;
import it.crs4.most.visualization.augmentedreality.mesh.Arrow;
import it.crs4.most.visualization.augmentedreality.mesh.CoordsConverter;
import it.crs4.most.visualization.augmentedreality.mesh.Mesh;
import it.crs4.most.visualization.augmentedreality.mesh.MeshManager;
import it.crs4.most.visualization.augmentedreality.mesh.Pyramid;
import it.crs4.most.visualization.augmentedreality.renderer.PubSubARRenderer;
import it.crs4.most.visualization.utils.zmq.ZMQPublisher;
import it.crs4.most.voip.VoipEventBundle;
import it.crs4.most.voip.VoipLib;
import it.crs4.most.voip.VoipLibBackend;
import it.crs4.most.voip.enums.CallState;
import it.crs4.most.voip.enums.VoipEvent;
import it.crs4.most.voip.enums.VoipEventType;


public class SpecTeleconsultationActivity extends AppCompatActivity implements
    IStreamFragmentCommandListener, IStreamProvider,
    ARFragment.OnCompleteListener, SurfaceHolder.Callback {

    private final static String TAG = "SpecTeleconsultActivity";

    public static final String TELECONSULTATION_ARG = "teleconsultation";
    public static final int TELECONSULT_ENDED_REQUEST = 1;

    private static final float DEFAULT_FRAME_SIZE = 0.5f;
    private static final float CAMERA_SMALL = 0.25f;
    private static final float ECO_LARGE = 0.75f;

    public final static int ZMQ_LISTENING_PORT = 5556;
    private String CAMERA_STREAM = "CAMERA_STREAM";
    private String ECO_STREAM = "ECO_STREAM";
    private String ECO_ARROW_ID = "ecoArrow";

    private Handler mEcoStreamHandler;
    private IStream mStreamCamera;
    private IStream mStreamEco;
    private ARFragment mStreamCameraFragment;
    private ARFragment mStreamEcoFragment;
    private FrameLayout mCameraFrame;
    private FrameLayout mEcoFrame;
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
    private MenuItem mCallMenuItem;
    private MenuItem mHangupMenuItem;
    private MenuItem mChangeEcoSizeMenuItem;
    private MenuItem mARToggle;
    private HashMap<String, String> mVoipParams;
    private RESTClient mRESTClient;
    private boolean mLocalHold = false;
    private boolean mAccountRegistered = false;
    private boolean mFirstCallStarted = false;
    private boolean mStreamCameraPrepared = false;
    private HashMap<String, Mesh> mMeshes = new HashMap<>();
    private MeshManager cameraMeshManager = new MeshManager();
    private MeshManager ecoMeshManager = new MeshManager();
    private PubSubARRenderer mARCameraRenderer;
    private PubSubARRenderer mAREcoRenderer;
    private Handler mCameraStreamHandler;
    private AudioManager mAudioManager;
    private int mOriginalAudioMode;
    private boolean arOnBoot = false;
    private ZMQPublisher publisher;
    private ARConfiguration arConf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mOriginalAudioMode = mAudioManager.getMode();
        Log.d(TAG, "Audio mode is: " + mOriginalAudioMode);

        String configServerIP = QuerySettings.getConfigServerAddress(this);
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(this));
        mRESTClient = new RESTClient(this, configServerIP, configServerPort);

        mEcoStreamHandler = new EcoStreamHandler(this);
        mCameraStreamHandler = new CameraStreamHandler(this);

        setContentView(R.layout.spec_teleconsultation_activity);
        setupTeleconsultationInfo();

        setTeleconsultationState(TeleconsultationState.IDLE);
        AssetHelper assetHelper = new AssetHelper(getAssets());
        assetHelper.cacheAssetFolder(this, "Data");


        arConf = mTeleconsultation.getLastSession().getRoom().getARConfiguration();
        if (arConf != null) {
            publisher = new ZMQPublisher(ZMQ_LISTENING_PORT);
            Thread pubThread = new Thread(publisher);
            pubThread.start();

            float[] redColor = new float[]{
                0, 0, 0, 1f,
                1, 0, 0, 1f,
                1, 0, 0, 1f,
                1, 0, 0, 1f,
                1, 0, 0, 1f
            };

            Arrow cameraArrow = new Arrow("arrow");
            Pyramid ecoArrow = new Pyramid(0.07f, 0.07f, 0.07f, ECO_ARROW_ID);
            ecoArrow.setCoordsConverter(new CoordsConverter(
                arConf.getScreenWidth() / 2, arConf.getScreenHeight() / 2, 1f));

            ecoArrow.setxLimits(-1f, 1f);
            ecoArrow.setyLimits(-1f, 1f - ecoArrow.getHeight() / 2);
            ecoArrow.setColors(redColor);
            cameraArrow.setMarker(MarkerFactory.getMarker(arConf.getEcoMarker().toString()));
            cameraMeshManager.addMesh(cameraArrow);
            ecoMeshManager.addMesh(ecoArrow);

            cameraArrow.publisher = publisher;
            ecoArrow.publisher = publisher;
            ecoMeshManager.configureScene();
        }

        mARCameraRenderer = new PubSubARRenderer(this, cameraMeshManager);
        mAREcoRenderer = new PubSubARRenderer(this, ecoMeshManager);
        mPTZPopupWindowController = new PTZ_ControllerPopupWindowFactory(this,
            new PTZHandler(this), true, true, true, 100, 100);

        setupStreamLib();
        setupVoipLib();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStreamCameraFragment.setPlayerButtonsVisible(false);
        mStreamEcoFragment.setPlayerButtonsVisible(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.teleconsultation_spec_menu, menu);
        boolean res = super.onCreateOptionsMenu(menu);
        mCallMenuItem = menu.findItem(R.id.button_call);
        mHangupMenuItem = menu.findItem(R.id.button_hangup);
        mChangeEcoSizeMenuItem = menu.findItem(R.id.change_eco_stream_size);

        mARToggle = menu.findItem(R.id.button_ar);
        if (arConf == null) {
            mARToggle.setVisible(false);
        }
        else {
            mARToggle.setChecked(arOnBoot);
        }
        return res;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_ptz:
                showPTZPopupWindow();
                break;
            case R.id.button_exit:
                endTeleconsultation();
                break;
            case R.id.button_call:
                handleButMakeCallClicked();
                break;
            case R.id.button_hangup:
                hangupCall();
                break;
            case R.id.change_eco_stream_size:
                changeEcoStreamSize();
                break;
            case R.id.button_ar:
                boolean isChecked = !item.isChecked();
                if (isChecked) {
                    item.setIcon(ContextCompat.getDrawable(this, android.R.drawable.checkbox_on_background));
                }
                else {
                    item.setIcon(ContextCompat.getDrawable(this, android.R.drawable.checkbox_off_background));
                }
                item.setChecked(isChecked);
                if (mStreamCameraFragment != null) mStreamCameraFragment.setEnabled(isChecked);
                if (mStreamEcoFragment != null) mStreamEcoFragment.setEnabled(isChecked);
                return true;
        }
        return false;
    }

    private void changeEcoStreamSize() {
        LinearLayout.LayoutParams cameraParams = (LinearLayout.LayoutParams) mCameraFrame.getLayoutParams();
        LinearLayout.LayoutParams ecoParams = (LinearLayout.LayoutParams) mEcoFrame.getLayoutParams();
        String title;
        if (cameraParams.weight == CAMERA_SMALL) {
            cameraParams.weight = DEFAULT_FRAME_SIZE;
            ecoParams.weight = DEFAULT_FRAME_SIZE;
            title = getString(R.string.increase_eco_stream);

        }
        else {
            cameraParams.weight = CAMERA_SMALL;
            ecoParams.weight = ECO_LARGE;
            title = getString(R.string.decrease_eco_stream);
        }

        mCameraFrame.setLayoutParams(cameraParams);
        mEcoFrame.setLayoutParams(ecoParams);
        mChangeEcoSizeMenuItem.setTitle(title);
    }

    private void setupTeleconsultationInfo() {
        Intent i = getIntent();
        mTeleconsultation = (Teleconsultation) i.getExtras().getSerializable(TELECONSULTATION_ARG);
//        TextView txtTeleconsultation = (TextView) findViewById(R.id.txtTeleconsultation);
//        txtTeleconsultation.setText(mTeleconsultation.getDescription());
    }

    private void setupStreamLib() {
        try {
            StreamingLib streamingLib = new StreamingLibBackend();
            streamingLib.initLib(getApplicationContext());

            Device camera = mTeleconsultation.getLastSession().getCamera();
            HashMap<String, String> streamCameraParams = new HashMap<>();
            streamCameraParams.put("name", CAMERA_STREAM);
            streamCameraParams.put("uri", camera.getStreamUri());
            mStreamCamera = streamingLib.createStream(streamCameraParams, mCameraStreamHandler);
            mCameraFrame = (FrameLayout) findViewById(R.id.container_stream_camera);

            mStreamCameraFragment = ARFragment.newInstance(mStreamCamera.getName());
            mStreamCameraFragment.setPlayerButtonsVisible(false);

            Device encoder = mTeleconsultation.getLastSession().getEncoder();
            HashMap<String, String> streamEcoParams = new HashMap<>();
            streamEcoParams.put("name", ECO_STREAM);
            streamEcoParams.put("uri", encoder.getStreamUri());

            mStreamEco = streamingLib.createStream(streamEcoParams, mEcoStreamHandler);
            mEcoFrame = (FrameLayout) findViewById(R.id.container_stream_eco);
            mStreamEcoFragment = ARFragment.newInstance(mStreamEco.getName());
            mStreamEcoFragment.setPlayerButtonsVisible(false);
            mStreamCameraFragment.setEnabled(arOnBoot);
            mStreamEcoFragment.setEnabled(false);

            mPTZManager = new PTZ_Manager(this,
                camera.getPtzUri(),
                camera.getUser(),
                camera.getPwd()
            );

            mStreamCameraFragment.setRenderer(mARCameraRenderer);
            mStreamCameraFragment.setStreamAR(mStreamCamera);
            mStreamCameraFragment.setGlSurfaceViewCallback(this);
            mStreamEcoFragment.setRenderer(mAREcoRenderer);

        }
        catch (Exception e) {
            Log.e(TAG, "Error creating streams");
            return;
        }
        mStreamCameraFragment.setGlSurfaceViewCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (arConf != null) {
                    mStreamCameraFragment.getGlView().setMeshManager(cameraMeshManager);
                    mStreamCameraFragment.getGlView().setPublisher(publisher);
                }
                mStreamCameraFragment.setPlayerButtonsVisible(false);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        mStreamEcoFragment.setGlSurfaceViewCallback(new SurfaceHolder.Callback2() {
            @Override
            public void surfaceRedrawNeeded(SurfaceHolder holder) {
                if (arConf != null) {
                    mStreamEcoFragment.getGlView().setMeshManager(ecoMeshManager);
                    mStreamEcoFragment.getGlView().setPublisher(publisher);
                }
                mStreamEcoFragment.setPlayerButtonsVisible(false);
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        // add the first fragment to the first container
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container_stream_camera, mStreamCameraFragment);
        fragmentTransaction.add(R.id.container_stream_eco, mStreamEcoFragment);
        fragmentTransaction.commit();
    }

    private void setupVoipLib() {
        mVoipParams = mTeleconsultation.getLastSession().getVoipParams();
        mSipServerIp = mVoipParams.get("sipServerIp");
        mSipServerPort = mVoipParams.get("sipServerPort");
        mEcoExtension = mVoipParams.get("ecoExtension");

        CallHandler voipHandler = new CallHandler(this);
        mVoipLib = new VoipLibBackend();
        mVoipLib.initLib(getApplicationContext(), mVoipParams, voipHandler);
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
//        mTextTcState.setTeleconsultationState(mTcState);

        try {
            if (mTcState == TeleconsultationState.IDLE) {
                mCallMenuItem.setTitle("Call");
                mCallMenuItem.setEnabled(false);
                mHangupMenuItem.setEnabled(false);
                mLocalHold = false;
                mAccountRegistered = false;
                pauseStreams();
            }
            else if (mTcState == TeleconsultationState.READY) {
                mCallMenuItem.setTitle("Call");
                mCallMenuItem.setEnabled(true);
                mHangupMenuItem.setEnabled(false);
                mLocalHold = false;
                mAccountRegistered = true;
                pauseStreams();

                if (mFirstCallStarted) {
//                    checkForSessionClosed();
                }
            }
            else if (mTcState == TeleconsultationState.CALLING) {
                mCallMenuItem.setTitle("Hold");
                mCallMenuItem.setEnabled(true);
                mHangupMenuItem.setEnabled(true);
                mLocalHold = false;
                mFirstCallStarted = true;
                playStreams();
                mStreamEcoFragment.setEnabled(mARToggle.isChecked());
            }
            else if (mTcState == TeleconsultationState.HOLDING) {
                mCallMenuItem.setTitle("Call");
                mCallMenuItem.setEnabled(true);
                mHangupMenuItem.setEnabled(true);
                mLocalHold = true;
                pauseStreams();
                mStreamEcoFragment.setEnabled(false);
            }
            else if (mTcState == TeleconsultationState.REMOTE_HOLDING) {
                mCallMenuItem.setEnabled(false);
                mHangupMenuItem.setEnabled(true);
                pauseStreams();
            }
        }
        catch (NullPointerException ne) {

        }
    }

    private void checkForSessionClosed() {
        final Timer t = new Timer();
        final String accessToken = QuerySettings.getAccessToken(this);
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                mRESTClient.getSessionState(mTeleconsultation.getLastSession().getId(),
                    accessToken,
                    new Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject res) {
                            Log.d(TAG, "Teleconsultation state response:" + res);
                            try {
                                String state = res.getJSONObject("data").getJSONObject("session").getString("state");
                                Log.d(TAG, "Teleconsultation state found:" + state);
                                if (state.equals(TeleconsultationSessionState.CLOSE.name())) {
                                    Log.d(TAG, "Closing session");
                                    t.cancel();
                                }
                            }
                            catch (JSONException e) {
                                Log.e(TAG, "Error retrieving session state:" + e);
                                e.printStackTrace();
                            }
                        }
                    },
                    new ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            Log.e(TAG, "Error reading Teleconsultation state response:" + arg0);
                        }
                    });
                // config.setTeleconsultation(selectedTc);
            }
        }, 0, 5000);
    }

    private void startReportActivity() {
        Intent i = new Intent(this, ReportActivity.class);
        startActivity(i);
        finish();
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
            mStreamCameraFragment.setStreamInvisible("PAUSED");
            mStreamCamera.pause();
        }

        if (mStreamEco != null && mStreamEco.getState() == StreamState.PLAYING) {
            mStreamEcoFragment.setStreamInvisible("PAUSED");
            mStreamEco.pause();
        }
    }

    private void stopStreams() {
        if (mStreamCamera != null) {
            mStreamCamera.destroy();
            mStreamCameraFragment.setStreamInvisible("");
        }

        if (mStreamEco != null) {
            mStreamEco.destroy();
            mStreamEcoFragment.setStreamInvisible("");
        }
    }

    private void endTeleconsultation() {
        if (mVoipLib.getCall().getState().equals(CallState.ACTIVE)) {
            hangupCall();
        }
        else {
            mVoipLib.destroyLib();
        }
    }

    @Override
    public void onPlay(String streamId) {
        // It is necessary to set the volume control here because Gstreamer override it
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    @Override
    public void onPause(String streamId) {}

    @Override
    public void onSurfaceViewCreated(String streamId, SurfaceView surfaceView) {
        if (surfaceView != null) {
            if (streamId.equals(CAMERA_STREAM)) {
                if (!mStreamCameraPrepared) {
                    mStreamCamera.prepare(surfaceView, true);
//                mStreamCameraFragment.setStreamAR(mStreamCamera);
//                mStreamCameraFragment.setRenderer(mARCameraRenderer);
                    if (arConf != null) {
                        mStreamCameraFragment.prepareRemoteAR();
                        mStreamCameraPrepared = true;
                    }
                }
            }
            else if (streamId.equals(ECO_STREAM)) {
                mStreamEco.prepare(surfaceView);
                if (arConf != null) {
                    TouchGLSurfaceView glView = mStreamEcoFragment.getGlView();
                    glView.setZOrderMediaOverlay(true);
                    glView.setMoveNormFactor(300f);
                }
            }
        }
    }

    @Override
    public void onSurfaceViewDestroyed(String streamId) {
        if (streamId.equals(CAMERA_STREAM)) {
            mStreamCamera.destroy();
        }
        else if (streamId.equals(ECO_STREAM)) {
            mStreamEco.destroy();
        }
    }

    @Override
    public List<IStream> getStreams() {
        List<IStream> streams = new ArrayList<>();
        streams.add(mStreamCamera);
        streams.add(mStreamEco);
        return streams;
    }

    @Override
    public List<StreamProperty> getStreamProperties() {
        ArrayList<StreamProperty> streamProps = new ArrayList<>();
        streamProps.add(StreamProperty.NAME);
        streamProps.add(StreamProperty.STATE);
        return streamProps;
    }

    // VOIP METHODS AND LOGIC
    private void handleButMakeCallClicked() {
        if (mTcState == TeleconsultationState.READY) {
            mVoipLib.makeCall(mEcoExtension);
        }
        else if (mTcState == TeleconsultationState.CALLING) {
            mVoipLib.holdCall();
        }
        else if (mTcState == TeleconsultationState.HOLDING) {
            mVoipLib.unholdCall();
        }
    }

    private void hangupCall() {
        mVoipLib.hangupCall();
    }

    private void subscribeBuddies() {
        String buddyExtension = mVoipParams.get("ecoExtension");
        Log.d(TAG, "adding buddies: " + getBuddyUri(buddyExtension));
        mVoipLib.getAccount().addBuddy(getBuddyUri(buddyExtension));
    }

    private String getBuddyUri(String extension) {
        return "sip:" + extension + "@" + mSipServerIp + ":" + mSipServerPort;
    }

    private void registerAccount() {
        mVoipLib.registerAccount();
    }

    @Override
    public void onFragmentCreate() {}

    @Override
    public void onFragmentResume() {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mStreamCameraFragment.getGlView().setMeshManager(cameraMeshManager);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    private static class CallHandler extends Handler {

        private final WeakReference<SpecTeleconsultationActivity> mOuterRef;

        private CallHandler(SpecTeleconsultationActivity outerRef) {
            mOuterRef = new WeakReference<>(outerRef);
        }

        @Override
        public void handleMessage(Message msg) {
            VoipEventBundle eventBundle = (VoipEventBundle) msg.obj;
            VoipEvent event = eventBundle.getEvent();
            SpecTeleconsultationActivity act = mOuterRef.get();
            Log.d(TAG, "Received VOIP event: " + eventBundle.getEvent() + " of type:" + eventBundle.getEventType());

            // Register the account after the Lib Initialization
            if (event == VoipEvent.LIB_INITIALIZED) {
                act.registerAccount();
            }
            else if (event == VoipEvent.ACCOUNT_REGISTERED) {
                if (!act.mAccountRegistered) {
                    act.subscribeBuddies();
                    act.mAccountRegistered = true;
                }
            }
            else if (event == VoipEvent.ACCOUNT_UNREGISTERED) {
                act.setTeleconsultationState(TeleconsultationState.IDLE);
            }
            else if (eventBundle.getEventType() == VoipEventType.BUDDY_EVENT) {
                // There is only one subscribed buddy in this app, so we don't need to get IBuddy informations
                if (event == VoipEvent.BUDDY_CONNECTED) {
                    // Probably the first condition treat cases of lost network
                    if (act.mTcState == TeleconsultationState.REMOTE_HOLDING ||
                        act.mTcState == TeleconsultationState.HOLDING) {
                        if (act.mLocalHold) {
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
                    if (act.mVoipLib.getCall().getState() == CallState.ACTIVE ||
                        act.mVoipLib.getCall().getState() == CallState.HOLDING) {
                        act.setTeleconsultationState(TeleconsultationState.REMOTE_HOLDING);
                    }
                }
                else if (event == VoipEvent.BUDDY_DISCONNECTED) {
                    act.setTeleconsultationState(TeleconsultationState.IDLE);
                }
            }

            //else if (myevent==VoipEvent.CALL_INCOMING)

            else if (event == VoipEvent.CALL_READY) {

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
                act.stopStreams();
//                act.startReportActivity();
                act.setResult(RESULT_OK);
                act.finish();
            }
            else if (event == VoipEvent.LIB_DEINITIALIZATION_FAILED) {
                //TODO: check what to do
            }
            else if (event == VoipEvent.LIB_INITIALIZATION_FAILED || event == VoipEvent.ACCOUNT_REGISTRATION_FAILED ||
                event == VoipEvent.LIB_CONNECTION_FAILED || event == VoipEvent.BUDDY_SUBSCRIPTION_FAILED) {
                showErrorEventAlert(eventBundle);
            }

        } // end of handleMessage()

        private void showErrorEventAlert(VoipEventBundle eventBundle) {
            AlertDialog.Builder miaAlert = new AlertDialog.Builder(mOuterRef.get());
            miaAlert.setTitle(eventBundle.getEventType() + ":" + eventBundle.getEvent());
            miaAlert.setMessage(eventBundle.getInfo());
            AlertDialog alert = miaAlert.create();
            alert.show();
        }
    }

    private static class EcoStreamHandler extends Handler {
        private final WeakReference<SpecTeleconsultationActivity> mOuterRef;
        private int width;
        private int height;

        private EcoStreamHandler(SpecTeleconsultationActivity outerRef) {
            mOuterRef = new WeakReference<>(outerRef);
        }

        @Override
        public void handleMessage(Message streamingMessage) {
            StreamingEventBundle event = (StreamingEventBundle) streamingMessage.obj;
            String infoMsg = "Event Type:" + event.getEventType() + " ->" + event.getEvent() + ":" + event.getInfo();
            Log.d(TAG, "handleMessage: Current Event:" + infoMsg);
            SpecTeleconsultationActivity act = mOuterRef.get();

            StreamState streamState = ((IStream) event.getData()).getState();
            Log.d(TAG, "event.getData().streamState " + streamState);
            if (event.getEventType() == StreamingEventType.STREAM_EVENT &&
                event.getEvent() == StreamingEvent.STREAM_STATE_CHANGED) {

                Log.d(TAG, "event.getData().streamState " + streamState);
                Log.d(TAG, "ready to call cameraPreviewStarted");

                Size videoSize = ((IStream) event.getData()).getVideoSize();
                if (videoSize != null) {
                    width = videoSize.getWidth();
                    height = videoSize.getHeight();
                    act.mAREcoRenderer.setViewportAspectRatio(Float.valueOf(width) / height);
                }

            }
        }
    }

    private static class CameraStreamHandler extends Handler {
        private static final String TAG = "CameraStreamHandler";
        private final WeakReference<SpecTeleconsultationActivity> mOuterRef;

        private CameraStreamHandler(SpecTeleconsultationActivity outerRef) {
            mOuterRef = new WeakReference<>(outerRef);
        }

        @Override
        public void handleMessage(Message streamingMessage) {
            StreamingEventBundle event = (StreamingEventBundle) streamingMessage.obj;
            String infoMsg = "Event Type:" + event.getEventType() + " ->" + event.getEvent() + ":" + event.getInfo();
            Log.d(TAG, "handleMessage: Current Event:" + infoMsg);
            SpecTeleconsultationActivity act = mOuterRef.get();

            StreamState streamState = ((IStream) event.getData()).getState();
            Log.d(TAG, "event.getData().streamState " + streamState);
            if (event.getEventType() == StreamingEventType.STREAM_EVENT &&
                event.getEvent() == StreamingEvent.STREAM_STATE_CHANGED) {
//                    if (streamState == StreamState.PLAYING) {

                Log.d(TAG, "event.getData().streamState " + streamState);
                Log.d(TAG, "ready to call cameraPreviewStarted");

                Size videoSize = ((IStream) event.getData()).getVideoSize();

                int width, height;
                if (videoSize != null) {
                    width = videoSize.getWidth();
                    height = videoSize.getHeight();
                }
                else { //FIXME
                    width = 704;
                    height = 576;


                }
                Log.d(TAG, "width " + width);
                Log.d(TAG, "height " + height);
                act.mStreamCameraFragment.setFixedSize(new int[]{width, height});
                act.mStreamCameraFragment.cameraPreviewStarted(width, height, 25, 0, false);


//                    }
            }
        }
    }

    private static class PTZHandler implements IPtzCommandReceiver {
        WeakReference<SpecTeleconsultationActivity> mOuterRef;

        PTZHandler(SpecTeleconsultationActivity activity) {
            mOuterRef = new WeakReference<>(activity);
        }

        @Override
        public void onPTZstartMove(PTZ_Direction dir) {
            mOuterRef.get().mPTZManager.startMove(dir);
        }

        @Override
        public void onPTZstopMove(PTZ_Direction dir) {
            mOuterRef.get().mPTZManager.stopMove();
        }

        @Override
        public void onPTZstartZoom(PTZ_Zoom dir) {
            mOuterRef.get().mPTZManager.startZoom(dir);
        }

        @Override
        public void onPTZstopZoom(PTZ_Zoom dir) {
            mOuterRef.get().mPTZManager.stopZoom();
        }

        @Override
        public void onGoHome() {
            mOuterRef.get().mPTZManager.goTo("home");
        }

        @Override
        public void onSnapshot() {
            IBitmapReceiver receiver = new IBitmapReceiver() {
                @Override
                public void onBitmapSaved(ImageDownloader imageDownloader, String filename) {
                    Toast.makeText(mOuterRef.get(), "Image saved:" + filename, Toast.LENGTH_LONG).show();
                    imageDownloader.logAppFileNames();
                }

                @Override
                public void onBitmapDownloaded(ImageDownloader imageDownloader, Bitmap image) {
                    imageDownloader.saveImageToInternalStorage(image, "test_image__" + String.valueOf(System.currentTimeMillis()));
                }

                @Override
                public void onBitmapDownloadingError(ImageDownloader imageDownloader, Exception ex) {
                    Toast.makeText(mOuterRef.get(), "Error downloading Image:" + ex.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onBitmapSavingError(ImageDownloader imageDownloader, Exception ex) {
                    Toast.makeText(mOuterRef.get(), "Error saving Image:" + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            };

            Device camera = mOuterRef.get().mTeleconsultation.getLastSession().getCamera();
            ImageDownloader imageDownloader = new ImageDownloader(receiver, mOuterRef.get(),
                camera.getUser(), // uriProps.getProperty("username_ptz"),
                camera.getPwd()); // uriProps.getProperty("password_ptz"));

            imageDownloader.downloadImage(camera.getShotUri()); // uriProps.getProperty("uri_still_image"));
        }
    }
}
