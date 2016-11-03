package it.crs4.most.demo;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.format.Formatter;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import it.crs4.most.demo.models.Teleconsultation;
import it.crs4.most.demo.models.TeleconsultationSessionState;
import it.crs4.most.demo.setup_fragments.SetupFragment;
import it.crs4.most.demo.setup_fragments.TeleconsultationSelectionFragment;
import it.crs4.most.demo.spec.SpecTeleconsultationActivity;

class SpecTeleconsultationController extends TeleconsultationController {
    private static final String TAG = "SpecTeleconsultSetup";
    private final TeleconsultationSetup mTeleconsultationSetup;

    SpecTeleconsultationController(FragmentManager fm, TeleconsultationSetup teleconsultationSetup) {
        super(fm);
        mTeleconsultationSetup = teleconsultationSetup;
    }

    @Override
    public void startTeleconsultationActivity(final Activity callingActivity, final Teleconsultation teleconsultation) {
        WifiManager wifiManager = (WifiManager) callingActivity.getSystemService(Activity.WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress()) +
            ":" + SpecTeleconsultationActivity.ZMQ_LISTENING_PORT;

        String configServerIP = QuerySettings.getConfigServerAddress(callingActivity);
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(callingActivity));
        RESTClient mConfigReader = new RESTClient(callingActivity, configServerIP, configServerPort);
        String accessToken = QuerySettings.getAccessToken(callingActivity);
        if (teleconsultation.getLastSession().getState() == TeleconsultationSessionState.WAITING) {
            mConfigReader.joinSession(teleconsultation.getLastSession().getId(),
                accessToken,
                ipAddress,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject sessionData;
                        try {
                            sessionData = response.getJSONObject("data").getJSONObject("session");
                            String role = QuerySettings.getRole(callingActivity);
                            // Only in this mooment the voipParam are complete with specialist information, so we set them
                            teleconsultation.getLastSession().setVoipParams(callingActivity, sessionData, role);
                            Intent i = new Intent(callingActivity,
                                SpecTeleconsultationActivity.class);
                            i.putExtra(SpecTeleconsultationActivity.TELECONSULTATION_ARG, teleconsultation);
                            callingActivity.startActivityForResult(i, SpecTeleconsultationActivity.TELECONSULT_ENDED_REQUEST);
                        }
                        catch (JSONException e) {
                            Log.e(TAG, "Something wrong happened with the JSON structure");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError err) {
                        Log.d(TAG, "Error in Session Join Response: " + err);
                    }
                });
        }

    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return TeleconsultationSelectionFragment.newInstance(mTeleconsultationSetup);
        }
        return null;
    }

    @Override
    public int getCount() {
        return 1;
    }
}
