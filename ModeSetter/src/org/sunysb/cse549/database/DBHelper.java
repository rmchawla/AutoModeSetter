package org.sunysb.cse549.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	
	private static final String DATABASE_NAME = "MODE_SETTER_DB";	
	private static final int DATABASE_VERSION = 25;
	
	private static DBHelper dbHelper = null;
	
	private DBHelper(Context context) 
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public static DBHelper getInstance(Context context)
	{
		if(dbHelper == null)
		{
			dbHelper = new DBHelper(context);
		}
		return dbHelper;
	}
	

	
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		// TODO Auto-generated method stub
		System.out.println("Inside create");
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		//System.out.println("hello");
		String query = "DROP TABLE if exists " + DBAdapter.PROFILE_TABLE;
		String query2 = "DROP TABLE if exists " + DBAdapter.SCHEDULE_TABLE;
		String query3 = "DROP TABLE if exists " + DBAdapter.LOCATION_TABLE;
		//this.getWritableDatabase().execSQL(query);
		//this.getWritableDatabase().execSQL(query2);
		//this.getWritableDatabase().execSQL(query3);
		db.execSQL(query);
		db.execSQL(query2);
		db.execSQL(query3);
	}
}
