package it.divito.touristexplorer;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import pl.mg6.android.maps.extensions.GoogleMap;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.graphics.Color;

import it.divito.touristexplorer.database.DatabaseAdapter;

import java.math.BigDecimal;
import java.util.Date;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;


/**
 * This class receives location updates from the Location Manager:
 * for that reason, it also implement the LocationListener interface
 * 
 * @author Stefano Di Vito
 *
 */
public class TouristExplorerLocationSource implements LocationSource, LocationListener {

	private static final String LOG_TAG = TouristExplorerLocationSource.class.getName();
	   
    private final Criteria mCriteria = new Criteria();		// criteri relativi al provider
    private String mBestAvailableProvider;					// miglior provider disponibile
    private LocationManager mLocationManager = null;
    private GoogleMap mMap;
    private Context mContext;
    private OnLocationChangedListener myLocationListener = null;	// listener delle posizioni
    private PolylineOptions polylineOptions;				// opzioni relative alla linea del percorso
    private boolean isTrackingStarted = false;			
    
    private int minUpdateTime = 0;     	  // intervallo di tempo minimo per gli update delle posizioni, in millisecondi
    private int minUpdateDistance = 0;    // distanza minima per gli update delle posizioni, in metri

    // variabili per il calcolo distanza percorsa
    private Location previousLocation = null;
    private LatLng previousLatLng = null;
    
    // variabili per il calcolo velocità 
    private double totalDistance;
    private int locationCounter;
    private double totalSpeed;
    private double avgSpeed;
    
    // variabili per determinare il colore della traccia
    private int movingType = 0;
    private int mLowSpeed;
    private int mMediumSpeed;
    private int mLineColor = Color.BLACK;
    private boolean automaticColor;
    
    private boolean mFollowMe;
    
	private Long mCurrentTrack;
	
	private MyApplication myApp;
	private DatabaseAdapter dbAdapter; 
	
	boolean hasGPSFix;
	
	public TouristExplorerLocationSource(Context context) {
    	
    	mContext = context;
    	
    	myApp = (MyApplication) context.getApplicationContext();
    	dbAdapter = myApp.getDbAdapter();
		    	        
    	// Specifica i criteri del provider delle location
    	mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        mCriteria.setPowerRequirement(Criteria.POWER_LOW);
        mCriteria.setBearingRequired(true);
        mCriteria.setSpeedRequired(true);
    
    }
    
    
    /**
     * Determines the best provider available, that satisfies the previous criteria
     */
    public void getBestAvailableProvider() {
    	
    	mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    	
    	// Ricava il provider ad ora presente
        String provider = mBestAvailableProvider;
        
        // Ricava il provider secondo i criteri prima specificati
        mBestAvailableProvider = mLocationManager.getBestProvider(mCriteria, true);
        
        // Se il provider ricavato dai criteri non coincide con quello già presente, allora
        // il nuovo provider sostituisce quello vecchio
        if(!(mBestAvailableProvider.equals(provider))){
        	mLocationManager.requestLocationUpdates(
        			mBestAvailableProvider, 0, 0, this);
        }
        	
        try{
        	//Se il dispositivo è impostato per prendere la posizione dalla rete WiFi/Mobile,
        	// ma non è attiva effettivamente la rete, allora il provider viene impostato a null 
        	if(mBestAvailableProvider.equals(TouristExplorer.NETWORK_PROVIDER) && !isNetworkAvailable()){
        		mBestAvailableProvider = null;
        	}
        }
        catch(NullPointerException ne){
        }
        finally{
        	Log.d(LOG_TAG, "Best available provider: " + mBestAvailableProvider);
        }
        
    }
    
    
    /**
     * Get the last known location
     * @return the last known location
     */
    public Location getLastKnownLocation() {
    	getBestAvailableProvider();
    	if(mBestAvailableProvider != null) {
    		Location lastKnownLocation = mLocationManager.getLastKnownLocation(mBestAvailableProvider);
    		return lastKnownLocation;
    	}
    	return null;
    }
    
    
    
