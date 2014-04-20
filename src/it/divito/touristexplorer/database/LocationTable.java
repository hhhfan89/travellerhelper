package it.divito.touristexplorer.database;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.TouristExplorer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
 
/**
 * Location table
 * @author Stefano Di Vito
 *
 */
public class LocationTable {
	
	private MyApplication myApp;
	private SQLiteDatabase database;
	
	public LocationTable(Context context) {
		
		myApp = (MyApplication) context.getApplicationContext();
		database = myApp.getDatabase();
		
	}
 
	
	public ContentValues createLocationContentValues(String latitude,
			String longitude, double currentSpeed, int color, int track) {
		
		ContentValues values = new ContentValues();
		values.put(TouristExplorer.COLUMN_LATITUDE, latitude);
		values.put(TouristExplorer.COLUMN_LONGITUDE, longitude);
		values.put(TouristExplorer.COLUMN_SPEED, currentSpeed);
		values.put(TouristExplorer.COLUMN_COLOR, color);
		values.put(TouristExplorer.COLUMN_TRACK, track);

		return values;
	
	}
	
	
	public long insertLocation(double latitude, double longitude, double currentSpeed, int color, int track) {
		
		ContentValues initialValues = createLocationContentValues(Double.toString(latitude),
				Double.toString(longitude), currentSpeed, color, track);
		return database.insertOrThrow(TouristExplorer.TABLE_LOCATION, null,
				initialValues);
	}
	
	
	public Cursor selectLocation(double longitude, double latitude) {
		
		String selection = "longitude = ? AND latitude = ?";
		String[] selectionArgs = { Double.toString(longitude), Double.toString(latitude) };
		return database.query(TouristExplorer.TABLE_LOCATION, null, selection, selectionArgs,
				null, null, null);
		
	}
	
	
	public Cursor selectLocationFromTrack(int track) {
		
		String selection = "track = ?";
		String[] selectionArgs = { Integer.toString(track) };
		return database.query(TouristExplorer.TABLE_LOCATION, null, selection, selectionArgs,
				null, null, null);
		
	}
	
	
	public Cursor selectFirstLocation(int track) {
		
		String selection = "track = ?";
		String[] selectionArgs = { Integer.toString(track) };
		String orderBy = "_id ASC";
		return database.query(TouristExplorer.TABLE_LOCATION, null, selection, selectionArgs,
				null, null, orderBy);
		
	}
	
	
	public Cursor selectLastLocation(int track) {
		
		String selection = "track = ?";
		String[] selectionArgs = { Integer.toString(track) };
		String orderBy = "_id DESC";
		return database.query(TouristExplorer.TABLE_LOCATION, null, selection, selectionArgs,
				null, null, orderBy);
		
	}


	public Cursor selectLastLocation(double longitude, double latitude) {
		
		String selection = "longitude = ? AND latitude = ?";
		String[] selectionArgs = { Double.toString(longitude), Double.toString(latitude) };
		String orderBy = "_id DESC";
		return database.query(TouristExplorer.TABLE_LOCATION, null, selection, selectionArgs,
				null, null, orderBy);
		
	}


	public Cursor selectLocationFromId(int locationID) {
		
		String selection = "_id = ?";
		String[] selectionArgs = { Integer.toString(locationID) };
		return database.query(TouristExplorer.TABLE_LOCATION, null, selection, selectionArgs,
				null, null, null);
	}

	
	public Cursor selectAllLocations() {
		
		return database.query(TouristExplorer.TABLE_LOCATION, null, null, null,
				null, null, null);
	}

	
	public boolean deleteLocation(int locationID) {
		database.execSQL("PRAGMA foreign_keys=ON;");
		String whereClause = "_id = ?";
		String[] whereArgs = { Integer.toString(locationID) };
		return database.delete(TouristExplorer.TABLE_LOCATION, whereClause, whereArgs) > 0;
	}

}