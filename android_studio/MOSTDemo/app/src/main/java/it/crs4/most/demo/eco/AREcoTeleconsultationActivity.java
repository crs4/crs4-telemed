package it.crs4.most.demo.eco;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ConfigurationInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.NativeInterface;
import org.artoolkit.ar.base.camera.CameraEventListener;
import org.artoolkit.ar.base.camera.CameraPreferencesActivity;
import org.artoolkit.ar.base.camera.CaptureCameraPreview;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.artoolkit.ar.base.rendering.gles20.ARRendererGLES20;

import java.util.Map;
import java.util.Timer;

import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RESTClient;
import it.crs4.most.streaming.GstreamerRTSPServer;
import it.crs4.most.streaming.StreamServer;
import it.crs4.most.visualization.augmentedreality.CalibrateTouchGLSurfaceView;
import it.crs4.most.visualization.augmentedreality.OpticalARToolkit;
import it.crs4.most.visualization.augmentedreality.TouchGLSurfaceView;
import it.crs4.most.visualization.augmentedreality.mesh.MeshManager;
import it.crs4.most.visualization.augmentedreality.renderer.OpticalRenderer;
import it.crs4.most.visualization.augmentedreality.renderer.PubSubARRenderer;
import it.crs4.most.visualization.utils.zmq.ARSubscriber;
import jp.epson.moverio.bt200.DisplayControl;
// For Epson Moverio BT-200. BT200Ctrl.jar must be in libs/ folder.


