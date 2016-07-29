package it.crs4.most.demo.ecoapp;


import java.util.HashMap;

import it.crs4.most.streaming.IStream;
import it.crs4.most.streaming.StreamingEventBundle;
import it.crs4.most.streaming.StreamingLib;
import it.crs4.most.streaming.StreamingLibBackend;
import it.crs4.most.streaming.enums.StreamState;
import it.crs4.most.visualization.IStreamFragmentCommandListener;
import it.crs4.most.visualization.StreamViewerFragment;

import it.crs4.most.demo.ecoapp.models.Teleconsultation;
import it.crs4.most.demo.ecoapp.ui.TcStateTextView;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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


import android.os.Handler;
import android.os.Message;

@SuppressLint("InlinedApi")
public class EcoTeleconsultationActivity extends BaseEcoTeleconsultationActivity
        implements IStreamFragmentCommandListener, Handler.Callback {

    private static final String TAG = "EcoTeleconsultActivity";

    private StreamViewerFragment mStreamCameraFragment;
    private IStream mStreamCamera;

    private MenuItem butCall;
    private MenuItem butCloseSession;
    private TcStateTextView txtTcState;
    private View mPopupView;
    private PopupWindow popupWindow;

    private Button popupCancelButton;
    private ImageButton popupHangupButton;
    private ImageButton popupHoldButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teleconsultation);
        txtTcState = (TcStateTextView) findViewById(R.id.txtTcState);

        String configServerIP = QuerySettings.getConfigServerAddress(this);
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(this));
        mConfigReader = new RemoteConfigReader(this, configServerIP, configServerPort);
        handler = new Handler(this);
        setupCallPopupWindow();
        setTeleconsultationState(TeleconsultationState.IDLE);

        Intent i = getIntent();
        teleconsultation = (Teleconsultation) i.getExtras().getSerializable("Teleconsultation");
        setupTeleconsultationInfo();
        setupStreamLib();
        setupVoipLib();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.teleconsultation_menu, menu);
        boolean res = super.onCreateOptionsMenu(menu);
        butCall = menu.findItem(R.id.button_call);
        butCloseSession = menu.findItem(R.id.button_close_session);
        return res;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_close_session:
                closeSession();
                break;
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
        teleconsultation = (Teleconsultation) i.getExtras().getSerializable("Teleconsultation");

        TextView txtEcoUser = (TextView) findViewById(R.id.txtEcoUser);
        txtEcoUser.setText(String.format("%s %s", teleconsultation.getApplicant().getFirstName(),
                teleconsultation.getApplicant().getLastName()));
    }

    protected void notifyTeleconsultationStateChanged() {

        txtTcState.setTeleconsultationState(mTcState);
        if (mTcState == TeleconsultationState.IDLE) {
            try {
                butCall.setEnabled(false);
                butCloseSession.setEnabled(false);
            }
            catch (NullPointerException ex) {

            }
            popupCancelButton.setEnabled(true);
            popupHoldButton.setEnabled(false);
            popupHangupButton.setEnabled(false);

            localHold = false;
            accountRegistered = false;
            remoteHold = false;
            pauseStream();
        }
        else if (mTcState == TeleconsultationState.READY) {
            try {
                butCall.setEnabled(false);
                butCloseSession.setEnabled(true);
            }
            catch (NullPointerException ex) {

            }
            popupCancelButton.setEnabled(true);
            popupHoldButton.setEnabled(false);
            popupHangupButton.setEnabled(false);

            localHold = false;
            accountRegistered = true;
            remoteHold = false;
            pauseStream();
        }
        else if (mTcState == TeleconsultationState.CALLING) {
            try {
                butCall.setEnabled(true);
                butCloseSession.setEnabled(false);

            }
            catch (NullPointerException ne) {}
            popupCancelButton.setEnabled(true);
            popupHoldButton.setEnabled(true);
            popupHangupButton.setEnabled(true);

            remoteHold = false;
            localHold = false;
            playStream();
        }
        else if (mTcState == TeleconsultationState.HOLDING) {
            try {
                butCall.setEnabled(true);
                butCloseSession.setEnabled(false);
            }
            catch (NullPointerException ne) {}
            popupCancelButton.setEnabled(true);
            popupHoldButton.setEnabled(true);
            popupHangupButton.setEnabled(true);

            localHold = true;
            pauseStream();
        }
        else if (mTcState == TeleconsultationState.REMOTE_HOLDING) {
            try {
                butCall.setEnabled(true);
                butCloseSession.setEnabled(false);
            }
            catch (NullPointerException ne) {

            }
            popupCancelButton.setEnabled(true);
            popupHoldButton.setEnabled(true);
            popupHangupButton.setEnabled(true);

            remoteHold = true;
            pauseStream();
        }
    }

    private void playStream() {
        if (mStreamCamera != null && mStreamCamera.getState() != StreamState.PLAYING) {
            mStreamCameraFragment.setStreamVisible();
            mStreamCamera.play();
        }
    }

    private void pauseStream() {
        if (mStreamCamera != null && mStreamCamera.getState() == StreamState.PLAYING) {
            mStreamCamera.pause();
            mStreamCameraFragment.setStreamInvisible("PAUSED");
        }
    }

    private void setupStreamLib() {
        String streamName = "Teleconsultation Stream";
        String streamUri = teleconsultation.getRoom().getCamera().getStreamUri();

        prepareStream(streamName, streamUri);

        mStreamCameraFragment = StreamViewerFragment.newInstance(streamName);
        mStreamCameraFragment.setPlayerButtonsVisible(false);

        // add the first fragment to the first container
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container_stream, mStreamCameraFragment);
        fragmentTransaction.commit();
    }

    private void prepareStream(String name, String uri) {
        StreamingLib streamingLib = new StreamingLibBackend();
        HashMap<String, String> stream1_params = new HashMap<>();
        stream1_params.put("name", name);
        stream1_params.put("uri", uri);

        try {

            // First of all, initialize the library
            streamingLib.initLib(getApplicationContext());

            mStreamCamera = streamingLib.createStream(stream1_params, handler);
        }
        catch (Exception e) {
            Log.e(TAG, "ERROR INITIALIZING STREAM:" + e);
            e.printStackTrace();
        }

    }

    private void showCallPopupWindow() {
        Log.d(TAG, "Opening Layout Window");
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        View popupView = inflater.inflate(R.layout.popup_call_selection, null);
        popupWindow.showAtLocation(mPopupView, Gravity.CENTER, 0, 0);
    }


    private void setupCallPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mPopupView = inflater.inflate(R.layout.popup_call_selection, null);

        if (popupWindow == null) {
            popupWindow = new PopupWindow(mPopupView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
            popupCancelButton = (Button) mPopupView.findViewById(R.id.button_call_cancel);
            popupCancelButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });

            popupHoldButton = (ImageButton) mPopupView.findViewById(R.id.buttton_call_hold);
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
        Log.d(TAG, "Surface View created: preparing surface for stream" + streamId);
        mStreamCamera.prepare(surfaceView);
    }

    @Override
    public void onSurfaceViewDestroyed(String streamId) {
        mStreamCamera.destroy();
    }

    @Override
    public boolean handleMessage(Message streamingMessage) {
        StreamingEventBundle myEvent = (StreamingEventBundle) streamingMessage.obj;

        String infoMsg = "Event Type:" + myEvent.getEventType() + " ->" + myEvent.getEvent() + ":" + myEvent.getInfo();
        Log.d(TAG, "handleMessage: Current Event:" + infoMsg);
        Log.d(TAG, "Stream State:" + mStreamCamera.getState());
        return false;
    }

    // TODO: should be moved to Base?
    private void handleButHoldClicked() {
//        if (mTcState != TeleconsultationState.READY && mTcState != TeleconsultationState.IDLE)
//            toggleHoldCall(popupHoldButton.isChecked());
    }
}
