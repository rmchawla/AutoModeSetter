package org.sunysb.cse549.maps;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sunysb.cse549.database.DBAdapter;
import org.sunysb.cse549.database.Location;
import org.sunysb.cse549.database.Profile;
import org.sunysb.cse549.database.Schedule;

import org.sunysb.cse549.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ListActivity extends Activity {
	Map<Long, Schedule> schedMap;
	Map<Integer, Profile> profMap;
	Map<String, Location> locMap;
	Button cancelButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v) {
				Intent intent = new Intent(ListActivity.this,MainActivity.class);
				startActivity(intent);
			}
			
		});
		
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.list_layout);
		if(layout == null)
			System.out.println("null");
		DBAdapter db = DBAdapter.getInstance(getBaseContext());
		schedMap = db.getAllSchedules();
		profMap = db.getAllPofiles();
		locMap = db.getAllLocations();
		
		if(schedMap!= null)
		{

			int arr_sz = schedMap.size();
			int ctr = 0;
			Set<Long> schedidSet = schedMap.keySet();
			Long schedid[] = new Long[arr_sz];
			schedidSet.toArray(schedid);
			Button button[] = new Button[arr_sz];
			while ( ctr < arr_sz ) {
				button[ctr] = new Button(this);
				button[ctr].setId(schedid[ctr].intValue());
				button[ctr].setText(schedMap.get(schedid[ctr]).getLocationName()+", "+schedMap.get(schedid[ctr]).getStartDatetime()+", "+profMap.get(schedMap.get(schedid[ctr]).getProfileId()).getName());
				
				button[ctr].setOnClickListener(new View.OnClickListener() {				
					public void onClick(View v) {
						Intent intent = new Intent(getBaseContext(), UpdateScheduleActivity.class);
						long id = v.getId();
						System.out.println("&&&id = "+ id);
						intent.putExtra("id", id);
						startActivity(intent);
					}
				});
				
				if(layout!=null)
				layout.addView((View) button[ctr]);
				ctr++;
		}
	}	
			else 
				Toast.makeText(getBaseContext(), "No schedules", Toast.LENGTH_LONG).show();
		
		
		
		//ArrayAdapter itemAdapter = new ArrayAdapter	( this, android.R.layout.activity_list_item, button );
	}
}
