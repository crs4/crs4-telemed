package it.crs4.most.demo;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomSpinnerAdapter<T> extends ArrayAdapter<T> {
    private int mHintMessage;

    public CustomSpinnerAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
        add(null);
        mHintMessage = R.string.spinner_item_hint_default;
    }

    public void setHintMessage(int resId) {
        mHintMessage = resId;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView tv = (TextView) view;
        if (position == 0) {
            // Set the hint text color gray
            tv.setText(mHintMessage);
            tv.setTextColor(Color.GRAY);
        }
        else {
            tv.setTextColor(Color.BLACK);
        }
        return view;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.spinner_dropdown, null);
        }

        TextView text = (TextView) convertView.findViewById(R.id.spinner_item);
        if (position == 0) {
            text.setText(mHintMessage);
        }
        else {
            text.setText(getItem(position).toString());
        }
        return convertView;
    }

    @Override
    public void clear() {
        super.clear();
        add(null);
    }
}
