package it.crs4.most.demo.spec;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RESTClient;
import it.crs4.most.demo.models.ARConfiguration;
import it.crs4.most.demo.models.ARMarker;
import it.crs4.most.demo.models.Room;
import it.crs4.most.visualization.utils.zmq.ZMQPublisher;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the

 * Use the {@link ARConfigurationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ARConfigurationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PUBLISHER = "publisher";
    private static final String ARG_ROOM = "room";
    private ZMQPublisher publisher;
    private ARConfiguration arConf;
    private Room room;
    private Spinner spinner;
    private EditText transX;
    private EditText transY;

    public ARConfigurationFragment() {}

    public static ARConfigurationFragment newInstance(ZMQPublisher publisher, Room room) {
        ARConfigurationFragment fragment = new ARConfigurationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PUBLISHER, publisher);
        args.putSerializable(ARG_ROOM, room);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publisher = (ZMQPublisher) getArguments().getSerializable(ARG_PUBLISHER);
            room = (Room) getArguments().getSerializable(ARG_ROOM);
            arConf = (ARConfiguration) room.getARConfiguration();
        }
    }

    private ARMarker getMarker(){
        ARMarker marker = null;
        String markerStr = spinner.getSelectedItem().toString().toLowerCase();
        switch (markerStr){
            case "eco_marker":
                marker = arConf.getEcoMarker();
                break;
            case "keyboard_marker":
                marker = arConf.getKeyboardMarker();
                break;
            case "patient_marker":
                marker = arConf.getPatientMarker();
                break;
        }
        return marker;
    }

    private void setARParams(){
        setARParams(getMarker());
    }
    private void setARParams(ARMarker marker){
        if (marker != null){
        transX.setText(String.valueOf(marker.getTransX()));
        transY.setText(String.valueOf(marker.getTransY()));
        }
        else{
            transX.setText("");
            transY.setText("");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.ar_conf, container, false);
        spinner = (Spinner) view.findViewById(R.id.markers);
        transX = (EditText) view.findViewById(R.id.transX);
        transY = (EditText) view.findViewById(R.id.transY);


//        setARParams(marker);

        String configServerIP = QuerySettings.getConfigServerAddress(getActivity());
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(getActivity()));
        final RESTClient restClient = new RESTClient(getActivity(), configServerIP, configServerPort);
        final String accessToken = QuerySettings.getAccessToken(getActivity());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                setARParams();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        Button saveButton = (Button) view.findViewById(R.id.save_ar_conf);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String markerStr = spinner.getSelectedItem().toString().toLowerCase();
                ARMarker marker = getMarker();
                float transXInt = Float.valueOf(transX.getText().toString());
                float transYInt = Float.valueOf(transY.getText().toString());

                if (marker != null){
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("msgType", "trans");
                        obj.put("marker", marker.getConf());
                        obj.put("transX", transXInt);
                        obj.put("transY", transYInt);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    publisher.send(obj.toString());
                    restClient.setARConf(accessToken, room.getId(), markerStr, transXInt, transYInt,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d("ARCONF", "DONE");
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("ARCONF", "FAIL");
                                }
                            }
                    );
                }
            }
        });


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.Markers, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        return view;
    }
}
