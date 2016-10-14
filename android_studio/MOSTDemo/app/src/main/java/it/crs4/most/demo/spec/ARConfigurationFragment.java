package it.crs4.most.demo.spec;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import it.crs4.most.demo.R;
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
    private static final String ARG_CONF = "room";
    private ZMQPublisher publisher;
    private ARConfiguration arConf;




//    private OnFragmentInteractionListener mListener;

    public ARConfigurationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ARConfigurationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ARConfigurationFragment newInstance(ZMQPublisher publisher, ARConfiguration arConf) {
        ARConfigurationFragment fragment = new ARConfigurationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PUBLISHER, publisher);
        args.putSerializable(ARG_CONF, arConf);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publisher = (ZMQPublisher) getArguments().getSerializable(ARG_PUBLISHER);
            arConf = (ARConfiguration) getArguments().getSerializable(ARG_CONF);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.ar_conf, container, false);
        final Spinner spinner = (Spinner) view.findViewById(R.id.markers);
        final EditText transX = (EditText) view.findViewById(R.id.transX);
        final EditText transY = (EditText) view.findViewById(R.id.transY);

        Button saveButton = (Button) view.findViewById(R.id.save_ar_conf);


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARMarker marker = null;

                switch (spinner.getSelectedItem().toString().toLowerCase()){
                    case "eco marker":
                        marker = arConf.getEcoMarker();
                        break;
                    case "keyboard marker":
                        marker = arConf.getKeyboardMarker();
                        break;
                    case "patient marker":
                        marker = arConf.getPatientMarker();
                        break;
                }
                if (marker != null){
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("msgType", "trans");
                        obj.put("marker", marker.getConf());
                        obj.put("transX", Integer.valueOf(transX.getText().toString()));
                        obj.put("transY", Integer.valueOf(transY.getText().toString()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    publisher.send(obj.toString());

                }

            }
        });


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.Markers, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        return view;
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
