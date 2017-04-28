package it.crs4.most.demo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

class TeleconsultationControllerFactory {

    static TeleconsultationController getTeleconsultationController(Context context,
                                                                    FragmentManager fragmentManager,
                                                                    @Nullable String action,
                                                                    TeleconsultationSetup teleconsultationSetup) {
        String role = QuerySettings.getRole(context);
        String roles[] = context.getResources().getStringArray(R.array.roles_entries_values);
        if (role.equals(roles[0])) {
            return new EcoTeleconsultationController(fragmentManager, action, teleconsultationSetup);
        }
        else {
            return new SpecTeleconsultationController(fragmentManager, teleconsultationSetup);
        }
    }
}
