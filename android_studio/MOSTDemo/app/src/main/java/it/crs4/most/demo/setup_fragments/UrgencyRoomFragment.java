package it.crs4.most.demo.setup_fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.demo.RESTClient;
import it.crs4.most.demo.ResponseHandlerDecorator;
import it.crs4.most.demo.TeleconsultationException;
import it.crs4.most.demo.TeleconsultationSetup;
import it.crs4.most.demo.models.Room;

public class UrgencyRoomFragment extends SetupFragment {

    private static final String TAG = "UrgencyRoomFragment";
    private Spinner mUrgencySpinner;
    private Spinner mRoomSpinner;
    private RESTClient mRESTClient;

    public static SetupFragment newInstance(TeleconsultationSetup teleconsultationSetup) {
        UrgencyRoomFragment fragment = new UrgencyRoomFragment();
        Bundle args = new Bundle();
        args.putSerializable(TELECONSULTATION_SETUP, teleconsultationSetup);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String configServerIP = QuerySettings.getConfigServerAddress(getActivity());
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(getActivity()));
        mRESTClient = new RESTClient(getActivity(), configServerIP, configServerPort);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        setupUrgencySpinner(v);
        setupRoomSpinner(v);
        Button confirmButton = (Button) v.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urgency = mUrgencySpinner.getSelectedItem().toString();
                Room room = (Room) mRoomSpinner.getSelectedItem();
                mTeleconsultationSetup.setUrgency(urgency);
                mTeleconsultationSetup.setRoom(room);
                stepDone();
            }
        });
        return v;
    }

    @Override
    public void onPause() {
        mRESTClient.cancelRequests();
        super.onPause();
    }

    @Override
    protected int getTitle() {
        return R.string.urgency_and_room_selection_title;
    }

    @Override
    protected int getLayoutContent() {
        return R.layout.urgency_room_fragment;
    }

    private void setupUrgencySpinner(View view) {
        mUrgencySpinner = (Spinner) view.findViewById(R.id.urgency_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            getActivity(), R.array.urgency_values, R.layout.spinner_dropdown);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        mUrgencySpinner.setAdapter(adapter);

    }

    private void setupRoomSpinner(View view) {
        mRoomSpinner = (Spinner) view.findViewById(R.id.room_spinner);
        String accessToken = QuerySettings.getAccessToken(getActivity());

        Response.Listener<JSONObject> listener = new ResponseHandlerDecorator<>(getActivity(),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject roomsData) {
                    ArrayList<Room> rooms = new ArrayList<>();
                    try {
                        JSONArray jrooms = roomsData.getJSONObject("data").getJSONArray("rooms");

                        for (int i = 0; i < jrooms.length(); i++) {
                            JSONObject roomData = jrooms.getJSONObject(i);
                            Room r = null;
                            try {
                                r = Room.fromJSON(roomData);
                                rooms.add(r);
                            }
                            catch (TeleconsultationException e) {
                                Log.e(TAG, "There's something wrong with the Room's JSON structure");
                            }
                        }
                    }
                    catch (JSONException e) {
                        Log.e(TAG, "There's something wrong with the Room's JSON structure");
                    }

                    ArrayAdapter<Room> adapter = new ArrayAdapter<>(
                        getActivity(), R.layout.spinner_dropdown, rooms);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown);
                    mRoomSpinner.setAdapter(adapter);
                }
            });


        mRESTClient.getRooms(accessToken,
            listener,
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError arg0) {
                    Log.e(TAG, "Error retrieving rooms:" + arg0);
                }
            });
    }
}
