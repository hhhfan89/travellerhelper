package it.divito.touristexplorer;

import it.divito.touristexplorer.database.DatabaseAdapter;
import it.divito.touristexplorer.path.ListPathsActivity;
import it.divito.touristexplorer.path.PathInformationActivity;
import it.divito.touristexplorer.poi.AudioActivity;
import it.divito.touristexplorer.poi.CameraActivity;
import it.divito.touristexplorer.poi.CommentActivity;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pl.mg6.android.maps.extensions.ClusteringSettings;
import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.GoogleMap.InfoWindowAdapter;
import pl.mg6.android.maps.extensions.GoogleMap.OnMapClickListener;
import pl.mg6.android.maps.extensions.Marker;
import pl.mg6.android.maps.extensions.SupportMapFragment;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;


/**
 * This activity is the initial activity of the application, where the user will
 * manage the tracking; in addition to this, from this activity the user 
 * will be able to record POIs when needed, and sees everything that was
 * previously carried out, from tracks to single POIs.
 * 
 * @author Stefano Di Vito
 * 
 */
@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity implements OnMapClickListener {

	private static final String LOG_TAG = MainActivity.class.getName();

	private static boolean isTrackingStarted = false;
	
	private GoogleMap mMap;
	private TouristExplorerLocationSource mLocationSource;	
	
	// Receiver di cambiamenti di provider (GPS, reti wireless)
	private BroadcastReceiver mProviderReceiver;
	// Receiver di cambiamenti di connessione (Wi-fi, rete mobile)
	private BroadcastReceiver mConnectionReceiver;
	
	private BroadcastReceiver mGPSFixReceiver;

	// Cartelle dell'applicazione
	private File mTouristExplorerFolder;
	private File mImageFolder;
	private File mAudioFolder; 
	private File mVideoFolder; 
	private File mKMZFolder;

	private DatabaseAdapter dbAdapter; 
    private MyApplication myApp;
	
    private Context context;
    
    // Pulsanti sulla action bar
    private MenuItem mMenuItemPoi;
    private MenuItem mMenuItemTracking;
    
    // Variabili relative alla side bar
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] menuItems;
    private String[] menuSubtitle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    
	private boolean mIsGPSFix = false;		// determina se il segnale GPS è stato acquisito
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		context = getApplicationContext();
		
		mLocationSource = new TouristExplorerLocationSource(this);
		
		myApp = ((MyApplication) this.getApplication());
		dbAdapter = myApp.getDbAdapter();
		
		Display display = getWindowManager().getDefaultDisplay(); 
		myApp.setMapContainerHeight(display.getHeight()/2);
	    
		// Inizializza la sidebar
		setSideBar();

		// Carica la mappa, se necessario
		setUpMapIfNeeded();
			
		// Creazione cartelle programma
		makeAppDirectories();
		
		// Inizializza i receiver
		inizializeReceiver();
		
		mLocationSource.setMap(mMap);
		
		mMap.setLocationSource(mLocationSource);
		mMap.setOnMapClickListener(this);
		mMap.setMyLocationEnabled(true);
		
	}

	
	/**
	 * Displays the error "No location available" message to the user
	 * @param message the message resource (present in strings.xml)
	 */
	private void noLocationMessage(int message){
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);

		alertDialogBuilder.setMessage(message);

		// Imposta il messaggio
		alertDialogBuilder
			.setCancelable(true)
			.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
			});
		
		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();
	
	}
	
	
	/**
	 * Displays the warning "No GPS available" to the user
	 */
	private void buildAlertMessageNoGps() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage(R.string.error_no_gps)
	           .setCancelable(false)
	           .setPositiveButton(R.string.turn_on_gps, new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	            	   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));     
	               }
	           })
	           .setNegativeButton(R.string.continue_without_GPS, new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	            	   dialog.cancel();
	                   setButtonsEnabled(!isTrackingStarted);
	                   isTrackingStarted  = !isTrackingStarted;
	                   mLocationSource.startTracking();
	               }
	           })
	    	   .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    		   public void onClick(final DialogInterface dialog, final int id) {
	    			   setButtonsEnabled(isTrackingStarted);
	                   dialog.cancel();
	    		   }
	    	   });
	    
	    final AlertDialog alert = builder.create();
	    alert.show();
	
	}
	
	
	/**
	 * Initialize the provider receivers
	 */
	private void inizializeReceiver() {
		
		mProviderReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				mLocationSource.getBestAvailableProvider();
			}
		};

		mConnectionReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(LOG_TAG, "connection receiver, onReceive");
			}
			
		};

		mGPSFixReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				
				if(!mIsGPSFix){
					mIsGPSFix = true;
					Toast toast = Toast.makeText(getApplicationContext(), 
						R.string.GPS_fixed, Toast.LENGTH_LONG);  
						toast.show();
				}
			}
			
		};
		
		registerReceiver(mConnectionReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		registerReceiver(mProviderReceiver, new IntentFilter("android.location.PROVIDERS_CHANGED"));
		
		IntentFilter filter = new IntentFilter("android.location.GPS_ENABLED_CHANGE");
		filter.addAction("android.location.GPS_FIX_CHANGE");
		registerReceiver(mGPSFixReceiver, new IntentFilter("android.location.GPS_FIX_CHANGE"));//, filter);

	}



	@Override
	public void onResume() {
		
		super.onResume();
		if(!isTrackingStarted){
			mLocationSource.getBestAvailableProvider();
			setUpMapIfNeeded();
			mMap.setMyLocationEnabled(true);
		}
	}
	
	
	
	@Override
	public void onBackPressed() {
		if (isTrackingStarted) {
			moveTaskToBack(true);
			//return true;
		} else {
			mMap.setMyLocationEnabled(false);
			unregisterReceiver(mConnectionReceiver);
			unregisterReceiver(mProviderReceiver);
			unregisterReceiver(mGPSFixReceiver);
			finish();
			//return true;
		}
	}
	


	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(LOG_TAG, "onPause");
	//	mMap.setMyLocationEnabled(false);
	}

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(LOG_TAG, "onDestroy");
	//	mMap.setMyLocationEnabled(false);
	}

	
	@Override
	public void onStop() {
		super.onStop();
		Log.d(LOG_TAG, "onStop");
	//	mMap.setMyLocationEnabled(false);
	}

	
	/**
	 * Create folders to save user's POI and KMZ files (for viewing in Google Earth)
	 */
	private void makeAppDirectories() {

		mTouristExplorerFolder = new File(TouristExplorer.TOURIST_EXPLORER_PATH);
		if (!mTouristExplorerFolder.exists()) {

			mImageFolder = new File(TouristExplorer.IMAGE_PATH);
			mVideoFolder = new File(TouristExplorer.VIDEO_PATH);
			mAudioFolder = new File(TouristExplorer.AUDIO_PATH);
			mKMZFolder = new File(TouristExplorer.KML_PATH);

			if (mTouristExplorerFolder.mkdir()) {
				mImageFolder.mkdir();
				mAudioFolder.mkdir();
				mVideoFolder.mkdir();
				mKMZFolder.mkdir();
			}
		}
		
	}
		
	
	/**
	 * Listener of tracking button
	 * @param v the tracking button 
	 */
	public void startOrStopTracking(MenuItem item){
		
		// Se si sta cominciando il tracking
		if(!isTrackingStarted){
			
			getUserSettings();
			
			// Se non si trova un provider, allora non si può effettuare nessun tipo di tracking,
			// e ciò viene comunicato all'utente
			if(mLocationSource.getAvailableProvider()==null){
				Toast.makeText(getApplicationContext(), 
						R.string.error_provider_unavailable,
						Toast.LENGTH_LONG).show();
			}
			if(mLocationSource.getAvailableProvider().equals(TouristExplorer.NETWORK_PROVIDER)){
				buildAlertMessageNoGps();
		    }
			else{
				if(!mIsGPSFix){
					Toast toast = Toast.makeText(getApplicationContext(), 
							R.string.GPS_not_fix, Toast.LENGTH_LONG);  
							toast.show();
				}
				else{
					Toast toast = Toast.makeText(getApplicationContext(), 
							R.string.message_start_tracking, Toast.LENGTH_LONG);  
							toast.show();
					
					setButtonsEnabled(!isTrackingStarted);
					
					// Se si trova un provider, si può cominciare il tracking
					mLocationSource.startTracking();
					
					item.setIcon(R.drawable.ic_action_stop);
					item.setTitle(R.string.tooltip_stop_tracking);
					
					isTrackingStarted  = !isTrackingStarted;
				}
			}
		}
		// Se si sta fermando il tracking
		else{
			
			setButtonsEnabled(!isTrackingStarted);
			
			// Si ferma il tracking
			Toast toast = Toast.makeText(getApplicationContext(), 
					R.string.message_stop_tracking, Toast.LENGTH_LONG);  
			toast.show();
			
			item.setIcon(R.drawable.ic_action_play);
			item.setTitle(R.string.tooltip_start_tracking);
					
			mLocationSource.stopTracking();
			
			showPathInformation();
			
			mMap.clear();
			isTrackingStarted = !isTrackingStarted;
			
		}
		
	}
	

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.menu_actionbar, menu);
		mMenuItemPoi = menu.getItem(1);
		mMenuItemTracking = menu.getItem(0);
		setButtonsEnabled(isTrackingStarted);
		
		return true;
		
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		Location location;
		
		switch (item.getItemId()) {
		
		// Opzioni
		case R.id.menu_settings:
			Log.d(LOG_TAG, "Start Settings Activity");
			Intent settingsIntent = new Intent(MainActivity.this, OptionsActivity.class);
			startActivityForResult(settingsIntent, TouristExplorer.SETTING_REQUEST);
			return true;
		
		// Sidebar
		case android.R.id.home: 
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
			return true;
		
		// Start/Stop tracking				
		case R.id.actionbar_tracking:
			Log.d(LOG_TAG, "Start Tracking Activity");
			
			startOrStopTracking(item);
			
			return true;
		
		// Aggiunta commento
		case R.id.actionbar_menuPoI_comment:
			Log.d(LOG_TAG, "actionbar_menuPoI_comment");
			
			location = mLocationSource.getLastSavedLocation();
			
			// Se non viene trovata un posizione già salvata, 
			// allora si visualizza un messaggio di errore, altrimenti
			// si passa all'activity per l'inserimento del commento
			if(location==null){
				noLocationMessage(R.string.error_no_locations);
			}
			else {
				Intent commentIntent = new Intent(MainActivity.this, CommentActivity.class);
				commentIntent.putExtra("location", location);
				startActivityForResult(commentIntent, TouristExplorer.COMMENT_REQUEST);
			}

			return true;
		
		// Aggiunta foto
		case R.id.actionbar_menuPoI_photo:
			Log.d(LOG_TAG, "actionbar_menuPoI_photo");
			
			location = mLocationSource.getLastSavedLocation();
			
			// Se non viene trovata un posizione già salvata, 
			// allora si visualizza un messaggio di errore, altrimenti
			// si passa all'activity per l'inserimento della foto		
			if(location==null){
				noLocationMessage(R.string.error_no_locations);
			}
			else{ 
				Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
				cameraIntent.putExtra("request_type", "photo");
				cameraIntent.putExtra("location", location);
				startActivityForResult(cameraIntent, TouristExplorer.PHOTO_REQUEST);
			}
			
			return true;
		
		// Aggiunta video
		case R.id.actionbar_menuPoI_video:
			Log.d(LOG_TAG, "actionbar_menuPoI_video");
			
			location = mLocationSource.getLastSavedLocation();
			
			// Se non viene trovata un posizione già salvata, 
			// allora si visualizza un messaggio di errore, altrimenti
			// si passa all'activity per l'inserimento del video	
			if(location==null){
				noLocationMessage(R.string.error_no_locations);
			}
			else{ 
				Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
				cameraIntent.putExtra("request_type", "video");
				cameraIntent.putExtra("location", location);
				startActivityForResult(cameraIntent, TouristExplorer.VIDEO_REQUEST);
			}
			
			return true;
		
		// Aggiunta file audio
		case R.id.actionbar_menuPoI_audio:
			Log.d(LOG_TAG, "actionbar_menuPoI_audio");
			
			location = mLocationSource.getLastSavedLocation();
			
			// Se non viene trovata un posizione già salvata, 
			// allora si visualizza un messaggio di errore, altrimenti
			// si passa all'activity per l'inserimento del file audio
			if(location==null){
				noLocationMessage(R.string.error_no_locations);
			}
			else{ 
				Intent audioIntent = new Intent(MainActivity.this, AudioActivity.class);
				audioIntent.putExtra("location", location);					
				startActivityForResult(audioIntent, TouristExplorer.AUDIO_REQUEST);
			}
			
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (data == null) {
			finish();
		}

		// Riceve i risultati dall'activity chiamata in precedenza
		switch (requestCode) {
		
		case TouristExplorer.SETTING_REQUEST:
			// Se l'activity è terminata in maniera normale, 
			// si procede a ricavare i dati impostanti in quest'ultima
			if (resultCode == RESULT_OK) {
				Log.d(LOG_TAG, "OnActivityResult, Setting request ok");
				if (!isTrackingStarted)
					getUserSettings();
				return;
			}

		case TouristExplorer.INFO_REQUEST:
			if (resultCode == RESULT_CANCELED) {
				Log.d(LOG_TAG, "OnActivityResult, Info request canceled");
				dbAdapter.open();
				dbAdapter.deleteTrack(myApp.getCurrentTrack().intValue());
				dbAdapter.close();
				
				return;
			}
			if (resultCode == RESULT_OK) {
				Log.d(LOG_TAG, "OnActivityResult, Info request ok");
				return;
			}		
			
		case TouristExplorer.PHOTO_REQUEST:
			// Se la foto non è stata scattata, oppure
			// l'activity è termininata in maniera anomala, 
			// viene notificato all'utente un messaggio di avvertimento;
			// altrimenti viene inserito un marker sulla mappa,
			// nella posizione dov'è stata scattata la foto
			if (resultCode == RESULT_CANCELED) {
				Log.d(LOG_TAG, "OnActivityResult, Photo request cancelled");
				Toast.makeText(this, "Foto non salvata", Toast.LENGTH_LONG)
						.show();
				return;
			}
			if (resultCode == RESULT_OK) {
				Log.d(LOG_TAG, "OnActivityResult, Photo request ok");

				int id = data.getIntExtra("id", -1);
				Location location = (Location) data.getParcelableExtra("location");
				String path = data.getStringExtra("path");
				String datetime = data.getStringExtra("datetime");
				
				new CustomMarkerOptions(this, TouristExplorer.PHOTO_REQUEST, id,
						location, datetime, path, false);
				
				return;
			}

		case TouristExplorer.VIDEO_REQUEST:
			// Se il video non è stato fatto, oppure
			// l'activity è terminata in maniera anomala, 
			// viene notificato all'utente un messaggio di avvertimento;
			// altrimenti viene inserito un marker sulla mappa,
			// nella posizione dov'è stato ripreso il video
			if (resultCode == RESULT_CANCELED) {
				Log.d(LOG_TAG, "OnActivityResult, Photo request cancelled");
				Toast.makeText(this, "Video non salvato", Toast.LENGTH_LONG)
						.show();
				return;
			}
			if (resultCode == RESULT_OK) {
				Log.d(LOG_TAG, "OnActivityResult, Video request ok");

				int id = data.getIntExtra("id", -1);
				Location location = (Location) data.getParcelableExtra("location");
				String path = data.getStringExtra("path");
				String datetime = data.getStringExtra("datetime");
				
				new CustomMarkerOptions(this, TouristExplorer.VIDEO_REQUEST, id,
						location, datetime, path, false);
				
				return;
				
			}

		case TouristExplorer.AUDIO_REQUEST:
			// Se il file audio non è stato registrato, oppure
			// l'activity è terminata in maniera anomala, 
			// viene notificato all'utente un messaggio di avvertimento;
			// altrimenti viene inserito un marker sulla mappa,
			// nella posizione dov'è stata registrata la traccia audio
			if (resultCode == RESULT_CANCELED) {
				Log.d(LOG_TAG, "OnActivityResult, Audio request cancelled");
				Toast.makeText(this, "Audio non salvato", Toast.LENGTH_LONG)
						.show();

				return;
			}
			if (resultCode == RESULT_OK) {
				Log.d(LOG_TAG, "OnActivityResult, Audio request ok");

				int id = data.getIntExtra("id", -1);
				Location location = (Location) data.getParcelableExtra("location");
				String path = data.getStringExtra("path");
				String datetime = data.getStringExtra("datetime");
				
				new CustomMarkerOptions(this, TouristExplorer.AUDIO_REQUEST, id,
						location, datetime, path, false);
		
				return;
			}
			
		case TouristExplorer.COMMENT_REQUEST:
			// Se il commento non è stato salvato, oppure
			// l'activity è terminata in maniera anomala, 
			// viene notificato all'utente un messaggio di avvertimento;
			// altrimenti viene inserito un marker sulla mappa,
			// nella posizione dov'è stato salvato il commento
			if (resultCode == RESULT_CANCELED) {
				Log.d(LOG_TAG, "OnActivityResult, Comment request cancelled");
				Toast.makeText(this, R.string.message_comment_not_saved,
						Toast.LENGTH_LONG).show();

				return;
			}
			if (resultCode == RESULT_OK) {
				Log.d(LOG_TAG, "OnActivityResult, Comment request ok");

				int id = data.getIntExtra("id", -1);
				Location location = (Location) data.getParcelableExtra("location");
				String datetime = data.getStringExtra("datetime");
				String path = data.getStringExtra("path");
				
				new CustomMarkerOptions(this, TouristExplorer.COMMENT_REQUEST, id,
						location, datetime, path, false);
				
				return;
				
			}
		
		case TouristExplorer.LIST_PATH_REQUEST:
			if (resultCode == RESULT_OK) {
				return;
			}
			
		case TouristExplorer.GALLERY_IMAGE_REQUEST:
			if (resultCode == RESULT_OK) {
				return;
			}
		
		case TouristExplorer.GALLERY_VIDEO_REQUEST:
			if (resultCode == RESULT_OK) {
				return;
			}
			
		case TouristExplorer.GALLERY_AUDIO_REQUEST:
			if (resultCode == RESULT_OK) {
				return;
			}
			
		case TouristExplorer.GALLERY_COMMENT_REQUEST:
			if (resultCode == RESULT_OK) {
				return;
			}
			
		case TouristExplorer.GALLERY_KMZ_REQUEST:
			if (resultCode == RESULT_OK) {
				return;
			}
		}
	}
		
	
	/**
	 * Get settings from "Options" activity
	 */
	private void getUserSettings() {

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		boolean automaticColor = sharedPrefs.getBoolean("automaticColor", true);
		boolean followMe = sharedPrefs.getBoolean("followMe", true);
		
		mLocationSource.setFollowMe(followMe);
		
		if (automaticColor) {
			mLocationSource.setAutomaticColor(true);
			String movingType = sharedPrefs.getString("automaticColorSetting", "WALK");
			
			if (movingType.equals("WALK")) {
				mLocationSource.setMovingType(TouristExplorer.WALK);
			}
			if (movingType.equals("VEHICLE")) {
				mLocationSource.setMovingType(TouristExplorer.VEHICLE);
			}
			if (movingType.equals("BIKE")) {
				mLocationSource.setMovingType(TouristExplorer.BIKE);
			}
		} else {
			mLocationSource.setAutomaticColor(false);
			String selectedColorMap = sharedPrefs.getString("listLineColorsMap", "BLACK");
			mLocationSource.setLineColor(Color.parseColor(selectedColorMap));
		}
		
	}
	 
	
	/**
	 * Show path information, after stopping the track
	 */
	private void showPathInformation(){
		
		Intent i = new Intent(this, PathInformationActivity.class);
		int track = myApp.getCurrentTrack().intValue();
		i.putExtra("trackID", track);
		i.putExtra("saveTrack", true);
		
		startActivityForResult(i, TouristExplorer.INFO_REQUEST);
		
	}
	
	
	/**
	 * Download the map from Google
	 */
	private void setUpMapIfNeeded() {
		
		if(mMap == null){
			FragmentManager myFragmentManager = getSupportFragmentManager();
			SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager
					.findFragmentById(R.id.map);			
			mMap = mySupportMapFragment.getExtendedMap();
			Log.i("map null", "map null");
		}
		// Check if we were successful in obtaining the map.
		if (mMap != null) {
			Log.i("map not null", "map not null");
			Location location = mLocationSource.getLastKnownLocation();
			LatLng actualLatLng;
			int zoom;
			
			if(location!=null){
				actualLatLng = new LatLng(location.getLatitude(), location.getLongitude());	
				zoom = TouristExplorer.ZOOM_DEFAULT;
			}
			else {
				actualLatLng = TouristExplorer.LATLNG_ITALY;
				zoom = TouristExplorer.ZOOM_ITALY;
			}
				
			mMap.setLocationSource(mLocationSource);
			mMap.setMyLocationEnabled(true);
			mMap.setOnMapClickListener(this);
			mMap.moveCamera(CameraUpdateFactory.newLatLng(actualLatLng));
			mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
			
			myApp.setMainMap(mMap);
			
			mMap.setClustering(new ClusteringSettings().iconDataProvider(new TouristExplorerIconProvider(getResources())).addMarkersDynamically(true));
			mMap.setInfoWindowAdapter(new InfoWindowAdapter() {

                    private TextView tv;
                    {
                            tv = new TextView(MainActivity.this);
                            tv.setTextColor(Color.BLACK);
                    }

                    private Collator collator = Collator.getInstance();
                    private Comparator<Marker> comparator = new Comparator<Marker>() {
                            public int compare(Marker lhs, Marker rhs) {
                                    String leftTitle = lhs.getTitle();
                                    String rightTitle = rhs.getTitle();
                                    if (leftTitle == null && rightTitle == null) {
                                            return 0;
                                    }
                                    if (leftTitle == null) {
                                            return 1;
                                    }
                                    if (rightTitle == null) {
                                            return -1;
                                    }
                                    return collator.compare(leftTitle, rightTitle);
                            }
                    };

                    @Override
                    public View getInfoWindow(Marker marker) {
                            return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                            if (marker.isCluster()) {
                            	    List<Marker> markers = marker.getMarkers();
                                    
                                    Marker m = Collections.min(markers, comparator);
                                    
                                    String title = "Ci sono " + markers.size() + " PoI";
                                    tv.setText(title);
                                    return tv;
                            } 

                            return null;
                    }
            });
		}
		
    }
		
	
	@Override
	public void onMapClick(LatLng point) {

		mMap.animateCamera(CameraUpdateFactory.newLatLng(point));
		
	}
	
	
	public void setButtonsEnabled(boolean status){
	
		mMenuItemPoi.setEnabled(status);
		if(status)
			mMenuItemTracking.setIcon(R.drawable.ic_action_stop);
		else
			mMenuItemTracking.setIcon(R.drawable.ic_action_play);
		
	}
	
	
	/**
	 * Set the sidebar for this activity: within this, there
	 * are buttons for displaying the various lists (tracks list,
	 * POIs lists, KMZ file list) 
	 */
	private void setSideBar(){

		final ActionBar actionBar = getSupportActionBar();
	
		mTitle = mDrawerTitle = getTitle();
		menuItems = getResources().getStringArray(R.array.drawer_menu_title);
		menuSubtitle = getResources().getStringArray(R.array.drawer_menu_subtitle);
		String[] menuItemsIcon = getResources().getStringArray(R.array.drawer_menu_items_icon);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		ArrayList<Integer> icons = new ArrayList<Integer>();
		int res = 0;
		for (String item : menuItems) {
			
			int id_icon = getResources().getIdentifier(menuItemsIcon[res], "drawable", this.getPackageName());
			icons.add(id_icon);
			res++;
		}
		
		
		ListViewAdapter adapter = new ListViewAdapter(context, menuItems, menuSubtitle, icons);
		
		mDrawerList.setAdapter(adapter);
		mDrawerList.setBackgroundResource(R.color.nypd_blue);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(
				this, 
				mDrawerLayout,
                R.drawable.ic_drawer, 
                R.string.drawer_open, 
                R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
		
	}
	
	
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
			
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	
	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private void selectItem(int position) {
		
		android.app.Fragment fragment = new MapActivity();
	    Bundle args = new Bundle();
	   /* args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
	    fragment.setArguments(args);*/

	    // Insert the fragment by replacing any existing fragment
	    android.app.FragmentManager fragmentManager = getFragmentManager();
	    fragmentManager.beginTransaction()
	                   .replace(R.id.map, fragment)
	                   .commit();

	    // Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(position, true);
	    setTitle("pos"+position);
	    Log.e("bla", "bla");
	    
		/*
		switch (position) {
		case 0:
			dbAdapter.open();
			Cursor c = dbAdapter.selectAllTracks();
			if (c.moveToNext()) {
				startActivityForResult(new Intent(MainActivity.this,
						ListPathsActivity.class),
						TouristExplorer.LIST_PATH_REQUEST);
			} else {
				noLocationMessage(R.string.error_no_path);
			}
			break;
		case 1:
			if (!Utils.isDirectoryEmpty(TouristExplorer.IMAGE_PATH))
				startActivityForResult(new Intent(MainActivity.this,
						GalleryImageActivity.class),
						TouristExplorer.GALLERY_IMAGE_REQUEST);
			else
				noLocationMessage(R.string.error_no_image);

			break;
		case 2:
			if (!Utils.isDirectoryEmpty(TouristExplorer.VIDEO_PATH))
				startActivityForResult(new Intent(MainActivity.this,
						GalleryVideoActivity.class),
						TouristExplorer.GALLERY_VIDEO_REQUEST);
			else
				noLocationMessage(R.string.error_no_video);
			break;

		case 3:
			if (!Utils.isDirectoryEmpty(TouristExplorer.AUDIO_PATH))
				startActivityForResult(new Intent(this,
						ListAudioTrackActivity.class),
						TouristExplorer.GALLERY_AUDIO_REQUEST);
			else
				noLocationMessage(R.string.error_no_audio);

			break;

		case 4:
			dbAdapter.open();

			if (dbAdapter.selectAllComment().getCount() > 0)
				startActivityForResult(new Intent(this,
						ListCommentTrackActivity.class),
						TouristExplorer.GALLERY_COMMENT_REQUEST);
			else
				noLocationMessage(R.string.error_no_comment);

			break;

		case 5:
			if (!Utils.isDirectoryEmpty(TouristExplorer.KML_PATH))
				startActivityForResult(new Intent(this, ListKmlActivity.class),
						TouristExplorer.GALLERY_KMZ_REQUEST);
			else
				noLocationMessage(R.string.error_no_kml);
			break;
		default:

			break;
		}
*/
		mDrawerLayout.closeDrawer(mDrawerList);

	}
	
}