package it.crs4.most.demo.spec;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;

import org.artoolkit.ar.base.assets.AssetHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import it.crs4.most.demo.BaseTeleconsultationActivity;
import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RESTClient;
import it.crs4.most.demo.ReportActivity;
import it.crs4.most.demo.TeleconsultationState;
import it.crs4.most.demo.models.ARConfiguration;
import it.crs4.most.demo.models.Device;
import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.models.TeleconsultationSessionState;
import it.crs4.most.demo.models.User;
import it.crs4.most.demo.spec.VirtualKeyboard.KeyboardCoordinatesStore;
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
import it.crs4.most.streaming.ptz.PTZ_Manager;
import it.crs4.most.streaming.utils.ImageDownloader;
import it.crs4.most.streaming.utils.ImageDownloader.IBitmapReceiver;
import it.crs4.most.streaming.utils.Size;
import it.crs4.most.visualization.IPtzCommandReceiver;
import it.crs4.most.visualization.IStreamFragmentCommandListener;
import it.crs4.most.visualization.PTZ_ControllerPopupWindowFactory;
import it.crs4.most.visualization.StreamInspectorFragment.IStreamProvider;
import it.crs4.most.visualization.augmentedreality.ARFragment;
import it.crs4.most.visualization.augmentedreality.TouchGLSurfaceView;
import it.crs4.most.visualization.augmentedreality.mesh.CoordsConverter;
import it.crs4.most.visualization.augmentedreality.mesh.Line;
import it.crs4.most.visualization.augmentedreality.mesh.Mesh;
import it.crs4.most.visualization.augmentedreality.mesh.MeshManager;
import it.crs4.most.visualization.augmentedreality.mesh.Pyramid;
import it.crs4.most.visualization.augmentedreality.renderer.PubSubARRenderer;
import it.crs4.most.visualization.sensors.ECGView;
import it.crs4.most.visualization.utils.zmq.ECGSubscriber;
import it.crs4.most.visualization.utils.zmq.ZMQPublisher;
import it.crs4.most.voip.VoipEventBundle;
import it.crs4.most.voip.enums.CallState;
import it.crs4.most.voip.enums.VoipEvent;
import it.crs4.most.voip.enums.VoipEventType;


