package it.divito.touristexplorer.database;

import it.divito.touristexplorer.TouristExplorer;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "tourist_explorer";
	private static final int DATABASE_VERSION = 1;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("PRAGMA foreign_keys=ON;");
		db.execSQL(createTrackTable());
		db.execSQL(createLocationTable());
		db.execSQL(createPhotoTable());
		db.execSQL(createVideoTable());
		db.execSQL(createAudioTable());
		db.execSQL(createCommentTable());
		db.execSQL(createFilePhotoTable());
		db.execSQL(createFileVideoTable());
		db.execSQL(createFileAudioTable());
		db.execSQL(createFileKMLTable());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// non previsto
	}
	
	private String createTrackTable(){
		String create = "";
		create += "CREATE TABLE " + TouristExplorer.TABLE_TRACK + "(";
		create += "  " + TouristExplorer.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
		create += "  " + TouristExplorer.COLUMN_NAME + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_START_ADDRESS + " TEXT,";
		create += "  " + TouristExplorer.COLUMN_FINISH_ADDRESS + " TEXT,";
		create += "  " + TouristExplorer.COLUMN_START_DATE + " TEXT,";
		create += "  " + TouristExplorer.COLUMN_START_TIME + " TEXT,";
		create += "  " + TouristExplorer.COLUMN_FINISH_DATE + " TEXT,";
		create += "  " + TouristExplorer.COLUMN_FINISH_TIME + " TEXT,";
		create += "  " + TouristExplorer.COLUMN_AVG_SPEED + " REAL,";
		create += "  " + TouristExplorer.COLUMN_TOTAL_DISTANCE + " REAL";
		create += ")";
		return create;
	}
	
	private String createLocationTable(){
		String create = "";
		create += "CREATE TABLE " + TouristExplorer.TABLE_LOCATION + "(";
		create += "  " + TouristExplorer.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
		create += "  " + TouristExplorer.COLUMN_LONGITUDE + " STRING NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_LATITUDE + " STRING NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_SPEED + " REAL NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_COLOR + " INT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_TRACK + " INTEGER NOT NULL REFERENCES " + TouristExplorer.TABLE_TRACK + "("+TouristExplorer.COLUMN_ID+") ON DELETE CASCADE";
		create += ")";
		return create;
	}
	
	
	private String createPhotoTable(){
		String create = "";
		create += "CREATE TABLE " + TouristExplorer.TABLE_PHOTO + "(";
		create += "  " + TouristExplorer.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
		create += "  " + TouristExplorer.COLUMN_TITLE + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_DESCRIPTION + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_PATH + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_DATETIME + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_LOCATION + " INTEGER NOT NULL REFERENCES " + TouristExplorer.TABLE_LOCATION + "("+TouristExplorer.COLUMN_ID+") ON DELETE CASCADE";
		create += ")";
		return create;
	}
	
	
	private String createVideoTable(){
		String create = "";
		create += "CREATE TABLE " + TouristExplorer.TABLE_VIDEO + "(";
		create += "  " + TouristExplorer.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
		create += "  " + TouristExplorer.COLUMN_TITLE + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_DESCRIPTION + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_PATH + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_DATETIME + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_LOCATION + " INTEGER NOT NULL REFERENCES " + TouristExplorer.TABLE_LOCATION + "("+TouristExplorer.COLUMN_ID+") ON DELETE CASCADE";
		create += ")";
		return create;
	}
	
	
	private String createAudioTable(){
		String create = "";
		create += "CREATE TABLE " + TouristExplorer.TABLE_AUDIO + "(";
		create += "  " + TouristExplorer.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
		create += "  " + TouristExplorer.COLUMN_TITLE + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_DESCRIPTION + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_PATH + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_DATETIME + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_LOCATION + " INTEGER NOT NULL REFERENCES " + TouristExplorer.TABLE_LOCATION + "("+TouristExplorer.COLUMN_ID+") ON DELETE CASCADE";
		create += ")";
		return create;
	}
	
	
	private String createCommentTable(){
		String create = "";
		create += "CREATE TABLE " + TouristExplorer.TABLE_COMMENT + "(";
		create += "  " + TouristExplorer.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
		create += "  " + TouristExplorer.COLUMN_TITLE + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_DESCRIPTION + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_PATH + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_DATETIME + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_LOCATION + " INTEGER NOT NULL REFERENCES " + TouristExplorer.TABLE_LOCATION + "("+TouristExplorer.COLUMN_ID+") ON DELETE CASCADE";
		create += ")";
		return create;
	}
	
	
	private String createFilePhotoTable(){
		String create = "";
		create += "CREATE TABLE " + TouristExplorer.TABLE_PHOTO_FILE + "(";
		create += "  " + TouristExplorer.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
		create += "  " + TouristExplorer.COLUMN_PATH + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_TRACK + " INTEGER NOT NULL REFERENCES " + TouristExplorer.TABLE_TRACK + "("+TouristExplorer.COLUMN_ID+") ON DELETE CASCADE";
		create += ")";
		return create;
	}
	
	private String createFileVideoTable(){
		String create = "";
		create += "CREATE TABLE " + TouristExplorer.TABLE_VIDEO_FILE + "(";
		create += "  " + TouristExplorer.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
		create += "  " + TouristExplorer.COLUMN_PATH + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_TRACK + " INTEGER NOT NULL REFERENCES " + TouristExplorer.TABLE_TRACK + "("+TouristExplorer.COLUMN_ID+") ON DELETE CASCADE";
		create += ")";
		return create;
	}
	
	private String createFileAudioTable(){
		String create = "";
		create += "CREATE TABLE " + TouristExplorer.TABLE_AUDIO_FILE + "(";
		create += "  " + TouristExplorer.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
		create += "  " + TouristExplorer.COLUMN_PATH + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_TRACK + " INTEGER NOT NULL REFERENCES " + TouristExplorer.TABLE_TRACK + "("+TouristExplorer.COLUMN_ID+") ON DELETE CASCADE";
		create += ")";
		return create;
	}
	
	private String createFileKMLTable(){
		String create = "";
		create += "CREATE TABLE " + TouristExplorer.TABLE_KML_FILE + "(";
		create += "  " + TouristExplorer.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
		create += "  " + TouristExplorer.COLUMN_PATH + " TEXT NOT NULL,";
		create += "  " + TouristExplorer.COLUMN_TRACK + " INTEGER NOT NULL REFERENCES " + TouristExplorer.TABLE_TRACK + "("+TouristExplorer.COLUMN_ID+") ON DELETE CASCADE";
		create += ")";
		return create;
	}
}
