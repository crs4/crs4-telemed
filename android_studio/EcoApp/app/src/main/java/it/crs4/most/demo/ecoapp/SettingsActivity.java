package it.crs4.most.demo.ecoapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends Activity {
    private SharedPreferences sharedPref;
    private EditText configServerUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        configServerUrl = (EditText) findViewById(R.id.config_server_url);
        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String config_url_key = getString(R.string.config_server_url);
        if (sharedPref.contains(config_url_key)){
            configServerUrl.setText(sharedPref.getString(config_url_key, ""));
        }
    }

    public void onSaveSettings(View v){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.config_server_url), configServerUrl.getText().toString());
        editor.apply();
        finish();
    }
}
