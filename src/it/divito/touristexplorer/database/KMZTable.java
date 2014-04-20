package it.divito.touristexplorer.database;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.TouristExplorer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
 
/**
 * KML table
 * @author Stefano Di Vito
 *
 */
public class KMZTable {
	
	private MyApplication myApp;
	private SQLiteDatabase database;
	
	public KMZTable(Context context) {
		
		myApp = (MyApplication) context.getApplicationContext();
		database = myApp.getDatabase();
		
	}
 
	
	private ContentValues createFileContentValues(String path, int track) {
		
		ContentValues values = new ContentValues();
		values.put(TouristExplorer.COLUMN_PATH, path);
		values.put(TouristExplorer.COLUMN_TRACK, track);
		
		return values;
	}
	
	
	public long insertKMLFile(String path, int track){
		
		ContentValues initialValues = createFileContentValues(path, track);
		return database.insertOrThrow(TouristExplorer.TABLE_KML_FILE, null,
				initialValues);
	}
	
	
	public Cursor selectKMLFile(int track) {
		
		String selection = "track = ?";
		String[] selectionArgs = { ""+track };
		return database.query(TouristExplorer.TABLE_KML_FILE, null, selection, selectionArgs,
				null, null, null);
		
	}


}