    /**
     * Get the last saved location in db; this is necessary for the proper POI saving
     * @return
     */
    public Location getLastSavedLocation() {
    	return previousLocation;
    }
    
    
    /**
     * 
     */
    public void startTracking() {
    	
    	dbAdapter.open();
    	
    	// Determina il nome temporaneo della traccia, e viene salvata nel db
    	String trackName = "track-" + TouristExplorer.DATETIME_FORMAT.format(new Date());
    	String startDate = TouristExplorer.DATE_FORMAT.format(new Date());
    	String startTime = TouristExplorer.TIME_FORMAT.format(new Date());
    	    	
    	mCurrentTrack = dbAdapter.insertTrack(trackName, startDate, startTime);
		myApp.setCurrentTrack(mCurrentTrack);
		
		// Inizializzazione delle variabili relative al calcolo della velocità
		locationCounter = 0;
		avgSpeed = 0;
		totalSpeed = 0;
		totalDistance = 0;
		
    	isTrackingStarted = true;
    	getBestAvailableProvider();
		setSpeedFromMovingType(getMovingType());
		mLocationManager.requestLocationUpdates(mBestAvailableProvider, minUpdateTime, minUpdateDistance, this);
		
		
    }

    
    
    /**
     * 
     */
    public void stopTracking(){
    	
    	// Variabili per calcolare il tempo di percorrenza
    	String finishDate = TouristExplorer.DATE_FORMAT.format(new Date());
    	String finishTime = TouristExplorer.TIME_FORMAT.format(new Date());
    	
    	BigDecimal avgSpeedDecimal = new BigDecimal(avgSpeed);
    	avgSpeedDecimal = avgSpeedDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    	
    	totalDistance = totalDistance / 1000f;
    	BigDecimal totalDistanceDecimal = new BigDecimal(totalDistance);
    	totalDistanceDecimal = totalDistanceDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    	
        // Update nel db della traccia 
    	dbAdapter.updateTrack(myApp.getCurrentTrack().intValue(), finishDate, finishTime, avgSpeedDecimal.doubleValue(), totalDistanceDecimal.doubleValue());
    	
    	isTrackingStarted = false;
    	mLocationManager.removeUpdates(this);
    	previousLocation = null;
		  
        myApp.setAvgSpeed(avgSpeed);
        dbAdapter.close();
        
    }
    
    /**
     * Determines if there is an internet connection (Wi-Fi or Mobile network)
     * @return true if there is an internet connection, false otherwise
     */
    private boolean isNetworkAvailable() {
    	ConnectivityManager connectivityManager 
	          = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if(activeNetworkInfo==null){
	    	return false;
	    }
	    else {
	    	return activeNetworkInfo.isConnected();
	    }
    }
    
    
	@Override
	public void onLocationChanged(Location location) {
		
		Log.d(LOG_TAG, "onLocationChanged");
		
		// Se il location listener è presente, si determina la nuovo posizione
		if (myLocationListener != null) {
			
			myLocationListener.onLocationChanged(location);
			
			Log.i(LOG_TAG, "location: lat " + location.getLatitude() + ", lon " + location.getLongitude());
			
			LatLng actualLatLng = new LatLng(location.getLatitude(), location.getLongitude());
			
			// Si sposta la visualizzazione della mappa sulla posizione corrente
			if(mFollowMe){
				CameraUpdate cameraCenter = CameraUpdateFactory.newLatLng(actualLatLng);
				CameraUpdate cameraZoom = CameraUpdateFactory.zoomTo(TouristExplorer.ZOOM_DEFAULT);
				mMap.moveCamera(cameraCenter);
				mMap.animateCamera(cameraZoom);
			}

			// Se il tracking è iniziato
			if (isTrackingStarted) {
				
				locationCounter++;
				
				// Si determina la velocità media
				if (previousLocation == null) {
					previousLocation = location;
					previousLatLng = actualLatLng;
				}
				
				double currentSpeed = location.getSpeed() * 3.6;
				totalSpeed = totalSpeed + currentSpeed;
				avgSpeed = totalSpeed/locationCounter;
				
				// Se l'utente ha scelto il colore della traccia automatico
				if (automaticColor) {
					// Colore della velocità bassa: rosso
					if (currentSpeed <= mLowSpeed) {
						setLineColor(Color.RED);
						Log.d(LOG_TAG, "Automatic line color: red");
						polylineOptions = new PolylineOptions().add(previousLatLng, actualLatLng).width(5).color(mLineColor);
					}
					// Colore della velocità media: giallo
					if (currentSpeed > mLowSpeed && currentSpeed <= mMediumSpeed) {
						setLineColor(Color.YELLOW);
						Log.d(LOG_TAG, "Automatic line color: yellow");
						polylineOptions = new PolylineOptions().add(previousLatLng, actualLatLng).width(5).color(mLineColor);
					}
					// Colore della velocità alta: verde
					if (currentSpeed >= mMediumSpeed) {
						setLineColor(Color.GREEN);
						Log.d(LOG_TAG, "Automatic line color: green");
						polylineOptions = new PolylineOptions().add(previousLatLng, actualLatLng).width(5).color(mLineColor);
					}
					
				} 
				// Se l'utente ha non scelto il colore della traccia automatico, 
				// si colora la linea del tracking con il colore scelto dall'utente (default: nero)
				else {
					polylineOptions = new PolylineOptions().add(previousLatLng, actualLatLng).width(5).color(mLineColor);
				}
				
				// Si aggiunge la linea alla mappa
				mMap.addPolyline(polylineOptions);
				
				// Si calcola la distanza totale della traccia
				totalDistance += previousLocation.distanceTo(location);
				
				previousLocation = location;
				previousLatLng = actualLatLng;
				
				// Si inserisce la corrente posizione all'interno del database
				dbAdapter.insertLocation(previousLocation.getLatitude(),previousLocation.getLongitude(), 
						currentSpeed, mLineColor, myApp.getCurrentTrack().intValue());
				
			}
		}
	}
    
	
	@Override
    public void onProviderDisabled(String provider) {
    	Log.d(LOG_TAG, "onProviderDisabled: " + provider);
    }
    
	
    @Override
    public void onProviderEnabled(String provider) {
    	Log.d(LOG_TAG, "onProviderEnabled: " + provider);
    }
    
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    
    @Override
    public void activate(OnLocationChangedListener listener) {
        // We need to keep a reference to my-location layer's listener so we can push forward
        // location updates to it when we receive them from Location Manager.
        myLocationListener = listener;

        Log.i("activate", "");
        // Request location updates from Location Manager
        if (mBestAvailableProvider != null) {
            mLocationManager.requestLocationUpdates(mBestAvailableProvider, minUpdateTime, minUpdateDistance, this);
        } else {
            // TODO (Display a message/dialog) No Location Providers currently available.
        }
        

    }

    
    @Override
    public void deactivate() {
        // Remove location updates from Location Manager
    	mLocationManager.removeUpdates(this);
        myLocationListener = null;
        Log.i("deactivate", "");
    }

    
    public String getAvailableProvider(){
    	return mBestAvailableProvider;
    }
    