public class SpecTeleconsultationActivity extends BaseTeleconsultationActivity implements
    IStreamFragmentCommandListener, IStreamProvider,
    ARFragment.OnCompleteListener, SurfaceHolder.Callback, ARFragment.ARListener {

    private final static String TAG = "SpecTeleconsultActivity";

    public static final int TELECONSULT_ENDED_REQUEST = 1;
    public final static int ZMQ_LISTENING_PORT = 5556;

    private static final float DEFAULT_FRAME_SIZE = 0.5f;
    private static final float CAMERA_SMALL = 0.25f;
    private static final float ECO_LARGE = 0.75f;

    private String CAMERA_STREAM = "CAMERA_STREAM";
    private String ECO_STREAM = "ECO_STREAM";
    private String ECO_ARROW_ID = "ecoArrow";
    private String CAMER_ARROW_ID = "cameraArrow";

    private Handler mEcoStreamHandler;
    private IStream mStreamCamera;
    private IStream mStreamEco;
    private ARFragment mStreamCameraFragment;
    private ARFragment mStreamEcoFragment;
    private FrameLayout mCameraFrame;
    private FrameLayout mEcoFrame;
    private TcStateTextView mTextTcState;
    private PTZ_Manager mPTZManager;
    private PTZ_ControllerPopupWindowFactory mPTZPopupWindowController;
    private String mEcoExtension;
    private MenuItem mCallMenuItem;
    private MenuItem mHangupMenuItem;
    private MenuItem mChangeEcoSizeMenuItem;
    private MenuItem mARToggle;

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

    private boolean arOnBoot = false;
    private ZMQPublisher mARPublisher;
    private ARConfiguration arConf;
    private Button resetCameraMesh;
    private Button resetEcoMesh;
    private Button saveKeyCoordinate;
    private User user;
    private ECGView mEcgView;
    private ECGSubscriber mEcgSubscriber;
    private LinearLayout mStreamLayout;
    private String mSensorsServer;
    private int maxECGData = 400;
    private float period = 0.02f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File cacheFolder = new File(getCacheDir().getAbsolutePath() + "/Data");
        File[] files = cacheFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        AssetHelper assetHelper = new AssetHelper(getAssets());
        assetHelper.cacheAssetFolder(this, "Data");

        String configServerIP = QuerySettings.getConfigServerAddress(this);
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(this));
        mRESTClient = new RESTClient(this, configServerIP, configServerPort);

        mEcoStreamHandler = new EcoStreamHandler(this, ECO_STREAM);
        mCameraStreamHandler = new CameraStreamHandler(this, CAMERA_STREAM);

        setContentView(R.layout.spec_teleconsultation_activity);

        arConf = teleconsultation.getLastSession().getRoom().getARConfiguration();
        if (arConf != null) {
            mARPublisher = new ZMQPublisher(ZMQ_LISTENING_PORT);
            Thread pubThread = new Thread(mARPublisher);
            pubThread.start();

            float[] redColor = new float[] {
                0, 0, 0, 1f,
                1, 0, 0, 1f,
                1, 0, 0, 1f,
                1, 0, 0, 1f,
                1, 0, 0, 1f
            };
            Pyramid ecoArrow = new Pyramid(0.07f, 0.07f, 0.07f, ECO_ARROW_ID);
            ecoArrow.setCoordsConverter(new CoordsConverter(
                arConf.getScreenWidth() / 2, arConf.getScreenHeight() / 2, 1f));

            ecoArrow.setxLimits(-1f + ecoArrow.getHeight() / 2, 1f - ecoArrow.getHeight() / 2);
            ecoArrow.setyLimits(-1f + ecoArrow.getHeight() / 2, 1f - ecoArrow.getHeight() / 2);
            ecoArrow.setColors(redColor);
            ecoMeshManager.addMesh(ecoArrow);
            ecoArrow.publisher = mARPublisher;

            Intent i = getIntent();
            teleconsultation = (Teleconsultation) i.getExtras().getSerializable(TELECONSULTATION_ARG);
            createARMeshes(cameraMeshManager);
            for (Mesh mesh : cameraMeshManager.getMeshes()) {
                mesh.publisher = mARPublisher;
            }
            ecoMeshManager.configureScene();
        }

        mARCameraRenderer = new PubSubARRenderer(this, cameraMeshManager);
        mARCameraRenderer.setLowFilterLevel(QuerySettings.getARLowFilterLevel(this));
        mAREcoRenderer = new PubSubARRenderer(this, ecoMeshManager);
        mPTZPopupWindowController = new PTZ_ControllerPopupWindowFactory(this,
            new PTZHandler(this), true, true, true, 100, 100);
        mStreamLayout = (LinearLayout) findViewById(R.id.stream_layout);

        user = QuerySettings.getUser(this);
        if (user != null && user.isAdmin()) {
            ARConfigurationFragment arConfigurationFragment = ARConfigurationFragment.
                newInstance(mARPublisher, teleconsultation.getLastSession().getRoom());

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.ar_conf_fragment, arConfigurationFragment);
            fragmentTransaction.commit();

            Line line;
            float[] whiteColor = new float[] {1.0F, 1F, 1F, 1.0F};
            float thick = 1f;

            for (int i = -3; i <= 3; i++) {
                line = new Line(
                    new float[] {i * 0.25f, -1, 0},
                    new float[] {i * 0.25f, 1, 0},
                    thick);
                line.setColors(whiteColor);
                ecoMeshManager.addMesh(line);

                line = new Line(
                    new float[] {-1, i * 0.25f, 0},
                    new float[] {1, i * 0.25f, 0},
                    thick);
                line.setColors(whiteColor);
                ecoMeshManager.addMesh(line);
            }
        }
        setupStreamLib();

        resetCameraMesh = (Button) findViewById(R.id.reset_camera_mesh_position);
        resetEcoMesh = (Button) findViewById(R.id.reset_eco_mesh_position);
        saveKeyCoordinate = (Button) findViewById(R.id.save_ar_key_coordinate);

        setupECGFrame();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStreamCameraFragment.setPlayerButtonsVisible(false);
        mStreamEcoFragment.setPlayerButtonsVisible(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mARPublisher.close();
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
                if (mStreamCameraFragment != null) {
                    mStreamCameraFragment.setEnabled(isChecked);
                }
                if (mStreamEcoFragment != null) {
                    mStreamEcoFragment.setEnabled(isChecked);
                }
                return true;
            case R.id.button_camera:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    item.setIcon(ContextCompat.getDrawable(this, android.R.drawable.checkbox_on_background));
                    playCamera();
                }
                else {
                    item.setIcon(ContextCompat.getDrawable(this, android.R.drawable.checkbox_off_background));
                    pauseCamera();
                }
            case R.id.button_ecg:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    item.setIcon(ContextCompat.getDrawable(this, android.R.drawable.checkbox_on_background));
                }
                else {
                    item.setIcon(ContextCompat.getDrawable(this, android.R.drawable.checkbox_off_background));
                }
                showECG(item.isChecked());
        }
        return false;
    }

    private void showECG(boolean show) {
        LinearLayout.LayoutParams streamLayoutParams = (LinearLayout.LayoutParams)
            mStreamLayout.getLayoutParams();

        if (show) {
            mEcgView.resetData();
            mEcgView.setVisibility(View.VISIBLE);
            streamLayoutParams.weight = 0.9f;
            mEcgSubscriber.startReceiving();
        }
        else {
            mEcgView.setVisibility(View.GONE);
            mEcgSubscriber.stopReceiving();
            streamLayoutParams.weight = 1f;
        }
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

    private void setupStreamLib() {
        try {
            StreamingLib streamingLib = new StreamingLibBackend();
            streamingLib.initLib(getApplicationContext());

            Device camera;
            String ecoAppAddress = teleconsultation.getLastSession().getEcoAppAddress();
            if (ecoAppAddress != null && ecoAppAddress.equals("")) {
                camera = teleconsultation.getLastSession().getCamera();
            }
            else {
                String streamUri = "rtsp://" + ecoAppAddress + ":8554/test";
                camera = new Device("Eco Camera", streamUri, "", "", "", "", "");
            }
            HashMap<String, String> streamCameraParams = new HashMap<>();
            streamCameraParams.put("name", CAMERA_STREAM);
            streamCameraParams.put("uri", camera.getStreamUri());
            mStreamCamera = streamingLib.createStream(streamCameraParams, mCameraStreamHandler);
            mCameraFrame = (FrameLayout) findViewById(R.id.container_stream_camera);

            mStreamCameraFragment = ARFragment.newInstance(mStreamCamera.getName());
            mStreamCameraFragment.setPlayerButtonsVisible(false);
            mStreamCameraFragment.setDeviceID("EPSON/embt2/embt2");

            Device encoder = teleconsultation.getLastSession().getEncoder();
            HashMap<String, String> streamEcoParams = new HashMap<>();
            streamEcoParams.put("name", ECO_STREAM);
            streamEcoParams.put("uri", encoder.getStreamUri());

            mStreamEco = streamingLib.createStream(streamEcoParams, mEcoStreamHandler);
            mEcoFrame = (FrameLayout) findViewById(R.id.container_stream_eco);
            mStreamEcoFragment = ARFragment.newInstance(mStreamEco.getName());
            mStreamEcoFragment.setPlayerButtonsVisible(false);
            mStreamCameraFragment.setEnabled(arOnBoot);
            mStreamEcoFragment.setEnabled(false);

            mStreamCameraFragment.setArListener(this);

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
                TouchGLSurfaceView glView = mStreamCameraFragment.getGlView();
                if (arConf != null) {
                    glView.setMeshManager(cameraMeshManager);
                    glView.setPublisher(mARPublisher);
                }
                else {
                    glView.setEnabled(false);
                }
                mStreamCameraFragment.setPlayerButtonsVisible(false);
                resetCameraMesh.setOnClickListener(new ResetButtonListener(cameraMeshManager, glView));

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
                TouchGLSurfaceView glView = mStreamEcoFragment.getGlView();
                if (arConf != null) {
                    glView.setMeshManager(ecoMeshManager);
                    glView.setPublisher(mARPublisher);
                }
                else {
                    glView.setEnabled(false);
                }
                mStreamEcoFragment.setPlayerButtonsVisible(false);
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                TouchGLSurfaceView glView = mStreamEcoFragment.getGlView();
                resetEcoMesh.setOnClickListener(new ResetButtonListener(ecoMeshManager, glView));
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

    protected void setupVoipLib() {
        super.setupVoipLib();
        mEcoExtension = mVoipParams.get("ecoExtension");
    }

    protected Handler getVoipHandler() {
        return new CallHandler(this);
    }

    protected void setupECGFrame() {
        mSensorsServer = teleconsultation.getLastSession().getRoom().getSensorsServer();
        if (!mSensorsServer.equals("")) {
            mEcgSubscriber = new ECGSubscriber("tcp", mSensorsServer, "5556", "ECG");
            mEcgSubscriber.start();
            mEcgSubscriber.getLooper();
            mEcgSubscriber.prepareResponseHandler();
            mEcgView = (ECGView) findViewById(R.id.ecg_graph);
            if (mEcgView != null) {
                mEcgView.setSubscriber(mEcgSubscriber);
                mEcgView.setMaxData(maxECGData);
                GridLabelRenderer gridLabelRenderer = mEcgView.getGridLabelRenderer();
                StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(mEcgView);

                String[] yLabels = new String[] {"0", "1000", "2000", "3000", "4000", "5000"};
                staticLabelsFormatter.setVerticalLabels(yLabels);

                int numOfLabels = Math.round(maxECGData * period) + 1;
                String[] xLabels = new String[numOfLabels];
                xLabels[0] = "0";
                for (int i = 1; i < numOfLabels; i++) {
                    DecimalFormat df = new DecimalFormat("#.#");
                    df.setRoundingMode(RoundingMode.CEILING);
                    xLabels[i] = df.format(period * 10 * (i));
                    Log.d(TAG, "Label: " + xLabels[i]);
                }
                staticLabelsFormatter.setHorizontalLabels(xLabels);

                gridLabelRenderer.setLabelFormatter(staticLabelsFormatter);
                gridLabelRenderer.setVerticalAxisTitle("mV");
                gridLabelRenderer.setHorizontalAxisTitle("s");
            }
        }
    }

    private void showPTZPopupWindow() {
        if (!mPTZPopupWindowController.isShowing()) {
            mPTZPopupWindowController.show();
        }
        else {
            mPTZPopupWindowController.dismiss();

        }
    }

    protected void notifyTeleconsultationStateChanged() {
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
                mRESTClient.getSessionState(teleconsultation.getLastSession().getId(),
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

    private void playCamera() {
        if (mStreamCamera != null && mStreamCamera.getState() != StreamState.PLAYING) {
            mStreamCameraFragment.setStreamVisible();
            mStreamCamera.play();
        }
    }

    private void playStreams() {
        playCamera();

        if (mStreamEco != null && mStreamEco.getState() != StreamState.PLAYING) {
            mStreamEcoFragment.setStreamVisible();
            mStreamEco.play();
        }
    }

    private void pauseCamera() {
        if (mStreamCamera != null && mStreamCamera.getState() == StreamState.PLAYING) {
            mStreamCameraFragment.setStreamInvisible("PAUSED");
            mStreamCamera.pause();
        }
    }

    private void pauseStreams() {
        pauseCamera();

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
    public void onPause(String streamId) {
    }

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
    public void onFragmentCreate() {
    }

    @Override
    public void onFragmentResume() {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mStreamCameraFragment.getGlView().setMeshManager(cameraMeshManager);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void ARInitialized() {
        cameraMeshManager.configureScene();
        //            KeyboardCoordinatesStore keyboardCoordinatesStore = new TXTKeyboardCoordinatesStore(assetManager.open(assetName));
        KeyboardCoordinatesStore keyboardCoordinatesStore = new RESTKeyboardCoordinatesStore(
            teleconsultation.getLastSession().getRoom(), mRESTClient, QuerySettings.getAccessToken(this)
        );
        Map<String, float[]> keymap = keyboardCoordinatesStore.read();
        Set<String> keys = keymap.keySet();

        List<Mesh> keyboardMeshes = cameraMeshManager.getMeshesByGroup("keyboard");
        if (keyboardMeshes.size() > 0) {
            VirtualKeyboard virtualKeyboard = new VirtualKeyboard(
                new SpinnerKeyboardViewer(
                    this,
                    (Spinner) findViewById(R.id.virtual_keyboard_spinner),
                    keymap.keySet().toArray(new String[keys.size()])
                ),
                keyboardCoordinatesStore,
                keyboardMeshes.get(0)
            );

            if (user.isAdmin()) {
                saveKeyCoordinate.setVisibility(View.VISIBLE);
                virtualKeyboard.setSaveButton(saveKeyCoordinate);
            }
        }
    }

    @Override
    public void ARStopped() {

    }

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

    private static abstract class StreamHandler extends Handler {
        private final WeakReference<SpecTeleconsultationActivity> activity;
        private String streamName;

        public StreamHandler(SpecTeleconsultationActivity activity, String streamName) {
            this.activity = new WeakReference<>(activity);
            this.streamName = streamName;
        }

        public SpecTeleconsultationActivity getActivity() {
            return activity.get();
        }

        public String getStreamName() {
            return streamName;
        }

        @Override
        public void handleMessage(Message streamingMessage) {
            StreamingEventBundle event = (StreamingEventBundle) streamingMessage.obj;
            String infoMsg = "Event Type:" + event.getEventType() + " ->" + event.getEvent() + ":" + event.getInfo();
            Log.d(TAG, "handleMessage: Current Event:" + infoMsg);


            StreamState streamState = ((IStream) event.getData()).getState();
            Log.d(TAG, "event.getData().streamState " + streamState);
            if (event.getEvent() == StreamingEvent.VIDEO_SIZE_CHANGED) {

                Log.d(TAG, "event.getData().streamState " + streamState);
                Log.d(TAG, "ready to call cameraPreviewStarted");

                Size videoSize = ((IStream) event.getData()).getVideoSize();
                if (((IStream) event.getData()).getName().equals(streamName) && videoSize != null) {
                    int width = videoSize.getWidth();
                    int height = videoSize.getHeight();
                    Log.d(TAG, String.format("VIDEOSIZE width %s, height %d", width, height));
                    onVideoSizeChanged(width, height);
//                    act.mAREcoRenderer.setViewportSize(width, height);

                }

            }
        }

        public abstract void onVideoSizeChanged(int width, int height);
    }

    private static class EcoStreamHandler extends StreamHandler {
        public EcoStreamHandler(SpecTeleconsultationActivity activity, String streamName) {
            super(activity, streamName);
        }

        @Override
        public void onVideoSizeChanged(int width, int height) {
            getActivity().mAREcoRenderer.setViewportSize(width, height);
        }
    }

    private static class CameraStreamHandler extends StreamHandler {
        public CameraStreamHandler(SpecTeleconsultationActivity activity, String streamName) {
            super(activity, streamName);
        }

        @Override
        public void onVideoSizeChanged(int width, int height) {
            Log.d(TAG, String.format("onVideoSizeChanged width %s, height %s", width, height));
            getActivity().mARCameraRenderer.setViewportSize(width, height);
            getActivity().mStreamCameraFragment.cameraPreviewStarted(width, height, 25, 0, false);
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

            Device camera = mOuterRef.get().teleconsultation.getLastSession().getCamera();
            ImageDownloader imageDownloader = new ImageDownloader(receiver, mOuterRef.get(),
                camera.getUser(), // uriProps.getProperty("username_ptz"),
                camera.getPwd()); // uriProps.getProperty("password_ptz"));

            imageDownloader.downloadImage(camera.getShotUri()); // uriProps.getProperty("uri_still_image"));
        }
    }

    private static class ResetButtonListener implements View.OnClickListener {

        private MeshManager meshManager;
        private GLSurfaceView view;

        public ResetButtonListener(MeshManager meshManager, GLSurfaceView view) {
            this.meshManager = meshManager;
            this.view = view;
        }

        @Override
        public void onClick(View v) {
            for (Mesh m : meshManager.getMeshes()) {
                m.setX(0);
                m.setY(0);
                m.setZ(0);
            }
            view.requestRender();
        }
    }
}
