package it.crs4.most.demo.ecoapp;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.NativeInterface;
import org.artoolkit.ar.base.assets.AssetHelper;
import org.artoolkit.ar.base.camera.CameraEventListener;
import org.artoolkit.ar.base.camera.CameraPreferencesActivity;
import org.artoolkit.ar.base.camera.CaptureCameraPreview;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.artoolkit.ar.base.rendering.gles20.ARRendererGLES20;

import java.util.HashMap;

import it.crs4.most.visualization.augmentedreality.OpticalARToolkit;
import it.crs4.most.visualization.augmentedreality.TouchGLSurfaceView;
import it.crs4.most.visualization.augmentedreality.mesh.Arrow;
import it.crs4.most.visualization.augmentedreality.mesh.Mesh;
import it.crs4.most.visualization.augmentedreality.renderer.OpticalRenderer;
import it.crs4.most.visualization.augmentedreality.renderer.PubSubARRenderer;
import it.crs4.most.visualization.utils.zmq.ZMQSubscriber;




import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.NativeInterface;
import org.artoolkit.ar.base.camera.CameraEventListener;
import org.artoolkit.ar.base.camera.CameraPreferencesActivity;
import org.artoolkit.ar.base.camera.CaptureCameraPreview;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.artoolkit.ar.base.rendering.gles20.ARRendererGLES20;

import java.util.HashMap;

import it.crs4.most.visualization.augmentedreality.OpticalARToolkit;
import it.crs4.most.visualization.augmentedreality.TouchGLSurfaceView;
import it.crs4.most.visualization.augmentedreality.mesh.Arrow;
import it.crs4.most.visualization.augmentedreality.mesh.Mesh;
import it.crs4.most.visualization.augmentedreality.renderer.OpticalRenderer;
import it.crs4.most.visualization.augmentedreality.renderer.PubSubARRenderer;
import it.crs4.most.visualization.utils.zmq.ZMQSubscriber;
import jp.epson.moverio.bt200.DisplayControl;
// For Epson Moverio BT-200. BT200Ctrl.jar must be in libs/ folder.


public class AREcoTeleconsultationActivity extends Activity implements CameraEventListener {
    protected static final String TAG = "LocalARActivity";
    protected PubSubARRenderer renderer;
    protected FrameLayout mainLayout;
    private CaptureCameraPreview preview;
    private TouchGLSurfaceView glView;
    //    private TouchGLSurfaceView glView;
    private boolean firstUpdate = false;
    private OpticalARToolkit mOpticalARToolkit;
    private EditText coordX, coordY, coordZ;
    private HashMap<String, Mesh> meshes = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.ar_eco);

        AssetHelper assetHelper = new AssetHelper(getAssets());
        assetHelper.cacheAssetFolder(this, "Data");


        if((Build.MANUFACTURER.equals("EPSON") && Build.MODEL.equals("embt2"))){
            Log.d(TAG, "loading optical files");
            mOpticalARToolkit = new OpticalARToolkit(ARToolKit.getInstance());
        }

