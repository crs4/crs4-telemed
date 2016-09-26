package it.crs4.most.demo;

import android.content.Context;
import android.preference.PreferenceManager;

import it.crs4.most.demo.models.User;

public class QuerySettings {

    // NOTE: the value of the constants MUST correspond to the value in settings_preferenceseferences.xml
    private static final String CONFIG_SERVER_IP = "config_server_address";
    private static final String CONFIG_SERVER_PORT = "config_server_port";
    private static final String TASK_GROUP = "select_task_group_preference";
    private static final String ROLE = "role_preference";
    private static final String USER_FIRSTNAME = "user_firstname";
    private static final String USER_LASTNAME = "user_lastname";
    private static final String USER_USERNAME = "user_username";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String LOGIN_CHECKED = "login_checked";

    private static String getStoredItem(Context context, String valueType, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(valueType, defaultValue);
    }

    public static void storeItem(Context context, String valueType, Object value) {
        if (value instanceof Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context).
                edit().
                putBoolean(valueType, (boolean) value).
                apply();
        }
        else {
            PreferenceManager.getDefaultSharedPreferences(context).
                edit().
                putString(valueType, (String) value).
                apply();
        }
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

    public static User getUser(Context context) {
        String firstname = getStoredItem(context, USER_FIRSTNAME, null);
        String lastname = getStoredItem(context, USER_LASTNAME, null);
        String username = getStoredItem(context, USER_USERNAME, null);
        if (firstname == null && lastname == null && username == null) {
            return null;
        }

        return new User(firstname, lastname, username);
    }

    public static void setUser(Context context, User user) {
        if (user != null) {
            storeItem(context, USER_FIRSTNAME, user.getFirstName());
            storeItem(context, USER_LASTNAME, user.getLastName());
            storeItem(context, USER_USERNAME, user.getUsername());
        }
        else {
            storeItem(context, USER_FIRSTNAME, null);
            storeItem(context, USER_LASTNAME, null);
            storeItem(context, USER_USERNAME, null);
        }
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

    public static boolean isLoginChecked(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(LOGIN_CHECKED, false);
    }

    public static void setLoginChecked(Context context, boolean loginChecked) {
        storeItem(context, LOGIN_CHECKED, loginChecked);
    }

}


