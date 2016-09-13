package it.crs4.most.demo;

import android.content.Context;
import android.preference.PreferenceManager;

public class QuerySettings {

    // NOTE: the value of the constants MUST correspond to the value in settings_preferenceseferences.xml
    private static final String CONFIG_SERVER_IP = "config_server_address";
    private static final String CONFIG_SERVER_PORT = "config_server_port";
    private static final String TASK_GROUP = "select_task_group_preference";
    private static final String ROLE = "role_preference";
    private static final String USER = "user";
    private static final String ACCESS_TOKEN = "access_token";

    private static String getStoredItem(Context context, String valueType, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(valueType, defaultValue);
    }

    public static String getConfigServerAddress(Context context) {
        return getStoredItem(context, CONFIG_SERVER_IP, null);
    }

    public static String getConfigServerPort(Context context) {
        return getStoredItem(context, CONFIG_SERVER_PORT, "8000");
    }

    public static String getTaskGroup(Context context) {
        return getStoredItem(context, TASK_GROUP, null);
    }

    public static String getRole(Context context) {
        String[] roles = context.getResources().getStringArray(R.array.roles_entries_values);
        return getStoredItem(context, ROLE, null);
    }

    public static String getUser(Context context) {
        return getStoredItem(context, USER, null);
    }

    public static String getAccessToken(Context context) {
        return getStoredItem(context, ACCESS_TOKEN, null);
    }
}


