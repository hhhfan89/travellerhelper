package it.divito.touristexplorer;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.google.android.gms.maps.model.LatLng;

/**
 * This interface contains only static variables,
 * used throughout the application
 * 
 * @author Stefano Di Vito
 *
 */
@SuppressLint("SimpleDateFormat")
public interface TouristExplorer {
	
	public String TABLE_TRACK = "track";
	public String TABLE_LOCATION = "location";
	public String TABLE_PHOTO = "photo";
	public String TABLE_VIDEO = "video";
	public String TABLE_COMMENT = "comment";
	public String TABLE_AUDIO = "audio";
	public String TABLE_PHOTO_FILE = "file_photo";
	public String TABLE_VIDEO_FILE = "file_video";
	public String TABLE_AUDIO_FILE = "file_audio";
	public String TABLE_KML_FILE = "kml";
	
	public String COLUMN_ID = "_id";
	public String COLUMN_LATITUDE = "latitude";
	public String COLUMN_LONGITUDE = "longitude";
	public String COLUMN_SPEED = "speed";
	public String COLUMN_COLOR = "color";
	public String COLUMN_TRACK = "track";
	
	public String COLUMN_PHOTO = "photo";
	public String COLUMN_VIDEO = "video";
	public String COLUMN_AUDIO = "audio";
	public String COLUMN_COMMENT = "comment";
	
	public String COLUMN_TITLE = "title";
	public String COLUMN_DESCRIPTION = "description";
	public String COLUMN_LOCATION = "location";
	public String COLUMN_PATH = "path";
	public String COLUMN_DATETIME = "datetime";
	
	public String COLUMN_NAME = "name";
	public String COLUMN_START_ADDRESS = "start_address";
	public String COLUMN_FINISH_ADDRESS = "finish_address";
	public String COLUMN_START_DATE = "start_date";
	public String COLUMN_START_TIME = "start_time";
	public String COLUMN_FINISH_DATE = "finish_date";
	public String COLUMN_FINISH_TIME = "finish_time";	
	public String COLUMN_AVG_SPEED = "avg_speed";
	public String COLUMN_TOTAL_DISTANCE = "total_distance";
	
	public static final int SETTING_REQUEST = 1;
	public static final int PHOTO_REQUEST = 2;
	public static final int VIDEO_REQUEST = 3;
	public static final int AUDIO_REQUEST = 4;
	public static final int COMMENT_REQUEST = 5;
	public static final int SAVE_PATH_REQUEST = 6;
	public static final int EXPORT_PATH_REQUEST = 7;	
	public static final int INFO_REQUEST = 8;
	public static final int LIST_PATH_REQUEST = 9;
	public static final int GALLERY_IMAGE_REQUEST = 10;
	public static final int GALLERY_VIDEO_REQUEST = 11;
	public static final int GALLERY_AUDIO_REQUEST = 12;
	public static final int GALLERY_COMMENT_REQUEST = 13;
	public static final int PREVIEW_REQUEST = 14;
	public static final int START_POINT_REQUEST = 15;
	public static final int FINISH_POINT_REQUEST = 16;
	public static final int GALLERY_KMZ_REQUEST = 17;
	
	
	//Marker: icona
	public static final int PHOTO_MARKER = R.drawable.photo_marker;
	public static final int VIDEO_MARKER = R.drawable.video_marker;
	public static final int AUDIO_MARKER = R.drawable.audio_marker;
	public static final int COMMENT_MARKER = R.drawable.comment_marker;
	public static final int START_MARKER = R.drawable.start_marker;
	public static final int FINISH_MARKER = R.drawable.finish_marker;
	
	//Marker: header
	public static final String PHOTO_HEADER = "Foto";
	public static final String VIDEO_HEADER = "Video";
	public static final String AUDIO_HEADER = "Audio";
	public static final String COMMENT_HEADER = "Commento";
	public static final String START_HEADER = "Partenza";
	public static final String FINISH_HEADER = "Arrivo";
	
