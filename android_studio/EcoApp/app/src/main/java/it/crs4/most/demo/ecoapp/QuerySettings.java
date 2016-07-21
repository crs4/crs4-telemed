package it.crs4.most.demo.ecoapp;

import android.content.Context;
import android.preference.PreferenceManager;

public class QuerySettings {

    // NOTE: the value of the constants MUST correspond to the value in settings_preferenceseferences.xml
    private static final String CONFIG_SERVER_IP = "config_server_address";
    private static final String CONFIG_SERVER_PORT = "config_server_port";
    private static final String TASK_GROUP = "select_task_group_preference";

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

//    public static void setConfigServerAddress(Context context, String value) {
//        PreferenceManager.getDefaultSharedPreferences(context).
//                edit().
//                putString(CONFIG_SERVER_IP, value).
//                apply();
//    }

//    public static void setTaskGroup(Context context, String value) {
//        PreferenceManager.getDefaultSharedPreferences(context).
//                edit().
//                putString(TASK_GROUP, value).
//                apply();
//
//    }

//    public static void setConfigServerPort(Context context, int value) {
//        PreferenceManager.getDefaultSharedPreferences(context).
//                edit().
//                putInt(CONFIG_SERVER_PORT, value).
//                apply();
//    }

}