    public void setMap(GoogleMap map){
    	mMap = map;
    }
    
    public void setLineColor(int color){
    	mLineColor = color;
	}
    
    public void setMovingType(int type){
    	movingType = type;
	}

    private int getMovingType(){
    	return movingType;
	}
	
    
    public void setAutomaticColor(boolean automatic){
    	automaticColor = automatic;
    }
    
    
    public boolean getAutomaticColor(){
    	return automaticColor;
	}
    
    
    /**
	 * Determines the low, medium and high speed according to the type of
	 * movement set by the user
	 * 
	 * @param type Moving type
	 */
    private void setSpeedFromMovingType(int type){
    	
    	switch(type){
    	
    	case TouristExplorer.WALK:
    		mLowSpeed = TouristExplorer.WALK_LOW_SPEED;
    		mMediumSpeed = TouristExplorer.WALK_MEDIUM_SPEED;
    		minUpdateDistance = TouristExplorer.WALK_UPDATE_DISTANCE;  
    		minUpdateTime = TouristExplorer.WALK_UPDATE_TIME; 
    		break;
    	
    	case TouristExplorer.BIKE: 
    		mLowSpeed = TouristExplorer.BIKE_LOW_SPEED;
    		mMediumSpeed = TouristExplorer.BIKE_MEDIUM_SPEED;
    		minUpdateDistance = TouristExplorer.BIKE_UPDATE_DISTANCE; 
    		minUpdateTime = TouristExplorer.BIKE_UPDATE_TIME; 
    		break;
    	
    	case TouristExplorer.VEHICLE: 
    		mLowSpeed = TouristExplorer.VEHICLE_LOW_SPEED;
    		mMediumSpeed = TouristExplorer.VEHICLE_MEDIUM_SPEED;
    		minUpdateDistance = TouristExplorer.VEHICLE_UPDATE_DISTANCE;  
    		minUpdateTime = TouristExplorer.VEHICLE_UPDATE_TIME; 
    		break;
    	
    	}
    }
    
    
    public void setFollowMe(boolean followMe){
    	mFollowMe = followMe;
    }    
    
   
}