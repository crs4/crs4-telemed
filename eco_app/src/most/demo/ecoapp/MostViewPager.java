package most.demo.ecoapp;

import most.demo.ecoapp.config_fragments.ConfigFragment;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class MostViewPager extends ViewPager {

	private static boolean manual_page_change_enabled;
    private static int targetPos = 0;
    private static int permittedPos = 0;
    private static String TAG = "MostViewPager";
    
	public MostViewPager(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    MostViewPager.manual_page_change_enabled = false;
	     
	}

	public MostViewPager(Context context)
	{
		 super(context);
		 MostViewPager.manual_page_change_enabled = false;
		 
	}
	
	public void setOnPageListener()
	{
		this.setOnPageChangeListener(new PageListener());
	}
	
	public void setInternalCurrentItem(int target_position, int permitted_position)
	{
		MostViewPager.manual_page_change_enabled = true;
		permittedPos = permitted_position;
		targetPos = target_position;
		Log.d(TAG, "Manual Page Enabled:" +MostViewPager.manual_page_change_enabled + " target_position:" +target_position + " permitted:" + permittedPos);
		this.setCurrentItem(targetPos);
		
		if (getCurrentItem()==targetPos && targetPos!=permittedPos)
			MostViewPager.manual_page_change_enabled = true;
	}
	
	@Override
	public void setCurrentItem(int position)
	{
		Log.d(TAG, "Called setCurrentItem:" + position + " with MostViewPager.manual_page_change_enabled:" + MostViewPager.manual_page_change_enabled);
		
		if (!MostViewPager.manual_page_change_enabled)
		{
			return;
		}
		
		((ConfigFragment)((MainActivity.MyPagerAdapter) this.getAdapter()).getItem(position)).updateConfigFields();
		super.setCurrentItem(position);
		MostViewPager.manual_page_change_enabled = false;
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


	 
	
	private class PageListener extends SimpleOnPageChangeListener{
        public void onPageSelected(int position) {
        	Log.d(TAG, "Page selected:" + position + " Permitted position;" + permittedPos);
           if (position!=permittedPos && position!=targetPos)
           {
        	   MostViewPager.this.setInternalCurrentItem(targetPos, permittedPos);
           }
           else 
        	   MostViewPager.manual_page_change_enabled = false;
    }
}
}
