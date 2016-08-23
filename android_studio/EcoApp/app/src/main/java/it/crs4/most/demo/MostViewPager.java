package it.crs4.most.demo;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class MostViewPager extends ViewPager {

    private static String TAG = "MostViewPager";

    private boolean manualPageChangeEnabled;
    private int targetPos = 0;
    private int permittedPos = 0;

    public MostViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        manualPageChangeEnabled = false;
        addOnPageChangeListener(new PageListener());
    }

    public MostViewPager(Context context) {
        super(context);
        manualPageChangeEnabled = false;
        addOnPageChangeListener(new PageListener());
    }

    public void setInternalCurrentItem(int targetPosition, int permittedPosition) {
        manualPageChangeEnabled = true;
        permittedPos = permittedPosition;
        targetPos = targetPosition;
        Log.d(TAG, "Manual Page Enabled:" + manualPageChangeEnabled +
                " target_position:" + targetPosition + " permitted:" + permittedPos);

        super.setCurrentItem(targetPos);
        ((ConfigFragment) ((TeleconsultationSetupActivity.PagerAdapter) getAdapter()).getItem(targetPos)).onShow();

        manualPageChangeEnabled = getCurrentItem() == targetPos && targetPos != permittedPos;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (manualPageChangeEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (manualPageChangeEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    private class PageListener extends ViewPager.SimpleOnPageChangeListener {
        public void onPageSelected(int position) {
            Log.d(TAG, "Page selected:" + position + " Permitted position: " + MostViewPager.this.permittedPos);
            if (position !=  permittedPos && position != targetPos) {
                MostViewPager.this.setInternalCurrentItem(targetPos, permittedPos);
            }
            else {
                MostViewPager.this.manualPageChangeEnabled = false;
            }
        }
    }
}
