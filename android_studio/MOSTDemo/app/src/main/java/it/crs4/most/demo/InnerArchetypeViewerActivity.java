/*!
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */

package it.crs4.most.demo;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import it.crs4.most.report.ehr.ArchetypeFragment;
import it.crs4.most.report.ehr.ArchetypeSchemaProvider;
import it.crs4.most.report.ehr.TemplateProvider;
import it.crs4.most.report.ehr.WidgetProvider;
import it.crs4.most.report.ehr.exceptions.InvalidDatatypeException;


public class InnerArchetypeViewerActivity extends AppCompatActivity {
    private static final String TAG = "InnerArchetypeViewerAc";
    private final String LANGUAGE = "en";

    private TemplateProvider mTemplateProvider;
    private List<ArchetypeFragment> mArchetypeFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_activity);
        try {
            // Provide an Archetype Schema Provider containing the informations about all the archetype schema used by the application)
            ArchetypeSchemaProvider asp = new ArchetypeSchemaProvider(this, "archetypes.properties", "archetypes");
            mTemplateProvider = new TemplateProvider(this,
                WidgetProvider.parseFileToString(this, "most_demo__template.json"), asp, LANGUAGE);
            buildArchetypeFragments();
            setupButtonsListener();
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

//    private JSONArray getTemplateJsonContent() {
//        List<WidgetProvider> wps = mTemplateProvider.getWidgetProviders();
//        JSONArray tmpJson = new JSONArray();
//        for (int i = 0; i < wps.size(); i++)
//            try {
//                tmpJson.put(i, wps.get(i).toJson());
//            }//    /**
//     * Show info dialog.
//     *
//     * @param content the content
//     */
//    private void showInfoDialog(String content) {
//        final Dialog dialog = new Dialog(this);
//        dialog.setTitle("Json Adl Structure");
//        dialog.setContentView(R.layout.custom_dialog);
//        TextView dialogText = (TextView) dialog.findViewById(R.id.dialogText);
//        dialogText.setMovementMethod(new ScrollingMovementMethod());
//        dialogText.setText(content);
//
//        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
//        dialogButton.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//
//            }
//        });
//
//        dialog.show();
//    }
//            catch (JSONException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//        return tmpJson;
//
//    }

    private void setupButtonsListener() {
//        Button butJson = (Button) findViewById(R.id.json_button);
//        butJson.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                String content;
//                try {
//                    content = getTemplateJsonContent().toString(2);
//                    showInfoDialog(content);
//                }
//                catch (JSONException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                    showInfoDialog(String.format("Error Parsing Json Content: \n\n %s", e.getMessage()));
//                }
//
//            }
//        });

        Button butSave = (Button) findViewById(R.id.save_button);
        butSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                submitTemplate();
            }
        });

        Button butReset = (Button) findViewById(R.id.reset_button);
        butReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTemplate();
            }
        });
    }

//    /**
//     * Show info dialog.
//     *
//     * @param content the content
//     */
//    private void showInfoDialog(String content) {
//        final Dialog dialog = new Dialog(this);
//        dialog.setTitle("Json Adl Structure");
//        dialog.setContentView(R.layout.custom_dialog);
//        TextView dialogText = (TextView) dialog.findViewById(R.id.dialogText);
//        dialogText.setMovementMethod(new ScrollingMovementMethod());
//        dialogText.setText(content);
//
//        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
//        dialogButton.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//
//            }
//        });
//
//        dialog.show();
//    }

    private void updateOntologies(String lang) {
        for (WidgetProvider wp : mTemplateProvider.getWidgetProviders()) {
            wp.updateOntologyLanguage(lang);
        }
    }

    private void resetTemplate() {
        for (ArchetypeFragment af : mArchetypeFragments) {
            af.getFormContainer().resetAllWidgets();
        }
    }

    private void submitTemplate() {
        boolean error = false;
        for (ArchetypeFragment af : mArchetypeFragments) {
            try {
                af.getFormContainer().submitAllWidgets();
                af.getwidgetProvider().updateSectionsJsonContent(0);
            }
            catch (InvalidDatatypeException e) {
                Log.e(TAG, "Error submitting forms:" + e.getMessage());
                e.printStackTrace();
                Toast.makeText(this, "Error Validating Template:" + e.getMessage(), Toast.LENGTH_LONG).show();
                error = true;
            }
            catch (JSONException e) {
                Log.e(TAG, "Error updating json content:" + e.getMessage());
                e.printStackTrace();
            }
        }

        if (!error) {
            Toast.makeText(this, "Template content successfully saved.", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "Template content was not successfully saved.", Toast.LENGTH_LONG).show();
        }
    }


    private void buildArchetypeFragments() {
        mArchetypeFragments = new ArrayList<>();

        List<WidgetProvider> wps = mTemplateProvider.getWidgetProviders();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        for (WidgetProvider wp : wps) {
            ArchetypeFragment af = new ArchetypeFragment();
            af.setWidgetProvider(wp);

            mArchetypeFragments.add(af);
            ft.add(R.id.report_container, af);
        }

        ft.commit();

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.action_template, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.menu_italy:
//                updateOntologies("it"); // es-ar
//                return true;
//            case R.id.menu_english:
//                updateOntologies("en");
//                return true;
//            case R.id.action_quit:
//                finish();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
