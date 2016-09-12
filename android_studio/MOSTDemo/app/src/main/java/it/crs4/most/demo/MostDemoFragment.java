package it.crs4.most.demo;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MostDemoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MostDemoFragment extends Fragment {

    private static final String TAG = "MostDemoFragment";
    private RemoteConfigReader mRemCfg;

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
        String cfgServerIP = QuerySettings.getConfigServerAddress(getActivity());
        int cfgServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(getActivity()));
        String taskGroup = QuerySettings.getTaskGroup(getActivity());
        String role = QuerySettings.getRole(getActivity());
        if (cfgServerIP == null || taskGroup == null || role == null) {
            TextView msgText = (TextView) v.findViewById(R.id.msg_text);
            String msgParts = "";
            int nullCounter = 0;
            if (cfgServerIP == null) {
                msgParts += getString(R.string.set_server) + ", ";
                nullCounter += 2; // Server and port
            }
            if (taskGroup == null) {
                msgParts += getString(R.string.select_taskgroup) + ", ";
                nullCounter++;
            }
            if (role == null) {
                msgParts += getString(R.string.select_role) + ", ";
                nullCounter++;
            }
            msgParts = msgParts.substring(0, msgParts.length() - 2);
            if (nullCounter > 1) {
                int pos = msgParts.lastIndexOf(",");
                msgParts = new StringBuilder(msgParts).replace(pos, pos + 1, " and").toString();
            }
            String msg = String.format(getString(R.string.most_demo_fragment_instructions), msgParts);
            msgText.setText(msg);
            msgText.setVisibility(View.VISIBLE);
        }
        else {
            mRemCfg = new RemoteConfigReader(getActivity(), cfgServerIP, cfgServerPort);
            Log.d(TAG, "Server set. Proceed to login");
        }

        return v;
    }

}
