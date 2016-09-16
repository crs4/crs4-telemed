package it.crs4.most.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MostDemoActivity extends SingleFragmentActivity {

    private static final String TAG = "MostDemoActivity";
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawer();
    }

    private void setDrawer() {
        String[] drawerItems = new String[2];
        drawerItems[0] = getString(R.string.settings);
        drawerItems[1] = getString(R.string.exit);

        try {
            mDrawerLayout = (DrawerLayout) findViewById(R.id.most_demo_drawer_layout);
            ListView drawerList = (ListView) findViewById(R.id.most_demo_left_drawer);
            ArrayAdapter<String> drawerAdapter = new ArrayAdapter<>(this, R.layout.drawer_list_item, drawerItems);
            drawerList.setAdapter(drawerAdapter);
            drawerList.setOnItemClickListener(new DrawerItemClickListener());
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);

            // Set the drawer toggle as the DrawerListener
            mDrawerLayout.addDrawerListener(mDrawerToggle);

            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        catch (NullPointerException ex) {
            Log.e(TAG, "There's something wrong with the layout");
        }
    }

    @Override
    protected Fragment createFragment() {
        return MostDemoFragment.newInstance();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_fragment_with_drawer;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            String value = (String) parent.getItemAtPosition(position);
            if (value.equals(getString(R.string.settings))) {
                Intent settingsIntent = new Intent(MostDemoActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
            else if (value.equals(getString(R.string.exit))) {
                finish();
            }
        }
    }
}
