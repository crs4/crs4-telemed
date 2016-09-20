package it.crs4.most.demo;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

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

    public static void storeItem(Context context, String valueType, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).
            edit().
            putString(valueType, value).
            apply();
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
        return getStoredItem(context, ROLE, null);
    }

    public static String getUser(Context context) {
        return getStoredItem(context, USER, null);
    }

    public static void setUser(Context context, String user) {
        storeItem(context, USER, user);
    }

    public static String getAccessToken(Context context) {
        return getStoredItem(context, ACCESS_TOKEN, null);
    }

    public static void setAccessToken(Context context, String accessToken) {
        storeItem(context, ACCESS_TOKEN, accessToken);
    }

    public static boolean isEcographist(Context context) {
        String role = QuerySettings.getRole(context);
        String[] roles = context.getResources().getStringArray(R.array.roles_entries_values);
        return role.equals(roles[0]);
    }

    public static boolean isSpecialist(Context context) {
        String role = QuerySettings.getRole(context);
        String[] roles = context.getResources().getStringArray(R.array.roles_entries_values);
        return role.equals(roles[1]);
    }

}


