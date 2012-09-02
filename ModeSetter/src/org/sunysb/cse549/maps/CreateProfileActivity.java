package org.sunysb.cse549.maps;


import java.util.ArrayList;
import java.util.List;

import org.sunysb.cse549.R;
import org.sunysb.cse549.database.DBAdapter;
import org.sunysb.cse549.database.Profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;



public class CreateProfileActivity extends Activity
{
 
	//a variable to store the seek bar from the XML file
	private SeekBar volumeBar;

	//an AudioManager object, to change the volume settings
	private AudioManager amanager;
	public static  int MAX_VOLUME;
	private Button silent = null;
    private Button normal = null;
    private Button vibrate = null;
    private Button save = null;
    private Ringtone ringtone;
    private TextView seekProgress;
    private TextView profileName;
    private TextView volumeText;
    private int volume;
    private int volumeOnScreen;
    private TextView modes;
    Spinner profiles;
    private static List<String> profileList = null;
    private int selectedProfile = 0;
    private CheckBox vibrator = null;
    private GeoPoint p;
    
    static
    {
    	profileList = new ArrayList<String>();
    	profileList.add("Silent");
    	profileList.add("Vibrate");
    	profileList.add("Normal");
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        Intent intent = getIntent();
        p = new GeoPoint(intent.getIntExtra("Latitude", 0), intent.getIntExtra("Longitude", 0));
		
        
        
        // get labels
        volumeText = (TextView)findViewById(R.id.volume);
        modes = (TextView)findViewById(R.id.mode);
        //get the seek bar from main.xml file
        volumeBar = (SeekBar) findViewById(R.id.sb);
        
        //get the audio manager
        amanager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        MAX_VOLUME = amanager.getStreamMaxVolume(AudioManager.STREAM_RING);
        
        seekProgress = (TextView)findViewById(R.id.progress);
        profileName = (TextView)findViewById(R.id.autoCompleteTextView1);
        
        //seek bar settings//
        //sets the range between 0 and the max volume
        volumeBar.setMax(110);
        //volumeBar.se
        //set the seek bar progress to 1
        volumeBar.setKeyProgressIncrement(1);
        
        
        //sets the progress of the seek bar based on the system's volume
        volumeBar.setProgress(amanager.getStreamVolume(AudioManager.STREAM_RING));
        
        
        amanager = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
         ringtone = RingtoneManager.getRingtone(this,
        		Settings.System.DEFAULT_RINGTONE_URI); 
        
        ringtone.play();
        
          
        profiles = (Spinner) findViewById(R.id.modes);
	    ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,profileList);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    //adapter.add(profiles);
	    profiles.setAdapter(adapter);
	    profiles.setOnItemSelectedListener(new MyOnItemSelectedListener());
	    
	    vibrator = (CheckBox) findViewById(R.id.isVibratory);
        vibrator.setChecked(false);

         save = (Button)findViewById(R.id.button4);
         save.setOnClickListener(saveListener);
           
      
        
        //register OnSeekBarChangeListener, so that the seek bar can change the volume
		volumeBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
		{
			public void onStopTrackingTouch(SeekBar seekBar) 
			{	
		        ringtone.play();
		        Toast.makeText(getBaseContext(),"Hello", Toast.LENGTH_LONG);
		        
			
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) 
			{
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				//change the volume, displaying a toast message containing the current volume and playing a feedback sound

				volume = (int)(((float)(MAX_VOLUME * progress))/100);
				volumeOnScreen = ((100/MAX_VOLUME)*volume)+2;
				amanager.setStreamVolume(AudioManager.STREAM_RING,volume, AudioManager.FLAG_PLAY_SOUND);
				ringtone.play();
				seekProgress.setText("Volume: "+volumeOnScreen);
				
			}
		});  
    }

    private OnClickListener vibrateListener = new OnClickListener()
    {
        public void onClick(View v)
        {                        
            amanager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        	Toast.makeText(getBaseContext(), "vibrate",Toast.LENGTH_LONG).show();
        }
    };
    
    private OnClickListener silentListener = new OnClickListener()
    {
        public void onClick(View v)
        {                        
            amanager.setRingerMode(AudioManager.RINGER_MODE_SILENT); 
        	Toast.makeText(getBaseContext(), "silent",Toast.LENGTH_LONG).show();
        }
    };
    
    private OnClickListener normalListener  = new OnClickListener()
    {
        public void onClick(View v)
        {                        
        	amanager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        	amanager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
    		Toast.makeText(getBaseContext(), "normal",Toast.LENGTH_LONG).show();
        }
    };
    
    private OnClickListener saveListener = new OnClickListener()
    {
        public void onClick(View v)
        {   
        	if(ringtone.isPlaying())
        		ringtone.stop();
        	Profile profile = new Profile();
        	profile.setMode(selectedProfile);
        	profile.setName(profileName.getText().toString());
        	profile.setVolume(volumeOnScreen);
        	profile.setVibratory(CreateProfileActivity.this.vibrator.isChecked());
        	DBAdapter db = DBAdapter.getInstance(CreateProfileActivity.this);
    		int success = db.createProfile(profile);
    		if(success > 0)
    		{	
    			Intent previousIntent = getIntent();
    			 Bundle bundle = previousIntent.getExtras();
    			 String locationName = bundle.getString("locationName");
    		     String date = bundle.getString("date");
    			 String time = bundle.getString("time");
    			 String tillTime = bundle.getString("tillTime");
    			//Toast.makeText(getBaseContext(), "Saved",Toast.LENGTH_LONG).show();
    			Intent intent = new Intent(CreateProfileActivity.this, LocationDetailEntryActivity.class);
    			
    			
    			intent.putExtra("locationName", locationName);
	    		intent.putExtra("date", date);
	    		intent.putExtra("time", time);
	    		intent.putExtra("tillTime", tillTime);
	    		intent.putExtra("Latitude", p.getLatitudeE6());
 	    		intent.putExtra("Longitude", p.getLongitudeE6());
    		     
	    		
	    		startActivity(intent);
    		}
    		else
    			Toast.makeText(getBaseContext(), "Profile with this Name already exists.",Toast.LENGTH_LONG).show();
    			
        }
    };
    
    public class MyOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
         
          selectedProfile = (int)id;
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		//if one of the volume keys were pressed
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)
		{
			//change the seek bar progress indicator position
			volumeBar.setProgress(amanager.getStreamVolume(AudioManager.STREAM_RING));
		}
		//propagate the key event
		return super.onKeyDown(keyCode, event);
	}
}