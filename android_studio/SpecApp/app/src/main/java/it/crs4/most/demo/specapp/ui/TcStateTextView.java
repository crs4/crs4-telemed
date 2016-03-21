package it.crs4.most.demo.specapp.ui;


import it.crs4.most.demo.specapp.TeleconsultationState;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

public class TcStateTextView extends TextView {

	private static final String TAG = "SpecTeleconsultationActivity";

	public TcStateTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	private TeleconsultationState tcState = TeleconsultationState.IDLE;
	
	

	
	public void setTeleconsultationState(TeleconsultationState tcState)
	{
		this.tcState = tcState;
		this.updateStyle();
	}


	private void updateStyle() {
	   
       this.setText(this.tcState.toString());
       this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
       
       switch (this.tcState) {
       	case IDLE:
       		Log.d(TAG,"Called IDLE:");
       		this.setTextColor(Color.BLUE);
       		//this.setBackgroundColor(Color.BLUE);
       		break;
       		
       	case READY:
       		this.setTextColor(Color.DKGRAY);
       		//this.setBackgroundColor(Color.GREEN);
       		break;
       		
       	case CALLING:
       		this.setTextColor(Color.MAGENTA);
       		//this.setBackgroundColor(Color.BLUE);
       		break;
       		
       	default:
       		Log.d(TAG,"Called DEFAULT:");
       		this.setTextColor(Color.BLACK);
       		//this.setBackgroundColor(Color.WHITE);
       		break;
       		 
       }
		
	}
}
