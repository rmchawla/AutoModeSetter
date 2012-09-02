package org.sunysb.cse549.listeners;

import org.sunysb.cse549.daemon.DaemonService;
import org.sunysb.cse549.daemon.LatestScheduleAsyncThread;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class NetworkLocationListener implements LocationListener
{
	DaemonService m;
	LocationManager locManager;
	
	public NetworkLocationListener(DaemonService m, LocationManager locManager) 
	{
		this.m = m;
		this.locManager = locManager;
		
		
	}
	
	public void onLocationChanged(Location location) 
	{
		m.networkLocation = location;
		this.locManager.removeUpdates(this);
		LatestScheduleAsyncThread.networkFlag = false;
	}

	
	public void onProviderDisabled(String provider) 
	{
		
	}

	
	public void onProviderEnabled(String provider) 
	{
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) 
	{
		
	}

}
