package it.crs4.most.demo.specapp;

import it.crs4.most.demo.specapp.config_fragments.ConfigFragment;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class MostViewPager extends ViewPager {

    private static boolean manual_page_change_enabled;
    private static int targetPos = 0;
    private static int permittedPos = 0;
    private static String TAG = "MostViewPager";
    private PagerTitleStrip titleStrip;
    private String[] pages;

    public MostViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        MostViewPager.manual_page_change_enabled = false;
    }

    public MostViewPager(Context context) {
        super(context);
        MostViewPager.manual_page_change_enabled = false;
    }

    public void setOnPageListener(String pages[]) {
        this.setOnPageChangeListener(new PageListener());
        titleStrip = (PagerTitleStrip) findViewById(R.id.pager_header);
        this.pages = pages;
    }

    private void updatePageTitleStyle() {
        Log.d(TAG, "Num Pages:" + pages.length + " Num Childs:" + titleStrip.getChildCount());
        for (int i = 0; i < titleStrip.getChildCount(); i++) {

            TextView titlePageView = (TextView) titleStrip.getChildAt(i);
            String title = titlePageView.getText().toString();
            if (permittedPos >= 0)
                Log.d(TAG, "Child:" + i + " ->" + title + " PER:" + pages[permittedPos]);
            else
                Log.d(TAG, "Child:" + i + " ->" + title + " NO PERMITTED POS ");


            if (permittedPos >= 0 && permittedPos != getCurrentItem() && title.contains(pages[permittedPos])) {
                Log.d(TAG, "Title to be changed in blu: " + title);
                //titlePageView.setText(Html.fromHtml("<u>" + pages[permittedPos] + "</u>"));

                titlePageView.setTextColor(Color.GREEN);
            } else {
                //titlePageView.setText(titlePageView.getText().toString());
                titlePageView.setTextColor(Color.WHITE);
            }
        }

        getAdapter().notifyDataSetChanged();

    }


    public void setInternalCurrentItem(int target_position, int permitted_position) {
        MostViewPager.manual_page_change_enabled = true;
        permittedPos = permitted_position;
        targetPos = target_position;
        Log.d(TAG, "Manual Page Enabled:" + MostViewPager.manual_page_change_enabled + " target_position:" + target_position + " permitted:" + permittedPos);


        ((ConfigFragment) ((SpecConfigActivity.PagerAdapter) this.getAdapter()).getItem(targetPos)).updateConfigFields();
        super.setCurrentItem(targetPos);
        MostViewPager.manual_page_change_enabled = false;
        updatePageTitleStyle();


        if (getCurrentItem() == targetPos && targetPos != permittedPos) {
            MostViewPager.manual_page_change_enabled = true;
        }
    }

    @Override
    public void setCurrentItem(int position) {
        Log.d(TAG, "MostViewer childs:" + getChildCount());
        Log.d(TAG, "Called setCurrentItem:" + position + " with MostViewPager.manual_page_change_enabled:" + MostViewPager.manual_page_change_enabled);

        if (!MostViewPager.manual_page_change_enabled || position != permittedPos) {
            return;
        }

        ((ConfigFragment) ((SpecConfigActivity.PagerAdapter) this.getAdapter()).getItem(position)).updateConfigFields();
        super.setCurrentItem(position);
        MostViewPager.manual_page_change_enabled = false;
        updatePageTitleStyle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MostViewPager.manual_page_change_enabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (MostViewPager.manual_page_change_enabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }


    private class PageListener extends SimpleOnPageChangeListener {
        public void onPageSelected(int position) {
            Log.d(TAG, "Page selected:" + position + " Permitted position;" + permittedPos);
            if (position != permittedPos && position != targetPos) {
                MostViewPager.this.setInternalCurrentItem(targetPos, permittedPos);
            } else {
                MostViewPager.manual_page_change_enabled = false;
                updatePageTitleStyle();
            }
        }
    }
}
