package org.sunysb.cse549.maps;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sunysb.cse549.R;
import org.sunysb.cse549.constants.Constants;
import org.sunysb.cse549.daemon.DaemonService;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.Overlay;


public class SetLocationActivity extends  MapActivity {
	MapView mapView;
	private boolean serviceStarted = true;
	Context context;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapmain);
        
        mapView = (MapView) findViewById(R.id.mapView);
        //mapView.setSatellite(true);
        mapView.setStreetView(true);
        
        context = getBaseContext();
        
        Button btnSearch=(Button) findViewById(R.id.SearchMapButton);
        btnSearch.setOnClickListener(locSearchListener);
        
        EditText searchText = (EditText)findViewById(R.id.MapSearchEditText);
        searchText.setOnFocusChangeListener(textListener);
        
        SetLocationActivityMapOverlay overlay = new SetLocationActivityMapOverlay(context, this);
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(overlay);
        
        mapView.invalidate();
        
        LinearLayout zoomLayout = (LinearLayout)findViewById(R.id.zoom);  
        View zoomView = mapView.getZoomControls(); 
 
        zoomLayout.addView(zoomView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); 
        
        mapView.displayZoomControls(true);
        
//        MapController mc = mapView.getController();
//        String coordinates[] = {"40.9255556", "-73.1413889"};
//        double lat = Double.parseDouble(coordinates[0]);
//        double lng = Double.parseDouble(coordinates[1]);
//                
//        GeoPoint p = new GeoPoint((int)(lat*1E6), (int)(lng*1E6));
//        mc.animateTo(p);
//        mc.zoomToSpan((int)(lat*1E6), (int)(lng*1E6));
        mapView.invalidate();
        if(serviceStarted)
        {	
        	Intent myIntent = new Intent(getApplicationContext(), DaemonService.class);
        	startService(myIntent);
        	serviceStarted = false;
        }
    }   
    
    // focus change click listener
    private OnFocusChangeListener textListener = new OnFocusChangeListener()
    {
    	public void onFocusChange(View v, boolean hasFocus)
        {       
    		if(hasFocus)
    		{
	        	EditText searchText = (EditText)findViewById(R.id.MapSearchEditText);
	    		if(searchText.getText().toString().equals(Constants.DefSearchLocation))
	    			searchText.setText("");	    	
    		}
        }
    };
    
    // Location Search Listener.
    private OnClickListener locSearchListener = new OnClickListener()
    {
    	public void onClick(View v) {         
            
	        EditText txtSearch=(EditText)findViewById(R.id.MapSearchEditText);
	        String searchLocation = txtSearch.getText().toString();
	        
	        //Toast.makeText(getBaseContext(), searchLocation, Toast.LENGTH_LONG).show();
	        JSONObject jo = getLocationInfo(searchLocation);
	        GeoPoint gp = getLatLong(jo);
		   
	        mapView = (MapView)findViewById(R.id.mapView);
		    MapController mc=mapView.getController();
		    mc.animateTo(gp);
		    mc.setZoom(10);
		    mapView.invalidate();
	        //Toast.makeText(GoogleMap.this, "Click-" + String.valueOf(area), Toast.LENGTH_SHORT).show();
	        //changeMap(area);
        }
    };
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    public void changeMap(String area)
    {     
	    GeoPoint myLocation=null;
	     
	    double lat = 0;
	    double lng = 0;
	    int res_sz = 0;
	    Geocoder g;
	    try
	    {
		     
		    g = new Geocoder(this, Locale.getDefault());
		     
		    java.util.List<android.location.Address> result=g.getFromLocationName(area, 1);
		    Thread.sleep(2000);
		    if(result.size()>0)
		    {
		    	res_sz = 1;
			    Toast.makeText(context, "country: " + String.valueOf(result.get(0).getCountryName()), Toast.LENGTH_SHORT).show();
			    lat = result.get(0).getLatitude();
			    lng = result.get(0).getLongitude();
		    }	            
		    else
		    {
			    Toast.makeText(context, "record not found", Toast.LENGTH_SHORT).show();
			    return;
		    }
	    }
	    catch(Exception io)
	    {
	    	Toast.makeText(context, "Connection Error " + io.toString()  , Toast.LENGTH_SHORT).show();
	    }
	    if( res_sz > 0 )
	    {
		    myLocation = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
		    mapView = (MapView) findViewById(R.id.mapView);
		    MapController mc=mapView.getController();
		    mc.animateTo(myLocation);
		    mc.setZoom(10);
		    mapView.invalidate();
	    }
    }
    
    
    
    public void enterLocationDetail(GeoPoint p)
    {
    	//Toast.makeText(getBaseContext(), ""+ p.getLatitudeE6() / 1E6 + ',' + p.getLongitudeE6() /1E6 , Toast.LENGTH_SHORT).show();
    	Intent intent = new Intent(this, LocationDetailEntryActivity.class);
		intent.putExtra("Latitude", p.getLatitudeE6());
		intent.putExtra("Longitude", p.getLongitudeE6());
		startActivity(intent);
    }
    
    public static JSONObject getLocationInfo(String address) {
        StringBuilder stringBuilder = new StringBuilder();
        try {

        address = address.replaceAll(" ","%20");    

        HttpPost httppost = new HttpPost("http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        stringBuilder = new StringBuilder();


            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jsonObject;
    }
    
    public static GeoPoint  getLatLong(JSONObject jsonObject) {

        Double lon = new Double(0);
        Double lat = new Double(0);

        try {

            lon = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                .getJSONObject("geometry").getJSONObject("location")
                .getDouble("lng");

            lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                .getJSONObject("geometry").getJSONObject("location")
                .getDouble("lat");

        } catch (Exception e) {
            e.printStackTrace();

        }

        return new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));
    }

    
}