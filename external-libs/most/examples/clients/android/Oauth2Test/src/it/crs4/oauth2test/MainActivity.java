/*!
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */

package it.crs4.oauth2test;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.os.Build;

public class MainActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		Button button1;
		Button button2;
		
		public PlaceholderFragment() {
		}
		
		public void addListenerOnButton(View inflatedView) {
			 
			button1 = (Button) inflatedView.findViewById(R.id.button1);
			Log.d("bb", button1.toString());

			
			button1.setOnClickListener(new OnClickListener() {
	 
				@Override
				public void onClick(View arg0) {

					RequestQueue rq = Volley.newRequestQueue(getActivity());
					
					StringRequest postReq = new StringRequest(Request.Method.POST, "http://156.148.132.223:8000/oauth2/access_token/", new Response.Listener<String>() {
					    @Override
					    public void onResponse(String response) {
					    	Log.d("most_example", response);
					    }
					}, new Response.ErrorListener() {
					    @Override
					    public void onErrorResponse(VolleyError error) {
					    	Log.d("most_example", "Error ["+error+"]");

					    }
					}){     
					    @Override
					    protected Map<String, String> getParams() 
					    {  
					            Map<String, String>  params = new HashMap<String, String>();  
							    params.put("username", "admin");  
							    params.put("password", "admin");
							    params.put("client_id", "d72835cfb6120e844e13");
							    params.put("client_secret", "8740cac9a53f2cdd1bded9cfbb60fdb3b5396863");
							    params.put("grant_type", "password");
					             
					            return params;  
					    }
					} ;

					rq.add(postReq);
					
					Log.d("most_example", "Click button 1");
	 
				}
	 
			});
			
			button2 = (Button) inflatedView.findViewById(R.id.button2);
			Log.d("bb", button2.toString());
			
			 
			button2.setOnClickListener(new OnClickListener() {
	 
				@Override
				public void onClick(View arg0) {
					
					RequestQueue rq = Volley.newRequestQueue(getActivity());
					StringRequest postReq = new StringRequest(Request.Method.GET, "http://156.148.132.223:8000/test?access_token=5da7485eddc0a5b89a097673499e1395340c0d2b", new Response.Listener<String>() {
					    @Override
					    public void onResponse(String response) {
					    	Log.d("most_example", response);
					    }
					}, new Response.ErrorListener() {
					    @Override
					    public void onErrorResponse(VolleyError error) {
					    	Log.d("most_example", "Error ["+error+"]");

					    }
					});

					rq.add(postReq);
					
					Log.d("most_example", "Click button 2");
	 
				}
			});		
	 
		}	
				

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			addListenerOnButton(rootView);

			return rootView;
		}
	}

}
