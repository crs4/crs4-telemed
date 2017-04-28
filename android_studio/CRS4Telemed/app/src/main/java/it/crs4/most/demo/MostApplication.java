package it.crs4.most.demo;

import android.app.Application;

/**
 * Created by vitto on 26/09/16.
 */

public class MostApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        QuerySettings.setLoginChecked(this, false);
    }
}
