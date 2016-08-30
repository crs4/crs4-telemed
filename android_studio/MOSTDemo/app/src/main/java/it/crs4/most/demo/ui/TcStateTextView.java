package it.crs4.most.demo.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import it.crs4.most.demo.TeleconsultationState;

public class TcStateTextView extends TextView {

    private static final String TAG = "TcStateTextView";
    private TeleconsultationState tcState = TeleconsultationState.IDLE;

    public TcStateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTeleconsultationState(TeleconsultationState tcState) {
        this.tcState = tcState;
        this.updateStyle();
    }

    private void updateStyle() {
        this.setText(this.tcState.toString());

        switch (this.tcState) {
            case IDLE:
                this.setTextColor(Color.BLUE);
                break;
            case READY:
                this.setTextColor(Color.GREEN);
                break;
            case CALLING:
                this.setTextColor(Color.MAGENTA);
                break;
            default:
                this.setTextColor(Color.WHITE);
                break;
        }
    }
}
