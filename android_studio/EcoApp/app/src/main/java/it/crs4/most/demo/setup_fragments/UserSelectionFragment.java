package it.crs4.most.demo.setup_fragments;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.Response;

import it.crs4.most.demo.ConfigFragment;
import it.crs4.most.demo.IConfigBuilder;
import it.crs4.most.demo.QuerySettings;
import it.crs4.most.demo.R;
import it.crs4.most.demo.TeleconsultationException;
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

    private ArrayList<User> mUsers;
    private ArrayAdapter<User> mUsersAdapter;
    private TaskGroup mTaskgroup;

    public static UserSelectionFragment newInstance(IConfigBuilder config) {
        UserSelectionFragment fragment = new UserSelectionFragment();
        fragment.setConfigBuilder(config);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_selection_list, container, false);
        mTaskgroup = new TaskGroup(QuerySettings.getTaskGroup(getActivity()), null);
        mUsers = new ArrayList<>();
        mUsersAdapter = new UserAdapter(this, R.layout.fragment_user_selection_item, mUsers);

        ListView listView = (ListView) view.findViewById(R.id.operator_list);
        listView.setAdapter(mUsersAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = mUsers.get(position);
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
            getConfigBuilder().getRemoteConfigReader()
                .getUsersByTaskgroup(taskgroupId,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject usersData) {
                            final JSONArray users;
                            try {
                                boolean success = usersData != null && usersData.getBoolean("success");
                                if (!success) {
                                    Log.e(TAG, "No valid users found for this taskgroup");
                                    return;
                                }
                                users = usersData.getJSONObject("data")
                                    .getJSONArray("applicants");
                            }
                            catch (JSONException e) {
                                Log.e(TAG, "Error loading user information");
                                e.printStackTrace();
                                return;
                            }

                            for (int i = 0; i < users.length(); i++) {
                                User u;
                                try {
                                    u = User.fromJSON(users.getJSONObject(i));
                                    u.setTaskGroup(mTaskgroup);
                                    mUsers.add(u);
                                    mUsersAdapter.notifyDataSetChanged();
                                }
                                catch (TeleconsultationException | JSONException e) {
                                    Log.e(TAG, "Error loading user information");
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            Log.e(TAG, "Error retrieving the taskgroup users: " + arg0);
                            // [TODO] Handle the error
                        }
                    });
        }
    }

    @Override
    public void onShow() {

    }

    @Override
    public int getTitle() {
        return R.string.user_selection_title;
    }

    private class UserAdapter extends ArrayAdapter<User> {

        public UserAdapter(UserSelectionFragment fragment, int textViewId, List<User> users) {
            super(fragment.getActivity(), textViewId, users);
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
                convertView = inflater.inflate(R.layout.fragment_user_selection_item, null);
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