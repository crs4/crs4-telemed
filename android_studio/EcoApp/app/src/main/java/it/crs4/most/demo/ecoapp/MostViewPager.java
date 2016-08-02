package it.crs4.most.demo.ecoapp;

import it.crs4.most.demo.ecoapp.config_fragments.ConfigFragment;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class MostViewPager extends ViewPager {

    private static String TAG = "MostViewPager";

    private static boolean manualPageChangeEnabled;
    private static int mTargetPos = 0;
    private static int mPermittedPos = 0;

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
        mPermittedPos = permitted_position;
        mTargetPos = target_position;
        Log.d(TAG, "Manual Page Enabled:" + MostViewPager.manualPageChangeEnabled +
                " target_position:" + target_position + " permitted:" + mPermittedPos);

        super.setCurrentItem(mTargetPos);
        ((ConfigFragment) ((EcoConfigActivity.PagerAdapter) this.getAdapter()).getItem(mTargetPos)).onShow();

        MostViewPager.manualPageChangeEnabled = getCurrentItem() == mTargetPos && mTargetPos != mPermittedPos;
    }

    @Override
    public void setCurrentItem(int position) {
        Log.d(TAG, "Called setCurrentItem:" + position +
                " with MostViewPager.manual_page_change_enabled:" +
                MostViewPager.manualPageChangeEnabled);

        if (!MostViewPager.manualPageChangeEnabled || position != mPermittedPos) {
            return;
        }

        ((ConfigFragment) ((EcoConfigActivity.PagerAdapter) this.getAdapter()).getItem(position)).onShow();
        super.setCurrentItem(position);
        MostViewPager.manualPageChangeEnabled = false;
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
            Log.d(TAG, "Page selected:" + position + " Permitted position;" + mPermittedPos);
            if (position != mPermittedPos && position != mTargetPos) {
                MostViewPager.this.setInternalCurrentItem(mTargetPos, mPermittedPos);
            } else {
                MostViewPager.manualPageChangeEnabled = false;
            }
        }
    }
}
