package it.crs4.most.demo;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import it.crs4.most.demo.models.Teleconsultation;

abstract class TeleconsultationController extends FragmentPagerAdapter {

    private FragmentManager mManager;

    TeleconsultationController(FragmentManager fm) {
        super(fm);
        mManager = fm;
    }

    public abstract void startTeleconsultationActivity(Activity activity,
                                                       Teleconsultation teleconsultation);

    private String getFragmentTag(int viewPagerId, int fragmentPosition) {
        //TODO: Override instantiateItem
        //This is the way FragmentPagerAdapter creates the tag for Fragments.
        //If they're code changes it's a problem...
        return "android:switcher:" + viewPagerId + ":" + fragmentPosition;
    }

    Fragment getFragment(int viewPagerId, int fragmentPosition) {
        return mManager.findFragmentByTag(getFragmentTag(viewPagerId, fragmentPosition));
    }
}
