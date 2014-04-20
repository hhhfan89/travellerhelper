package it.divito.touristexplorer.database;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.TouristExplorer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
 
/**
 * Photo table
 * @author Stefano Di Vito
 *
 */
public class PhotoTable extends MediaTable {
	
	private MyApplication myApp;
	private SQLiteDatabase database;
	
	
	public PhotoTable(Context context) {
		
		myApp = (MyApplication) context.getApplicationContext();
		database = myApp.getDatabase();
		
	}
 
	
	@Override
	public Cursor selectFromLocation(int location) {
		
		Log.d("media table, select photo", "location" + location);
		
		String selection = "location = ?";
		String[] selectionArgs = { Integer.toString(location) };
		return database.query(TouristExplorer.TABLE_PHOTO, null, selection, selectionArgs,
				null, null, null);
		
	}
	
	
	@Override
	public Cursor selectFromId(int id) {
		
		String selection = "_id = ?";
		String[] selectionArgs = { Integer.toString(id) };
		return database.query(TouristExplorer.TABLE_PHOTO, null, selection, selectionArgs,
				null, null, null);
		
	}
	
	
	@Override
	public Cursor selectFromPath(String path) {
		
		String selection = "path = ?";
		String[] selectionArgs = { path };
		return database.query(TouristExplorer.TABLE_PHOTO, null, selection, selectionArgs,
				null, null, null);	
		
	}
	
	
	@Override
	public Cursor selectAll() {
		return database.query(TouristExplorer.TABLE_PHOTO, null, null, null, null, null, null);
	}


	@Override
	public boolean deleteFromPath(String path) {
		
		database.execSQL("PRAGMA foreign_keys=ON;");
		String whereClause = "path = ?";
		String[] whereArgs = { path };
		return database.delete(TouristExplorer.TABLE_PHOTO, whereClause, whereArgs) > 0;
	
	}
	
	
	@Override
	public boolean update(String oldPath, String newPath, String title, String description) {
		ContentValues updateValues = createUpdateValues(title, description, newPath);
		String whereClause = "path = ?";
		String[] whereArgs = { oldPath };
		return database.update(TouristExplorer.TABLE_PHOTO, updateValues, whereClause, whereArgs) > 0;
	
	}


	@Override
	public long insert(String title, String description, String path, String datetime, int location) {
		
		ContentValues initialValues = createContentValues(title, description, path, datetime, location);
		return database.insertOrThrow(TouristExplorer.TABLE_PHOTO, null, initialValues);
	
	}
	
}