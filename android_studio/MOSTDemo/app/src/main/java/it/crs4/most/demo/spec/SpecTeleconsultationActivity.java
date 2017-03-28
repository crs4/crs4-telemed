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
import it.crs4.most.streaming.IEventListener;
import it.crs4.most.streaming.IStream;
import it.crs4.most.streaming.StreamingLib;
import it.crs4.most.streaming.StreamingLibBackend;
import it.crs4.most.streaming.enums.PTZ_Direction;
import it.crs4.most.streaming.enums.PTZ_Zoom;
import it.crs4.most.streaming.enums.StreamProperty;
import it.crs4.most.streaming.enums.StreamState;
import it.crs4.most.streaming.ptz.PTZ_Manager;
import it.crs4.most.streaming.utils.ImageDownloader;
import it.crs4.most.streaming.utils.ImageDownloader.IBitmapReceiver;
import it.crs4.most.visualization.IPtzCommandReceiver;
import it.crs4.most.visualization.IStreamFragmentCommandListener;
import it.crs4.most.visualization.PTZ_ControllerPopupWindowFactory;
import it.crs4.most.visualization.StreamInspectorFragment.IStreamProvider;
import it.crs4.most.visualization.augmentedreality.ARFragment;
import it.crs4.most.visualization.augmentedreality.TouchGLSurfaceView;
import it.crs4.most.visualization.augmentedreality.mesh.Circle;
import it.crs4.most.visualization.augmentedreality.mesh.CoordsConverter;
import it.crs4.most.visualization.augmentedreality.mesh.Mesh;
import it.crs4.most.visualization.augmentedreality.mesh.MeshManager;
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

    private final String CAMERA_STREAM = "CAMERA_STREAM";
    private final String ON_BOARD_CAMERA_STREAM = "ON_BOARD_CAMERA_STREAM";
    private String currentCameraStream = ON_BOARD_CAMERA_STREAM;
    private String ECO_STREAM = "ECO_STREAM";
    private String ECO_ARROW_ID = "ecoArrow";
    private String CAMER_ARROW_ID = "cameraArrow";

    private Handler mEcoStreamHandler;
    private Handler mOnBoardCameraStreamHandler;
    private IStream mStreamCamera;
    private IStream mStreamOnBoardCamera;
    private IStream mStreamEco;
    private ARFragment mStreamCameraFragment;
    private ARFragment mStreamOnBoardCameraFragment;
    private ARFragment mStreamEcoFragment;
    private LinearLayout mCamerasFrame;
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
    private boolean mStreamOnBoardCameraPrepared = false;
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
    private LinearLayout mEcgContainer;
    private LinearLayout mStreamLayout;
    private String mSensorsServer;
    private int maxECGData = 400;
    private float period = 0.02f;
    Circle ecoArrow;
    private float viewportAspectRatio;
    private boolean firstSwitch = true;
    private int switchPhase = 0;
    FrameLayout onBoardCameraFrameLayout;
    FrameLayout cameraFrameLayout;

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
        setContentView(R.layout.spec_teleconsultation_activity);

        arConf = teleconsultation.getLastSession().getRoom().getARConfiguration();
        if (arConf != null) {
            mARPublisher = new ZMQPublisher(ZMQ_LISTENING_PORT);
            Thread pubThread = new Thread(mARPublisher);
            pubThread.start();

//          Populating with eco meshes
            createARMeshes(
                    ecoMeshManager,
                    "eco",
                    new float[] {
                            2f/arConf.getScreenHeight(),
                            2f/arConf.getScreenHeight(),
                            1
                    }
            );
            for (Mesh mesh: ecoMeshManager.getMeshes()) {
                mesh.publisher = mARPublisher;
                mesh.removeAllMarkers();
            }


            Intent i = getIntent();
            teleconsultation = (Teleconsultation) i.getExtras().getSerializable(TELECONSULTATION_ARG);
            createARMeshes(cameraMeshManager, "keyboard");
            for (Mesh mesh : cameraMeshManager.getMeshes()) {
                mesh.publisher = mARPublisher;
            }
//            ecoMeshManager.configureScene();
        }

        mARCameraRenderer = new PubSubARRenderer(this, cameraMeshManager);
        mARCameraRenderer.setLowFilterLevel(QuerySettings.getARLowFilterLevel(this));
        mAREcoRenderer = new PubSubARRenderer(this, ecoMeshManager);
        mAREcoRenderer.setEnableGrid(true);
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


        }
        setupStreamLib();

        mCamerasFrame = (LinearLayout) findViewById(R.id.container_cameras);
        resetCameraMesh = (Button) findViewById(R.id.reset_camera_mesh_position);
        resetEcoMesh = (Button) findViewById(R.id.reset_eco_mesh_position);
        saveKeyCoordinate = (Button) findViewById(R.id.save_ar_key_coordinate);

        setupECGFrame();

        onBoardCameraFrameLayout = (FrameLayout) findViewById(R.id.container_stream_on_board_camera);
        cameraFrameLayout = (FrameLayout) findViewById(R.id.container_stream_camera);
        Button switchCameraButton = (Button) findViewById(R.id.switch_camera);
        switchCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchCamera();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStreamCameraFragment.setPlayerButtonsVisible(false);
        mStreamEcoFragment.setPlayerButtonsVisible(false);
        mStreamOnBoardCameraFragment.setPlayerButtonsVisible(false);
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
            if (arOnBoot)
                getCurrentFragment().startAR();
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
                    getCurrentFragment().startAR();
                }
                else {
                    item.setIcon(ContextCompat.getDrawable(this, android.R.drawable.checkbox_off_background));
                }
                item.setChecked(isChecked);
                if (mStreamEcoFragment != null) {
                    mStreamEcoFragment.setEnabled(isChecked);
                }
                getCurrentFragment().setEnabled(isChecked);
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
                break;
            case R.id.button_ecg:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    item.setIcon(ContextCompat.getDrawable(this, android.R.drawable.checkbox_on_background));
                }
                else {
                    item.setIcon(ContextCompat.getDrawable(this, android.R.drawable.checkbox_off_background));
                }
                showECG(item.isChecked());
                break;
        }
        return false;
    }

    private void switchCamera() {
        final LinearLayout.LayoutParams currentLayoutParams, newVisibleLayoutParams;
        final FrameLayout currentStreamView, newVisibleStreamView;

        final IStream currentStream;
        final IStream newVisibleStream;

        if (currentCameraStream.equals(CAMERA_STREAM)) {
            Log.d(TAG, "new currentCameraStream == ON_BOARD_CAMERA_STREAM");
            currentStreamView = (FrameLayout) findViewById(R.id.container_stream_camera);
            newVisibleStreamView = (FrameLayout) findViewById(R.id.container_stream_on_board_camera);
            currentStream = mStreamCamera;
            newVisibleStream = mStreamOnBoardCamera;

            currentLayoutParams = (LinearLayout.LayoutParams) currentStreamView.getLayoutParams();
            newVisibleLayoutParams = (LinearLayout.LayoutParams) newVisibleStreamView.getLayoutParams();
//            currentCameraStream = ON_BOARD_CAMERA_STREAM;

        }

        else {
            Log.d(TAG, "new currentCameraStream == CAMERA_STREAM");
            currentStreamView = (FrameLayout) findViewById(R.id.container_stream_on_board_camera);
            newVisibleStreamView = (FrameLayout) findViewById(R.id.container_stream_camera);

            currentStream = mStreamOnBoardCamera;
            newVisibleStream = mStreamCamera;

            currentLayoutParams = (LinearLayout.LayoutParams) currentStreamView.getLayoutParams();
            newVisibleLayoutParams = (LinearLayout.LayoutParams) newVisibleStreamView.getLayoutParams();
//            currentCameraStream = CAMERA_STREAM;

        }


        FrameLayout onBoardCameraFrameLayout = (FrameLayout) findViewById(R.id.container_stream_on_board_camera);
        FrameLayout cameraFrameLayout = (FrameLayout) findViewById(R.id.container_stream_camera);

        LinearLayout.LayoutParams onBoardCameraLayoutParams = (LinearLayout.LayoutParams) onBoardCameraFrameLayout.getLayoutParams();

//        LinearLayout.LayoutParams cameraLayoutParams = (LinearLayout.LayoutParams) cameraFrameLayout .getLayoutParams();

        if (firstSwitch) {
            onBoardCameraFrameLayout.addOnLayoutChangeListener(new OnLayoutChangeListener());
            cameraFrameLayout.addOnLayoutChangeListener(new OnLayoutChangeListener());
            firstSwitch = false;
        }
//        currentStream.pause();
//        newVisibleStream.play();
        switchPhase = 0;

        currentLayoutParams.weight = 0f;
        currentStreamView.setLayoutParams(currentLayoutParams);
        newVisibleLayoutParams.weight = 1f;
        newVisibleStreamView.setLayoutParams(newVisibleLayoutParams);
//        onBoardCameraLayoutParams.weight = currentCameraStream.equals(CAMERA_STREAM)? 1f: 0f;
//        onBoardCameraFrameLayout.setLayoutParams(onBoardCameraLayoutParams);

    }

    private void showECG(boolean show) {
        LinearLayout.LayoutParams streamLayoutParams = (LinearLayout.LayoutParams)
                mStreamLayout.getLayoutParams();

        if (show) {
            mEcgView.resetData();
            mEcgContainer.setVisibility(View.VISIBLE);
            streamLayoutParams.weight = 0.9f;
            mEcgSubscriber.startReceiving();
        }
        else {
            mEcgContainer.setVisibility(View.GONE);
            mEcgSubscriber.stopReceiving();
            streamLayoutParams.weight = 1f;
        }
    }

    private void changeEcoStreamSize() {
        LinearLayout.LayoutParams cameraParams = (LinearLayout.LayoutParams) mCamerasFrame.getLayoutParams();
//        LinearLayout.LayoutParams onBoardCameraParams = (LinearLayout.LayoutParams) mOnBoardCameraFrame.getLayoutParams();
//        LinearLayout.LayoutParams cameraParams = (LinearLayout.LayoutParams) mCamerasFrame.getLayoutParams();
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

        mCamerasFrame.setLayoutParams(cameraParams);
//        mOnBoardCameraFrame.setLayoutParams(onBoardCameraParams);
        mEcoFrame.setLayoutParams(ecoParams);
        mChangeEcoSizeMenuItem.setTitle(title);
    }

    private void setupStreamLib() {
        try {
            StreamingLib streamingLib = new StreamingLibBackend();
            streamingLib.initLib(getApplicationContext());

            String ecoAppAddress = teleconsultation.getLastSession().getEcoAppAddress();
            if (ecoAppAddress != null && !ecoAppAddress.equals("")) {
                String streamUri = "rtsp://" + ecoAppAddress + ":8554/test";
//                String streamUri = "file:///sdcard/Movies/eco2.mp4";
                Device onBoardCamera = new Device("OnBoardCamera", streamUri, "", "", "", "", "");
                HashMap<String, String> streamOnBoardCameraParams = new HashMap<>();
                streamOnBoardCameraParams.put("name", ON_BOARD_CAMERA_STREAM);
                streamOnBoardCameraParams.put("uri", onBoardCamera.getStreamUri());
                mStreamOnBoardCamera = streamingLib.createStream(streamOnBoardCameraParams);
                mStreamOnBoardCamera.addEventListener(new IEventListener() {
                    @Override
                    public void frameReady(byte[] bytes) {

                    }

                    @Override
                    public void onPlay() {
                        mStreamCamera.pause();
                        if (mARToggle.isChecked())
                            mStreamOnBoardCameraFragment.startAR();
                    }

                    @Override
                    public void onPause() {
                        mStreamCamera.play();

                    }

                    @Override
                    public void onVideoChanged(int i, int i1) {

                    }
                });

                mStreamOnBoardCameraFragment = ARFragment.newInstance(mStreamOnBoardCamera.getName());
                mStreamOnBoardCameraFragment.setPlayerButtonsVisible(false);
                mStreamOnBoardCameraFragment.setDeviceID("EPSON/embt2/embt2");
                PubSubARRenderer renderer = new PubSubARRenderer(this, cameraMeshManager);
                mStreamOnBoardCameraFragment.setRenderer(renderer);
                mStreamOnBoardCameraFragment.setStream(mStreamOnBoardCamera);
                mStreamOnBoardCameraFragment.setGlSurfaceViewCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder surfaceHolder) {
                        TouchGLSurfaceView glView = mStreamOnBoardCameraFragment.getGlView();
                        glView.setMeshManager(cameraMeshManager);
                        glView.setPublisher(mARPublisher);
                        glView.setEnabled(false);
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                    }
                });
            }

            Device camera;
            camera = teleconsultation.getLastSession().getCamera();
            HashMap<String, String> streamCameraParams = new HashMap<>();
            streamCameraParams.put("name", CAMERA_STREAM);
            streamCameraParams.put("uri", camera.getStreamUri());
            mStreamCamera = streamingLib.createStream(streamCameraParams);
            mStreamCamera.addEventListener(new IEventListener() {
                @Override
                public void frameReady(byte[] bytes) {

                }

                @Override
                public void onPlay() {
                    mStreamOnBoardCamera.pause();
                    if (mARToggle.isChecked())
                        mStreamCameraFragment.startAR();
                }

                @Override
                public void onPause() {
                    mStreamOnBoardCamera.play();

                }

                @Override
                public void onVideoChanged(int i, int i1) {

                }
            });
            mStreamCameraFragment = ARFragment.newInstance(mStreamCamera.getName());
            mStreamCameraFragment.setPlayerButtonsVisible(false);
            mStreamCameraFragment.setRenderer(mARCameraRenderer);
            mStreamCameraFragment.setStream(mStreamCamera);
            mStreamCameraFragment.setGlSurfaceViewCallback(this);
            mStreamCameraFragment.setEnabled(arOnBoot);
            mStreamCameraFragment.setArListener(this);

            Device encoder = teleconsultation.getLastSession().getEncoder();
            HashMap<String, String> streamEcoParams = new HashMap<>();
            streamEcoParams.put("name", ECO_STREAM);
            streamEcoParams.put("uri", encoder.getStreamUri());
            mStreamEco = streamingLib.createStream(streamEcoParams);

            mEcoFrame = (FrameLayout) findViewById(R.id.container_stream_eco);
            mStreamEcoFragment = ARFragment.newInstance(mStreamEco.getName());
            mStreamEcoFragment.setPlayerButtonsVisible(false);
            mStreamEcoFragment.setEnabled(false);
            mStreamEcoFragment.setStream(mStreamEco);
            mStreamEcoFragment.setFrameCallback(false);

            mPTZManager = new PTZ_Manager(this,
                    camera.getPtzUri(),
                    camera.getUser(),
                    camera.getPwd()
            );

            mStreamEcoFragment.setRenderer(mAREcoRenderer);
        }
        catch (Exception e) {
            e.printStackTrace();
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

        mStreamEcoFragment.getRenderer().setViewportListener(new PubSubARRenderer.ViewportListener() {
            @Override
            public void onViewportChanged(int x, int y, int width, int height) {
                viewportAspectRatio = ((float) width)/height;
                Log.d(TAG, "viewportAspectRatio " + viewportAspectRatio);
                for (Mesh mesh: ecoMeshManager.getMeshes()) {
//                    mesh.setSx(2f/arConf.getScreenHeight());
//                    mesh.setSy(2f/arConf.getScreenHeight());
                    mesh.setCoordsConverter(
                            new CoordsConverter(
                                arConf.getScreenWidth() / (2f * viewportAspectRatio),
                                arConf.getScreenHeight() / 2f,
                                1f
                            )
                    );

//                    mesh.setSx(2f/arConf.getScreenHeight());
//                    mesh.setSy(2f/arConf.getScreenHeight());
                    mesh.setxLimits(-viewportAspectRatio, viewportAspectRatio);
                    mesh.setyLimits(-1, 1);
                }

            }
        });
        // add the first fragment to the first container
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container_stream_camera, mStreamCameraFragment);
        if (mStreamOnBoardCameraFragment != null) {
            fragmentTransaction.add(R.id.container_stream_on_board_camera, mStreamOnBoardCameraFragment);
        }
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
            mEcgContainer = (LinearLayout) findViewById(R.id.ecg_graph_container);
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
//        if (currentCameraStream.equals(CAMERA_STREAM)) {

        IStream currentStream = getCurrentStream();
        if (currentStream != null && currentStream.getState() != StreamState.PLAYING) {
                getCurrentFragment().setStreamVisible();
            currentStream.play();
            }
    }

    private void playStreams() {
        playCamera();
//        switchCamera();
        if (mStreamEco != null && mStreamEco.getState() != StreamState.PLAYING) {
            mStreamEcoFragment.setStreamVisible();
            mStreamEco.play();
        }
    }

    private void pauseCamera() {
        if (currentCameraStream.equals(CAMERA_STREAM)) {
            if (mStreamCamera != null && mStreamCamera.getState() == StreamState.PLAYING) {
                mStreamCameraFragment.setStreamInvisible("PAUSED");
                mStreamCamera.pause();
            }
        }
        else {
            if (mStreamOnBoardCamera != null && mStreamOnBoardCamera.getState() == StreamState.PLAYING) {
                mStreamOnBoardCameraFragment.setStreamInvisible("PAUSED");
                mStreamOnBoardCamera.pause();
            }
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
        Log.d(TAG, "Surface created for stream " + streamId);
        if (surfaceView != null) {
            if (streamId.equals(CAMERA_STREAM)) {
//                if (!mStreamCameraPrepared) {
//                    mStreamCamera.prepare(surfaceView, true);
////                mStreamCameraFragment.setStream(mStreamCamera);
////                mStreamCameraFragment.setRenderer(mARCameraRenderer);
////                    if (arConf != null) {
////                        mStreamCameraFragment.prepareRemoteAR();
////                        mStreamCameraPrepared = true;
////                    }
//                }
            }
            else if (streamId.equals(ECO_STREAM)) {
//                mStreamEco.prepare(surfaceView);
                if (arConf != null) {
                    TouchGLSurfaceView glView = mStreamEcoFragment.getGlView();
                    glView.setZOrderMediaOverlay(true);
                    glView.setMoveNormFactor(300f);
                }
            }
            else if (streamId.equals(ON_BOARD_CAMERA_STREAM)) {
//                if (!mStreamOnBoardCameraPrepared) {
//                    mStreamOnBoardCamera.prepare(surfaceView, true);
//                    mStreamOnBoardCameraPrepared = true;
//                }
            }
        }
    }

    @Override
    public void onSurfaceViewDestroyed(String streamId) {
        Log.d(TAG, "Surface destroyed " + streamId);
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
        streams.add(mStreamOnBoardCamera);
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
        Log.d(TAG, "ARInitialized()");
        getCurrentFragment().setEnabled(mARToggle.isChecked());
//        cameraMeshManager.configureScene(true);
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
                act.setResult(RESULT_OK);
                act.finish();
//                act.startReportActivity();
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

                m.setRx(0);
                m.setRy(0);
                m.setRz(0);

                m.setSx(1);
                m.setSy(1);
                m.setSz(1);
            }
            view.requestRender();
        }
    }

    private class CurrentStreamOnLayoutChangeListener implements View.OnLayoutChangeListener {
        LinearLayout.LayoutParams newVisibleLayoutParams;
        FrameLayout newVisibleStreamView;
        public CurrentStreamOnLayoutChangeListener(LinearLayout.LayoutParams newVisibleLayoutParams,
                FrameLayout newVisibleStreamView) {

            this.newVisibleLayoutParams = newVisibleLayoutParams;
            this.newVisibleStreamView = newVisibleStreamView;
        }


        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            newVisibleLayoutParams.width = 0;
            newVisibleLayoutParams.weight = 1f;
            newVisibleStreamView.setLayoutParams(newVisibleLayoutParams);
            v.removeOnLayoutChangeListener(this);
        }
    }

    private ARFragment getCurrentFragment() {
        return currentCameraStream.equals(CAMERA_STREAM)? mStreamCameraFragment: mStreamOnBoardCameraFragment;
    }

    private ARFragment getHiddenFragment() {
        return currentCameraStream.equals(CAMERA_STREAM)? mStreamOnBoardCameraFragment: mStreamCameraFragment;
    }

    private FrameLayout getCurrentStreamView() {
        return currentCameraStream.equals(CAMERA_STREAM)?
                (FrameLayout) findViewById(R.id.container_stream_camera):
                (FrameLayout) findViewById(R.id.container_stream_on_board_camera);
    }

    private FrameLayout getHiddenStreamView() {
        return (FrameLayout)
                (currentCameraStream.equals(CAMERA_STREAM)?
                 findViewById(R.id.container_stream_on_board_camera):
                findViewById(R.id.container_stream_camera));
    }

    private IStream getCurrentStream() {
        return currentCameraStream.equals(CAMERA_STREAM)? mStreamCamera: mStreamOnBoardCamera;
    }

    private IStream getHiddenStream() {
        return currentCameraStream.equals(CAMERA_STREAM)? mStreamOnBoardCamera: mStreamCamera;
    }

    private class OnLayoutChangeListener implements View.OnLayoutChangeListener {


        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            Log.d(TAG, "onLayoutChange switchPhase" + switchPhase);
            switchPhase++;
            if (switchPhase == 2){
                Log.d(TAG, String.format("switch phase %s, time to pause/play", switchPhase));
//                getCurrentStream().pause();
                getCurrentFragment().stopAR();

                getHiddenFragment().setStreamVisible();
                getHiddenStream().play();
//
//                if (mARToggle.isChecked())
//                    getHiddenFragment().startAR();
//                cameraMeshManager.configureScene(true);
                currentCameraStream = currentCameraStream.equals(CAMERA_STREAM)? ON_BOARD_CAMERA_STREAM: CAMERA_STREAM;
                }
            }
    }
}
