package most.demo.ecoapp.config_fragments;

import most.demo.ecoapp.IConfigBuilder;
import android.support.v4.app.Fragment;

public abstract class ConfigFragment extends Fragment {
   
  protected IConfigBuilder config = null;
  
  public void setConfigBuilder(IConfigBuilder config)
  {
	  this.config = config;
  }
  
  public abstract void updateConfigFields();
}
