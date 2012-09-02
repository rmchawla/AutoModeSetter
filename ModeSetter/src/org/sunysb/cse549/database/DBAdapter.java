package org.sunysb.cse549.database;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sunysb.cse549.beans.CalendarEvent;
import org.sunysb.cse549.constants.Constants;
import org.sunysb.cse549.daemon.DaemonService;
import org.sunysb.cse549.daemon.LatestScheduleAsyncThread;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class DBAdapter
{
	private static final String TAG = "DBAdapter";
	
	//schedule table, column names and queries
	public static final String SCHEDULE_TABLE = "SCHEDULE";
	public static final String SCHED_SCHEDID = "schedId";
	public static final String SCHED_START_TIME = "startTime";
	public static final String SCHED_END_TIME = "endTime";
	public static final String SCHED_PROFILE_ID = "profileId";
	public static final String SCHED_LOCATION_NAME = "locationName";
	public static final String SCHED_DAILY = "dailyFlag";
	private static final String SCHEDULE_CREATE = "create table if not exists "+SCHEDULE_TABLE+" ("+SCHED_SCHEDID+" integer primary key autoincrement, "+ SCHED_LOCATION_NAME +" text not null, " +SCHED_START_TIME+" datetime not null, "+SCHED_END_TIME+" datetime not null, "+SCHED_PROFILE_ID+" text not null, " +SCHED_DAILY+" integer not null DEFAULT 0);";
	
	//location table, column names and queries
	public static final String LOCATION_TABLE = "LOCATION";
	public static final String LOC_LOCATION_NAME = "locationName";
	public static final String LOC_LATITUDE = "latitude";
	public static final String LOC_LONGITUDE = "longitude";
	private static final String LOCATION_CREATE = "create table if not exists "+LOCATION_TABLE+" ("+LOC_LOCATION_NAME+" text primary key, "+LOC_LATITUDE+" double not null, "+LOC_LONGITUDE+" double not null);";
	
	//profile table, column names and queries
	public static final String PROFILE_TABLE = "PROFILE";
	public static final String PROFILE_ID = "profileId";
	public static final String PROFILE_NAME = "profileName";
	public static final String PROFILE_VOLUME = "volume";
	public static final String PROFILE_MODE = "mode";
	public static final String PROFILE_ISVIBRATORY = "isVibratory";
	public static final String PROFILE_CREATE = "create table if not exists "+PROFILE_TABLE+" (" +PROFILE_ID+ " integer primary key, " +PROFILE_NAME+" text not null , "+PROFILE_VOLUME+" integer not null , " +PROFILE_MODE+" integer not null ," +PROFILE_ISVIBRATORY+" integer not null DEFAULT 0);";	
	
	private static DBAdapter dbAdapter = null;
	private Context context = null;
	
	private SQLiteDatabase db;
	
	private String dailyQuery = "update "+ SCHEDULE_TABLE +" set "+SCHED_START_TIME +"= datetime(strftime(\'%s\',"+SCHED_START_TIME +") + 86400,\'unixepoch\'),"+SCHED_END_TIME +"= datetime(strftime(\'%s\',"+SCHED_END_TIME +") + 86400,\'unixepoch\') where strftime(\'%s\',"+SCHED_START_TIME +") + "+ Constants.localeDifference+" - strftime(\'%s\',\'now\') < 0 and " +SCHED_DAILY+"=1";
	public static final String query = "select strftime(\'%s\',\'now\')";
	public static final String dbquery = "select strftime(\'%s\','2012-05-13 17:45')";
	
	private  DBAdapter(Context context)
	{
		this.context = context;
		open();
		db.execSQL(SCHEDULE_CREATE);
		db.execSQL(LOCATION_CREATE);
		db.execSQL(PROFILE_CREATE);
		close();
	}
	
	public static DBAdapter getInstance(Context context)
	{
		
		if(dbAdapter == null)
		{
			dbAdapter = new DBAdapter(context);
			
		}
		return dbAdapter;
	}
	
	public synchronized SQLiteDatabase open() throws SQLException
	{
		
		DBHelper dbh = DBHelper.getInstance(context);
		db = dbh.getWritableDatabase();
		return db;
	}
	
	public synchronized void close()
	{
		if(db.isOpen())
		{	
			db.close();
		}	
	}
	
	public long insertLocation(String locationName, double latitude, double longitude)
	{
		open();
		ContentValues initialValues = new ContentValues();
		initialValues.put(LOC_LOCATION_NAME, locationName.toLowerCase().trim());
		initialValues.put(LOC_LATITUDE, latitude);
		initialValues.put(LOC_LONGITUDE, longitude);
		long retVal = db.insert(LOCATION_TABLE, null, initialValues);
		close();
		return retVal;
	}
	
	public long insertSchedule(String startTime, String endTime, int profileId, String location_name , boolean dailyFlag)
	{
		long retVal = -1;
		if(checkValidity(startTime,endTime))
		{
			open();
			ContentValues initialValues = new ContentValues();
			initialValues.put(SCHED_PROFILE_ID, profileId);
			initialValues.put(SCHED_LOCATION_NAME, location_name.toLowerCase().trim());
			initialValues.put(SCHED_START_TIME, startTime);
			initialValues.put(SCHED_END_TIME, endTime);
			if(dailyFlag)
				initialValues.put(SCHED_DAILY, 1);
			else
				initialValues.put(SCHED_DAILY, 0);
			retVal = db.insert(SCHEDULE_TABLE, null, initialValues);
			close();
			// for killing the existing thread on update.
			if(retVal > 0)
			{
				if(DaemonService.m.latestScheduleThread != null)
				{
					DaemonService.m.latestScheduleThread.selfKill = true;
				}
				DaemonService.m.latestScheduleThread = new LatestScheduleAsyncThread(DaemonService.m);
				CalendarEvent calendarEvent = null;
				String passingString = null;
				if(( calendarEvent = DaemonService.m.previousCalendarEvent) != null)
				{
					 passingString = ""+calendarEvent.getEventTime();	
				}
				if(passingString == null)	
					DaemonService.m.latestScheduleThread.execute("null");
				else
					DaemonService.m.latestScheduleThread.execute(passingString);
			}
		}
		return retVal;
	}

	private boolean checkValidity(String startTime, String endTime) {
		// TODO Auto-generated method stub
		DateFormat formatter = null;
		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date startDate = null;
		Date endDate = null;
		try {
			startDate = (Date)formatter.parse(startTime);
			endDate = (Date)formatter.parse(endTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(startDate.getTime() >= endDate.getTime())
			return false;
		else
			return true;
	}

	public int createProfile(Profile profile) {
		open();
		ContentValues values = new ContentValues();
		if(profile.getName()!=null)
			values.put(PROFILE_NAME, profile.getName().toLowerCase().trim());
		values.put(PROFILE_VOLUME, (int)(profile.getVolume()));
		values.put(PROFILE_MODE, profile.getMode());
		if(profile.isVibratory())
			values.put(PROFILE_ISVIBRATORY, 1);
		else	
			values.put(PROFILE_ISVIBRATORY, 0);
		int insertId = (int)db.insert(PROFILE_TABLE, null,
				values);
		
		return insertId;
	}	
	
	public boolean deleteSchedule(long sched_id)
	{
		open();
		boolean retVal = db.delete(SCHEDULE_TABLE, SCHED_SCHEDID + "=" + sched_id, null) > 0;
		close();
		return retVal;
	}
	
	public boolean deleteLocation(long loc_id)
	{
		open();
		boolean retVal = db.delete(LOCATION_TABLE, LOC_LOCATION_NAME + "=" + loc_id, null) > 0;
		close();
		return retVal;
	}

	public boolean deleteProfile(String profileName)
	{
		open();
		boolean retVal = db.delete(PROFILE_TABLE, PROFILE_NAME + "=" + profileName, null) > 0;
		close();
		return retVal;
	}
	
	
	public List<Profile> getProfiles() {
		open();
		List<Profile> profileList = new ArrayList<Profile>();

		Cursor cursor = db.query(PROFILE_TABLE,
				null, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Profile profile = cursorToProfile(cursor);
			profileList.add(profile);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		close();
		return profileList;
	}

	
	public void updateDailySchedules()
	{
		open();
		db.execSQL(dailyQuery);
		//print();
		close();
	}
	
	
	public void checkTime()
	{
		open();
		Cursor cursor = db.rawQuery(dbquery, null);
		cursor.moveToFirst();
		long dbtime = cursor.getLong(0);
		close();
		String str_date = "2012-05-13 17:45";
		DateFormat formatter  = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = null;
		try {
			date = (Date)formatter.parse(str_date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		long systemtime = date.getTime()/1000;
		System.out.println(systemtime);
		System.out.println(dbtime);
		
		long absDiff = 0;
		long diff = 0;
		if((absDiff = (Math.abs(systemtime - dbtime))) > 10)
		{
			diff = (systemtime) - dbtime;
			// if system is behind
			if(diff > 0)
			{	
				Constants.localeDifference = absDiff;
			}
			// if system is ahead
			else
			{
				Constants.localeDifference = -absDiff;
			}
		}
		
		String scheduleQuery = "select schedId,(strftime(\'%s\',startTime) + " + Constants.localeDifference+ " - strftime(\'%s\',\'now\'))  as startTimeDiff, (strftime(\'%s\',endTime) + "+ Constants.localeDifference+" - strftime(\'%s\',\'now\')) as endTimeDiff from SCHEDULE where startTimeDiff > 0";		
		System.out.println(scheduleQuery);
		open();
		Cursor scheduleCursor = db.rawQuery(scheduleQuery,null);
		Schedule schedule = getLatestSchedule(scheduleCursor);
		close();
	}
	
	private void print() {
		
		Cursor cursor = db.query(SCHEDULE_TABLE, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast())
		{
			Schedule s = new Schedule();
		
			s.setStartDatetime(cursor.getString(2));
			s.setEndDatetime(cursor.getString(3));
			s.setDaily(cursor.getInt(5));
			cursor.moveToNext();
		}
		
	}
	
	

	private Profile cursorToProfile(Cursor cursor) {
		Profile p = new Profile();
		p.setProfileId(cursor.getInt(0));
		p.setName(cursor.getString(1));
		p.setMode(cursor.getInt(3));
		p.setVolume(cursor.getInt(2));
		if(cursor.getInt(4) == 1)
			p.setVibratory(true);
		else
			p.setVibratory(false);
		return p;
	}

	public void setLocale() {
		
		open();
		Cursor cursor = db.rawQuery(dbquery, null);
		cursor.moveToFirst();
		long dbtime = cursor.getLong(0);
		close();
		String str_date = "2012-05-13 17:45";
		DateFormat formatter  = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = null;
		try {
			date = (Date)formatter.parse(str_date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		long systemtime = date.getTime()/1000;
		System.out.println(systemtime);
		System.out.println(dbtime);
		
		long absDiff = 0;
		long diff = 0;
		if((absDiff = (Math.abs(systemtime - dbtime))) > 10)
		{
			diff = (systemtime) - dbtime;
			// if system is behind
			if(diff > 0)
			{	
				Constants.localeDifference = absDiff;
			}
			// if system is ahead
			else
			{
				Constants.localeDifference = -absDiff;
			}
		}
		
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
	
	public Map<Long, Schedule> getAllSchedules() {
		Map<Long, Schedule> schedmap = null;
		open();
		Cursor c = db.query(SCHEDULE_TABLE, new String[] {SCHED_SCHEDID, SCHED_START_TIME, SCHED_END_TIME, SCHED_PROFILE_ID, SCHED_LOCATION_NAME, SCHED_DAILY}, null, null, null, null, null);
		if(c.moveToFirst()) {
			schedmap = new HashMap<Long, Schedule>();
			do {
				Schedule sched = new Schedule();
				sched.setScheduleId(Long.parseLong(c.getString(0)));
				sched.setStartDatetime(c.getString(1));
				sched.setEndDatetime(c.getString(2));
				sched.setProfileId(Integer.parseInt(c.getString(3)));
				sched.setLocationName(c.getString(4));
				sched.setDaily(Boolean.getBoolean(c.getString(5)));
				schedmap.put(sched.getScheduleId(), sched);
				System.out.println(sched.getScheduleId() + " " + sched.getProfileId() +" " + sched.getLocationName());
			} while(c.moveToNext());
		} else {
			System.out.println("Nothing found");
		}
		c.close();
		close();
		return schedmap;
	}

	public Schedule getSchedule(int id) {
		Schedule sched = null;
		open();
		Cursor c = db.query(SCHEDULE_TABLE, new String[] {SCHED_SCHEDID, SCHED_START_TIME, SCHED_END_TIME, SCHED_PROFILE_ID, SCHED_LOCATION_NAME, SCHED_DAILY}, SCHED_SCHEDID+"=?", new String[] {id+""}, null, null, null);
		if(c.moveToFirst()) {
			sched = new Schedule();
			sched.setScheduleId(Long.parseLong(c.getString(0)));
			sched.setStartDatetime(c.getString(1));
			sched.setEndDatetime(c.getString(2));
			sched.setProfileId(Integer.parseInt(c.getString(3)));
			sched.setLocationName(c.getString(4));
			if(c.getInt(5) == 1)
				sched.setDaily(true);
			else
				sched.setDaily(false);
			System.out.println("******"+sched.getScheduleId() + " " + sched.getProfileId() +" " + sched.getLocationName());
		}
		return sched;
	}
	
	public Location getLocation(String locname) {
		Location location = null;
		open();
		Cursor c = db.query(LOCATION_TABLE, new String[] {LOC_LOCATION_NAME, LOC_LATITUDE, LOC_LONGITUDE}, LOC_LOCATION_NAME+"=?", new String[] {locname}, null, null, null);
		if(c.moveToFirst()) {
			location = new Location();			
			location.setLocationName(c.getString(0));
			location.setLatitude(Double.parseDouble(c.getString(1)));
			location.setLongitude(Double.parseDouble(c.getString(2)));
			System.out.println(location.getLatitude() + " " + location.getLocationName() + " " + location.getLongitude());
		} else {
			System.out.println("Nothing found");
		}
		c.close();
		close();
		return location;
	}
	
	public Profile getProfile(int id) {
		Profile prof = null;
		open();
		Cursor c = db.query(PROFILE_TABLE, new String[] {PROFILE_ID, PROFILE_NAME, PROFILE_VOLUME, PROFILE_MODE, PROFILE_ISVIBRATORY}, PROFILE_ID+"=?", new String[] {""+id}, null, null, null);
		if(c.moveToFirst()) {
			prof = new Profile();
			prof.setProfileId(Integer.parseInt(c.getString(0)));
			prof.setName(c.getString(1));
			prof.setVolume(Integer.parseInt(c.getString(2)));
			prof.setMode(Integer.parseInt(c.getString(3)));
			prof.setVibratory(Boolean.parseBoolean(c.getString(4)));
			System.out.println(prof.getMode() + " "+ prof.getName() + " " + prof.getProfileId() + " "+ prof.getVolume());
		} else {
			System.out.println("Nothing found");
		}
		c.close();
		close();
		return prof;
	}
	
	public Map<Integer, Profile> getAllPofiles() {
		Map<Integer, Profile> profmap = null;
		open();
		Cursor c = db.query(PROFILE_TABLE, new String[] {PROFILE_ID, PROFILE_NAME, PROFILE_VOLUME, PROFILE_MODE, PROFILE_ISVIBRATORY}, null, null, null, null, null);
		if(c.moveToFirst()) {
			profmap = new HashMap<Integer, Profile>();
			do {
				Profile prof = new Profile();
				prof.setProfileId(Integer.parseInt(c.getString(0)));
				prof.setName(c.getString(1));
				prof.setVolume(Integer.parseInt(c.getString(2)));
				prof.setMode(Integer.parseInt(c.getString(3)));
				prof.setVibratory(Boolean.parseBoolean(c.getString(4)));
				profmap.put(prof.getProfileId(), prof);
				System.out.println(prof.getMode() + " "+ prof.getName() + " " + prof.getProfileId() + " "+ prof.getVolume());
			} while(c.moveToNext());
		} else {
			System.out.println("Nothing found");
		}
		c.close();
		close();
		return profmap;
	}	
	
	public Map<String, Location> getAllLocations() {
		Map<String, Location> locmap = null;
		open();
		Cursor c = db.query(LOCATION_TABLE, new String[] {LOC_LOCATION_NAME, LOC_LATITUDE, LOC_LONGITUDE}, null, null, null, null, null);
		if(c.moveToFirst()) {
			locmap = new HashMap<String, Location>();
			do {
				Location loc = new Location();
				loc.setLocationName(c.getString(0));
				loc.setLatitude(Double.parseDouble(c.getString(1)));
				loc.setLongitude(Double.parseDouble(c.getString(2)));
				locmap.put(loc.getLocationName(), loc);
				System.out.println(loc.getLatitude() + " " + loc.getLocationName() + " " + loc.getLongitude());
			} while(c.moveToNext());
		} else {
			System.out.println("Nothing found");
		}
		c.close();
		close();
		return locmap;
	}	

	public long updateSchedule(long SchedId, String startTime, String endTime, int profileId, String location_name , boolean dailyFlag)
	{
		long retVal = -1;
		if(checkValidity(startTime,endTime))
		{
			open();
			ContentValues initialValues = new ContentValues();
			initialValues.put(SCHED_PROFILE_ID, profileId);
			initialValues.put(SCHED_LOCATION_NAME, location_name);
			initialValues.put(SCHED_START_TIME, startTime);
			initialValues.put(SCHED_END_TIME, endTime);
			if(dailyFlag)
				initialValues.put(SCHED_DAILY, 1);
			else
				initialValues.put(SCHED_DAILY, 0);
			retVal = db.update(SCHEDULE_TABLE, initialValues, SCHED_SCHEDID+"=?", new String[] {""+SchedId});
			close();
			// for killing the existing thread on update.
			if(retVal > 0)
			{
				if(DaemonService.m.latestScheduleThread != null)
				{
					DaemonService.m.latestScheduleThread.selfKill = true;
				}
				DaemonService.m.latestScheduleThread = new LatestScheduleAsyncThread(DaemonService.m);
				CalendarEvent calendarEvent = null;
				String passingString = null;
				if(( calendarEvent = DaemonService.m.previousCalendarEvent) != null)
				{
					 passingString = ""+calendarEvent.getEventTime();	
				}
				if(passingString == null)	
					DaemonService.m.latestScheduleThread.execute("null");
				else
					DaemonService.m.latestScheduleThread.execute(passingString);
			}
		}
		return retVal;
	}
	
}
