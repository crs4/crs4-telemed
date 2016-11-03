package it.crs4.most.demo;

import android.support.v7.app.AppCompatActivity;

import it.crs4.most.demo.setup_fragments.SetupFragment;

/**
 * Abstract Activity class that implements SetupFragment.StepEventListener. Activities that wants to
 * use SetupFragments must extends this class
 */
public abstract class SetupActivity extends AppCompatActivity implements SetupFragment.StepEventListener {

    protected int mNavDirection = 0;

    @Override
    public void onStepDone() {
        nextStep();
    }

    @Override
    public void onSkipStep() {
        if (mNavDirection == 0) {
            nextStep();
        }
        else {
            previousStep();
        }
    }

    protected abstract void nextStep();
    protected abstract void previousStep();
}
