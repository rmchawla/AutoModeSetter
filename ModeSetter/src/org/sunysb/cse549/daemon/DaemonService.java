package org.sunysb.cse549.daemon;

import org.sunysb.cse549.beans.CalendarEvent;
import org.sunysb.cse549.database.DBAdapter;
import org.sunysb.cse549.database.Schedule;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

public class DaemonService extends Service 
{
	public Location networkLocation;
	public Location gpsLocation;
	public long gpsLocationTime = 99999999999999L;
	public static LatestScheduleAsyncThread latestScheduleThread;
	public static SynchronizeCalendarAsynchThread synchronizeCalendarThread;
	public boolean killSignal = false;
	public CalendarEvent previousCalendarEvent = null;
	private boolean preventReExecute = true;
	public static DaemonService m;
	
	@Override	
	public void onCreate() 
	{
		//Toast.makeText(this, "MyAlarmService.onCreate()", Toast.LENGTH_LONG).show();
		m = this;
	}
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		Toast.makeText(this, "MyAlarmService.onBind()", Toast.LENGTH_LONG).show();
		return null;
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		Toast.makeText(this, "MyAlarmService.onDestroy()", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onStart(Intent intent, int startId) 
	{	
		// TODO Auto-generated method stub	
		super.onStart(intent, startId);
		if(preventReExecute)
		{	
			DBAdapter dbAdapter = DBAdapter.getInstance(getBaseContext());
			dbAdapter.setLocale();
			synchronizeCalendarThread = new SynchronizeCalendarAsynchThread(this);
			synchronizeCalendarThread.execute("1");
			latestScheduleThread = new LatestScheduleAsyncThread(this);
			latestScheduleThread.execute("null");
			preventReExecute = false;
		}	
	}
	
	
	@Override
	public boolean onUnbind(Intent intent) 
	{
		Toast.makeText(this, "MyAlarmService.onUnbind()", Toast.LENGTH_LONG).show();
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	public void setGpsListener(LocationManager locManager, LocationListener locListener)
	{
		locManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locListener, Looper.myLooper());
		//locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
		if(gpsLocation!=null)
		{	
			Toast.makeText(this, "GPS Location: "+gpsLocation.getLongitude() +" , "+ gpsLocation.getLatitude(), Toast.LENGTH_LONG).show();
		}
	}
	
	public void setNetworkListener(LocationManager locManager, LocationListener locListener)
	{
		locManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locListener, Looper.myLooper());
		//locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
		if(networkLocation!=null)
		{	
			Toast.makeText(this, "Network Location: "+ networkLocation.getLongitude() +" , "+ networkLocation.getLatitude(), Toast.LENGTH_LONG).show();
		}
	}
	
	public void spawnNewThread(CalendarEvent calendarEvent)
	{
		// set the self kill to true for the previous thread
		 if(latestScheduleThread != null)
			 latestScheduleThread.selfKill = true;
		 // spawn new thread.
		 latestScheduleThread = new LatestScheduleAsyncThread(this);
		 // setting the calendarEvent object
		 latestScheduleThread.setLatestCalendarEvent(calendarEvent);
		 latestScheduleThread.execute(calendarEvent.getEventTime());
		 // change the previous calendarEvent to the current one.
		 previousCalendarEvent = calendarEvent;
	}

	public void decideForSpawning(CalendarEvent calendarEvent) {
		
		if(previousCalendarEvent != null)
		{
			 if(Long.parseLong(previousCalendarEvent.getEventTime()) == Long.parseLong(calendarEvent.getEventTime()))
			 {
				 // do nothing
			 }
			 else
			 {
				 previousCalendarEvent = calendarEvent;
				 spawnNewThread(calendarEvent);
			 }
		}
		else
		{
			previousCalendarEvent = calendarEvent;
			spawnNewThread(calendarEvent);
		}
		
		
	}

	public void printDistance(Integer integer,int retryCount) {
		// TODO Auto-generated method stub
		if(retryCount == 2 && integer == 100)
			Toast.makeText(this, "Unable to detect Location",Toast.LENGTH_LONG).show();
		else if(integer != 100)
			Toast.makeText(this, "Distance from desired Location: "+integer,Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "Didn't find Location: Retrying... ",Toast.LENGTH_LONG).show();
	}

	public void calendarNameWrong() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Calendar location Name Wrong. ",Toast.LENGTH_LONG).show();
	}
		
}