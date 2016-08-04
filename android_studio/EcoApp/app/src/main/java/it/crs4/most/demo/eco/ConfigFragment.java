package it.crs4.most.demo.eco;

import android.support.v4.app.Fragment;

public abstract class ConfigFragment extends Fragment {

    private IConfigBuilder mConfigBuilder = null;

    public void setConfigBuilder(IConfigBuilder config) {
        this.mConfigBuilder = config;
    }

    public IConfigBuilder getConfigBuilder() {
        return mConfigBuilder;
    }

    public abstract void onShow();

    public abstract int getTitle();
}