	//Percorsi
	public static final String SD = Environment.getExternalStorageDirectory().toString(); 
	public static final String INTERNAL_MEMORY = Environment.getRootDirectory().toString();
	public static final String TOURIST_EXPLORER_PATH = SD + "/_TouristExplorer";
	public static final String VIDEO_PATH = TOURIST_EXPLORER_PATH + "/video";
	public static final String IMAGE_PATH = TOURIST_EXPLORER_PATH + "/image";
	public static final String AUDIO_PATH = TOURIST_EXPLORER_PATH + "/audio";
	public static final String KML_PATH = TOURIST_EXPLORER_PATH + "/kml";
	public static final String KMZ_PATH = "sdcard/_TouristExplorer/kml";
	
	public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	public static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	public static final int MEDIA_TYPE_AUDIO = 3;
	
	public static final String COMMENT_PLACEMARK = "http://maps.google.com/mapfiles/kml/shapes/info.png";
	public static final String IMAGE_PLACEMARK = "http://maps.google.com/mapfiles/kml/shapes/camera.png";
	public static final String VIDEO_PLACEMARK = "http://maps.google.com/mapfiles/kml/shapes/movies.png";
	public static final String AUDIO_PLACEMARK = "http://maps.google.com/mapfiles/kml/shapes/earthquake.png";  
	public static final String START_PLACEMARK = "http://maps.google.com/mapfiles/kml/paddle/P.png";  
	public static final String FINISH_PLACEMARK = "http://maps.google.com/mapfiles/kml/paddle/A.png";  
	
	public static final int COMMENT_PLACEMARK_CODE = 0;
	public static final int IMAGE_PLACEMARK_CODE = 1;
	public static final int VIDEO_PLACEMARK_CODE = 2;
	public static final int AUDIO_PLACEMARK_CODE = 3;
	public static final int START_PLACEMARK_CODE = 4;
	public static final int FINISH_PLACEMARK_CODE = 5;
	
	public static final String PHOTO_MARK = "#photo_mark";
	public static final String VIDEO_MARK = "#video_mark";
	public static final String AUDIO_MARK = "#audio_mark";
	public static final String COMMENT_MARK = "#comment_mark";
	public static final String START_MARK = "#start_mark";
	public static final String FINISH_MARK = "#finish_mark";
	
	public static final String PLACEMARK_SCALE = "0.8";
	
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HHmmss");
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd-MM-yyyy-HHmmss");
	public static final SimpleDateFormat DATETIME_MARKER_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	
	public static final LatLng LATLNG_ITALY = new LatLng(41.902277, 12.429657);
	public static final LatLng LATLNG_00 = new LatLng(0, 0);
	
	public static final int ZOOM_DEFAULT = 15;
	public static final int ZOOM_ITALY = 5;
	
	public static final int TYPE_ADDRESS_START = 0;	
	public static final int TYPE_ADDRESS_FINISH = 1;	
	
	public String NETWORK_PROVIDER = "network";
	
	public static final int WALK = 0;
	public static final int VEHICLE = 1;
	public static final int BIKE = 2;
	
	public static final int WALK_LOW_SPEED = 1;
	public static final int WALK_MEDIUM_SPEED = 4;
	public static final int WALK_UPDATE_DISTANCE = 2;
	public static final int WALK_UPDATE_TIME = 1000;
	
	public static final int BIKE_LOW_SPEED = 10;
	public static final int BIKE_MEDIUM_SPEED = 30;
	public static final int BIKE_UPDATE_DISTANCE = 5;
	public static final int BIKE_UPDATE_TIME = 5000;
	
	public static final int VEHICLE_LOW_SPEED = 40;
	public static final int VEHICLE_MEDIUM_SPEED = 80;
	public static final int VEHICLE_UPDATE_DISTANCE = 10;
	public static final int VEHICLE_UPDATE_TIME = 2000;
	
	public static final String NO_TITLE = "(nessun titolo)";
	public static final String NO_DESCRIPTION = "(nessuna descrizione)";
	
	// dimensioni video
	// altezza
	public static final String KML_VIDEO_HEIGHT = "300";
	// lunghezza
	public static final String KML_VIDEO_WIDTH = "400";
	

}
