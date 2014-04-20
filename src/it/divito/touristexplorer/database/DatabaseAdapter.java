package it.divito.touristexplorer.database;

import java.util.ArrayList;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.TouristExplorer;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
 
public class DatabaseAdapter {
	
    private Context context;
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private MyApplication myApp;
    
    private TrackTable trackTable;
    private LocationTable locationTable;
    private MediaTable photoTable;
    private MediaTable videoTable;
    private MediaTable commentTable;
    private MediaTable audioTable;
    
    private MediaFileTable mediaFileTable;
    private KMZTable kmlTable;
   
    public DatabaseAdapter(Context context) {
    	this.context = context;
    }
 
   
    public DatabaseAdapter open() throws SQLException {
    	dbHelper = new DatabaseHelper(context);
    	database = dbHelper.getWritableDatabase();
    	
    	myApp = (MyApplication) context.getApplicationContext();
		myApp.setDatabase(database);
    	
    	trackTable = new TrackTable(context);
    	locationTable = new LocationTable(context);
    	photoTable = new PhotoTable(context);
        videoTable = new VideoTable(context);
    	audioTable = new AudioTable(context);
    	commentTable = new CommentTable(context);
    	kmlTable = new KMZTable(context);
    	
    	mediaFileTable = new MediaFileTable(context);
    	
    	return this;
    }
 
   
    public void close() {
    	dbHelper.close();
    }
 

    public long insertLocation(double latitude, double longitude, double currentSpeed,
			int color, int track) {
		return locationTable.insertLocation(latitude, longitude, currentSpeed, color, track);
	}
   
   
    public long insertMedia(String request_type, String title, String description, String path, String datetime, int location) {
    	
    	if(request_type.equals("video")){
    		 return videoTable.insert(title, description, path, datetime, location);
    	} 
    	else{
    		return photoTable.insert(title, description, path, datetime, location);
    	}
    		
    	
	}
	
	public long insertAudio(String title, String description, String path, String datetime, int location) {
		return audioTable.insert(title, description, path, datetime, location);
	}

	public long insertComment(String title, String description, String path, String datetime, int location) {
		return commentTable.insert(title, description, path, datetime, location);
	}
   
	public long insertTrack(String name, String startDate, String startTime) {
		return trackTable.insertTrack(name, startDate, startTime);
	}
	
	public long insertPhotoFile(String path, int track){
		return mediaFileTable.insertPhotoFile(path, track);
	}
	
	
	public long insertVideoFile(String path, int track){
		return mediaFileTable.insertVideoFile(path, track);
	}
	
	
	public long insertAudioFile(String path, int track){
		return mediaFileTable.insertAudioFile(path, track);
	}	
	
	public long insertKMLFile(String path, int track){
		return kmlTable.insertKMLFile(path, track);
	}

	public Cursor selectLocation(double longitude, double latitude) {
		return locationTable.selectLocation(longitude, latitude);
	}
	
	public Cursor selectLocation(int track) {
		return locationTable.selectLocationFromTrack(track);		
	}
	
	public Cursor selectFirstLocation(int track) {
		return locationTable.selectFirstLocation(track);
	}
	
	public Cursor selectLastLocation(int track) {
		return locationTable.selectLastLocation(track);
	}
	
	public Cursor selectLastLocation(double longitude, double latitude) {
		return locationTable.selectLastLocation(longitude, latitude);
	}
	
	public Cursor selectLocationFromID(int locationID) {
		return locationTable.selectLocationFromId(locationID);
	}
	
	public Cursor selectAllLocation() {
		return locationTable.selectAllLocations();
	}
	
	public Cursor selectTrack(String track) {
		return trackTable.selectTrack(track);		
	}
	
	
	public Cursor selectTrack(int track) {
		return trackTable.selectTrack(track);		
	}
	
	
	public Cursor selectAllTracks() {
		return trackTable.selectAllTracks();
	}

	
	public ArrayList<String> selectPoiPath(int track, int request) {
		
		ArrayList<String> poiPath = new ArrayList<String>();
		Cursor location = selectLocation(track);
		
		while(location.moveToNext()){
			
			Cursor c = null;
			
			switch(request){
			case TouristExplorer.AUDIO_REQUEST:
				c = audioTable.selectFromLocation(location.getInt(0));
				break;
			case TouristExplorer.PHOTO_REQUEST:
				c = photoTable.selectFromLocation(location.getInt(0));
				break;
			case TouristExplorer.VIDEO_REQUEST:
				c = videoTable.selectFromLocation(location.getInt(0));
				break;
			case TouristExplorer.COMMENT_REQUEST:
				c = commentTable.selectFromLocation(location.getInt(0));
				break;
			}
			
			if(c.getCount()>0){
				c.moveToFirst();
				poiPath.add(c.getString(c.getColumnIndex(TouristExplorer.COLUMN_PATH)));
			}
		}
		
		return poiPath;
		
	}
	
