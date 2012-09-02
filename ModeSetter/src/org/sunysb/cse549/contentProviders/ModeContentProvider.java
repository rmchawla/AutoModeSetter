package org.sunysb.cse549.contentProviders;

import org.sunysb.cse549.constants.Constants;
import org.sunysb.cse549.database.DBAdapter;
import org.sunysb.cse549.database.Schedule;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class ModeContentProvider extends ContentProvider {

	private DBAdapter dba;
	
	private static final int SCHEDULE = 10;
	private static final int SCHEDULE_ID = 20;
	private static final int SCHEDULES = 70;
	private static final int LOCATIONS = 30;
	private static final int LOCATION_ID = 40;
	private static final int PROFILES = 50;
	private static final int PROFILE_ID = 60;
	private static final int JOIN = 80;

	private static final String AUTHORITY = "org.sunysb.cse549.contentProviders.ModeContentProvider";

	private static final String BASE_PATH_SCHEDULE = "schedule";
	private static final String BASE_PATH_SCHEDULES = "schedules";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH_SCHEDULE);
	private static final String BASE_PATH_PROFILE = "profile";
	private static final String BASE_PATH_LOCATION = "location";
	private static final String BASE_PATH_JOIN = "join";

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/schedules";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/schedule";
	
	//public static final String query = "select t1.location_name, latitude , longitude, min(t1.timediff),t1.datetime,is_vibratory,volume from (select location_name , strftime(\'%s\',datetime) + 3600*4 - strftime(\'%s\',\'now\')  as timediff, datetime , profile_id from SCHEDULE where timediff>0) as t1, location l, profile p where t1.location_name = l.location_name and p.profile_id = t1.profile_id";
	public final String query = "select schedId,strftime(\'%s\',startTime) + " + Constants.localeDifference+ " - strftime(\'%s\',\'now\')  as startTimeDiff, strftime(\'%s\',endTime) + "+ Constants.localeDifference+" - strftime(\'%s\',\'now\') as endTimeDiff from SCHEDULE where startTimeDiff > 0";
	//select min(t1.timediff),t1.sched_id from (  ) as t1 group by t1.sched_id


	private static final UriMatcher uriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	
	static {
		uriMatcher.addURI(AUTHORITY, BASE_PATH_SCHEDULE, SCHEDULE);
		uriMatcher.addURI(AUTHORITY, BASE_PATH_SCHEDULES, SCHEDULES);
		uriMatcher.addURI(AUTHORITY, BASE_PATH_SCHEDULE + "/#", SCHEDULE_ID);
		uriMatcher.addURI(AUTHORITY, BASE_PATH_PROFILE,PROFILES);
		uriMatcher.addURI(AUTHORITY, BASE_PATH_PROFILE + "/#", PROFILE_ID);
		uriMatcher.addURI(AUTHORITY, BASE_PATH_LOCATION,LOCATIONS);
		uriMatcher.addURI(AUTHORITY, BASE_PATH_LOCATION + "/#", LOCATION_ID);
		uriMatcher.addURI(AUTHORITY, BASE_PATH_JOIN,JOIN);
	}
	
	
	
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		dba = DBAdapter.getInstance(getContext());
		return true;
	}

	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		
		//System.out.println(selection);
		//System.out.println(projection[0]);
		// Uisng SQLiteQueryBuilder instead of query() method
				SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
				
				
				SQLiteDatabase db = dba.open();
				
				Cursor cursor = null;
				
				int uriType = uriMatcher.match(uri);
				switch (uriType) {
				case SCHEDULES:
					queryBuilder.setTables(DBAdapter.SCHEDULE_TABLE);
					cursor = queryBuilder.query(db,projection,selection,null,null,null,null);
					break;
				case SCHEDULE:	
					//queryBuilder.setTables(DBAdapter.SCHEDULE_TABLE);
					String query = "select schedId,strftime(\'%s\',startTime) + " + Constants.localeDifference+ " - strftime(\'%s\',\'now\')  as startTimeDiff, strftime(\'%s\',endTime) + "+ Constants.localeDifference+" - strftime(\'%s\',\'now\') as endTimeDiff from SCHEDULE where startTimeDiff > 0";
					cursor = db.rawQuery(query, null);
				//	queryBuilder.s
					break;
				case SCHEDULE_ID:
					// Adding the ID to the original query
					break;
				case LOCATIONS:
					queryBuilder.setTables(DBAdapter.LOCATION_TABLE);
					String locationQuery = "select * from location where locationName = \'"+selection+"\'";
					//cursor = queryBuilder.query(db,projection,selection,null,null,null,null);
					cursor = db.rawQuery(locationQuery, null);
					//findSuitableLocation(cursor);
					break;
				case LOCATION_ID:
					break;
				case PROFILES:
					//queryBuilder.setTables(DBAdapter.PROFILE_TABLE);
					//cursor = queryBuilder.query(db,projection,selection,null,null,null,null);
					String profileQuery = "select * from PROFILE where profileId = \'"+selection+"\'";
					cursor = db.rawQuery(profileQuery, null);
					break;
				case PROFILE_ID:
					break;
				case JOIN:
					int id = Integer.parseInt(selection);
					String joinQuery = "select l.locationName, latitude , longitude, startTime, endTime, profileName, isVibratory, volume, mode from Profile p , Location l , Schedule s where s.schedId = \'"+id+"\' and p.profileId = s.profileId and l.locationName = s.locationName";
					cursor = db.rawQuery(joinQuery, null);
					break;
				default:
					throw new IllegalArgumentException("Unknown URI: " + uri);
				}

				
				//Cursor cursor = queryBuilder.query(db, projection, selection,
					//	selectionArgs, null, null, sortOrder);
				// Make sure that potential listeners are getting notified
			/*
				cursor.moveToFirst();
				
				while (!cursor.isAfterLast()) {
					Schedule s = cursorToSchedule(cursor);
					cursor.moveToNext();
				}
				
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
				//can't close it here
				//dba.close();
				*/
				return cursor;
		
	}

	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	/*
	private Schedule cursorToSchedule(Cursor cursor) {
		Schedule s = new Schedule();
		
		s.setLatitude(cursor.getDouble(1));
		s.setLongitude(cursor.getDouble(2));
		s.setLocationName(cursor.getString(0));
		s.setTimeDiff(cursor.getLong(3));
		s.setDatetime(cursor.getString(4));
		if(cursor.getInt(5) == 1)
			s.setIsvibrating(true);
		s.setVolume(cursor.getInt(6));
		
		s.setDatetime(cursor.getString(1));
		s.setTimeDiff(cursor.getLong(0));
		return s;
	
	}

*/

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
		int loc = location.getLocationName().length();
		location.setLatitude(locationCursor.getDouble(1));
		location.setLongitude(locationCursor.getDouble(2));
		return location;
	}


	
	
}
