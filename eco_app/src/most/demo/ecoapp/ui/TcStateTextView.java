package most.demo.ecoapp.ui;

import most.demo.ecoapp.TeleconsultationState;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class TcStateTextView extends TextView {

	private static final String TAG = "EcoTeleconsultationActivity";


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
    
       switch (this.tcState) {
       	case IDLE:
       		Log.d(TAG,"Called IDLE:");
       		this.setTextColor(Color.BLUE);
       		//this.setBackgroundColor(Color.BLUE);
       		break;
       		
       	case READY:
       		this.setTextColor(Color.GREEN);
       		//this.setBackgroundColor(Color.GREEN);
       		break;
       		
       	case CALLING:
       		this.setTextColor(Color.MAGENTA);
       		//this.setBackgroundColor(Color.BLUE);
       		break;
       		
       	default:
       		Log.d(TAG,"Called DEFAULT:");
       		this.setTextColor(Color.BLUE);
       		//this.setBackgroundColor(Color.WHITE);
       		break;
       		 
       }
		
	}
}
