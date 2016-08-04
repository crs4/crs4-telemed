package it.crs4.most.demo.config_fragments;

import it.crs4.most.demo.IConfigBuilder;
import android.support.v4.app.Fragment;

public abstract class ConfigFragment extends Fragment {
   
  private IConfigBuilder mConfigBuilder = null;
  
  public void setConfigBuilder(IConfigBuilder config)
  {
	  this.mConfigBuilder = config;
  }

  public IConfigBuilder getConfigBuilder() {
    return mConfigBuilder;
  }

  public abstract void onShow();
}
