package org.sunysb.cse549.daemon;

import java.util.Date;

import org.sunysb.cse549.beans.CalendarEvent;
import org.sunysb.cse549.database.DBAdapter;
import org.sunysb.cse549.maps.CreateProfileActivity;

import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.style.BackgroundColorSpan;
import android.util.Log;



public class SynchronizeCalendarAsynchThread extends AsyncTask<String, Integer, String>{
	
	DaemonService m;
	LocationManager locManager;
	LocationListener networkListener;
	LocationListener gpsListener;
	public static boolean flag = false;
	static Boolean gpsOn = false;
	private long sleepFor = 10000;
	public static Boolean gpsFlag = false;
	private Location location;
	private static final String DEBUG_TAG = "SynchronizeCalendarAsynchThread";
	private CalendarEvent calendarEvent = null;
	private int successEvent = 0;
	// for daily updates..
	private long baseTime = System.currentTimeMillis();
	
	
	public SynchronizeCalendarAsynchThread(DaemonService m)
	{
		this.m = m;
	}
	
	
    @Override
    protected String doInBackground(String selectedImage[]) {
    
    	long timeDiff = 0;
    	while(true)
    	{	
    		timeDiff = System.currentTimeMillis()-baseTime;
    		//change to 24 hour..
    		//checkDB();
    			
    		if(timeDiff > 24*60*60*1000)
    		{
    			updateDB();
    			baseTime = System.currentTimeMillis();
    		}
    		int iTestCalendarID = ListSelectedCalendars();
    		 if (iTestCalendarID != 0)
    		 {
            	Log.i(DEBUG_TAG, "****Anuj: reached here*****");
            	calendarEvent = ListAllCalendarEntrySummary(iTestCalendarID);
            	if(calendarEvent != null)
            	{
            		long timediff = Long.parseLong(calendarEvent.getEventTime()) - System.currentTimeMillis();
            		// configure time
            		if(timediff < 5*60*1000)
            			successEvent = 1;
            	}
            	publishProgress(successEvent);
    		 }
    		 
	    	try 
	    	{
				Thread.sleep(60*2*1000);
			}
	    	catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	
    	}
    	
    }
	
	
    private void updateDB() {
		
    	DBAdapter dbAdapter = DBAdapter.getInstance(m.getBaseContext());
		dbAdapter.updateDailySchedules();
    	
	}


	protected void onProgressUpdate(Integer... progress) {
        
    	if(progress[0] == 1)
    	{
    		m.decideForSpawning(calendarEvent);
    		successEvent = 0;
    	}
    	
    }
    
	
	private void checkDB()
	{
		DBAdapter dbAdapter = DBAdapter.getInstance(m.getBaseContext());
		dbAdapter.checkTime();
    	
	}
    
    private Cursor getCalendarManagedCursor(String[] projection,
            String selection, String path) {
        Uri calendars = Uri.parse("content://calendar/" + path);

        Cursor managedCursor = null;
        try {
        	managedCursor = m.getContentResolver().query(calendars, projection, selection, null, null);
           
        } catch (IllegalArgumentException e) {
            Log.w(DEBUG_TAG, "Failed to get provider at ["
                    + calendars.toString() + "]");
        }

        if (managedCursor == null) {
            // try again
            calendars = Uri.parse("content://com.android.calendar/" + path);
            try {
            	managedCursor = m.getContentResolver().query(calendars, projection, selection, null, null);
            } catch (IllegalArgumentException e) {
                Log.w(DEBUG_TAG, "Failed to get provider at ["
                        + calendars.toString() + "]");
            }
        }
        return managedCursor;
    }
    //"dtend"
    private CalendarEvent ListAllCalendarEntrySummary(int calID) {
    	String[] projection = new String[] { "_id", "title", "dtstart","eventLocation","dtend" };
 
    	Cursor managedCursor = getCalendarManagedCursor(projection, "calendar_id="
                + calID, "events");

    	String eventTitle = null;
        String eventLocation = null;
        String eventEndTime = null;
        long eventTime = 99999999999999L;
        int min = 0;
        long temp = 0;
        long diff = 0;
        boolean flag = false;
        
        if (managedCursor != null && managedCursor.moveToFirst()) {
            Log.i(DEBUG_TAG, "Listing Calendar Event Summary");
            do {
                Log.i(DEBUG_TAG, "**START Calendar Event Summary**");
                
                temp = managedCursor.getLong(2);
                diff = temp - System.currentTimeMillis();
                
                if(temp < eventTime && diff > 0)
                {
                	eventTitle = managedCursor.getString(1);
                	eventLocation = managedCursor.getString(3);
                	eventEndTime = managedCursor.getString(4);
                	eventTime = temp;
                	flag = true;
                }
                for (int i = 0; i < managedCursor.getColumnCount(); i++) {
                    Log.i(DEBUG_TAG, managedCursor.getColumnName(i) + "="
                            + managedCursor.getString(i));
                }
                Log.i(DEBUG_TAG, "**END Calendar Event Description**");
            } while (managedCursor.moveToNext());
          //   = new CalendarEvent();
            
            //System.out.println(eventLocation);
            //System.out.println(new Date(eventTime));
            //System.out.println(eventTitle);
            if( flag && eventLocation != null && eventTime != 99999999999999L)
            {	
	            calendarEvent = new CalendarEvent();
	            calendarEvent.setEventLocation(eventLocation);
	            calendarEvent.setEventTime(""+eventTime);
	            calendarEvent.setEventDateTime(new Date(eventTime));
	            calendarEvent.setEventTitle(eventTitle);
	            calendarEvent.setEndTime(eventEndTime);
            }   
            else 
            	return null;
        } else {
            Log.i(DEBUG_TAG, "No Calendar Entry");
        }
        return calendarEvent;
    }

    
    private int ListSelectedCalendars() {
        int result = 0;
        String[] projection = new String[] { "_id", "name", "_sync_account_type" };
        String selection = "selected=1";
        String path = "calendars";

        Cursor managedCursor = getCalendarManagedCursor(projection, selection,
                path);

        if (managedCursor != null && managedCursor.moveToFirst()) {

            Log.i(DEBUG_TAG, "Listing Selected Calendars Only");

            int nameColumn = managedCursor.getColumnIndex("name");
            int idColumn = managedCursor.getColumnIndex("_id");
            int accountColumn = managedCursor.getColumnIndex("_sync_account_type");
            
            do {
                String calName = managedCursor.getString(nameColumn);
                String calId = managedCursor.getString(idColumn);
                String calAccount = managedCursor.getString(accountColumn);
                Log.i(DEBUG_TAG, "Found Calendar '" + calName + "' (ID="
                        + calId + ")");
                //if (calName != null && calName.contains("anuj")) {
                if (calAccount != null && calAccount.contains("com.google")) {
                    result = Integer.parseInt(calId);
                    break;
                }
            } while (managedCursor.moveToNext());
        } else {
            Log.i(DEBUG_TAG, "No Calendars");
        }

        return result;

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

    
    
    
}    
