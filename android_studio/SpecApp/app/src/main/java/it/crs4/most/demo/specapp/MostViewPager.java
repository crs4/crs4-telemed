package it.crs4.most.demo.specapp;

import it.crs4.most.demo.specapp.config_fragments.ConfigFragment;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class MostViewPager extends ViewPager {

    private static String TAG = "MostViewPager";

    private static boolean manualPageChangeEnabled;
    private static int targetPos = 0;
    private static int permittedPos = 0;

    public MostViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        MostViewPager.manualPageChangeEnabled = false;
        addOnPageChangeListener(new PageListener());
    }

    public MostViewPager(Context context) {
        super(context);
        MostViewPager.manualPageChangeEnabled = false;
        addOnPageChangeListener(new PageListener());
    }

    public void setInternalCurrentItem(int target_position, int permitted_position) {
        MostViewPager.manualPageChangeEnabled = true;
        permittedPos = permitted_position;
        targetPos = target_position;
        Log.d(TAG, "Manual Page Enabled:" + MostViewPager.manualPageChangeEnabled +
                " target_position:" + target_position + " permitted:" + permittedPos);

        super.setCurrentItem(targetPos);
        ((ConfigFragment) ((SpecConfigActivity.PagerAdapter) getAdapter()).getItem(targetPos)).onShow();
        MostViewPager.manualPageChangeEnabled = false;

        if (getCurrentItem() == targetPos && targetPos != permittedPos) {
            MostViewPager.manualPageChangeEnabled = true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MostViewPager.manualPageChangeEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (MostViewPager.manualPageChangeEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    private class PageListener extends SimpleOnPageChangeListener {
        public void onPageSelected(int position) {
            Log.d(TAG, "Page selected:" + position + " Permitted position;" + permittedPos);
            if (position != permittedPos && position != targetPos) {
                MostViewPager.this.setInternalCurrentItem(targetPos, permittedPos);
            }
            else {
                MostViewPager.manualPageChangeEnabled = false;
            }
        }
    }
}
