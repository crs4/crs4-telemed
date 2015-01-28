package most.demo.ecoapp;



import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TeleconsultationStateView extends RelativeLayout {

	private TextView txtTcState;
	
	private TeleconsultationState teleconsultationState;
	private static final int[] STATE_TELECONSULTATION = {R.attr.teleconsultation_state};
	
	public TeleconsultationStateView(Context context) {
		this(context, null);
	}


    public  TeleconsultationStateView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        loadViews();
    }

    public  TeleconsultationStateView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);

        loadViews();
    }
    
    private void loadViews() {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.tc_state_item, this, true);
        
        /*
        int fiveDPInPixels = convertDIPToPixels(5);
        int fiftyDPInPixels = convertDIPToPixels(50);
        setPadding(fiveDPInPixels, fiveDPInPixels, fiveDPInPixels, fiveDPInPixels);
        setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, fiftyDPInPixels));
        */
        setBackgroundResource(R.drawable.tc_state_background);

        txtTcState = (TextView) findViewById(R.id.txt_tc_state);
    }

    public int convertDIPToPixels(int dip) {
        // In production code this method would exist in a utility library.
        // e.g see my ScreenUtils class: https://gist.github.com/2504204
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        // If the message is unread then we merge our custom message unread state into
        // the existing drawable state before returning it.
        
            // We are going to add 1 extra state.
            final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

            mergeDrawableStates(drawableState, STATE_TELECONSULTATION);

            return drawableState;
        
    }


    public void setTeleconsultationState(TeleconsultationState tcState) {
        // Performance optimisation: only update the state if it has changed.
        if (this.teleconsultationState != tcState) {
            this.teleconsultationState = tcState;
           
            txtTcState.setText(tcState.toString());
            // Refresh the drawable state so that it includes the message unread state if required.
            refreshDrawableState();
        }
    }
    
}
