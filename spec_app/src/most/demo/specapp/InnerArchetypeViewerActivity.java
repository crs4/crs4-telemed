/*!
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */

package most.demo.specapp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import it.crs4.most.ehrlib.ArchetypeFragment;
import it.crs4.most.ehrlib.ArchetypeSchemaProvider;
import it.crs4.most.ehrlib.TemplateProvider;
import it.crs4.most.ehrlib.WidgetProvider;
import it.crs4.most.ehrlib.exceptions.InvalidDatatypeException;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class InnerArchetypeViewerActivity extends ActionBarActivity {
    private final String LANGUAGE = "en";
	private TemplateProvider tp = null;
	private List<ArchetypeFragment> archetypeFragments;
	private static final String TAG = "InnerArchetypeViewerActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		try {
			// Provide an Archetype Schema Provider containing the informations about all the archetype schema used by the application)
			ArchetypeSchemaProvider asp = new ArchetypeSchemaProvider(getApplicationContext(), "archetypes.properties", "archetypes");
			this.tp = new TemplateProvider(getApplicationContext(),WidgetProvider.parseFileToString(getApplicationContext(), "most_demo__template.json"), asp, LANGUAGE);
		    this.buildArchetypeFragments();
		    this.setupButtonsListener();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private JSONArray getTemplateJsonContent()
	{
		List<WidgetProvider> wps = this.tp.getWidgetProviders();
		JSONArray tmpJson = new JSONArray();
	    for (int i=0;i<wps.size();i++)
			try {
				tmpJson.put(i,wps.get(i).toJson());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return tmpJson;
		
	}
	private void setupButtonsListener()
	{
		Button butJson = (Button) findViewById(R.id.butJson);
    	butJson.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String content;
				try {
					content = getTemplateJsonContent().toString(2);
					showInfoDialog(content);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					showInfoDialog(String.format("Error Parsing Json Content: \n\n %s" , e.getMessage()));
				}
				
			}
		});
    	
		Button butSave =  (Button) findViewById(R.id.butSave);
		butSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				submitTemplate();
				
			}
		});
		
		Button butReset =  (Button) findViewById(R.id.butReset);
		butReset.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				resetTemplate();
				
			}
		});
	}
	
	/**
     * Show info dialog.
     *
     * @param content the content
     */
    private void showInfoDialog(String content)
    { 
    	final Dialog dialog = new Dialog(this);
    	dialog.setTitle("Json Adl Structure");
    	dialog.setContentView(R.layout.custom_dialog);
    	TextView dialogText = (TextView) dialog.findViewById(R.id.dialogText);
    	dialogText.setMovementMethod(new ScrollingMovementMethod());
    	dialogText.setText(content);
    	
    	Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
    	dialogButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				
			}
		});
    	
    	dialog.show();
    }
    
	private void updateOntologies(String lang)
	{
		for (WidgetProvider wp : this.tp.getWidgetProviders())
		{
			wp.updateOntologyLanguage(lang);  
		}
	}
	
	private void resetTemplate()
	{
		for (ArchetypeFragment af : this.archetypeFragments)
		{
		 af.getFormContainer().resetAllWidgets();	 
		}
	}
	
	private void submitTemplate()
	{
		boolean error = false;
		for (ArchetypeFragment af : this.archetypeFragments)
		{
			try {
				af.getFormContainer().submitAllWidgets();
				af.getwidgetProvider().updateSectionsJsonContent(0);
			} catch (InvalidDatatypeException e) {
				Log.e(TAG, "Error submitting forms:" + e.getMessage());
				e.printStackTrace();
				Toast.makeText(this, "Error Validating Template:" + e.getMessage(), Toast.LENGTH_LONG).show();
				error=true;
			} catch (JSONException e) {
				Log.e(TAG, "Error updating json content:" + e.getMessage());
				e.printStackTrace();
			}
		}
		
		if (!error)
			Toast.makeText(this, "Template content successfully saved." , Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "Template content was not successfully saved." , Toast.LENGTH_LONG).show();
	}
	
 
	private void buildArchetypeFragments()
	{
		this.archetypeFragments = new ArrayList<ArchetypeFragment>();
		
		List<WidgetProvider> wps = this.tp.getWidgetProviders();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
		for (WidgetProvider wp : wps )
		{ 
			  ArchetypeFragment af = new ArchetypeFragment();
			  af.setWidgetProvider(wp);
			  
			  this.archetypeFragments.add(af);
		      ft.add(R.id.container, af);
		}
		
	    ft.commit();
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_template, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		 switch (item.getItemId()) {
         case R.id.menu_italy:
                 updateOntologies("it"); // es-ar
                 return true;
         case R.id.menu_english:
        	 updateOntologies("en");
             return true;
         case R.id.action_quit:
        	 finish();
        	 return true;
         }
         return super.onOptionsItemSelected(item);
	}
}
