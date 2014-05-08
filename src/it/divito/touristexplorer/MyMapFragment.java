package it.divito.touristexplorer;

import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.MarkerOptions;
import pl.mg6.android.maps.extensions.SupportMapFragment;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

@SuppressLint("NewApi")
public class MyMapFragment extends android.support.v4.app.Fragment {
	/**
	 * Note that this may be null if the Google Play services APK is not
	 * available.
	 */
	private static final String LOG_TAG = MyMapFragment.class.getName();
	
	private GoogleMap mMap;
	View rootView ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if (rootView != null) {
	        ViewGroup parent = (ViewGroup) rootView.getParent();
	        if (parent != null) {
	            parent.removeView(rootView);
	        }
	    }

	    try {
			rootView = inflater.inflate(R.layout.map_fragment_layout,
					container, false);
			setUpMapIfNeeded();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			Log.d(LOG_TAG, "setUpMapIfNeeded:map null");
			// Try to obtain the map from the SupportMapFragment.
			FragmentManager myFragmentManager = getFragmentManager();
			SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager
					.findFragmentById(R.id.map);
			mMap = mySupportMapFragment.getExtendedMap();
			setGoogleMap(mMap);
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	private void setGoogleMap(GoogleMap mMap){
		this.mMap = mMap;
	}
	
	public GoogleMap getGoogleMap(){
		return mMap;
	}
	
	private void setUpMap() {
		Log.d(LOG_TAG, "setUpMap");
		mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title(
				"Marker"));
		mMap.setMyLocationEnabled(true);
	}
	
	/*
	@Override
	public void onDestroyView() 
	 {
	    super.onDestroyView(); 
	    Fragment fragment = (getFragmentManager().findFragmentById(R.id.map));  
	    getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
	}*/
	@Override
	public void onDestroyView() {

	    SupportMapFragment f = (SupportMapFragment) getFragmentManager()
	            .findFragmentById(R.id.map);

	    if (f != null) {
	        try {
	            getFragmentManager().beginTransaction().remove(f).commit();

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	    }

	    super.onDestroyView();
	}
}