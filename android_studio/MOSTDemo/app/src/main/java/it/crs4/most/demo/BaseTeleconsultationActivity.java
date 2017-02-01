package it.crs4.most.demo;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.artoolkit.ar.base.assets.AssetHelper;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.crs4.most.demo.eco.BaseEcoTeleconsultationActivity;
import it.crs4.most.demo.models.ARConfiguration;
import it.crs4.most.demo.models.ARMarker;
import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.visualization.augmentedreality.MarkerFactory;
import it.crs4.most.visualization.augmentedreality.mesh.Mesh;
import it.crs4.most.visualization.augmentedreality.mesh.MeshManager;
import it.crs4.most.voip.VoipLib;
import it.crs4.most.voip.VoipLibBackend;

public abstract class BaseTeleconsultationActivity extends AppCompatActivity {
    public static final String TELECONSULTATION_ARG = "teleconsultation";
    protected TeleconsultationState mTcState = TeleconsultationState.IDLE;
    protected HashMap<String, String> mVoipParams;
    protected Teleconsultation teleconsultation;
    protected RESTClient mRESTClient;
    protected String mSipServerIp;
    protected String mSipServerPort;
    protected VoipLib mVoipLib;
    protected AudioManager mAudioManager;
    protected int mOriginalAudioMode;
    protected abstract void notifyTeleconsultationStateChanged();
    protected abstract Handler getVoipHandler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mOriginalAudioMode = mAudioManager.getMode();

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
        setTeleconsultationState(TeleconsultationState.IDLE);
        Intent i = getIntent();
        teleconsultation = (Teleconsultation) i.getExtras().getSerializable(TELECONSULTATION_ARG);
        setupVoipLib();
    }

    protected  void setTeleconsultationState(TeleconsultationState tcState) {
        mTcState = tcState;
        notifyTeleconsultationStateChanged();
    }

    protected void setupVoipLib() {
        // Voip Lib Initialization Params
        mVoipParams = teleconsultation.getLastSession().getVoipParams();
        mSipServerIp = mVoipParams.get("sipServerIp");
        mSipServerPort = mVoipParams.get("sipServerPort");
        mVoipLib = new VoipLibBackend();
        mVoipLib.initLib(getApplicationContext(), mVoipParams, getVoipHandler());
    }

    protected void createARMeshes(MeshManager meshManager){
        float [] redColor = new float []{
                0, 0, 0, 1f,
                1, 0, 0, 1f,
                1, 0, 0, 1f,
                1, 0, 0, 1f,
                1, 0, 0, 1f
        };

        Map<String, Mesh> meshes = new HashMap<>();
        ARConfiguration arConf = teleconsultation.getLastSession().getRoom().getARConfiguration();
        if (arConf != null){
            for (ARMarker markerModel: arConf.getMarkers()){
                MarkerFactory.Marker marker = MarkerFactory.getMarker(markerModel.getConf());
                float [] trans = new float[16];
                Matrix.setIdentityM(trans, 0);
                trans[12] = markerModel.getTransX();
                trans[13] = markerModel.getTransY();
                marker.setModelMatrix(trans);

                it.crs4.most.demo.models.Mesh meshModel = markerModel.getMesh();
                Mesh mesh;
                if (meshes.containsKey(meshModel.getName())) {
                    mesh = meshes.get(meshModel.getName());
                }
                else {
                    try {

                        Class clsMesh = Class.forName(meshModel.getCls());
                        Class[] cArg = new Class[] {
                                float.class, float.class, float.class, String.class
                        };
                        mesh = (Mesh) clsMesh.getDeclaredConstructor(cArg).newInstance(
                                meshModel.getSizeX(),
                                meshModel.getSizeY(),
                                meshModel.getSizeZ(),
                                meshModel.getName()
                        );
                        meshes.put(meshModel.getName(), mesh);
                        mesh.setColors(redColor);
                        meshManager.addMesh(mesh);

                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                mesh.addMarker(marker);
            }
        }

    }



}
