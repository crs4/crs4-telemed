package it.crs4.most.demo;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.crs4.most.demo.models.User;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MostDemoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MostDemoFragment extends Fragment {

    private static final String TAG = "MostDemoFragment";
    private RemoteConfigReader mRemCfg;
    private String mServerIP;
    private int mServerPort;
    private String mTaskGroup;
    private String mRole;
    private TextView mMsgText;
    private String mAccessToken;
    private String mUser;

    public MostDemoFragment() {
        // Required empty public constructor
    }

    public static MostDemoFragment newInstance() {
        return new MostDemoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_most_demo, container, false);
        mMsgText = (TextView) v.findViewById(R.id.msg_text);
        mServerIP = QuerySettings.getConfigServerAddress(getActivity());
        mServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(getActivity()));
        mTaskGroup = QuerySettings.getTaskGroup(getActivity());
        mRole = QuerySettings.getRole(getActivity());

        if (checkSettings()) {
            mRemCfg = new RemoteConfigReader(getActivity(), mServerIP, mServerPort);
            mUser = QuerySettings.getUser(getActivity());
            mAccessToken = QuerySettings.getAccessToken(getActivity());
            if (mAccessToken == null) {
                mMsgText.setText(R.string.login_instructions);
                mMsgText.setVisibility(View.VISIBLE);
            }
        }

        return v;
    }

    public boolean checkSettings() {
        if (mServerIP == null || mTaskGroup == null || mRole == null) {
            String msgParts = "";
            int nullCounter = 0;
            if (mServerIP == null) {
                msgParts += getString(R.string.set_server) + ", ";
                nullCounter += 2; // Server and port
            }
            if (mTaskGroup == null) {
                msgParts += getString(R.string.select_taskgroup) + ", ";
                nullCounter++;
            }
            if (mRole == null) {
                msgParts += getString(R.string.select_role) + ", ";
                nullCounter++;
            }
            msgParts = msgParts.substring(0, msgParts.length() - 2);
            if (nullCounter > 1) {
                int pos = msgParts.lastIndexOf(",");
                msgParts = new StringBuilder(msgParts).replace(pos, pos + 1, " and").toString();
            }
            String msg = String.format(getString(R.string.most_demo_fragment_instructions), msgParts);
            mMsgText.setText(msg);
            mMsgText.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

}
