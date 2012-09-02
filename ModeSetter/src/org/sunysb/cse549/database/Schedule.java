package org.sunysb.cse549.database;

public class Schedule 
{
	private double longitude;
	private double latitude;
	private long scheduleId;
	private String startDatetime;
	private String endDatetime;
	private String locationName;
	private long startTimeDiff;
	private long endTimeDiff;
	private Profile profile;
	private int profileId;

	
	public int getProfileId() {
		return profileId;
	}

	public void setProfileId(int profileId) {
		this.profileId = profileId;
	}

	public String getStartDatetime() {
		return startDatetime;
	}

	public void setStartDatetime(String startDatetime) {
		this.startDatetime = startDatetime;
	}

	public String getEndDatetime() {
		return endDatetime;
	}

	public void setEndDatetime(String endDatetime) {
		this.endDatetime = endDatetime;
	}

	public long getStartTimeDiff() {
		return startTimeDiff;
	}

	public void setStartTimeDiff(long startTimeDiff) {
		this.startTimeDiff = startTimeDiff;
	}

	public long getEndTimeDiff() {
		return endTimeDiff;
	}

	public void setEndTimeDiff(long endTimeDiff) {
		this.endTimeDiff = endTimeDiff;
	}

	public void setDaily(boolean isDaily) {
		this.isDaily = isDaily;
	}

		
	public long getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(long scheduleId) {
		this.scheduleId = scheduleId;
	}

	
	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public boolean isDaily() {
		return isDaily;
	}

	public void setDaily(int isDaily) {
	
		if(isDaily == 1)
			this.isDaily = true;
		else
			this.isDaily = false;
	}

	private boolean isDaily;

	
	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public double getLongitude() 
	{
		return longitude;
	}
	
	public void setLongitude(double longitude) 
	{
		this.longitude = longitude;
	}
	
	public double getLatitude() 
	{
		return latitude;
	}
	
	public void setLatitude(double latitude) 
	{
		this.latitude = latitude;
	}
	
	
	
	
}
