package it.divito.touristexplorer;

import pl.mg6.android.maps.extensions.GoogleMap;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("NewApi")
public class MapActivity extends Fragment {

	private GoogleMap mMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	   }
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
	    //setUpMapIfNeeded();
	     View rootView = inflater.inflate(R.layout.activity_comment, container, false);
	     return rootView;
	}
	
	/*public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
	    //setUpMapIfNeeded();
	     View rootView = inflater.inflate(R.layout.basic_demo, container, false);
	     setUpMapIfNeeded();
	     return rootView;
	}
/*
	@Override
	public void onResume() {
	    super.onResume();
	    setUpMapIfNeeded();
	}

	private void setUpMapIfNeeded() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (mMap == null) {
	        // Try to obtain the map from the SupportMapFragment.
	        mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	        // Check if we were successful in obtaining the map.
	        if (mMap != null) {
	            setUpMap();
	        }
	    }
	}

	private void setUpMap() {
	    mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
	}*/
}
