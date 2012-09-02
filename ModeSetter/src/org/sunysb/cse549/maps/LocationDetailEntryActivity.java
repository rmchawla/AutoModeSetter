package org.sunysb.cse549.maps;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.sunysb.cse549.R;
import org.sunysb.cse549.constants.Constants;
import org.sunysb.cse549.database.DBAdapter;
import org.sunysb.cse549.database.Profile;
import org.sunysb.cse549.database.Schedule;
import org.sunysb.cse549.maps.CreateProfileActivity.MyOnItemSelectedListener;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.maps.GeoPoint;

public class LocationDetailEntryActivity extends Activity 
{
	EditText locationNameEditText;
	EditText latitudeEditText;
	EditText longitudeEditText;
	EditText startTime;
	EditText dateEditText;
	EditText timeEditDurationText;
	Button saveButton;
	Button cancelButton;
	DBAdapter db;
	Spinner profiles;
	Button createProfileButton;
	int selectedProfile = 1;
	CheckBox dailyCheckBox = null;
	GeoPoint p;
	List<Profile> profilesList;
	
		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_detail_entry);
	
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		p = new GeoPoint(intent.getIntExtra("Latitude", 0), intent.getIntExtra("Longitude", 0));
		
		
		db= DBAdapter.getInstance(getBaseContext());
		latitudeEditText = (EditText) findViewById(R.id.LatitudeEditText);
		longitudeEditText = (EditText) findViewById(R.id.LongitudeEditTest);
		locationNameEditText = (EditText) findViewById(R.id.LocationNameEditText);
		dateEditText = (EditText) findViewById(R.id.DateEditText);
		startTime = (EditText) findViewById(R.id.startTime);
		timeEditDurationText = (EditText) findViewById(R.id.TimeDurationEditText);
		
		saveButton = (Button) findViewById(R.id.SaveButton);
		cancelButton = (Button) findViewById(R.id.Cancel);
		createProfileButton = (Button) findViewById(R.id.createProfile);
		latitudeEditText.setText("" + p.getLatitudeE6() / 1E6);
		longitudeEditText.setText("" + p.getLongitudeE6() / 1E6);
		
		dailyCheckBox = (CheckBox)findViewById(R.id.daily);
		if(intent.getStringExtra("locationName") != null)
			locationNameEditText.setText(intent.getStringExtra("locationName").toString());
		locationNameEditText.setOnFocusChangeListener(locNameListener);
		
		if(intent.getStringExtra("date") != null)
			dateEditText.setText(intent.getStringExtra("date").toString());
		else
			dateEditText.setText("yyyy-mm-dd");
		
		if(intent.getStringExtra("time") != null)
			startTime.setText(intent.getStringExtra("time").toString());
		else
			startTime.setText("hh:mm");
		
		if(intent.getStringExtra("tillTime") != null)
			timeEditDurationText.setText(intent.getStringExtra("tillTime").toString());
		else
			timeEditDurationText.setText("hh:mm");
		
		profilesList = db.getProfiles();
		profiles = (Spinner) findViewById(R.id.ProfileSpinner);
	    ArrayAdapter adapter = new ArrayAdapter<Profile>(this, android.R.layout.simple_spinner_item,profilesList);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    //adapter.add(profiles);
	    profiles.setAdapter(adapter);
		
	    
	    cancelButton.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v) {
				Intent intent = new Intent(LocationDetailEntryActivity.this,MainActivity.class);
				startActivity(intent);
			}
			
		});

	    
	    
		saveButton.setOnClickListener(new View.OnClickListener() {
	
			public void onClick(View v) {
				//Toast.makeText(getBaseContext(), timeEditText.getText().toString().substring(0, 2)+", "+timeEditText.getText().toString().substring(3)+"*", Toast.LENGTH_SHORT).show();
			if(profilesList.size()!=0)
			{	
				
				if(locationNameEditText.getText().toString().equals("Enter Location") || locationNameEditText.getText().toString().trim().length() == 0)
					Toast.makeText(LocationDetailEntryActivity.this.getBaseContext(),"Please Enter location name" , Toast.LENGTH_LONG).show();
				else
				{	
					boolean dateCheck = LocationDetailEntryActivity.this.dateValidation(dateEditText.getText().toString());
					if(dateCheck)
					{	
						boolean timeCheck = LocationDetailEntryActivity.this.timeValidation(startTime.getText().toString(),timeEditDurationText.getText().toString());
						
						if(timeCheck)
						{	
							long loc_id = db.insertLocation(locationNameEditText.getText().toString(), Double.parseDouble(latitudeEditText.getText().toString()), Double.parseDouble(longitudeEditText.getText().toString()));
							String timeduration = timeEditDurationText.getText().toString();
					//Date date = new Date(Integer.parseInt(dateEditText.getText().toString().substring(6)), Integer.parseInt(dateEditText.getText().toString().substring(3, 5)), Integer.parseInt(dateEditText.getText().toString().substring(0, 2)), Integer.parseInt(startTime.getText().toString().substring(0, 2)), Integer.parseInt(startTime.getText().toString().substring(3)));
							//String temp[] = startTime.getText().toString().split(":");
							//int i = Integer.parseInt(temp[1])+5;
							long schedId = db.insertSchedule(dateEditText.getText()+ " "+startTime.getText(),dateEditText.getText()+" "+timeduration, selectedProfile, locationNameEditText.getText().toString(),dailyCheckBox.isChecked());								
							if(schedId < 0)
							{
								Toast.makeText(LocationDetailEntryActivity.this.getBaseContext(), "Enter Proper Time", Toast.LENGTH_LONG).show();
							}
							else
							{
									Toast.makeText(LocationDetailEntryActivity.this.getBaseContext(), "Schedule saved.", Toast.LENGTH_LONG).show();
									Intent intent = new Intent(LocationDetailEntryActivity.this,MainActivity.class);
									startActivity(intent);
							}
						}	
					}
				}	
			}	
			else
			{
				Toast.makeText(LocationDetailEntryActivity.this.getBaseContext(), "Please Create one Profile.", Toast.LENGTH_LONG).show();
			}
		}

			private void dateValidation() {
				// TODO Auto-generated method stub
				
			}

			
		});
		
		locationNameEditText.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				locationNameEditText.setText(" ");	
				Toast.makeText(getBaseContext(), "hello", Toast.LENGTH_LONG);
			}
		});
		
		
		createProfileButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(LocationDetailEntryActivity.this, CreateProfileActivity.class);
				intent.putExtra("Latitude", p.getLatitudeE6());
				intent.putExtra("Longitude", p.getLongitudeE6());
	    		intent.putExtra("locationName", locationNameEditText.getText().toString());
	    		intent.putExtra("time", startTime.getText().toString());
	    		intent.putExtra("tillTime", timeEditDurationText.getText().toString());
	    		intent.putExtra("date", dateEditText.getText().toString());
				startActivity(intent);
			}
		});
		profiles.setOnItemSelectedListener(new MyOnItemSelectedListener());
	}
	
    protected boolean timeValidation(String time,String time2) {
		// TODO Auto-generated method stub
    		DateFormat formatter = null;
    		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    		try {
				Date startDate = (Date)formatter.parse(dateEditText.getText().toString()+" "+time);
				Date endDate = (Date)formatter.parse(dateEditText.getText().toString()+" "+time2);
				String timer[] = time.split(":");
				String timer2[] = time2.split(":");
				if(timer.length != 2 || timer2.length != 2)
				{
					Toast.makeText(LocationDetailEntryActivity.this.getBaseContext(), "Please Enter proper Time in HH:MM", Toast.LENGTH_LONG).show();
					return false;
				}
				if((Integer.parseInt(timer[0])>=0 && Integer.parseInt(timer[0]) <= 23) && (Integer.parseInt(timer[1])>=0 && Integer.parseInt(timer[1])<=59) && (Integer.parseInt(timer2[0])>=0 && Integer.parseInt(timer2[0]) <= 23) && (Integer.parseInt(timer2[1])>=0 && Integer.parseInt(timer2[1])<=59))
				{
					
					if(startDate.getTime()>0 && endDate.getTime()>0)
					{
						if(startDate.getTime() > endDate.getTime())
						{
							Toast.makeText(LocationDetailEntryActivity.this.getBaseContext(), "Start date should be earlier than end Date", Toast.LENGTH_LONG).show();
							return false;
						}
						else
							return true;
					}
					else
					{
						Toast.makeText(LocationDetailEntryActivity.this.getBaseContext(), "Please Enter proper Time in HH:MM", Toast.LENGTH_LONG).show();
						return false;
					}
						
				}
				else
				{
					Toast.makeText(LocationDetailEntryActivity.this.getBaseContext(), "Please Enter proper Time in HH:MM", Toast.LENGTH_LONG).show();
					return false;

				}
					
    		}
    		catch (Exception e) {
				// TODO Auto-generated catch block
				Toast.makeText(LocationDetailEntryActivity.this.getBaseContext(), "Please Enter proper Time in HH:MM", Toast.LENGTH_LONG).show();
				return false;
    		}
	}

	// Location name change listener
    private OnFocusChangeListener  locNameListener = new OnFocusChangeListener()
    {
    	public void onFocusChange(View v, boolean hasFocus)
        {       
    		if(hasFocus)
    		{
	        	EditText searchText = (EditText)findViewById(R.id.LocationNameEditText);
	    		if(searchText.getText().toString().equals(Constants.DefLocName))
	    			searchText.setText("");	    	
    		}
        }
    };
	
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
	
	
	public boolean dateValidation(String date)
	{
		
			DateFormat formatter = null;
			formatter = new SimpleDateFormat("yyyy-MM-dd");
			try {
				Date startDate = (Date)formatter.parse(date);
				if(startDate.getTime()>0)
					return true;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				Toast.makeText(LocationDetailEntryActivity.this.getBaseContext(), "Please Enter proper Date yyyy-mm-dd.", Toast.LENGTH_LONG).show();
				return false;
			}
		return false;
	}
	
	/*
	private Schedule cursorToSchedule(Cursor cursor) {
		Schedule s = new Schedule();
		s.setSDatetime(cursor.getString(2));
		return s;
	}
	*/
}
