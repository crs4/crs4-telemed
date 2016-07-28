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

import it.crs4.most.voip.VoipLib;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;


import android.os.Handler;
import android.os.Message;

@SuppressLint("InlinedApi")
public class EcoTeleconsultationActivity extends BaseEcoTeleconsultationActivity
        implements IStreamFragmentCommandListener, Handler.Callback {

    private static final String TAG = "EcoTeleconsultActivity";

    private StreamViewerFragment stream1Fragment = null;
    private IStream stream1 = null;

    private MenuItem butCall;
    private MenuItem butCloseSession;

    private TcStateTextView txtTcState;
    private PopupWindow popupWindow;

    private Button popupCancelButton;
    private Button popupHangupButton;
    private ToggleButton popupHoldButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teleconsultation);
        txtTcState = (TcStateTextView) findViewById(R.id.txtTcState);

        String configServerIP = QuerySettings.getConfigServerAddress(this);
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(this));
        this.rcr = new RemoteConfigReader(this, configServerIP, configServerPort);
        this.handler = new Handler(this);
        this.setupCallPopupWindow();
        this.setTeleconsultationState(TeleconsultationState.IDLE);

        Intent i = getIntent();
        teleconsultation = (Teleconsultation) i.getExtras().getSerializable("Teleconsultation");
        this.setupTeleconsultationInfo();
        this.setupStreamLib();
        this.setupVoipLib();
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
        this.teleconsultation = (Teleconsultation) i.getExtras().getSerializable("Teleconsultation");

        TextView txtEcoUser = (TextView) findViewById(R.id.txtEcoUser);
        txtEcoUser.setText(String.format("%s %s", this.teleconsultation.getApplicant().getFirstName(),
                this.teleconsultation.getApplicant().getLastName()));
    }

    protected void notifyTeleconsultationStateChanged() {

        txtTcState.setTeleconsultationState(this.mTcState);
        if (this.mTcState == TeleconsultationState.IDLE) {
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
        else if (this.mTcState == TeleconsultationState.CALLING) {
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
        else if (this.mTcState == TeleconsultationState.HOLDING) {
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
        else if (this.mTcState == TeleconsultationState.REMOTE_HOLDING) {
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
        if (this.stream1 != null && this.stream1.getState() != StreamState.PLAYING) {
            this.stream1Fragment.setStreamVisible();
            this.stream1.play();
        }
    }

    private void pauseStream() {
        if (this.stream1 != null && this.stream1.getState() == StreamState.PLAYING) {
            this.stream1.pause();
            this.stream1Fragment.setStreamInvisible("PAUSED");
        }
    }

    private void setupStreamLib() {
        String streamName = "Teleconsultation Stream";
        String streamUri = this.teleconsultation.getRoom().getCamera().getStreamUri();

        this.prepareStream(streamName, streamUri);

        this.stream1Fragment = StreamViewerFragment.newInstance(streamName);
        this.stream1Fragment.setPlayerButtonsVisible(false);

        // add the first fragment to the first container
        FragmentTransaction fragmentTransaction = getFragmentManager()
                .beginTransaction();
        fragmentTransaction.add(R.id.container_stream,
                stream1Fragment);
        fragmentTransaction.commit();
    }

    private void prepareStream(String name, String uri) {
        StreamingLib streamingLib = new StreamingLibBackend();
        HashMap<String, String> stream1_params = new HashMap<String, String>();
        stream1_params.put("name", name);
        stream1_params.put("uri", uri);

        try {

            // First of all, initialize the library
            streamingLib.initLib(this.getApplicationContext());

            this.stream1 = streamingLib.createStream(stream1_params, this.handler);
        }
        catch (Exception e) {
            Log.e(TAG, "ERROR INITIALIZING STREAM:" + e);
            e.printStackTrace();
        }

    }

    private void showCallPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) EcoTeleconsultationActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.popup_call_selection, null);
        this.popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }


    private void setupCallPopupWindow() {

        LayoutInflater inflater = (LayoutInflater) EcoTeleconsultationActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.popup_call_selection, null);

        if (popupWindow == null) {
            popupWindow = new PopupWindow(popupView,
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);


            popupCancelButton = (Button) popupView.findViewById(R.id.butCallCancel);

            popupCancelButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();

                }
            });

            popupHoldButton = (ToggleButton) popupView.findViewById(R.id.butCallHold);

            popupHoldButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    handleButHoldClicked();
                    popupWindow.dismiss();
                }
            });

            popupHangupButton = (Button) popupView.findViewById(R.id.butCallHangup);

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
        this.stream1.play();

    }

    @Override
    public void onPause(String streamId) {
        this.stream1.pause();
    }

    @Override
    public void onSurfaceViewCreated(String streamId, SurfaceView surfaceView) {
        Log.d(TAG, "Surface View created: preparing surface for stream" + streamId);
        this.stream1.prepare(surfaceView);
    }

    @Override
    public void onSurfaceViewDestroyed(String streamId) {
        this.stream1.destroy();
    }

    @Override
    public boolean handleMessage(Message streamingMessage) {
        StreamingEventBundle myEvent = (StreamingEventBundle) streamingMessage.obj;

        String infoMsg = "Event Type:" + myEvent.getEventType() + " ->" + myEvent.getEvent() + ":" + myEvent.getInfo();
        Log.d(TAG, "handleMessage: Current Event:" + infoMsg);
        Log.d(TAG, "Stream State:" + this.stream1.getState());
        return false;
    }

    // TODO: shoul be moved to Base?
    private void handleButHoldClicked() {
        if (this.mTcState != TeleconsultationState.READY && this.mTcState != TeleconsultationState.IDLE)
            toggleHoldCall(popupHoldButton.isChecked());
    }
}
