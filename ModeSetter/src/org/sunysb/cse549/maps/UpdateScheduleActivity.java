package org.sunysb.cse549.maps;

import java.util.List;

import org.sunysb.cse549.R;
import org.sunysb.cse549.maps.CreateProfileActivity.MyOnItemSelectedListener;
import org.sunysb.cse549.database.DBAdapter;
import org.sunysb.cse549.database.Location;
import org.sunysb.cse549.database.Profile;
import org.sunysb.cse549.database.Schedule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.maps.GeoPoint;

public class UpdateScheduleActivity extends Activity 
{
	EditText latitudeEditText;
	EditText longitudeEditText;
	EditText locationNameEditText;
	EditText dateEditText;
	EditText startTime;
	EditText timeEditDurationText;
	Button updateButton;
	Button cancelButton;
	DBAdapter db;
	Spinner profiles;
	Button createProfileButton;
	int selectedProfile = 1;
	CheckBox dailyCheckBox = null;
	Schedule sched;
	GeoPoint p;
	Location loc;
	Profile prof;
	List<Profile> profilesList;
	
		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{		
		super.onCreate(savedInstanceState);
		db= DBAdapter.getInstance(getBaseContext());
		Intent intent = getIntent();
		long id = intent.getLongExtra("id", -1L);
		sched = db.getSchedule((int)id);
		setContentView(R.layout.location_update_entry);
		loc = db.getLocation(sched.getLocationName());
		prof = db.getProfile(sched.getProfileId());
	//	p = new GeoPoint(intent.getIntExtra("Latitude", 0), intent.getIntExtra("Longitude", 0));
				
		latitudeEditText = (EditText) findViewById(R.id.LatitudeEditText);
		longitudeEditText = (EditText) findViewById(R.id.LongitudeEditTest);
		locationNameEditText = (EditText) findViewById(R.id.LocationNameEditText);
		dateEditText = (EditText) findViewById(R.id.DateEditText);
		startTime = (EditText) findViewById(R.id.startTime);
		timeEditDurationText = (EditText) findViewById(R.id.TimeDurationEditText);
		
		updateButton = (Button) findViewById(R.id.UpdateButton);
		cancelButton = (Button) findViewById(R.id.Cancel);
		createProfileButton = (Button) findViewById(R.id.createProfile);
		latitudeEditText.setText("" + loc.getLatitude());
		longitudeEditText.setText("" + loc.getLongitude());
		
		
		cancelButton.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v) {
				Intent intent = new Intent(UpdateScheduleActivity.this,ListActivity.class);
				startActivity(intent);
			}
			
		});
		
		dailyCheckBox = (CheckBox)findViewById(R.id.daily);
		if(sched.isDaily())
			dailyCheckBox.setChecked(true);
		
		locationNameEditText.setText(loc.getLocationName());
		if(sched.getStartDatetime().length() > 10)
			dateEditText.setText(sched.getStartDatetime().substring(0, 10));
		if(sched.getStartDatetime().length() > 11)
			startTime.setText(sched.getStartDatetime().substring(11));
		if(sched.getEndDatetime().length() > 11)
			timeEditDurationText.setText(sched.getEndDatetime().substring(11));
		
		profilesList = db.getProfiles();
		profiles = (Spinner) findViewById(R.id.ProfileSpinner);
	    ArrayAdapter adapter = new ArrayAdapter<Profile>(this, android.R.layout.simple_spinner_item,profilesList);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    //adapter.add(profiles);
	    profiles.setAdapter(adapter);
		
		updateButton.setOnClickListener(new View.OnClickListener() {
	
			public void onClick(View v) {
				//Toast.makeText(getBaseContext(), timeEditText.getText().toString().substring(0, 2)+", "+timeEditText.getText().toString().substring(3)+"*", Toast.LENGTH_SHORT).show();
			if(profilesList.size()!=0)
			{	
				
				if(locationNameEditText.getText().toString().equals("Enter Location Name") || locationNameEditText.getText().toString().equals(""))
					Toast.makeText(UpdateScheduleActivity.this.getBaseContext(), "Please Enter Location Name.", Toast.LENGTH_LONG).show();
				else
				{	
					
			//Date date = new Date(Integer.parseInt(dateEditText.getText().toString().substring(6)), Integer.parseInt(dateEditText.getText().toString().substring(3, 5)), Integer.parseInt(dateEditText.getText().toString().substring(0, 2)), Integer.parseInt(timeEditText.getText().toString().substring(0, 2)), Integer.parseInt(timeEditText.getText().toString().substring(3)));
					long schedId = db.updateSchedule(sched.getScheduleId(), dateEditText.getText()+ " "+startTime.getText(),dateEditText.getText()+" "+timeEditDurationText.getText().toString(), selectedProfile, locationNameEditText.getText().toString(),dailyCheckBox.isChecked());								
					if(schedId < 0)
					{
						Toast.makeText(UpdateScheduleActivity.this.getBaseContext(), "Enter Proper Time", Toast.LENGTH_LONG).show();
					}
					else
					{
						Toast.makeText(UpdateScheduleActivity.this.getBaseContext(), "Schedule updated.", Toast.LENGTH_LONG).show();
						Intent intent = new Intent(UpdateScheduleActivity.this,MainActivity.class);
						startActivity(intent);
						
					}
				}
			}	
			else
			{
				Toast.makeText(UpdateScheduleActivity.this.getBaseContext(), "Please Create one Profile.", Toast.LENGTH_LONG).show();
			}
		}

			
		});
		
		
		createProfileButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(UpdateScheduleActivity.this, CreateProfileActivity.class);
				intent.putExtra("Latitude", loc.getLatitude());
				intent.putExtra("Longitude", loc.getLongitude());
	    		intent.putExtra("locationName", locationNameEditText.getText().toString());
	    		intent.putExtra("time", startTime.getText().toString());
	    		intent.putExtra("tillTime", timeEditDurationText.getText().toString());
	    		intent.putExtra("date", dateEditText.getText().toString());
				startActivity(intent);
	            
				
			}
		});
		
	
		profiles.setOnItemSelectedListener(new MyOnItemSelectedListener());
				
		
	}
	
	public class MyOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
         
          selectedProfile = (int)id+1;
  		Toast.makeText(getBaseContext(), ""+selectedProfile, Toast.LENGTH_LONG);
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
	
	/*
	private Schedule cursorToSchedule(Cursor cursor) {
		Schedule s = new Schedule();
		s.setSDatetime(cursor.getString(2));
		return s;
	}
	*/
}
