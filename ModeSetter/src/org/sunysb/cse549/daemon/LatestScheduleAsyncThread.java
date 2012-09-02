package org.sunysb.cse549.daemon;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.sunysb.cse549.beans.CalendarEvent;
import org.sunysb.cse549.database.DBAdapter;
import org.sunysb.cse549.database.Schedule;
import org.sunysb.cse549.listeners.GpsLocationListener;
import org.sunysb.cse549.listeners.NetworkLocationListener;
import org.sunysb.cse549.maps.CreateProfileActivity;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import org.sunysb.cse549.database.Profile;



public class LatestScheduleAsyncThread extends AsyncTask<String, Integer, String>{
	
	// main daemon service
	private DaemonService m;
	
	private LocationManager locManager;
	// network listener
	private static LocationListener networkListener;
	// gps Listener
	private static LocationListener gpsListener;
	// for network provider
	public static boolean networkFlag = false;

	private static Boolean gpsOn = false;
	// default sleepvalue in order to wait for the location from network or gps.. For gps net says 20 seconds...
	private long sleepFor = 10000;
	// for gps .... if true indicates gpslistener has received the location..
	public static Boolean gpsFlag = false;
	// in this the present location of the user is stored, after retrieving from the listeners.
	private Location location;
	// if true, this thread is no longer valid
	public boolean selfKill = false;
	// for keeping the time after which the event should happen,,, DB one... Initialized to max value, bcz used for comparision
	private long scheduleTimeDiff = 99999999999999L;
	// for keeping the time after which the event should happen,,, Calendar one....Initialized to max value, bcz used for comparision.
	private long calendarEventTimeDiff = 99999999999999L;
	// if the location name retrieved from the claendar is wrong..
	private boolean calendarNameWrong = false;
	// volume to be set for profile.
	private int volume;
	// 
	private boolean isVibratory = false;
	// mode for suitable profile
	private int mode = 2;
	// profile name
	private String profileName;
	 // for storing the final difference by taking min b/w calendar & DB....
	private long finalStartTimeDiff = 99999999999999L;
	// for storing the end time of the final difference 
	private long finalEndTimeDiff = 99999999999999L;
// location details from db 
	private double dbLongitude;
	//location details from db
	private double dbLatitude;
	private int count = 0;
	// for retrying inside match function.
	int retryCount = 0;
	// latest calendar event object.
	private CalendarEvent latestCalendarEvent = null;
	
	
	public LatestScheduleAsyncThread(DaemonService m)
	{
		this.m = m;
		
		locManager = (LocationManager) m.getSystemService(Context.LOCATION_SERVICE);				
		
	}
	
	
    @Override
    protected String doInBackground(String eventTimeLocation[]) {
        
    	
    while(true)
    {	
    	
    	if(selfKill)
		{
			break;
		}
    	
    	// initialization.
		scheduleTimeDiff = 99999999999999L;
		calendarEventTimeDiff = 99999999999999L;
		finalStartTimeDiff = 99999999999999L;
		finalEndTimeDiff = 99999999999999L;
		calendarNameWrong = false;
		// get the latest row from the database.
		Cursor cursor = m.getContentResolver().query(Uri.parse("content://org.sunysb.cse549.contentProviders.ModeContentProvider/schedule"), null, null, null, null);
		Schedule sched = getNearestSchedule(cursor);
		cursor.close();
		DBAdapter dba = DBAdapter.getInstance(m.getBaseContext());
		dba.close();
		
		if(!eventTimeLocation[0].equals("null"))
		{
			long eventTime = Long.parseLong(eventTimeLocation[0]);
			calendarEventTimeDiff = eventTime - System.currentTimeMillis();
			Date eventDateTime = new Date(eventTime);
			System.out.println(eventDateTime);
		}
				
		
		// no schedule is found, or if not able to get the location, do nothing..
		if((sched == null && calendarEventTimeDiff < 0))
		{
			//Toast.makeText(this, "Null", Toast.LENGTH_LONG).show();
		}
		// either there is a valid schedule or calendar event.
		else if((calendarEventTimeDiff > 0) || (sched != null))
		{
			
			// convert in milliseconds
			if(sched != null)
			{	
				scheduleTimeDiff = sched.getStartTimeDiff() * 1000;
			}
			
			if((calendarEventTimeDiff < scheduleTimeDiff) && (calendarEventTimeDiff > 0))
			{
				// fetch location & profile details from DB for the same name...
				// if the name is not correct execute the schedule thing.
				// and set calendarNameWrong = true; 
				
				CalendarEvent calendarEvent = m.previousCalendarEvent;
				String location = null;
				if(calendarEvent.getEventLocation() != null)
					location = calendarEvent.getEventLocation().toLowerCase().trim();
				Date date = calendarEvent.getEventDateTime();
				Cursor schedulesCursor = m.getContentResolver().query(Uri.parse("content://org.sunysb.cse549.contentProviders.ModeContentProvider/schedules"), null, " locationName = \'"+location+"\'", null, null);
				// for the latitude and longitude of the location.
				Cursor locationCursor = m.getContentResolver().query(Uri.parse("content://org.sunysb.cse549.contentProviders.ModeContentProvider/location"), null, location, null, null);
				// get the location from the returned locationCursor
				
				org.sunysb.cse549.database.Location calendarLocation = findSuitableLocation(locationCursor);
				
				if( calendarLocation == null)
				{	
					publishProgress(5);
					calendarNameWrong = true;
				}	
				else
				{
					// for finding the best profile
					Schedule s = iterateAndFindSuitableSchedule(schedulesCursor,date);
					// profile corresponding to the schedule found
					Cursor profileCursor = m.getContentResolver().query(Uri.parse("content://org.sunysb.cse549.contentProviders.ModeContentProvider/profile"), null, ""+s.getProfileId(), null, null);
					Profile profile = IterateAndGetProfile(profileCursor);
					volume = profile.getVolume();
					isVibratory = profile.isVibratory();
					finalStartTimeDiff = calendarEventTimeDiff;
					finalEndTimeDiff = Long.parseLong(this.latestCalendarEvent.getEndTime())-System.currentTimeMillis();
					dbLatitude = calendarLocation.getLatitude();
					dbLongitude = calendarLocation.getLongitude();
				}	
			}
			
			if((calendarNameWrong && sched !=null) || (sched !=null && (calendarEventTimeDiff < 0)) || (scheduleTimeDiff < calendarEventTimeDiff))
			{
				// get details of the profile from the schedule object.
				Profile profile = sched.getProfile();
				volume = profile.getVolume()*(CreateProfileActivity.MAX_VOLUME/100); // 100 is the max on screen volume
				isVibratory = profile.isVibratory();
				mode = profile.getMode();
				profileName = profile.getName();
				finalStartTimeDiff = scheduleTimeDiff;
				finalEndTimeDiff = sched.getEndTimeDiff()*1000-60000;
				dbLatitude = sched.getLatitude();
				dbLongitude = sched.getLongitude();
			}
			
			// compare time and location and change the profile if both same
			
			Boolean matchFlag = false;
			if(finalStartTimeDiff != 99999999999999L)
			{
				if(((matchFlag = match(dbLatitude,dbLongitude,finalStartTimeDiff)) != null) && matchFlag )
				{
			    	AudioManager amanager = (AudioManager) m.getSystemService(Context.AUDIO_SERVICE);
			    	int currentMode = amanager.getRingerMode();
			    	int currentVolume = amanager.getStreamVolume(AudioManager.STREAM_RING);
			    	amanager.setRingerMode(mode);
			    	if(isVibratory)
			    	{
			    		amanager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
			    	}
			    	// set the volume in case. In case if volume is zero mode is silent..
			    	amanager.setStreamVolume(AudioManager.STREAM_RING,volume, AudioManager.FLAG_PLAY_SOUND);
			    	//turn off the gpslistener...
			    	if(gpsFlag)
			    	{
			    		if(gpsListener != null)
			    			this.locManager.removeUpdates(gpsListener);
			    		gpsFlag = false;
			    	}
			    	// set to previous modes again.
			    	try
			    	{
			    		
			    		Cursor killCursor = m.getContentResolver().query(Uri.parse("content://org.sunysb.cse549.contentProviders.ModeContentProvider/schedule"), null, null, null, null);
			    		Schedule killSched = getNearestSchedule(killCursor);
			    		cursor.close();
			    		dba.close();
			    		
			    		if(killSched != null)
			    		{
			    			//interval overlap....
			    			if(finalEndTimeDiff > (killSched.getEndTimeDiff()*1000) && finalEndTimeDiff > (killSched.getStartTimeDiff()*1000))
			    			{
			    				this.selfKill = true;
			    				m.latestScheduleThread = new LatestScheduleAsyncThread(m);
			    				m.latestScheduleThread.execute(eventTimeLocation);
			    			}
			    			
			    		}
			    		
			    		// for reducing the time we retried..
			    		finalEndTimeDiff = finalEndTimeDiff -retryCount*1*60*1000;
			    		if(finalEndTimeDiff > 0)
			    		{
			    			Thread.sleep(finalEndTimeDiff);
			    		}
			    		amanager.setRingerMode(currentMode);
			    		amanager.setStreamVolume(AudioManager.STREAM_RING,currentMode, AudioManager.FLAG_PLAY_SOUND);
			    		
			    	}
			    	catch(Exception e)
			    	{
			    		e.printStackTrace();
			    	}
				}
				else if (matchFlag != null && !matchFlag)
				{
					continue;
				}
				// if self kill got true.
				else
					break;
			}
			// nothing found in db nor is there anything from calendar. so sleep...
			else
			{	
				count++;
				if(count > 5)
				{	
					try {
						Thread.sleep(5*60*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					count = 0;
				}	
			
			}	
		}
    }
    	return null;
    
	}
	
    
    private Boolean match(double storedLongitude , double storedLatitude , long sleepTime)
    {
    	retryCount = 0;
        // if the new location is required...take it...
        try
        {
        	if(sleepTime > 20000)
        		Thread.sleep(sleepTime-20000);
        	if(selfKill)
                return null;
        	
        	while(retryCount < 3)
        	{	
        		double distance = getDistance(storedLatitude,storedLongitude); 
        		publishProgress(2,(int)distance);
        	
        		if((int)distance < 25)
        		{	
        			return true;
        		}
        		// change to 3 minutes....retry time.
        		Thread.sleep(60*1*1000);
        		if(selfKill && retryCount == 0)
        			return null;
        		retryCount++;
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
       
        // didn't match after 3 retries.
        return false;
    }
				
	private double getDistance(double storedLongitude , double storedLatitude) {
		// TODO Auto-generated method stub
		// updates the current location...
		updateLocation();
   	 // after every sleep check if selfKill is set to true..

	   if(location == null)
	   {
		   return 100.0;
	   }
       double currentLatitude = location.getLatitude();
       double currentLongitude = location.getLongitude();
       Location dbLocation = new Location(location);
       dbLocation.setLatitude(storedLatitude);
       dbLocation.setLongitude(storedLongitude);
       location.setLatitude(currentLatitude);
       location.setLongitude(currentLongitude);
       //System.out.println("**********"+ dbLocation.distanceTo(location) + "*****");
       return dbLocation.distanceTo(location);
     
	}


	private void updateLocation() {
		// TODO Auto-generated method stub
		// have to check it again and again, in case user switches it on.
		try
		{
			if(networkListener == null)
			{
				networkListener = new NetworkLocationListener(m,locManager);
			}
			// check gps on.
			gpsOn = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			// if on register gps listener...
			if(gpsOn && (gpsListener == null))
			{
				gpsListener = new GpsLocationListener(m,locManager);
			}
			
			// if gps on & gps has provided the location, then update the location.
			if(gpsOn && (!gpsFlag)  )
			{
				gpsFlag = true;
				publishProgress(0);
			}	
			
			else if(!networkFlag)
			{
				// update the location, if network provider has given the location.
				networkFlag = true;
				publishProgress(1);
			}
			  // wait for gps location.//change to 20 sec.
	        Thread.sleep(20*1000);
	        long gpsTimeDifference = System.currentTimeMillis()- m.gpsLocationTime;
	        // given preference to the gps location.. if no location is there, get the most recent one..
	        if(m.gpsLocation == null && m.networkLocation == null)
			{	
				location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
			// for preventing the use of previously stored location of gps...
			
			else if(m.gpsLocation == null || gpsTimeDifference > 2*60*1000)
			{
				location = m.networkLocation;
				// fix for network location = null, then use old gps location..
				if(location == null)
				{
					location = m.gpsLocation;
				}
			}
			else
				location = m.gpsLocation;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
				
	}


	private Schedule getNearestSchedule(Cursor tempCursor) {
		// TODO Auto-generated method stub
		Schedule dummySchedule = getLatestSchedule(tempCursor);
		Schedule scheduleDetials = null;
		if(dummySchedule != null )
		{	
			Cursor joinCursor = m.getContentResolver().query(Uri.parse("content://org.sunysb.cse549.contentProviders.ModeContentProvider/join"), null, ""+dummySchedule.getScheduleId(), null, null);
			 scheduleDetials = this.getScheduleDetails(joinCursor);
			 scheduleDetials.setStartTimeDiff(dummySchedule.getStartTimeDiff());
			 scheduleDetials.setEndTimeDiff(dummySchedule.getEndTimeDiff());
			 System.out.println(scheduleDetials.getStartDatetime());
		}
		return scheduleDetials;
	}


	private Profile cursorToProfile(Cursor profileCursor) {
		// TODO Auto-generated method stub
		Profile p = new Profile();
		p.setName(profileCursor.getString(0));
		p.setMode(profileCursor.getInt(2));
		p.setVolume(profileCursor.getInt(1));
		if(profileCursor.getInt(3) == 1)
			p.setVibratory(true);
		else
			p.setVibratory(false);
		return p;
	}


	private Profile IterateAndGetProfile(Cursor profileCursor) {
		
		profileCursor.moveToFirst();
		Profile p = null;
		while (!profileCursor.isAfterLast()) {
			p = cursorToProfile(profileCursor);
			profileCursor.moveToNext();
		}
		return p;
	}


	private org.sunysb.cse549.database.Location findSuitableLocation(Cursor locationCursor) {
		
		locationCursor.moveToFirst();
		org.sunysb.cse549.database.Location location = null;
		
		while (!locationCursor.isAfterLast()) {
			location = cursorToLocation(locationCursor);
			locationCursor.moveToNext();
		}
		
		return location;
	}


	private org.sunysb.cse549.database.Location cursorToLocation(Cursor locationCursor) {
		org.sunysb.cse549.database.Location location = new org.sunysb.cse549.database.Location();
		location.setLocationName(locationCursor.getString(0));
		location.setLatitude(locationCursor.getDouble(1));
		location.setLongitude(locationCursor.getDouble(2));
		return location;
	}


	private Schedule iterateAndFindSuitableSchedule(Cursor scheduleCursor, Date date) {
		
		scheduleCursor.moveToFirst();
		// hours in a day
		int modeValue = 24;
		int temp = 24;
		Schedule closestMatchingSchedule = null;
		try 
		{
			while (!scheduleCursor.isAfterLast())
			{
				Schedule s = cursorToSchedules(scheduleCursor);
				String scheduleDateTime = s.getStartDatetime();
				scheduleCursor.moveToNext();
				DateFormat formatter ;   
				formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String calendarEventDateTime = formatter.format(date);
				modeValue = compareDates(scheduleDateTime,calendarEventDateTime);
				if(modeValue < temp)
				{
					closestMatchingSchedule = s;
					modeValue = temp;
				}
			}	
			
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return closestMatchingSchedule;
	}
		
		
	private Schedule cursorToSchedules(Cursor scheduleCursor) {
		// TODO Auto-generated method stub
		Schedule s = new Schedule();
		s.setScheduleId(scheduleCursor.getInt(0));
		s.setLocationName(scheduleCursor.getString(1));
		s.setStartDatetime(scheduleCursor.getString(2));
		s.setEndDatetime(scheduleCursor.getString(3));
		s.setProfileId(scheduleCursor.getInt(4));
		s.setDaily(scheduleCursor.getInt(5));
		return s;
	}


	private int compareDates(String scheduleDateTime,
			String calendarEventDateTime) {
		// TODO Auto-generated method stub
		int scheduleHour = getHourFromDateTime(scheduleDateTime);
		int calendarEventHour = getHourFromDateTime(calendarEventDateTime);		
		return Math.abs(scheduleHour - calendarEventHour);
	}
	
	private int getHourFromDateTime(String dateTime)
	{
		String[] scheduledateAndTime = dateTime.split(" ");
		String[] scheduleTime = scheduledateAndTime[1].split(":");
		String hour = scheduleTime[0];
		return Integer.parseInt(hour);
	}


	private Schedule getLatestSchedule(Cursor cursor) {
		
		Schedule s = null;
		long id = 0;
		long timeDiff = 99999999999999L;
		Schedule finalSchedule = null;
		cursor.moveToFirst();
		if(!cursor.moveToFirst())
			return null;
		
		while (!cursor.isAfterLast()) {
			s = cursorToSchedule(cursor);
			if(s.getStartTimeDiff() < timeDiff)
			{
				timeDiff = s.getStartTimeDiff();
				finalSchedule = s;
			}
			cursor.moveToNext();
		}
	  return finalSchedule;
	}
	
	private Schedule cursorToSchedule(Cursor cursor) {
		Schedule s = new Schedule();
		s.setScheduleId(cursor.getLong(0));
		s.setStartTimeDiff(cursor.getLong(1));
		s.setEndTimeDiff(cursor.getLong(2));
		return s;
	}

	
	private Schedule cursorToScheduleDetails(Cursor cursor) {
		Schedule s = new Schedule();
		s.setLatitude(cursor.getDouble(1));
		s.setLongitude(cursor.getDouble(2));
		s.setLocationName(cursor.getString(0));
		s.setStartDatetime(cursor.getString(3));
		s.setEndDatetime(cursor.getString(4));
		Profile profile = new Profile();
		profile.setName(cursor.getString(5));
		if(cursor.getInt(6) == 1)
			profile.setVibratory(true);
		else
			profile.setVibratory(false);
		profile.setVolume(cursor.getInt(7));
		profile.setMode(cursor.getInt(8));
		s.setProfile(profile);
		return s;

	}

private Schedule getScheduleDetails(Cursor cursor) {
		
		Schedule s = null;
		cursor.moveToFirst();
		
		while (!cursor.isAfterLast()) {
			s = cursorToScheduleDetails(cursor);
			cursor.moveToNext();
		}
	  return s;
	}		
    protected void onProgressUpdate(Integer... progress) {
        
    	if(progress[0] == 5)
    	{
    		this.m.calendarNameWrong();
    	}
    	else
    	{	
    		if(progress[0] == 0)
    			this.m.setGpsListener(locManager, gpsListener);
    		else if(progress[0] == 1)
    		{	
    			this.m.setNetworkListener(locManager,networkListener);
    		}
    		else
    			this.m.printDistance(progress[1],retryCount);
    	}	
    }


	public CalendarEvent getLatestCalendarEvent() {
		return latestCalendarEvent;
	}


	public void setLatestCalendarEvent(CalendarEvent latestCalendarEvent) {
		this.latestCalendarEvent = latestCalendarEvent;
	}
}    
