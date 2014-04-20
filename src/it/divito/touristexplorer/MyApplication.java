package it.divito.touristexplorer;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.gms.maps.model.LatLng;

import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.Marker;

import it.divito.touristexplorer.database.DatabaseAdapter;
import android.app.Application;
import android.database.sqlite.SQLiteDatabase;


/**
 * This class maintains global application state. 
 * 
 * @author Stefano
 *
 */

public class MyApplication extends Application {
	
	public DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
	public SQLiteDatabase database;
	public GoogleMap mainMap, pathMap;
	public Long currentTrack;
	public int mapContainerHeight;
	public String trackStartTime;
	public String trackEndTime;
	public double trackAvgSpeed;
	
	public HashMap<String, Marker> markerMap = new HashMap<String, Marker>();
	public HashMap<String, CustomMarkerOptions> customMarkerOptionsMap = new HashMap<String, CustomMarkerOptions>();
	
	public DatabaseAdapter getDbAdapter() {
		return dbAdapter;
	}

	public void setDbAdapter(DatabaseAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}

	public GoogleMap getMainMap() {
		return mainMap;
	}

	public void setMainMap(GoogleMap mainMap) {
		this.mainMap = mainMap;
	}

	public GoogleMap getPathMap() {
		return pathMap;
	}

	public void setPathMap(GoogleMap map) {
		this.pathMap = map;
	}

	public Long getCurrentTrack() {
		return currentTrack;
	}

	public void setCurrentTrack(Long currentTrack) {
		this.currentTrack = currentTrack;
	}

	public String getStartTime() {
		return trackStartTime;
	}

	public void setStartTime(String trackStartTime) {
		this.trackStartTime = trackStartTime;
	}

	public String getEndTime() {
		return trackEndTime;
	}

	public void setEndTime(String trackEndTime) {
		this.trackEndTime = trackEndTime;
	}

	public double getAvgSpeed() {
		return trackAvgSpeed;
	}

	public void setAvgSpeed(double trackAvgSpeed) {
		this.trackAvgSpeed = trackAvgSpeed;
	}

	public SQLiteDatabase getDatabase() {
		return database;
	}

	public void setDatabase(SQLiteDatabase database) {
		this.database = database;
	}

	public int getMapContainerHeight() {
		return mapContainerHeight;
	}

	public void setMapContainerHeight(int mapContainerHeight) {
		this.mapContainerHeight = mapContainerHeight;
	}

	
	public void addMarker(String id, Marker arg0) {
		markerMap.put(id, arg0);
	}
	
	public Marker getMarker(String id){
		return markerMap.get(id);
	}
	
	public HashMap<String, Marker> getMarkerMap(){
		return markerMap;
	}
	
	
	/**
	 * Determines the markers present in a same position
	 * 
	 * @param position a posizion on the map
	 * @return list of the markers present in @link position
	 */
	public ArrayList<Marker> getMarkerInSamePositionList(LatLng position){
		ArrayList<Marker> list = new ArrayList<Marker>();
		for (Marker marker : markerMap.values()) {
			if(marker.getPosition().equals(position))
				list.add(marker);
		}
		return list;
	}

	public void addMarker(String id, CustomMarkerOptions customMarkerOptions) {
		customMarkerOptionsMap.put(id, customMarkerOptions);
	}
		
	public CustomMarkerOptions getCustomMarkerOptions(String id) {
		return customMarkerOptionsMap.get(id);
	}
}