package it.crs4.most.demo.setup_fragments;

import android.support.v4.app.Fragment;

import it.crs4.most.demo.IConfigBuilder;

public abstract class SetupFragment extends Fragment {

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
