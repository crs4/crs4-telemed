package it.crs4.most.demo;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class TeleconsultationSetupPager extends ViewPager {
    public TeleconsultationSetupPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TeleconsultationSetupPager(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}
