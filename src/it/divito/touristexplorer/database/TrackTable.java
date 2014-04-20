package it.divito.touristexplorer.database;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.TouristExplorer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
 
/**
 * Track table
 * @author Stefano Di Vito
 *
 */
public class TrackTable {
	
	private MyApplication myApp;
	private SQLiteDatabase database;
	
	public TrackTable(Context context) {
		
		myApp = (MyApplication) context.getApplicationContext();
		database = myApp.getDatabase();
		
	}
 
	
	public ContentValues createTrackContentValues(String name, String startDate, String startTime) {
		
		ContentValues values = new ContentValues();
		values.put(TouristExplorer.COLUMN_NAME, name);
		values.put(TouristExplorer.COLUMN_START_DATE, startDate);
		values.put(TouristExplorer.COLUMN_START_TIME, startTime);
		
		return values;
	}
	
	
	public ContentValues createTrackContentValues(String name) {
		
		ContentValues values = new ContentValues();
		values.put(TouristExplorer.COLUMN_NAME, name);
		
		return values;
	}
	
	
	public ContentValues createStartAddressTrackContentValues(String startAddress) {
		
		ContentValues values = new ContentValues();
		values.put(TouristExplorer.COLUMN_START_ADDRESS, startAddress);
		
		return values;
	}
	
	
	public ContentValues createFinishAddressTrackContentValues(String finishAddress) {
		
		ContentValues values = new ContentValues();
		values.put(TouristExplorer.COLUMN_FINISH_ADDRESS, finishAddress);
		
		return values;
	}
	
	
	public ContentValues createTrackContentValues(String finishDate, String finishTime, double avgSpeed, double totalDistance) {
		
		ContentValues values = new ContentValues();
		values.put(TouristExplorer.COLUMN_FINISH_DATE, finishDate);
		values.put(TouristExplorer.COLUMN_FINISH_TIME, finishTime);
		values.put(TouristExplorer.COLUMN_AVG_SPEED, avgSpeed);
		values.put(TouristExplorer.COLUMN_TOTAL_DISTANCE, totalDistance);
		
		return values;
		
	}
	
	
	public long insertTrack(String name, String startDate, String startTime) {
		
		ContentValues initialValues = createTrackContentValues(name, startDate, startTime);
		return database.insertOrThrow(TouristExplorer.TABLE_TRACK, null,
				initialValues);
		
	}
	
	public Cursor selectTrack(String track) {
		
		String selection = "name = ?";
		String[] selectionArgs = { track };
		return database.query(TouristExplorer.TABLE_TRACK, null, selection, selectionArgs,
				null, null, null);
		
	}
	
	
	public Cursor selectTrack(int id) {
		
		String selection = "_id = ?";
		String[] selectionArgs = { Integer.toString(id) };
		return database.query(TouristExplorer.TABLE_TRACK, null, selection, selectionArgs,
				null, null, null);
		
	}
	
	
	public Cursor selectAllTracks() {
		
		return database.query(TouristExplorer.TABLE_TRACK, null, null, null, null, null, null);
		
	}
	
	
	public boolean deleteTrackWithoutPoi(int track) {
		String whereClause = "_id = ?";
		String[] whereArgs = { Integer.toString(track) };
		return database.delete(TouristExplorer.TABLE_TRACK, whereClause, whereArgs) > 0;
	}
	
	
	public boolean deleteTrack(String name) {
		database.execSQL("PRAGMA foreign_keys=ON;");
		String whereClause = "name = ?";
		String[] whereArgs = { name };
		return database.delete(TouristExplorer.TABLE_TRACK, whereClause, whereArgs) > 0;
	}
	
	
	public boolean deleteTrack(int track) {
		database.execSQL("PRAGMA foreign_keys=ON;");
		String whereClause = "_id = ?";
		String[] whereArgs = { ""+track };
		return database.delete(TouristExplorer.TABLE_TRACK, whereClause, whereArgs) > 0;
	}
	
	
	public boolean updateTrack(int track, String name) {
	
		ContentValues updateValues = createTrackContentValues(name);
		String whereClause = "_id = ?";
		String[] whereArgs = { ""+track };
		return database.update(TouristExplorer.TABLE_TRACK, updateValues, whereClause, whereArgs) > 0;
	
	}
	
	
	public boolean updateTrack(int track, String finishDate, String finishTime, double avgSpeed, double totalDistance) {
		
		ContentValues updateValues = createTrackContentValues(finishDate, finishTime, avgSpeed, totalDistance);
		String whereClause = "_id = ?";
		String[] whereArgs = { ""+track };
		return database.update(TouristExplorer.TABLE_TRACK, updateValues, whereClause, whereArgs) > 0;
	
	}

	
	public boolean updateStartAddressTrack(int track, String startAddress) {
		
		ContentValues updateValues = createStartAddressTrackContentValues(startAddress);
		String whereClause = "_id = ?";
		String[] whereArgs = { ""+track };
		return database.update(TouristExplorer.TABLE_TRACK, updateValues, whereClause, whereArgs) > 0;
	
	}
	
	
	public boolean updateFinishAddressTrack(int track, String finishAddress) {
		
		ContentValues updateValues = createFinishAddressTrackContentValues(finishAddress);
		String whereClause = "_id = ?";
		String[] whereArgs = { ""+track };
		return database.update(TouristExplorer.TABLE_TRACK, updateValues, whereClause, whereArgs) > 0;
	
	}
	
}