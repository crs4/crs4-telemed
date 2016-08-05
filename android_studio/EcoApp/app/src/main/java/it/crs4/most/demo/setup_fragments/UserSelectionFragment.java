package it.crs4.most.demo.setup_fragments;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import it.crs4.most.demo.ConfigFragment;
import it.crs4.most.demo.IConfigBuilder;
import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.demo.models.User;
import it.crs4.most.demo.models.TaskGroup;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class UserSelectionFragment extends ConfigFragment {
    protected static final String TAG = "UserSelectionFragment";

    private ArrayList<User> mEcoArray;
    private ArrayAdapter<User> mEcoArrayAdapter;
    private TaskGroup mSelectedTaskgroup;

    public static UserSelectionFragment newInstance(IConfigBuilder config) {
        UserSelectionFragment fragment = new UserSelectionFragment();
        fragment.setConfigBuilder(config);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.eco_list, container, false);
        mSelectedTaskgroup = new TaskGroup(QuerySettings.getTaskGroup(getActivity()), null);
        mEcoArray = new ArrayList<>();
        mEcoArrayAdapter = new UserArrayAdapter(this, R.layout.eco_row, mEcoArray);

        ListView listView = (ListView) view.findViewById(R.id.operator_list);
        listView.setAdapter(mEcoArrayAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = mEcoArray.get(position);
                getConfigBuilder().setUser(selectedUser);
            }
        });

        retrieveUsers();

        return view;
    }

    /**
     * Retrieves the applicants associated to a specific taskgroup
     */
    private void retrieveUsers() {
        String taskgroupId = QuerySettings.getTaskGroup(getActivity());
        if (taskgroupId != null) {
            getConfigBuilder().getRemoteConfigReader().
                getUsersByTaskgroup(taskgroupId,
                    new Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject users) {
                            retrieveSelectedUser(users);
                        }
                    },
                    new ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            Log.e(TAG, "Error retrieving the taskgroup users: " + arg0);
                            //mLoadingConfigDialog.setMessage("No users found for the selected taskgroup: " + arg0);
                            // [TODO] Handle the error
                        }
                    });
        }
    }

    private void retrieveSelectedUser(final JSONObject users_data) {
        try {
            boolean success = (users_data != null && users_data.getBoolean("success"));
            if (!success) {
                Log.e(TAG, "No valid users found for this taskgroup");
                return;
            }

            final JSONArray users = users_data.getJSONObject("data").getJSONArray("applicants");

            for (int i = 0; i < users.length(); i++) {
                User u = User.fromJSON(users.getJSONObject(i));
                if (u != null) {
                    u.setTaskGroup(mSelectedTaskgroup);
                    mEcoArray.add(u);
                }
            }
            mEcoArrayAdapter.notifyDataSetChanged();

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShow() {

    }

    @Override
    public int getTitle() {
        return R.string.user_selection_title;
    }

    private class UserArrayAdapter extends ArrayAdapter<User> {

        public UserArrayAdapter(UserSelectionFragment userSelectionFragment, int textViewResourceId,
                                List<User> objects) {

            super(userSelectionFragment.getActivity(), textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getViewOptimize(position, convertView, parent);
        }

        public View getViewOptimize(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.eco_row, null);
                viewHolder = new ViewHolder();
                viewHolder.username = (TextView) convertView.findViewById(R.id.text_operator_username);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            User user = getItem(position);
            viewHolder.username.setText(String.format("%s %s", user.getLastName(), user.getFirstName()));
            return convertView;
        }

        private class ViewHolder {
            public TextView username;
        }
    }
}