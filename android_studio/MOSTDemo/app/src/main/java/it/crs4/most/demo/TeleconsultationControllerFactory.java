package it.crs4.most.demo;

import android.content.Context;

public class TeleconsultationControllerFactory {

    public static TeleconsultationController getTeleconsultationController(Context context) {
        String role = QuerySettings.getRole(context);
        String roles[] = context.getResources().getStringArray(R.array.roles_entries_values);
        if (role.equals(roles[0])) {
            return new EcoTeleconsultationController();
        }
        else {
            return new SpecTeleconsultationController();
        }
    }
}
