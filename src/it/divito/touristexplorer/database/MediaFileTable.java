package it.divito.touristexplorer.database;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.TouristExplorer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
 
/**
 * Media file (audio, video, photo)  table
 * @author Stefano Di Vito
 *
 */
public class MediaFileTable {
	
	private MyApplication myApp;
	private SQLiteDatabase database;
	
	public MediaFileTable(Context context) {
		
		myApp = (MyApplication) context.getApplicationContext();
		database = myApp.getDatabase();
		
	}
 
	
	private ContentValues createFileContentValues(String path, int track) {
		
		ContentValues values = new ContentValues();
		values.put(TouristExplorer.COLUMN_PATH, path);
		values.put(TouristExplorer.COLUMN_TRACK, track);
		
		return values;
	}

	
	public long insertPhotoFile(String path, int track){
		ContentValues initialValues = createFileContentValues(path, track);
		return database.insertOrThrow(TouristExplorer.TABLE_PHOTO_FILE, null,
				initialValues);
	}
	
	
	public long insertVideoFile(String path, int track){
		
		ContentValues initialValues = createFileContentValues(path, track);
		return database.insertOrThrow(TouristExplorer.TABLE_VIDEO_FILE, null,
				initialValues);
	}
	
	
	public long insertAudioFile(String path, int track){
		
		ContentValues initialValues = createFileContentValues(path, track);
		return database.insertOrThrow(TouristExplorer.TABLE_AUDIO_FILE, null,
				initialValues);
	}	
	
	
	public Cursor selectPhotoFile(int track) {
		
		String selection = "track = ?";
		String[] selectionArgs = { ""+track };
		return database.query(TouristExplorer.TABLE_PHOTO_FILE, null, selection, selectionArgs,
				null, null, null);
		
	}
	
	
	public Cursor selectVideoFile(int track) {
		
		String selection = "track = ?";
		String[] selectionArgs = { ""+track };
		return database.query(TouristExplorer.TABLE_VIDEO_FILE, null, selection, selectionArgs,
				null, null, null);
		
	}
	
	
	public Cursor selectAudioFile(int track) {
		
		String selection = "track = ?";
		String[] selectionArgs = { ""+track };
		return database.query(TouristExplorer.TABLE_AUDIO_FILE, null, selection, selectionArgs,
				null, null, null);
		
	}

}