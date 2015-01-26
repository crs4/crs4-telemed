package most.demo.ecoapp;


import most.demo.ecoapp.models.EcoUser;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;



public class TeleconsultationActivity   extends ActionBarActivity {

	private EcoUser ecoUser = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_teleconsultation_main);
		
		Intent i = getIntent();
		this.ecoUser =  (EcoUser) i.getExtras().getSerializable("EcoUser");
		 TextView txtEcoUser =  (TextView)findViewById(R.id.textTCEcoUser);
		 if (this.ecoUser!=null)
			 txtEcoUser.setText(this.ecoUser.getUsername());
		 else
			 txtEcoUser.setText("USER NON CARICATO!");
	}
	
}
