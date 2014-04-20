package it.divito.touristexplorer.path;
import it.divito.touristexplorer.CustomMarkerOptions;
import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.R;
import it.divito.touristexplorer.TouristExplorer;
import it.divito.touristexplorer.TouristExplorerIconProvider;
import it.divito.touristexplorer.database.DatabaseAdapter;
import it.divito.touristexplorer.model.Audio;
import it.divito.touristexplorer.model.Comment;
import it.divito.touristexplorer.model.Photo;
import it.divito.touristexplorer.model.Poi;
import it.divito.touristexplorer.model.Point;
import it.divito.touristexplorer.model.Video;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import pl.mg6.android.maps.extensions.ClusteringSettings;
import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.SupportMapFragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


/**
 * This activity allows to show a selected track on the map; with this track
 * will also be shown the related POI
 * @author Stefano Di Vito
 *
 */

public class ShowPathActivity extends FragmentActivity {

	private int trackID;
	private int arrayPos = 0;
	
	private String mTrackName;
	private String mTrackStartTime;
	private String mTrackFinishTime;
	
	private ArrayList<Point> points;
	private LatLng src;
	private LatLng dest;
	
	private MyApplication myApp;
	private DatabaseAdapter dbAdapter;
	private GoogleMap mMap;
	private Handler mHandler;
	private ProgressDialog mProgressDialog;

	
	@SuppressLint("HandlerLeak")
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.show_path);

		myApp = ((MyApplication) this.getApplication());
		dbAdapter = myApp.getDbAdapter();
		
		setUpMapIfNeeded();
		Intent intent = getIntent();
		
		boolean showingPath = intent.getBooleanExtra("showingPath", false);
		
		// Ci sono due possibilità: da una parte si sta visualizzando un tracciato,
		// e di conseguenza si caricano le posizioni dalla db relative alla traccia,
		// e vengono visualizzate assieme ai POI relativi.
		// Dall'altra abbiamo invece la visualizzazione di un solo POI su mappa,
		// quindi si carica dal database solo le informazioni relative al POI selezionato
		if(showingPath){
			trackID = intent.getIntExtra("trackID", -1); 
			mTrackName = intent.getStringExtra("trackName");
			mTrackStartTime = getTrackStartTime(trackID);
			mTrackFinishTime = getTrackFinishTime(trackID);
			
			setTitle(mTrackName); 
			
			mProgressDialog = ProgressDialog.show(this,"","Sto caricando le posizioni..");
			mHandler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what==0) {
					mProgressDialog.dismiss();
					showTrack();
					}
				}
			};   
			
			Thread thread = new Thread() {
				@Override
				public void run() {
					points = getTrackLocations(trackID);
					mHandler.sendEmptyMessage(0);
				}
			};
			thread.start();
		}
		else{
			int location = intent.getIntExtra("location", -1); 
			int request = intent.getIntExtra("request", -1);
			showPoi(location, request);
		}
		
	}
	
	
	
	/**
	 * Show the selected POI
	 * @param locationID the id of the POI's location
	 * @param request POI's type
	 */
	private void showPoi(int locationID, int request){
		
		Cursor cursorLocation = dbAdapter.selectLocationFromID(locationID);
		points = getPoiInLocation(cursorLocation, request);
		showTrack();
		
	}
	
	
	/**
	 * Get the start time of the track
	 * @param trackID the id of the track
	 * @return track's start time
	 */
	private String getTrackStartTime(int trackID){
		
		Cursor c = dbAdapter.selectTrack(trackID);
		while(c.moveToNext()){
			String time = c.getString(c.getColumnIndex(TouristExplorer.COLUMN_START_TIME));
			return c.getString(c.getColumnIndex(TouristExplorer.COLUMN_START_DATE)) + " " + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4, 6);
		}
		
		return null;
	}
	
	
	/**
	 * Get the finish time of the track
	 * @param trackID the id of the track
	 * @return track's finish time
	 */
	private String getTrackFinishTime(int trackID){
		
		Cursor c = dbAdapter.selectTrack(trackID);
		while(c.moveToNext()){
			String time = c.getString(c.getColumnIndex(TouristExplorer.COLUMN_FINISH_TIME));
			return c.getString(c.getColumnIndex(TouristExplorer.COLUMN_FINISH_DATE))+ " " + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4, 6);
		}
		
		return null;
	}


	/**
	 * Get POI list in each track's location
	 * @param cursorLocation cursor that contains location informations
	 * @return list of point with POIs
	 */
	private ArrayList<Point> getTrackListPoi(Cursor cursorLocation){
		
		ArrayList<Point> listPoint = new ArrayList<Point>();
		
		while(cursorLocation.moveToNext()) {
			Point point = new Point();
			Poi poi = null;
			Location location = new Location("aProvider");
			
			location.setLongitude(cursorLocation.getFloat(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LONGITUDE))); 
			location.setLatitude(cursorLocation.getFloat(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LATITUDE))); 
			point.setLocation(location);
			point.setColor(cursorLocation.getInt(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_COLOR)));
			
			// POI foto
			Cursor cursorPhotoInLocation = dbAdapter.selectPhoto(cursorLocation.getInt(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_ID)));
			if(cursorPhotoInLocation.getCount()>0){
				poi = new Photo();
				cursorPhotoInLocation.moveToFirst();
				point.addPoi(setPoiInfo(poi, cursorPhotoInLocation));
			}
			
			// POI video
			Cursor cursorVideoInLocation = dbAdapter.selectVideo(cursorLocation.getInt(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_ID)));
			if(cursorVideoInLocation.getCount()>0){
				poi = new Video();
				cursorVideoInLocation.moveToFirst();
				point.addPoi(setPoiInfo(poi, cursorVideoInLocation));
			}
			
			// POI audio
			Cursor cursorAudioInLocation = dbAdapter.selectAudio(cursorLocation.getInt(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_ID)));
			if(cursorAudioInLocation.getCount()>0){
				poi = new Audio();
				cursorAudioInLocation.moveToFirst();
				point.addPoi(setPoiInfo(poi, cursorAudioInLocation));
			}
			
			// POI commento
			Cursor cursorCommentInLocation = dbAdapter.selectComment(cursorLocation.getInt(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_ID)));
			if(cursorCommentInLocation.getCount()>0){
				poi = new Comment();
				cursorCommentInLocation.moveToFirst();
				point.addPoi(setPoiInfo(poi, cursorCommentInLocation));
			}
			
			listPoint.add(point);
			
		} 
		
		return listPoint;
	}
	
	
	/**
	 * Get the locations of the track
	 * @param trackID the id of the track
	 * @return track's finish time
	 */
	private ArrayList<Point> getTrackLocations(int track){
		
		Cursor cursorLocation = dbAdapter.selectLocation(track);
		return getTrackListPoi(cursorLocation);
	
	}

	
	/**
	 * Get the POI in the location
	 * @param cursorLocation cursor that contains the location of the POI
	 * @param request POI's type
	 * @return list of POI 
	 */
	private ArrayList<Point> getPoiInLocation(Cursor cursorLocation, int request){
		
		ArrayList<Point> listPoint = new ArrayList<Point>();
		Cursor cursor = null;
		
		while(cursorLocation.moveToNext()) {
			Point point = new Point();
			Poi poi = null;
			Location location = new Location("aProvider");
			
			location.setLongitude(cursorLocation.getFloat(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LONGITUDE))); 
			location.setLatitude(cursorLocation.getFloat(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LATITUDE))); 
			point.setLocation(location);
			point.setColor(cursorLocation.getInt(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_COLOR)));
			
			switch(request){
			
			case TouristExplorer.COMMENT_REQUEST:
				cursor = dbAdapter.selectComment(cursorLocation.getInt(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_ID)));
				poi = new Comment();
				break;
				
			case TouristExplorer.PHOTO_REQUEST:
				cursor = dbAdapter.selectPhoto(cursorLocation.getInt(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_ID)));
				poi = new Photo();
				break;
			
			case TouristExplorer.VIDEO_REQUEST:
				cursor = dbAdapter.selectVideo(cursorLocation.getInt(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_ID)));
				poi = new Video();
				break;
			
			case TouristExplorer.AUDIO_REQUEST:
				cursor = dbAdapter.selectAudio(cursorLocation.getInt(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_ID)));
				poi = new Audio();
				break;
				
			}
			
			cursor.moveToFirst();
			point.addPoi(setPoiInfo(poi, cursor));
			listPoint.add(point);
			
		} 
		
		return listPoint;
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.show_path_menu, menu);
		return true;
	}
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
		
        case R.id.info:
        	showPathInformation();
			return true;
        }
        return false;
    }
	
	
	/**
	 * Show path information
	 */
	public void showPathInformation(){
		
		Intent i = new Intent(ShowPathActivity.this, PathInformationActivity.class);
		i.putExtra("trackID", trackID);
		i.putExtra("trackName", mTrackName);
			
		startActivity(i);
		
	}
	

	/**
	 * Set the POI info
	 * @param poi the selected POI
	 * @param cursor cursor that contains POI information
	 * @return the POI with the informations
	 */
	private Poi setPoiInfo(Poi poi, Cursor cursor){
		
		poi.setId(cursor.getInt(cursor.getColumnIndex(TouristExplorer.COLUMN_ID)));
		poi.setTitle(cursor.getString(cursor.getColumnIndex(TouristExplorer.COLUMN_TITLE)));
		poi.setDescription(cursor.getString(cursor.getColumnIndex(TouristExplorer.COLUMN_DESCRIPTION)));
		poi.setDatetime(cursor.getString(cursor.getColumnIndex(TouristExplorer.COLUMN_DATETIME)));
		poi.setPath(cursor.getString(cursor.getColumnIndex(TouristExplorer.COLUMN_PATH)));
		
		return poi;
		
	}
	
	
	/**
	 * Show track on map
	 */
	private void showTrack(){
		
		final LatLngBounds.Builder builder = new LatLngBounds.Builder();
		final int size = points.size();
		
		if(size==1){
			Log.d("showTrack", "size1");
			Point point = points.get(arrayPos);
			getPoi(point);
			mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(point.getLocation().getLatitude(), point.getLocation().getLongitude())));
			mMap.moveCamera(CameraUpdateFactory.zoomTo(TouristExplorer.ZOOM_DEFAULT));
		}
		else{
			Log.d("showTrack", "size >1");
			
			while(arrayPos < size-1) {    
				
				Point point1 = points.get(arrayPos);
				Point point2 = points.get(arrayPos+1);
				
				src = new LatLng(point1.getLocation().getLatitude(), point1.getLocation().getLongitude());
				dest = new LatLng(point2.getLocation().getLatitude(), point2.getLocation().getLongitude());
				
				PolylineOptions polylineOpt = new PolylineOptions().add(src, dest).width(5).color(point1.getColor());
				mMap.addPolyline(polylineOpt);
				
				builder.include(src);
				
				if(arrayPos==0){
					setStartPoint(point1.getLocation());
				}
				
				getPoi(point1);
				
				if(arrayPos+1 == size-1){
					builder.include(dest);
					getPoi(point2);
					setFinishPoint(point2.getLocation());
				}
				
				arrayPos++;
	
			}
			
			mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 25));
			
		}
	}
	
	
	/**
	 * Set track's start placemark on the map
	 * @param location start track's location 
	 */
	private void setStartPoint(Location location){
		
		new CustomMarkerOptions(this, TouristExplorer.START_POINT_REQUEST, 0,
				location, mTrackStartTime, null, true);
	
	}
	
	
	/**
	 * Set track's finish placemark on the map
	 * @param location finish track's location 
	 */
	private void setFinishPoint(Location location){
		
		new CustomMarkerOptions(this, TouristExplorer.FINISH_POINT_REQUEST, 0,
				location, mTrackFinishTime, null, true);
	
	}
	
	
	/**
	 * Get POI from a point
	 * @param point 
	 */
	private void getPoi(Point point){
		
		ArrayList<Poi> listPoi = point.getPoiList();
		int type = -1;
		
		for(Poi poi : listPoi){
			
			if(poi instanceof Comment){
				type = TouristExplorer.COMMENT_REQUEST;
				Log.e("getPoi", "comment");
			}
			if(poi instanceof Photo){
				type = TouristExplorer.PHOTO_REQUEST;
			}
			if(poi instanceof Video){
				type = TouristExplorer.VIDEO_REQUEST;
			}
			if(poi instanceof Audio){
				type = TouristExplorer.AUDIO_REQUEST;
			}
		
			Log.e("getPoi", "long" + point.getLocation().getLongitude());
			new CustomMarkerOptions(this, type, poi.getId(),
					point.getLocation(), poi.getDatetime(), 
					poi.getPath(), true);
		}
	}
		
	
	@Override
	public void onBackPressed() {
		Log.d("onback", "onback");
		finish();
	}
	
	
	/**
	 * Download the map from Google
	 */
	@SuppressWarnings("deprecation")
	private void setUpMapIfNeeded() {
		
		if (mMap == null) {
			FragmentManager myFragmentManager = getSupportFragmentManager();
			SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager
					.findFragmentById(R.id.map_path);
			
			mMap = mySupportMapFragment.getExtendedMap();
			
		}
		if (mMap != null) {
			myApp.setPathMap(mMap);		
			mMap.moveCamera(CameraUpdateFactory.newLatLng(TouristExplorer.LATLNG_ITALY));
			mMap.animateCamera(CameraUpdateFactory.zoomTo(TouristExplorer.ZOOM_DEFAULT));
		}
		
		mMap.setClustering(new ClusteringSettings().iconDataProvider(new TouristExplorerIconProvider(getResources())).addMarkersDynamically(true));

	}
	
	 
}