/*!
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014-15, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */


package it.crs4.most.ehrlib.example;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import it.crs4.ehrlib.example.R;
import it.crs4.most.ehrlib.ArchetypeSchemaProvider;
import it.crs4.most.ehrlib.FormContainer;
import it.crs4.most.ehrlib.WidgetProvider;
import it.crs4.most.ehrlib.example.models.MedicalRecord;
import it.crs4.most.ehrlib.exceptions.InvalidDatatypeException;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.method.ScrollingMovementMethod;
import android.app.Dialog;
import android.content.Context;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * This example allow you to load an Archetype
 */
public class RemoteArchetypeViewerActivityExample extends ActionBarActivity{

	/** The Constant LANGUAGE. */
	public static final String LANGUAGE = "es-ar"; // "es-ar"; //
	
	/** The Constant TAG. */
	public static final String TAG = "RemoteArchetypeViewerActivityExample";

	
	// REMOTE PYEHR SERVER CONNECTION PARAMS
	private String serverIp = "156.148.132.223"; //"156.148.132.223";
	private static int serverPort = 8000;
	private static RemotePyEHRConnector rc =null;  //new RemotePyEHRConnector(getActivity(), serverIp, serverPort);
	private static ArchetypeSchemaProvider asp = null;
	
	private  static String clientId = "8c96bf8cea26fa555fa8";
	private static  String clientSecret = "4fd1f508b7b03fba6509da4c193157d7a2b20838";
	//'grant_type': 'password',
	private static String username = "admin";
	private static String password = "admin";
	private static String taskgroup = "5dw2x3jfkftxue5a5izw6yiplbbn4dlo";
	private static String patientId = "wj7zfhwdfvdy3djrjize2dn5rzlcu5i7";
	private static String accessToken = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_blood_pressure);
       
        if (savedInstanceState == null) {
        	  // remote connector
            rc = new RemotePyEHRConnector(this, serverIp, serverPort);
            asp = new ArchetypeSchemaProvider(this, "archetypes.properties", "archetypes");
            retrieveAccessToken(true);
        }
        
      
    }
    
    
   private void retrieveAccessToken(final boolean loadPatientsFragment)
   {
	   rc.getAccessToken(clientId, clientSecret, username, password, taskgroup, new Listener<String>() {

		@Override
		public void onResponse(String response) {
			try {
				accessToken = new JSONObject(response).getString("access_token");
				rc.setAccessToken(accessToken);
				Toast.makeText(getApplicationContext(), "ACCESS TOKEN:" + accessToken, Toast.LENGTH_LONG).show();
				if (loadPatientsFragment)
				{ 
					loadMedicalrecordFragment();
				}
			} catch (JSONException e) {
				Toast.makeText(getApplicationContext(), "INVALID ACCESS TOKEN" , Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			
			
		 
		}
	}, new ErrorListener() {

		@Override
		public void onErrorResponse(VolleyError arg0) {
			Toast.makeText(getApplicationContext(), "ERROR RETRIEVING ACCESS TOKEN" , Toast.LENGTH_LONG).show();
			accessToken = null;
		}
	});
   }
   
   public void loadMedicalrecordFragment()
   {
	   FragmentManager fm = getSupportFragmentManager();
	   PatientMedicalRecordFragment pmrf = new PatientMedicalRecordFragment(this, rc);
	 
	   fm.beginTransaction().replace(R.id.container, pmrf).commit();
   }
   
   public void loadArchetypeFragment(MedicalRecord mr, String instances)
   {
	   
	   Log.d(TAG, "Replacing the fragment....");
	   FragmentManager fm = getSupportFragmentManager();
   
	   ArchetypeViewerFragment f =  new ArchetypeViewerFragment(getApplicationContext(), mr.getArchetypeClass(), null, null, instances, null, null);
	   
	   //fm.beginTransaction().remove(archetypeFragment).add(R.id.container, f).commit();
        fm.beginTransaction().replace(R.id.container, f).commit();
       //this.archetypeFragment = f;
   }
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.blood_pressure, menu);
        return true;
    }

   
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_quit) {
        	finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A fragment containing a form for The Archetype (e.g Blood Pressure) handling.
     */
    public static class ArchetypeViewerFragment extends Fragment {

    	 
 	    @Override
         public void onCreateOptionsMenu( Menu menu, MenuInflater inflater) {   
            inflater.inflate(R.menu.action_fragment, menu);
         }
    	 
    	 
 	     @Override
         public boolean onOptionsItemSelected(MenuItem item) {
                 switch (item.getItemId()) {
                 case R.id.menu_spain:
                         widgetProvider.updateOntologyLanguage("es-ar");
                         return true;
                 case R.id.menu_english:
                    
                     widgetProvider.updateOntologyLanguage("en");
                     return true;


                 }
                 return super.onOptionsItemSelected(item);
         }
    	 
        
        @Override
        public void onCreate (Bundle savedInstanceState)
        {
        	super.onCreate(savedInstanceState);
        	setHasOptionsMenu(true);
        }
        
    	/** The form container. */
	    private FormContainer formContainer = null;
		
		/** The widget provider. */
		private WidgetProvider widgetProvider;
    	
		
		private String datatypes = null;
		private String ontology  = null;
		private String instances = null;
		private String schema = null;
		private String language = null;
		
		
         
        
        public ArchetypeViewerFragment(Context ctx, String archetypeClass, String datatypes, String ontology, String instances, String schema, String language) {
        	
        	this.datatypes = (datatypes == null ?  asp.getDatatypesSchema(archetypeClass): datatypes);
        	this.ontology = (ontology == null ?    asp.getOntologySchema(archetypeClass) : ontology);
        	this.instances = (instances== null ?  asp.getAdlStructureSchema(archetypeClass) : instances);
        	this.schema = (schema == null ?  asp.getLayoutSchema(archetypeClass) : schema);
        	this.language = (language== null ?  LANGUAGE : language);
        }

       
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            //View rootView = inflater.inflate(R.layout.fragment_blood_pressure, container, false);
            
            //View rootView = WidgetProvider.getDatatypeWidget(getActivity().getApplicationContext(), "DV_QUANTITY", null).getView();

			try {
				Log.d(TAG, String.format("--->>>> Activity:%s DT:%s,ON:%s, IN:%s. SC: %s, LA:%s", getActivity(),datatypes,ontology,instances,schema,language));
				widgetProvider = new WidgetProvider(getActivity(), 
													this.datatypes,
													this.ontology, 
													this.instances,
													this.schema,
													this.language);
				
				this.formContainer = widgetProvider.buildFormView(0);
			
				
			} catch (JSONException e1) {
				Log.e(TAG, "Error reading json data: " + e1);
				e1.printStackTrace();
			} catch (InvalidDatatypeException e) {
				Log.e(TAG, "Error building widgets: " + e);
				e.printStackTrace();
			}
            
           
			
            // Buttons Panel
    		View buttonsPanel = inflater.inflate(R.layout.datatype_form_buttons, container, false);
    		ViewGroup rootView = formContainer.getLayout();
    		
    		
    		//FrameLayout fl = (FrameLayout) rootView.findViewById(R.id.container);
			//fl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			
    	    rootView.addView(buttonsPanel);
    	    
            rootView.setBackgroundColor(Color.BLACK);
            setupButtonsListeners(buttonsPanel);
            return rootView;
        }
        
        
        /**
         * Show info dialog.
         *
         * @param content the content
         */
        private void showInfoDialog(String content)
        { 
        	final Dialog dialog = new Dialog(getActivity());
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
        
        /**
         * Sets the up buttons listeners.
         *
         * @param buttonsPanel the new up buttons listeners
         */
        private void setupButtonsListeners(View buttonsPanel)
        {
        	Button butJson = (Button) buttonsPanel.findViewById(R.id.butJson);
        	butJson.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String content;
					try {
						content = widgetProvider.toJson().toString(2);
						showInfoDialog(content);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						showInfoDialog(String.format("Error Parsing Json Content: \n\n %s" , e.getMessage()));
					}
					
				}
			});
        	
        	
        	Button butExport = (Button) buttonsPanel.findViewById(R.id.butExport);
        	butExport.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					exportArchetype();
				}});

        	Button butLoad = (Button) buttonsPanel.findViewById(R.id.butLoad);
        	butLoad.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					((RemoteArchetypeViewerActivityExample) getActivity()).loadMedicalrecordFragment();
				}
			});
        	
        	
        	
        	Button butReset = (Button) buttonsPanel.findViewById(R.id.butReset);
        	butReset.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					formContainer.resetAllWidgets();
					
				}
			});
        	
        	Button butSave = (Button) buttonsPanel.findViewById(R.id.butSave);
        	butSave.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					try {
						
						formContainer.submitAllWidgets();
						widgetProvider.updateSectionsJsonContent(0);
						
					} catch (InvalidDatatypeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e(TAG, "Invalid datatype:" + e.getMessage());
						Toast.makeText(getActivity(), "Invalid Input value:" + e.getMessage(), Toast.LENGTH_LONG).show();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e(TAG, "Invalid JSON PARSING:" + e.getMessage());
					}				
				}
			});
        }


		protected void exportArchetype() {
		 
			JSONObject json = widgetProvider.toJson();
			rc.createPatientMedicalRecord(patientId, json, new Response.Listener<JSONObject>() {

				@Override
				public void onResponse(JSONObject arg0) {
					Toast.makeText(getActivity(), "ARCHETYPE EXPORTED", Toast.LENGTH_LONG).show();
					
				}}  , new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError err) {
						Toast.makeText(getActivity(), "ERROR exporting ARCHETYPE:" + err.getMessage(), Toast.LENGTH_LONG).show();
						
					}});
		}
    }
    
    
}
