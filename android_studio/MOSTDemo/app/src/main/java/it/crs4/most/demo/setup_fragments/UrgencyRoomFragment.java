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
import it.crs4.most.demo.RemoteConfigReader;
import it.crs4.most.demo.TeleconsultationException;
import it.crs4.most.demo.TeleconsultationSetup;
import it.crs4.most.demo.models.Room;

public class UrgencyRoomFragment extends SetupFragment {

    private static final String TAG = "UrgencyRoomFragment";
    private Spinner mUrgencySpinner;
    private Spinner mRoomSpinner;
    private RemoteConfigReader mRemCfg;

    public static SetupFragment newInstance(TeleconsultationSetup teleconsultationSetup) {
        UrgencyRoomFragment f = new UrgencyRoomFragment();
        f.setTeleconsultationSetup(teleconsultationSetup);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String configServerIP = QuerySettings.getConfigServerAddress(getActivity());
        int configServerPort = Integer.valueOf(QuerySettings.getConfigServerPort(getActivity()));
        mRemCfg = new RemoteConfigReader(getActivity(), configServerIP, configServerPort);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.urgency_room_fragment, container, false);
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
                mTeleconsultationSetup.nextStep();
            }
        });
        return v;
    }

    @Override
    public void onShow() {

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
        mRemCfg.getRooms(accessToken,
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
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError arg0) {
                    Log.e(TAG, "Error retrieving rooms:" + arg0);
                }
            });
    }
}
