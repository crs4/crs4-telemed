package it.crs4.most.demo.spec;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SpinnerKeyboardViewer extends KeyboardViewer implements AdapterView.OnItemSelectedListener {
    private Spinner spinner;

    public SpinnerKeyboardViewer(Context context, Spinner spinner, String [] keys){
        this.spinner = spinner;

        // Create an ArrayAdapter using a default spinner layout
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item);
        adapter.addAll(keys);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        this.getKeySelectionListener().onKeySelected((String) parent.getItemAtPosition(pos));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
