package it.crs4.most.demo.ecoapp;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import it.crs4.most.streaming.IStream;
import it.crs4.most.streaming.StreamingEventBundle;
import it.crs4.most.streaming.StreamingLib;
import it.crs4.most.streaming.StreamingLibBackend;
import it.crs4.most.streaming.enums.StreamState;
import it.crs4.most.visualization.IStreamFragmentCommandListener;
import it.crs4.most.visualization.StreamViewerFragment;

import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import it.crs4.most.demo.ecoapp.models.EcoUser;
import it.crs4.most.demo.ecoapp.models.Teleconsultation;
import it.crs4.most.demo.ecoapp.ui.TcStateTextView;
import it.crs4.most.demo.ecoapp.TeleconsultationState;

import it.crs4.most.voip.Utils;
import it.crs4.most.voip.VoipEventBundle;
import it.crs4.most.voip.VoipLib;
import it.crs4.most.voip.VoipLibBackend;
import it.crs4.most.voip.enums.CallState;
import it.crs4.most.voip.enums.VoipEvent;
import it.crs4.most.voip.enums.VoipEventType;
import it.crs4.most.voip.interfaces.IBuddy;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;


import android.os.Handler;
import android.os.Message;

@SuppressLint("InlinedApi")
public class EcoTeleconsultationActivity extends BaseEcoTeleconsultationActivity
        implements IStreamFragmentCommandListener, Handler.Callback{

    private static final String TAG = "EcoTeleconsultActivity";

    private StreamViewerFragment stream1Fragment = null;
    private IStream stream1 = null;
    private ProgressDialog progressWaitingSpec;

    private TeleconsultationState tcState = TeleconsultationState.IDLE;
    private TcStateTextView txtTcState;
    private ImageButton butCall;
    private Button butCloseSession;

    private String sipServerIp;
    private String sipServerPort;

    private VoipLib myVoip;
    private CallHandler voipHandler;
    private PopupWindow popupWindow;

    private Button popupCancelButton;
    private Button popupHangupButton;
    private ToggleButton popupHoldButton;

    private boolean localHold = false;
    private boolean remoteHold = false;

    private boolean accountRegistered = false;
    private boolean exitFromAppRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teleconsultation);
        txtTcState = (TcStateTextView) findViewById(R.id.txtTcState);

        String configServerIP = QuerySettings.getConfigServerAddress(this);
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(this));
        this.rcr = new RemoteConfigReader(this, configServerIP, configServerPort);
        this.handler = new Handler(this);
//        init();
        this.setupActionBar();
        this.setupCallPopupWindow();
        this.setTeleconsultationState(TeleconsultationState.IDLE);

        Intent i = getIntent();
        teleconsultation = (Teleconsultation) i.getExtras().getSerializable("Teleconsultation");
        this.setupTeleconsultationInfo();

        this.setupStreamLib();
        this.setupVoipLib();
    }

    private void setupTeleconsultationInfo() {
        Intent i = getIntent();
        this.teleconsultation = (Teleconsultation) i.getExtras().getSerializable("Teleconsultation");

        TextView txtEcoUser = (TextView) findViewById(R.id.txtEcoUser);
        txtEcoUser.setText(String.format("%s %s", this.teleconsultation.getApplicant().getFirstName(),
                this.teleconsultation.getApplicant().getLastName()));
    }

    protected void notifyTeleconsultationStateChanged() {

        txtTcState.setTeleconsultationState(this.tcState);
        if (this.tcState == TeleconsultationState.IDLE) {
            butCall.setEnabled(false);
            butCloseSession.setEnabled(false);
            popupCancelButton.setEnabled(true);

            popupHoldButton.setEnabled(false);
            popupHangupButton.setEnabled(false);

            localHold = false;
            accountRegistered = false;
            remoteHold = false;
            pauseStream();
        }
        else if (tcState == TeleconsultationState.READY) {
            butCall.setEnabled(false);
            butCloseSession.setEnabled(true);
            popupCancelButton.setEnabled(true);

            popupHoldButton.setEnabled(false);
            popupHangupButton.setEnabled(false);

            localHold = false;
            accountRegistered = true;
            remoteHold = false;
            pauseStream();
        }
        else if (this.tcState == TeleconsultationState.CALLING) {
            butCall.setEnabled(true);
            butCloseSession.setEnabled(false);
            popupCancelButton.setEnabled(true);
            popupHoldButton.setEnabled(true);

            popupHangupButton.setEnabled(true);
            remoteHold = false;
            localHold = false;
            playStream();
        }
        else if (this.tcState == TeleconsultationState.HOLDING) {
            butCall.setEnabled(true);
            butCloseSession.setEnabled(false);
            popupCancelButton.setEnabled(true);
            popupHoldButton.setEnabled(true);
            popupHangupButton.setEnabled(true);

            localHold = true;
            pauseStream();
        }
        else if (this.tcState == TeleconsultationState.REMOTE_HOLDING) {
            butCall.setEnabled(true);
            butCloseSession.setEnabled(false);
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

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.actionbar_view);
        butCall = (ImageButton) actionBar.getCustomView().findViewById(R.id.butCallActionBar);
        butCall.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showCallPopupWindow();
            }
        });

        ImageButton butExit = (ImageButton) actionBar.getCustomView().findViewById(R.id.butExit);
        butExit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                exitFromApp();

            }
        });

        butCloseSession = (Button) actionBar.getCustomView().findViewById(R.id.butCloseSession);

        butCloseSession.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                closeSession();

            }
        });

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME);

    }

    private void showCallPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) EcoTeleconsultationActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.popup_call_selection,
                null);
        this.popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    private void setupCallPopupWindow() {

        LayoutInflater inflater = (LayoutInflater) EcoTeleconsultationActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.popup_call_selection,
                null);

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


    private void waitForSpecialist() {
        //Toast.makeText(EcoConfigActivity.this, "Connecting to:" + deviceName + "(" + macAddress +")" , Toast.LENGTH_LONG).show();
        progressWaitingSpec = new ProgressDialog(EcoTeleconsultationActivity.this);
        progressWaitingSpec.setTitle("Preparing Teleconsultation Session");
        progressWaitingSpec.setMessage("Waiting for specialist...");
        progressWaitingSpec.setCancelable(false);
        progressWaitingSpec.setCanceledOnTouchOutside(false);
        progressWaitingSpec.show();

    }

// VOIP METHODS AND LOGIC


    private void handleButHoldClicked() {
        if (this.tcState != TeleconsultationState.READY && this.tcState != TeleconsultationState.IDLE)
            toggleHoldCall(popupHoldButton.isChecked());
    }
}
