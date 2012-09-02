package org.sunysb.cse549.maps;

import org.sunysb.cse549.R;
import org.sunysb.cse549.daemon.DaemonService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	
	Boolean serviceStarted = true;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button listButton = (Button) findViewById(R.id.listAllSchedule);
        Button createButton = (Button) findViewById(R.id.CreateNewSchedule);
        
        if(serviceStarted)
        {	
        	Intent myIntent = new Intent(getApplicationContext(), DaemonService.class);
        	startService(myIntent);
        	serviceStarted = false;
        }
        
        listButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), ListActivity.class); 
				startActivity(intent);
			}
		});
        
            createButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), SetLocationActivity.class); 
				startActivity(intent);
			}
		});
	}
}