//        String address = "156.148.33.87:5555";
        String address = "172.16.4.20:5555";
        ZMQSubscriber subscriber = new ZMQSubscriber(address);
        Thread subThread = new Thread(subscriber);
        subThread.start();
        if (mOpticalARToolkit != null){
            Log.d(TAG, "setting OpticalRenderer");
            renderer = new OpticalRenderer(this, subscriber, mOpticalARToolkit);
        }
        else{
            renderer = new PubSubARRenderer(this, subscriber);
        }

        Arrow arrow = new Arrow("arrow");
        meshes.put(arrow.getId(), arrow);
        renderer.setMeshes(meshes);
    }

    protected void onStart() {
        super.onStart();
        Log.i("ARActivity", "onStart(): Activity starting.");
        if(!ARToolKit.getInstance().initialiseNative(this.getCacheDir().getAbsolutePath())) {
            (new AlertDialog.Builder(this)).setMessage("The native library is not loaded. The application cannot continue.").setTitle("Error").setCancelable(true).setNeutralButton(17039360, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    AREcoTeleconsultationActivity.this.finish();
                }
            }).show();
        } else {
            this.mainLayout = this.supplyFrameLayout();
            if(this.mainLayout == null) {
                Log.e("ARActivity", "onStart(): Error: supplyFrameLayout did not return a layout.");
            }
        }

        if (Build.MANUFACTURER.equals("EPSON") && Build.MODEL.equals("embt2")) {
            DisplayControl displayControl = new DisplayControl(this);
            boolean stereo = PreferenceManager.getDefaultSharedPreferences(this).
                    getBoolean("pref_stereoDisplay", false);
            displayControl.setMode(DisplayControl.DISPLAY_MODE_3D, stereo);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.preview = new CaptureCameraPreview(this, this);
        Log.i("ARActivity", "onResume(): CaptureCameraPreview created");
//        this.glView = new GLSurfaceView(this);
        this.glView = new TouchGLSurfaceView(this);

        ActivityManager activityManager = (ActivityManager)this.getSystemService("activity");
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 131072;
        if(supportsEs2) {
            Log.i("ARActivity", "onResume(): OpenGL ES 2.x is supported");
            if((ARRenderer)renderer instanceof ARRendererGLES20) {
                this.glView.setEGLContextClientVersion(2);
            } else {
                Log.w("ARActivity", "onResume(): OpenGL ES 2.x is supported but only a OpenGL 1.x renderer is available. \n Use ARRendererGLES20 for ES 2.x support. \n Continuing with OpenGL 1.x.");
                this.glView.setEGLContextClientVersion(1);
            }
        } else {
            Log.i("ARActivity", "onResume(): Only OpenGL ES 1.x is supported");
            if((ARRenderer)renderer instanceof ARRendererGLES20) {
                throw new RuntimeException("Only OpenGL 1.x available but a OpenGL 2.x renderer was provided.");
            }

            this.glView.setEGLContextClientVersion(1);
        }

//        this.glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        Log.d(TAG, "ready to call setRenderer with " + (renderer !=null));
        this.glView.getHolder().setFormat(-3);
        this.glView.setRenderer((PubSubARRenderer) renderer);
        Log.d(TAG, "setRenderer called");
        this.glView.setRenderMode(0);
        this.glView.setZOrderMediaOverlay(true);
        Log.i("ARActivity", "onResume(): GLSurfaceView created");
        this.mainLayout.addView(this.preview, new ViewGroup.LayoutParams(-1, -1));
        this.mainLayout.addView(this.glView, new ViewGroup.LayoutParams(-1, -1));
        Log.i("ARActivity", "onResume(): Views added to main layout.");
        if(this.glView != null) {
            this.glView.onResume();
        }

    }

    protected void onPause() {
        super.onPause();
        if(this.glView != null) {
            this.glView.onPause();
        }

        this.mainLayout.removeView(this.glView);
        this.mainLayout.removeView(this.preview);
    }

    public void onStop() {
        Log.i("ARActivity", "onStop(): Activity stopping.");
        super.onStop();
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == org.artoolkit.ar.base.R.id.settings) {
            this.startActivity(new Intent(this, CameraPreferencesActivity.class));
            return true;
        } else {
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
        if(ARToolKit.getInstance().initialiseAR(width, height, "Data/camera_para.dat", cameraIndex, cameraIsFrontFacing)) {
            Log.d(TAG, String.format("Build.MANUFACTURER %s", Build.MANUFACTURER));
            Log.d(TAG, String.format("Build.MODEL %s", Build.MODEL));

            if(mOpticalARToolkit != null){
                Log.d(TAG, "loading optical files");
                if ( mOpticalARToolkit.initialiseAR(
                        "Data/optical_param_left.dat", "Data/optical_param_right") > 0){
                    Log.d(TAG, "loaded optical files");
                    Log.d(TAG, "getEyeRproject len " + mOpticalARToolkit.getEyeRproject().length);
                }
                else {
                    Log.e("ARActivity", "Error initialising optical device. Cannot continue.");
                    this.finish();
                }
            }
            Log.i("ARActivity", "getGLView(): Camera initialised");
        } else {
            Log.e("ARActivity", "getGLView(): Error initialising camera. Cannot continue.");
            this.finish();
        }

        Toast.makeText(this, "Camera settings: " + width + "x" + height + "@" + rate + "fps", 0).show();
        this.firstUpdate = true;
    }

    public void cameraPreviewFrame(byte[] frame) {
        if(this.firstUpdate) {
            if(this.renderer.configureARScene()) {
                Log.i("ARActivity", "cameraPreviewFrame(): Scene configured successfully");
            } else {
                Log.e("ARActivity", "cameraPreviewFrame(): Error configuring scene. Cannot continue.");
                this.finish();
            }

            this.firstUpdate = false;
        }

        if(ARToolKit.getInstance().convertAndDetect(frame)) {

            if(this.glView != null) {
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

}