public class AREcoTeleconsultationActivity extends BaseEcoTeleconsultationActivity implements
    CameraEventListener, SensorEventListener {
    protected static final String TAG = "LocalARActivity";
    protected PubSubARRenderer renderer;
    protected FrameLayout mainLayout;
    private CaptureCameraPreview preview;
    private TouchGLSurfaceView glView;
    private boolean firstUpdate = false;
    private OpticalARToolkit mOpticalARToolkit;
    private MeshManager meshManager = new MeshManager();
    private boolean arInitialized = false;
    private boolean arEnabled = false;
    private ARSubscriber mARSubscriber;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    protected float accX, accY, accZ;
    private boolean isOptical = false;
    private StreamServer streamServer;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        private boolean toggle = true;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive, intent.getAction() " + intent.getAction());
            if (intent.getAction().equals("HOLDCALL")) {
                toggleHoldCall(toggle);
                toggle = !toggle;
            }
            else if (intent.getAction().equals("HANGUP")) {
                hangupCall();
            }
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        accX = event.values[0];
        accY = event.values[1];
        accZ = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public static class RemoteControlReceiver extends BroadcastReceiver {
        String TAG = "RemoteControlReceiver";
        final static Timer timer = new Timer();
        static long actionDownTime;
        static boolean actionDownReceived = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive, intent.getAction() " + intent.getAction());
            if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
                Log.d(TAG, "OMG key pressed");
                KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event == null) {
                    context.sendBroadcast(new Intent("HOLDCALL"));
                }
                else {
                    int keycode = event.getKeyCode();
                    int action = event.getAction();
                    long eventTime = event.getEventTime();

                    Log.d(TAG, "eventTime " + eventTime);
                    if (action == KeyEvent.ACTION_DOWN && !actionDownReceived) {
                        Log.d("EVENT", "ACTION_DOWN, eventTime " + eventTime);
                        actionDownTime = eventTime;
                        actionDownReceived = true;
                    }
                    else if (action == KeyEvent.ACTION_UP) {
                        Log.d("EVENT", "ACTION_UP eventTime " + eventTime);
                        Log.d("EVENT", "eventTime - actionDownTime " + (eventTime - actionDownTime));
                        if (eventTime - actionDownTime < 1000) {
                            Log.d("EVENT", "HOLDCALL intent");
                            context.sendBroadcast(new Intent("HOLDCALL"));
                        }
                        else {
                            Log.d("EVENT", "HANGUP intent");
                            context.sendBroadcast(new Intent("HANGUP"));
                        }
                        actionDownReceived = false;
                    }
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ComponentName componentName = new ComponentName(this, RemoteControlReceiver.class);
        mAudioManager.registerMediaButtonEventReceiver(componentName);
        setContentView(R.layout.ar_eco);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if ((Build.MANUFACTURER.equals("EPSON") && Build.MODEL.equals("embt2"))) {
            getWindow().addFlags(0x80000000);
//            Log.d(TAG, "loading optical files");
            mOpticalARToolkit = new OpticalARToolkit(ARToolKit.getInstance());
            isOptical = true;
        }

        String configServerIP = QuerySettings.getConfigServerAddress(this);
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(this));
        mRESTClient = new RESTClient(this, configServerIP, configServerPort);

        String specAppAddress = teleconsultation.getLastSession().getSpecAppAddress();
        Log.d(TAG, "SpecApp Address: " + specAppAddress);
        mARSubscriber = new ARSubscriber("tcp://" + specAppAddress, null);
        mARSubscriber.start();
        mARSubscriber.getLooper();
        mARSubscriber.prepareResponseHandler();
        mARSubscriber.startReceiving();
        createARMeshes(meshManager);

        if (mOpticalARToolkit != null) {
            Log.d(TAG, "setting OpticalRenderer");
            renderer = new OpticalRenderer(this, mOpticalARToolkit, meshManager);

            ((OpticalRenderer) renderer).setEye(
                OpticalRenderer.EYE.valueOf(teleconsultation.getLastSession().getRoom().getARConfiguration().getEye()));
        }
        else {
            renderer = new PubSubARRenderer(this, meshManager);
        }
        renderer.setEnabled(arEnabled);
        for (Map.Entry<String, float []> calibration: teleconsultation.getLastSession().getRoom().getARConfiguration().getCalibrations().entrySet()) {
            renderer.setExtraCalibration(calibration.getKey(), calibration.getValue());
        }
//        float [] calibration = QuerySettings.getARCalibration(this);
//        renderer.setExtraCalibration(new float[]{calibration[0], calibration[1], 0});
        renderer.setLowFilterLevel(QuerySettings.getARLowFilterLevel(this));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        streamServer = new GstreamerRTSPServer(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("ARActivity", "onStart(): Activity starting.");
        if (!ARToolKit.getInstance().initialiseNative(this.getCacheDir().getAbsolutePath())) {
            (new AlertDialog.Builder(this)).
                setMessage("The native library is not loaded. The application cannot continue.").
                setTitle("Error").setCancelable(true).setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    AREcoTeleconsultationActivity.this.finish();
                }
            }).show();
        }
        else {
            this.mainLayout = this.supplyFrameLayout();
            if (this.mainLayout == null) {
                Log.e("ARActivity", "onStart(): Error: supplyFrameLayout did not return a layout.");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter("HOLDCALL"));
        registerReceiver(broadcastReceiver, new IntentFilter("HANGUP"));

        preview = new CaptureCameraPreview(this, this);
        preview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (Build.MANUFACTURER.equals("EPSON") && Build.MODEL.equals("embt2")) {
                    DisplayControl displayControl = new DisplayControl(AREcoTeleconsultationActivity.this);
                    boolean stereo = PreferenceManager.getDefaultSharedPreferences(AREcoTeleconsultationActivity.this).
                        getBoolean("pref_stereoDisplay", false);
                    displayControl.setMode(DisplayControl.DISPLAY_MODE_3D, stereo);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
        Log.i("ARActivity", "onResume(): CaptureCameraPreview created");
        glView = new TouchGLSurfaceView(this);

        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 131072;
        if (supportsEs2) {
            Log.i("ARActivity", "onResume(): OpenGL ES 2.x is supported");
            if ((ARRenderer) renderer instanceof ARRendererGLES20) {
                this.glView.setEGLContextClientVersion(2);
            }
            else {
                Log.w("ARActivity", "onResume(): OpenGL ES 2.x is supported but only a OpenGL 1.x renderer is available. \n Use ARRendererGLES20 for ES 2.x support. \n Continuing with OpenGL 1.x.");
                this.glView.setEGLContextClientVersion(1);
            }
        }
        else {
            Log.i("ARActivity", "onResume(): Only OpenGL ES 1.x is supported");
            if ((ARRenderer) renderer instanceof ARRendererGLES20) {
                throw new RuntimeException("Only OpenGL 1.x available but a OpenGL 2.x renderer was provided.");
            }
            this.glView.setEGLContextClientVersion(1);
        }

        if (!isOptical) {
            glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        }

        glView.getHolder().setFormat(-3);
        glView.setRenderer((PubSubARRenderer) renderer);
        glView.setSubscriber(mARSubscriber);
        glView.setMeshManager(meshManager);

        glView.setRenderMode(0);
        glView.setZOrderMediaOverlay(true);
        glView.setEnabled(false);

        mainLayout.addView(this.preview, new ViewGroup.LayoutParams(-1, -1));
        mainLayout.addView(this.glView, new ViewGroup.LayoutParams(-1, -1));

        if (this.glView != null) {
            this.glView.onResume();
        }

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.glView != null) {
            this.glView.onPause();
//            float [] extraCalibration = renderer.getExtraCalib ion[0], extraCalibration[1]);
        }

        this.mainLayout.removeView(this.glView);
        this.mainLayout.removeView(this.preview);
        unregisterReceiver(broadcastReceiver);
        sensorManager.unregisterListener(this);
        mARSubscriber.close();
    }

    @Override
    public void onStop() {
        Log.i("ARActivity", "onStop(): Activity stopping.");
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == org.artoolkit.ar.base.R.id.settings) {
            this.startActivity(new Intent(this, CameraPreferencesActivity.class));
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    public CaptureCameraPreview getCameraPreview() {
        return this.preview;
    }

    public GLSurfaceView getGLView() {
        return this.glView;
    }

    public void cameraPreviewStarted(int width, int height, int rate, int cameraIndex, boolean cameraIsFrontFacing) {
        Log.d(TAG, "cameraPreviewStarted");

        if (!streamServer.isRunning()) {
            Log.d(TAG, String.format("start server with %s %s %s", width, height, rate));
            streamServer.start(width, height, rate);
        }
        if (arInitialized) {
            return;
        }
//        if (arInitialized) {
//            ARToolKit.getInstance().cleanup();
//            if (!ARToolKit.getInstance().initialiseNative(this.getCacheDir().getAbsolutePath())) {
//                this.finish();
//            }
//            arInitialized = false;
//        }
        if (ARToolKit.getInstance().initialiseAR(width, height, null, cameraIndex, cameraIsFrontFacing)) {
            Log.d(TAG, String.format("Build.MANUFACTURER %s", Build.MANUFACTURER));
            Log.d(TAG, String.format("Build.MODEL %s", Build.MODEL));

            if (mOpticalARToolkit != null) {
                Log.d(TAG, "loading optical files");
                if (mOpticalARToolkit.initialiseAR(
                    "Data/optical_param_left.dat", "Data/optical_param_right") > 0) {
                    Log.d(TAG, "loaded optical files");
                    Log.d(TAG, "getEyeRproject len " + mOpticalARToolkit.getEyeRproject().length);
                }
                else {
                    Log.e("ARActivity", "Error initialising optical device. Cannot continue.");
                    this.finish();
                }
            }
            Log.i("ARActivity", "getGLView(): Camera initialised");

            arInitialized = true;
        }
        else {
            Log.e("ARActivity", "getGLView(): Error initialising camera. Cannot continue.");
            this.finish();
        }

        Toast.makeText(this, "Camera settings: " + width + "x" + height + "@" + rate + "fps", Toast.LENGTH_SHORT).show();
        this.firstUpdate = true;
    }

    public void cameraPreviewFrame(byte[] frame) {
        if (this.firstUpdate) {

            if (this.renderer.configureARScene()) {
                Log.i("ARActivity", "cameraPreviewFrame(): Scene configured successfully");
            }
            else {
                Log.e("ARActivity", "cameraPreviewFrame(): Error configuring scene. Cannot continue.");
                this.finish();
            }

            this.firstUpdate = false;
            arInitialized = true;
        }


        final float accLimit = 0.00f;
        if (renderer.isEnabled() && (accX > accLimit || accY > accLimit || accZ > accLimit)) {
            if (ARToolKit.getInstance().convertAndDetect(frame)) {
                if (this.glView != null) {
                    this.glView.requestRender();
                }
                this.onFrameProcessed();
            }
        }

        if (frame != null) {
            streamServer.feedData(frame);
        }

    }

    public void onFrameProcessed() {
    }

    public void cameraPreviewStopped() {
        ARToolKit.getInstance().cleanup();
    }

    protected void showInfo() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("ARToolKit Version: " + NativeInterface.arwGetARToolKitVersion());
        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = dialogBuilder.create();
        alert.setTitle("ARToolKit");
        alert.show();
    }

    protected ARRenderer supplyRenderer() {
        return renderer;
    }

    protected FrameLayout supplyFrameLayout() {
        return (FrameLayout) this.findViewById(R.id.local_ar_frame);
    }

    @Override
    protected void notifyTeleconsultationStateChanged() {
    }
}
