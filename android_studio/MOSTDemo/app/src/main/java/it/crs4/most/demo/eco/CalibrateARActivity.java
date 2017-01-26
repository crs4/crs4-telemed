package it.crs4.most.demo.eco;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.assets.AssetHelper;
import org.artoolkit.ar.base.camera.CameraEventListener;
import org.artoolkit.ar.base.camera.CaptureCameraPreview;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.artoolkit.ar.base.rendering.gles20.ARRendererGLES20;

import java.io.File;

import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.visualization.augmentedreality.MarkerFactory;
import it.crs4.most.visualization.augmentedreality.OpticalARToolkit;
import it.crs4.most.visualization.augmentedreality.TouchGLSurfaceView;
import it.crs4.most.visualization.augmentedreality.mesh.Arrow;
import it.crs4.most.visualization.augmentedreality.mesh.CoordsConverter;
import it.crs4.most.visualization.augmentedreality.mesh.MeshManager;
import it.crs4.most.visualization.augmentedreality.mesh.Pyramid;
import it.crs4.most.visualization.augmentedreality.renderer.OpticalRenderer;
import it.crs4.most.visualization.augmentedreality.renderer.PubSubARRenderer;
import jp.epson.moverio.bt200.DisplayControl;

public class CalibrateARActivity extends AppCompatActivity implements CameraEventListener {
    private OpticalARToolkit mOpticalARToolkit;
    private MeshManager meshManager = new MeshManager();
    private OpticalRenderer renderer;
    private CaptureCameraPreview preview;
    private TouchGLSurfaceView glView;
    private boolean firstUpdate = false;
    protected FrameLayout mainLayout;
    private final String TAG = "CalibrateARActivity";
    private boolean arInitialized = false;
    private final static String ARROW_ID = "ARROW" ;
    Pyramid arrow = new Pyramid(10f, 10f, 10f, ARROW_ID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_ar);
        getWindow().addFlags(0x80000000);
//            Log.d(TAG, "loading optical files");
        mOpticalARToolkit = new OpticalARToolkit(ARToolKit.getInstance());
        renderer = new OpticalRenderer(this, mOpticalARToolkit, meshManager);
        renderer.setEye(OpticalRenderer.EYE.valueOf(QuerySettings.getAREyes(this).toString()));
        renderer.setEnabled(true);
        renderer.setLowFilterLevel(QuerySettings.getARLowFilterLevel(this));
        float [] calibration = QuerySettings.getARCalibration(this);
        Log.d(TAG, String.format("calibration %f %f", calibration[0], calibration[1]));
//        renderer.adjustCalibration(calibration[0], calibration[1], 0);

        File cacheFolder = new File(getCacheDir().getAbsolutePath() + "/Data");
        File[] files = cacheFolder.listFiles();
        if (files != null){
            for (File file : files) {
                if (!file.delete()){
//                    throw new RuntimeException("cannot delete cached files");
                }
            }
        }
        AssetHelper assetHelper = new AssetHelper(getAssets());
        assetHelper.cacheAssetFolder(this, "Data");



        arrow.addMarker(MarkerFactory.getMarker("single;Data/calib.patt;80"));
        meshManager.addMesh(arrow);
        meshManager.configureScene();
    }

    protected FrameLayout supplyFrameLayout() {
        return (FrameLayout) this.findViewById(R.id.ar_calibration_frame);
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
                    CalibrateARActivity.this.finish();
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

        preview = new CaptureCameraPreview(this, this);
        preview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (Build.MANUFACTURER.equals("EPSON") && Build.MODEL.equals("embt2")) {
                    DisplayControl displayControl = new DisplayControl(CalibrateARActivity.this);
                    boolean stereo = PreferenceManager.getDefaultSharedPreferences(CalibrateARActivity.this).
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
        glView.setEnabled(true);
//        if (isOptical) {
//            glView.setEnabled(false);
//        }

        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 131072;
        if (supportsEs2) {
            Log.i("ARActivity", "onResume(): OpenGL ES 2.x is supported");
            if ((ARRenderer) renderer instanceof ARRendererGLES20) {
                this.glView.setEGLContextClientVersion(2);
            } else {
                Log.w("ARActivity", "onResume(): OpenGL ES 2.x is supported but only a OpenGL 1.x renderer is available. \n Use ARRendererGLES20 for ES 2.x support. \n Continuing with OpenGL 1.x.");
                this.glView.setEGLContextClientVersion(1);
            }
        } else {
            Log.i("ARActivity", "onResume(): Only OpenGL ES 1.x is supported");
            if ((ARRenderer) renderer instanceof ARRendererGLES20) {
                throw new RuntimeException("Only OpenGL 1.x available but a OpenGL 2.x renderer was provided.");
            }
            this.glView.setEGLContextClientVersion(1);
        }

//        if (!isOptical) {
//            glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
//        }
//


        glView.setRenderer((PubSubARRenderer) renderer);
        glView.getHolder().setFormat(-3);
        glView.setMeshManager(meshManager);

        glView.setRenderMode(0);
        glView.setZOrderMediaOverlay(true);

        mainLayout.addView(this.preview, new ViewGroup.LayoutParams(-1, -1));
        mainLayout.addView(this.glView, new ViewGroup.LayoutParams(-1, -1));

        if (this.glView != null) {
            this.glView.onResume();
        }
    }

    public void cameraPreviewStarted(int width, int height, int rate, int cameraIndex, boolean cameraIsFrontFacing) {
        Log.d(TAG, "cameraPreviewStarted");
        if(arInitialized){
            return;
        }
        if (ARToolKit.getInstance().initialiseAR(width, height, "Data/camera_para.dat", cameraIndex, cameraIsFrontFacing)) {
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
                    Log.e(TAG, "Error initialising optical device. Cannot continue.");
                    this.finish();
                }
            }
            Log.i(TAG, "getGLView(): Camera initialised");

            arInitialized = true;
        }
        else {
            Log.e(TAG, "getGLView(): Error initialising camera. Cannot continue.");
            this.finish();
        }
        this.firstUpdate = true;
    }

    public void cameraPreviewFrame(byte[] frame) {
        if (this.firstUpdate) {

            if (this.renderer.configureARScene()) {
                Log.i(TAG, "cameraPreviewFrame(): Scene configured successfully");
            }
            else {
                Log.e(TAG, "cameraPreviewFrame(): Error configuring scene. Cannot continue.");
                this.finish();
            }

            this.firstUpdate = false;
            arInitialized = true;
        }

        if (ARToolKit.getInstance().convertAndDetect(frame)) {
            if (this.glView != null) {
                this.glView.requestRender();
            }
            this.onFrameProcessed();
        }
    }

    public void onFrameProcessed() {
    }

    public void cameraPreviewStopped() {
        ARToolKit.getInstance().cleanup();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.glView != null) {
            this.glView.onPause();
        }
        this.mainLayout.removeView(this.glView);
        this.mainLayout.removeView(this.preview);
        QuerySettings.setARCalibration(this, arrow.getX(), arrow.getY());
    }

}
