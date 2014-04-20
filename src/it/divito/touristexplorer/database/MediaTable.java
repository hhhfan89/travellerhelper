package it.divito.touristexplorer.database;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.TouristExplorer;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
 
/**
 * KML table
 * @author Stefano Di Vito
 *
 */
abstract class MediaTable {
	
	public MyApplication myApp = null;
	public SQLiteDatabase database = null;
		
	
	// tutti i poi
	public ContentValues createContentValues(String title, String description, String path, String datetime, int location) {
		
		ContentValues values = new ContentValues();
		values.put(TouristExplorer.COLUMN_TITLE, title);
		values.put(TouristExplorer.COLUMN_DESCRIPTION, description);
		values.put(TouristExplorer.COLUMN_PATH, path);
		values.put(TouristExplorer.COLUMN_DATETIME, datetime);
		values.put(TouristExplorer.COLUMN_LOCATION, location);
		
		return values;
	}

	
	//media / comment
	public ContentValues createUpdateValues(String title, String description, String path) {
		
		ContentValues values = new ContentValues();
		values.put(TouristExplorer.COLUMN_TITLE, title);
		values.put(TouristExplorer.COLUMN_DESCRIPTION, description);
		values.put(TouristExplorer.COLUMN_PATH, path);
		
		return values;
		
	}
	
	public abstract long insert(String title, String description, String path, String datetime, int location);
	
	public abstract Cursor selectFromLocation(int location);
	public abstract Cursor selectFromId(int id);
	public abstract Cursor selectFromPath(String path);
	public abstract Cursor selectAll();
	
	public abstract boolean update(String oldPath, String newPath, String title, String description);
		
	public abstract boolean deleteFromPath(String path);
	
}