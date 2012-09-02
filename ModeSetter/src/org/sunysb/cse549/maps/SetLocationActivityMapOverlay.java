package org.sunysb.cse549.maps;

import java.io.IOException;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.sax.StartElementListener;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class SetLocationActivityMapOverlay extends Overlay {
	public Context context;
	public SetLocationActivity activity;
	private long hack = -1;
	
	public SetLocationActivityMapOverlay(Context c, SetLocationActivity a)
	{
		context = c;
		activity = a;
	}
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		// TODO Auto-generated method stub
		return super.draw(canvas, mapView, shadow, when);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		// TODO Auto-generated method stub
		//return super.onTouchEvent(e, mapView);
		if(e.getAction() == 1)
		{	
			long thisTime = System.currentTimeMillis();

			if((thisTime - hack) < 250)
			{	
				GeoPoint pt = mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY());
				hack = -1;
				activity.enterLocationDetail(pt);
			}
			else
				hack = thisTime;
			
		}
		return false;
	}
}