	public Cursor selectPhoto(int location) {
		return photoTable.selectFromLocation(location);
	}
	
	public Cursor selectVideo(int location) {
		return videoTable.selectFromLocation(location);
	}
	
	
	public Cursor selectAudio(int location) {
		return audioTable.selectFromLocation(location);
	}
	
	
	public Cursor selectComment(int location) {
		return commentTable.selectFromLocation(location);
	}
	
	
	public Cursor selectPoi(int id, int request) {
		switch(request){
		case TouristExplorer.AUDIO_REQUEST:
			return audioTable.selectFromId(id);
		case TouristExplorer.PHOTO_REQUEST:
			return photoTable.selectFromId(id);
		case TouristExplorer.VIDEO_REQUEST:
			return videoTable.selectFromId(id);
		case TouristExplorer.COMMENT_REQUEST:
			return commentTable.selectFromId(id);
		}
		
		return null;
		
	}
	
	public Cursor selectComment(String path) {
		return commentTable.selectFromPath(path);
	}
	
	public Cursor selectPoi(String path, int request) {
		
		switch(request){
		case TouristExplorer.AUDIO_REQUEST:
			return audioTable.selectFromPath(path);
		case TouristExplorer.PHOTO_REQUEST:
			return photoTable.selectFromPath(path);
		case TouristExplorer.VIDEO_REQUEST:
			return videoTable.selectFromPath(path);
		case TouristExplorer.COMMENT_REQUEST:
			return commentTable.selectFromPath(path);
		}
		
		return null;
	}
	
	
	public Cursor selectKMLFile(int track) {
		return kmlTable.selectKMLFile(track);
	}
	
	
	public Cursor selectPhotoFile(int track) {
		return mediaFileTable.selectPhotoFile(track);
	}
	
	
	public Cursor selectVideoFile(int track) {
		return mediaFileTable.selectVideoFile(track);
	}
	
	
	public Cursor selectAudioFile(int track) {
		return mediaFileTable.selectAudioFile(track);
	}
	
	

	
	
	public boolean deleteMedia(String path, int request) {		
		
		switch(request){
		case TouristExplorer.AUDIO_REQUEST:
			return audioTable.deleteFromPath(path);
		case TouristExplorer.PHOTO_REQUEST:
			return photoTable.deleteFromPath(path);
		case TouristExplorer.VIDEO_REQUEST:
			return videoTable.deleteFromPath(path);
		}
		
		return false;
	}
	
	public boolean deleteTrack(String name) {
		return trackTable.deleteTrack(name);
	}
	
	public boolean deleteTrack(int track) {
		return trackTable.deleteTrack(track);
	}
	
	public boolean deleteTrackWithoutPoi(int track) {
		return trackTable.deleteTrackWithoutPoi(track);
	}
	
	public boolean updateTrack(int track, String name) {	
		return trackTable.updateTrack(track, name);
	}
	
	
	public boolean updateTrack(int track, String finishDate, String finishTime, double avgSpeed, double totalDistance) {		
		return trackTable.updateTrack(track, finishDate, finishTime, avgSpeed, totalDistance);
	}
	
	
	public boolean updateStartAddressTrack(int track, String startAddress) {		
		return trackTable.updateStartAddressTrack(track, startAddress);
	}
	
	public boolean updateFinishAddressTrack(int track, String finishAddress) {		
		return trackTable.updateFinishAddressTrack(track, finishAddress);
	}
	
	
	public boolean updatePoi(String oldPath, String newPath, String title,
			String description, int request) {
		
		switch(request){
		case TouristExplorer.AUDIO_REQUEST:
			return audioTable.update(oldPath, newPath, title, description);
		case TouristExplorer.PHOTO_REQUEST:
			return photoTable.update(oldPath, newPath, title, description);
		case TouristExplorer.VIDEO_REQUEST:
			return videoTable.update(oldPath, newPath, title, description);
		case TouristExplorer.COMMENT_REQUEST:
			return commentTable.update(oldPath, newPath, title, description);
		}
		
		return false;
		
	}
	
	
	public Cursor selectAllPhoto() {
		return photoTable.selectAll();
	}

	
	public Cursor selectAllComment() {
		return commentTable.selectAll();
	}
	
}