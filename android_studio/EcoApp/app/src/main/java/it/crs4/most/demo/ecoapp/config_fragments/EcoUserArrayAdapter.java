package it.crs4.most.demo.ecoapp.config_fragments;

import java.util.List;

import it.crs4.most.demo.ecoapp.R;

import it.crs4.most.demo.ecoapp.models.EcoUser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EcoUserArrayAdapter extends ArrayAdapter<EcoUser> {

    public EcoUserArrayAdapter(Fragment_UserSelection fragment_UserSelection, int textViewResourceId,
                 List<EcoUser> objects) {
    	
        super(fragment_UserSelection.getActivity(), textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getViewOptimize(position, convertView, parent);
    }

    public View getViewOptimize(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                      .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.eco_row, null);
            viewHolder = new ViewHolder();
            viewHolder.username = (TextView)convertView.findViewById(R.id.textEcoUsername);
            viewHolder.opUnit = (TextView)convertView.findViewById(R.id.textEcoOpUnit);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        EcoUser ecoUser = getItem(position);
        viewHolder.username.setText(String.format("%s %s", ecoUser.getLastName(), ecoUser.getFirstName()));
        viewHolder.opUnit.setText(ecoUser.getTaskGroup().getDescription());
        return convertView;
    }

    private class ViewHolder {
        public TextView username;
        public TextView opUnit;
    }
}