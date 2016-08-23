package it.crs4.most.demo.eco;


import java.util.HashMap;

import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RemoteConfigReader;
import it.crs4.most.demo.TeleconsultationState;
import it.crs4.most.streaming.IStream;
import it.crs4.most.streaming.StreamingLib;
import it.crs4.most.streaming.StreamingLibBackend;
import it.crs4.most.streaming.enums.StreamState;
import it.crs4.most.visualization.IStreamFragmentCommandListener;
import it.crs4.most.visualization.StreamViewerFragment;

import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.ui.TcStateTextView;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

@SuppressLint("InlinedApi")
public class EcoTeleconsultationActivity extends BaseEcoTeleconsultationActivity
        implements IStreamFragmentCommandListener {

    private static final String TAG = "EcoTeleconsultActivity";

    public static final String TELECONSULTATION_ARG = "Teleconsultation";
    public static final int TELECONSULT_ENDED_REQUEST = 0;

    private StreamViewerFragment mStreamCameraFragment;
    private IStream mStreamCamera;

    private MenuItem mButCall;
    private TcStateTextView txtTcState;
    private View mPopupView;
    private PopupWindow popupWindow;

    private ImageButton popupHangupButton;
    private ImageButton popupHoldButton;
    private boolean mIsOnHold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teleconsultation);
        txtTcState = (TcStateTextView) findViewById(R.id.txtTcState);

        String configServerIP = QuerySettings.getConfigServerAddress(this);
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(this));
        mConfigReader = new RemoteConfigReader(this, configServerIP, configServerPort);
        Intent i = getIntent();
        teleconsultation = (Teleconsultation) i.getExtras().getSerializable(TELECONSULTATION_ARG);
        mIsOnHold = false;
        setupCallPopupWindow();
        setTeleconsultationState(TeleconsultationState.IDLE);
        setupStreamLib();
        setupVoipLib();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.teleconsultation_eco_menu, menu);
        boolean res = super.onCreateOptionsMenu(menu);
        mButCall = menu.findItem(R.id.button_call);
        return res;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_call:
                showCallPopupWindow();
                break;
            case R.id.button_exit:
                exitFromApp();
                break;
        }
        return true;
    }

    private void setupTeleconsultationInfo() {
        Intent i = getIntent();
        teleconsultation = (Teleconsultation) i.getExtras().getSerializable(TELECONSULTATION_ARG);

        TextView textUser = (TextView) findViewById(R.id.text_eco_user);
        textUser.setText(String.format("%s %s", teleconsultation.getUser().getFirstName(),
                teleconsultation.getUser().getLastName()));
    }

    protected void notifyTeleconsultationStateChanged() {
        txtTcState.setTeleconsultationState(mTcState);
        if (mTcState == TeleconsultationState.IDLE) {
            try {
                mButCall.setEnabled(false);
            }
            catch (NullPointerException ex) {

            }
            popupHoldButton.setEnabled(false);
            popupHangupButton.setEnabled(false);

            localHold = false;
            accountRegistered = false;
            remoteHold = false;
            pauseStream();
        }
        else if (mTcState == TeleconsultationState.READY) {
            try {
                mButCall.setEnabled(false);
            }
            catch (NullPointerException ex) {}
            popupHoldButton.setEnabled(false);
            popupHangupButton.setEnabled(false);

            localHold = false;
            accountRegistered = true;
            remoteHold = false;
            pauseStream();
        }
        else if (mTcState == TeleconsultationState.CALLING) {
            try {
                mButCall.setEnabled(true);
            }
            catch (NullPointerException ne) {}
            popupHoldButton.setEnabled(true);
            popupHangupButton.setEnabled(true);

            remoteHold = false;
            localHold = false;
            playStream();
        }
        else if (mTcState == TeleconsultationState.HOLDING) {
            try {
                mButCall.setEnabled(true);
            }
            catch (NullPointerException ne) {}
            popupHoldButton.setEnabled(true);
            popupHangupButton.setEnabled(true);

            localHold = true;
            pauseStream();
        }
        else if (mTcState == TeleconsultationState.REMOTE_HOLDING) {
            try {
                mButCall.setEnabled(true);
            }
            catch (NullPointerException ne) {}
            popupHoldButton.setEnabled(false);
            popupHangupButton.setEnabled(true);
            remoteHold = true;
            pauseStream();
        }
        else if (mTcState == TeleconsultationState.FINISHED) {
            try {
                mButCall.setEnabled(false);
                popupHangupButton.setEnabled(false);
                popupHoldButton.setEnabled(false);
            }
            catch (NullPointerException ne) {}
            stopStream();
        }
    }

    private void playStream() {
        if (mStreamCamera != null && mStreamCamera.getState() != StreamState.PLAYING) {
            mStreamCamera.play();
            mStreamCameraFragment.setStreamVisible();
        }
    }

    private void pauseStream() {
        if (mStreamCamera != null && mStreamCamera.getState() == StreamState.PLAYING) {
            mStreamCamera.pause();
            mStreamCameraFragment.setStreamInvisible(getString(R.string.paused));
        }
    }

    private void stopStream() {
        if (mStreamCamera != null) {
            mStreamCamera.destroy();
            mStreamCameraFragment.setStreamInvisible("");
        }
    }

    private void setupStreamLib() {
        mStreamHandler = new StreamHandler();

        String streamName = "Teleconsultation Stream";
        String streamUri = teleconsultation.getLastSession().getRoom().getCamera().getStreamUri();
        StreamingLib streamingLib = new StreamingLibBackend();
        HashMap<String, String> cameraStream = new HashMap<>();
        cameraStream.put("name", streamName);
        cameraStream.put("uri", streamUri);

        try {
            streamingLib.initLib(getApplicationContext());
            mStreamCamera = streamingLib.createStream(cameraStream, mStreamHandler);
        }
        catch (Exception e) {
            Log.e(TAG, "Error initializing the stream:" + e);
            e.printStackTrace();
        }

        mStreamCameraFragment = StreamViewerFragment.newInstance(streamName);
        mStreamCameraFragment.setPlayerButtonsVisible(false);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container_stream, mStreamCameraFragment);
        fragmentTransaction.commit();
    }

    private void showCallPopupWindow() {
        popupWindow.showAtLocation(mPopupView, Gravity.CENTER, 0, 0);
    }


    private void setupCallPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mPopupView = inflater.inflate(R.layout.popup_call_selection, null);

        if (popupWindow == null) {
            popupWindow = new PopupWindow(mPopupView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
            Button popupCancelButton = (Button) mPopupView.findViewById(R.id.button_call_cancel);
            popupCancelButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });

            popupHoldButton = (ImageButton) mPopupView.findViewById(R.id.button_call_hold);
            popupHoldButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleButHoldClicked();
                    popupWindow.dismiss();
                }
            });

            popupHangupButton = (ImageButton) mPopupView.findViewById(R.id.button_call_hangup);
            popupHangupButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    hangupCall();
                    popupWindow.dismiss();
                }
            });

            popupWindow.setTouchable(true);
            popupWindow.setFocusable(true);
        }
    }

    @Override
    public void onPlay(String streamId) {
        mStreamCamera.play();
    }

    @Override
    public void onPause(String streamId) {
        mStreamCamera.pause();
    }

    @Override
    public void onSurfaceViewCreated(String streamId, SurfaceView surfaceView) {
        mStreamCamera.prepare(surfaceView);
    }

    @Override
    public void onSurfaceViewDestroyed(String streamId) {
        mStreamCamera.destroy();
    }

    private void handleButHoldClicked() {
        if (mTcState != TeleconsultationState.READY && mTcState != TeleconsultationState.IDLE) {
            mIsOnHold = !mIsOnHold;
            if (mIsOnHold) {
                popupHoldButton.setImageResource(R.drawable.ic_call_white_36dp);
            }
            else {
                popupHoldButton.setImageResource(R.drawable.ic_phone_paused_white_36dp);
            }
            toggleHoldCall(mIsOnHold);
        }
    }
}
