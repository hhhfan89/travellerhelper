package it.divito.touristexplorer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.GoogleMap.InfoWindowAdapter;
import pl.mg6.android.maps.extensions.GoogleMap.OnMarkerClickListener;
import pl.mg6.android.maps.extensions.Marker;
import pl.mg6.android.maps.extensions.MarkerOptions;


/**
 * This class allows for the creation and display of the marker on the map,
 * indicating POIs recorded by the user during the tracking
 * 
 * @author Stefano Di Vito
 * 
 */

public class CustomMarkerOptions {
	
	private int mIcon; 				//icona del marker			
	private int mRequestType;		//tipo di richiesta, determinerà l'icona del marker
	private int mId = -1;			//id del POI
	private String mPath;			//percorso dove è salvato il POI
	private String mDatetime;		//data e ora del POI
	private String mHeader;			//header della preview del POI
	
	private GoogleMap mMap;			
	private Context mContext;
	
	private Marker mMarker;
	private MyApplication mMyApp;
	private ImageView mImageView;
	private Bitmap mImagePreview;
	
	private View mView;
	private TextView mTextView;
	private LayoutInflater mLayoutInflater;
	
	private boolean enableShowPathButton = false;	

	
	public CustomMarkerOptions(){
		
	}
	
	
	@SuppressWarnings("deprecation")
	public CustomMarkerOptions(final Context context, final int requestType, int id,
			Location location, String datetime, String path, boolean showingPath){
		
		this.mContext = context;
		this.mRequestType = requestType;
		this.mPath = path;
		this.mDatetime = datetime;
		this.mId = id;
		
		mTextView = new TextView(context);
		
		// Determina su quale mappa si andrà a posizionare il marker, se su
		// quella principale o su quella relativa alla visualizzazione della traccia
		if(showingPath){
			mMap = ((MyApplication)context.getApplicationContext()).pathMap;
		}
		else {
			mMap = ((MyApplication)context.getApplicationContext()).mainMap;
		}
		
		mMyApp = (MyApplication) context.getApplicationContext();
	
		setInfoWindowLayout();
		setMarkerContent();

		mMarker = mMap.addMarker(setMarkerOptions(location, false));
		
		setOnMarkerClick();
		
		mMarker.setData(this);
		mMyApp.addMarker(mMarker.getId(), mMarker);
				
	}
	
	
	/**
	 * Determines the layout of the preview frame for the selected marker
	 */
	private void setInfoWindowLayout(){
		
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mMap.setInfoWindowAdapter(new InfoWindowAdapter() {

	        // Utilizzo della finestra di default
	        @Override
	        public View getInfoWindow(Marker marker) {
	            return null;
	        }

	        // Definisce il contenuto della finestra
	        @Override
	        public View getInfoContents(Marker marker) {
	        	
				// Se il marker è un marker cluster, ovvero quello che racchiude
				// più marker in posizioni vicine, allora nella preview viene
				// visualizzato il numero di POI clusterizzati
	        	if (marker.isCluster()) {
	        		String title = "Ci sono " + marker.getMarkers().size() + " PoI";
	        		mTextView.setText(title);
                    return mTextView;
				}
	        	// Se il marker non è un cluster, in base al tipo di richiesta si 
	        	// determina il messaggio che si dovrà visualizzare nella preview:
	        	// se il marker si riferisce ad un'immagine/video, allora all'interno 
	        	// della finestra viene visualizzata anche un immagine di preview
	        	else{
	        		CustomMarkerOptions markerOpt = (CustomMarkerOptions) marker.getData();
		        	
	        		Log.e("markerOpt.mRequestType", ""+ markerOpt.mRequestType);
	        		switch(markerOpt.mRequestType){
		        	
	        		case TouristExplorer.START_POINT_REQUEST:
	        			mView = mLayoutInflater.inflate(R.layout.marker_window_general, null);
		        		break;
		        		
	        		case TouristExplorer.FINISH_POINT_REQUEST:
	        			mView = mLayoutInflater.inflate(R.layout.marker_window_general, null);
		        		break;
		        		
		        	case TouristExplorer.COMMENT_REQUEST:
		        		mView = mLayoutInflater.inflate(R.layout.marker_window_general, null);
		        		break;
		        		
		        	case TouristExplorer.AUDIO_REQUEST:
		        		mView = mLayoutInflater.inflate(R.layout.marker_window_general, null);
		        		break;
			        		
		        	case TouristExplorer.PHOTO_REQUEST:
		        		mView = mLayoutInflater.inflate(R.layout.marker_window_camera, null);
		        		mImageView = (ImageView) mView.findViewById(R.id.marker_info_image);
		        		mImageView.setImageBitmap(Bitmap.createScaledBitmap(markerOpt.mImagePreview, (int)(markerOpt.mImagePreview.getWidth()*0.3), (int)(markerOpt.mImagePreview.getHeight()*0.3), true));
		        		break;
			        	
		        	case TouristExplorer.VIDEO_REQUEST:
		        		mView = mLayoutInflater.inflate(R.layout.marker_window_camera, null);
		        		mImageView = (ImageView) mView.findViewById(R.id.marker_info_image);
		        		mImageView.setImageBitmap(Bitmap.createScaledBitmap(markerOpt.mImagePreview, (int)(markerOpt.mImagePreview.getWidth()*0.5), (int)(markerOpt.mImagePreview.getHeight()*0.5), true));
		        		break;
			        	
		        	}	
	            
		        	TextView headerView = (TextView) mView.findViewById(R.id.marker_info_header);
		        	headerView.setText(markerOpt.mHeader);
		            
		            TextView datetimeView = (TextView) mView.findViewById(R.id.marker_info_datetime);
		            datetimeView.setText("Ora: " + markerOpt.mDatetime);
		            
		            return mView;
	        	}
	        }
	    });
	}
	
	
	/**
	 * Determines the content of the preview frame for the selected marker
	 */
	private void setMarkerContent(){
		
		// Per ogni tipo di marker, viene impostata un icona, un header ed,
		// eventualmente, un'immagine di preview
		switch (mRequestType) {
		case TouristExplorer.PHOTO_REQUEST:
			mHeader = TouristExplorer.PHOTO_HEADER;
			mIcon = TouristExplorer.PHOTO_MARKER;
			mImagePreview = BitmapFactory.decodeFile(mPath);
			break;
		
		case TouristExplorer.VIDEO_REQUEST:
			mHeader = TouristExplorer.VIDEO_HEADER;
			mIcon = TouristExplorer.VIDEO_MARKER;
			mImagePreview = ThumbnailUtils.createVideoThumbnail(mPath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
			mImagePreview = Utils.setBitmapOrientation(mImagePreview, mPath);
			break;
		
		case TouristExplorer.AUDIO_REQUEST:
			mHeader = TouristExplorer.AUDIO_HEADER;
			mIcon = TouristExplorer.AUDIO_MARKER;
			break;
		
		case TouristExplorer.COMMENT_REQUEST:
			mHeader = TouristExplorer.COMMENT_HEADER;
			mIcon = TouristExplorer.COMMENT_MARKER;
			break;
			
		case TouristExplorer.START_POINT_REQUEST:
			mHeader = TouristExplorer.START_HEADER;
			mIcon = TouristExplorer.START_MARKER;
			break;
				
		case TouristExplorer.FINISH_POINT_REQUEST:
			mHeader = TouristExplorer.FINISH_HEADER;
			mIcon = TouristExplorer.FINISH_MARKER;
			break;
		
		}
		
	}
	
	
	
	/**
	 * Sets the options for the marker to be included on the map
	 * @param location coordinates of the marker
	 * @param isClustered indicates whether the marker is a cluster or not
	 * @return marker options
	 */
	public MarkerOptions setMarkerOptions(Location location, boolean isClustered){
		
		MarkerOptions markerOptions = new MarkerOptions();
		
		markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
		markerOptions.title(mPath);
		markerOptions.snippet("" + mRequestType);
		markerOptions.icon(BitmapDescriptorFactory.fromResource(mIcon));

		return markerOptions;
		
	}

	
	/**
	 * Specifies the action to be carried out once the user selects on the
	 * marker, and also that when you click on its preview
	 */
	private void setOnMarkerClick() {

		// Determina l'azione relativa alla selezione sul marker
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(final Marker marker) {
				
				// Se il marker non è un cluster
				if(!marker.isCluster()){
					CustomMarkerOptions markerOpt = (CustomMarkerOptions) marker.getData();
					
					// Se il marker rappresenta un POI immagine o video, allora
					// per centrare la preview del POI bisogna spostare
					// leggermente la visuale della mappa
					if(markerOpt.mRequestType==TouristExplorer.VIDEO_REQUEST || markerOpt.mRequestType==TouristExplorer.PHOTO_REQUEST){
						
						int container_height = mMyApp.getMapContainerHeight();
		
						Projection projection = mMap.getProjection();
		
						LatLng markerLatLng = new LatLng(marker.getPosition().latitude,
								marker.getPosition().longitude);
						Point markerScreenPosition = projection
								.toScreenLocation(markerLatLng);
						Point pointHalfScreenAbove = new Point(markerScreenPosition.x,
								markerScreenPosition.y - (container_height / 2));
		
						LatLng aboveMarkerLatLng = projection
								.fromScreenLocation(pointHalfScreenAbove);
		
						marker.showInfoWindow();
						
						CameraUpdate center = CameraUpdateFactory
								.newLatLng(aboveMarkerLatLng);
						mMap.moveCamera(center);
						
						return true;
					}
				}
				return false;
			}
		});

		// Determina l'azione relativa alla selezione sulla preview del marker
		mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onInfoWindowClick(Marker marker) {
				
				CustomMarkerOptions markerOptions = (CustomMarkerOptions) marker.getData();
				
				// Se il marker è un cluster, si determina la lista dei POI
				// inclusi nel marker cluster
				if (marker.isCluster()) {
					Intent i = new Intent(mContext, ListMarkerActivity.class);
					List<Marker> markers = marker.getMarkers();
					ArrayList<String> listPath = new ArrayList<String>();
					ArrayList<String> listMarker = new ArrayList<String>();
					ArrayList<Integer> listType = new ArrayList<Integer>();
					
					for(Marker m : markers){
						listPath.add(m.getTitle());
						listType.add(Integer.parseInt(m.getSnippet()));
						listMarker.add(m.getId());
					}
					
					i.putExtra("enableShowPath", enableShowPathButton);
					i.putStringArrayListExtra("path_array", listPath);
					i.putIntegerArrayListExtra("request_array", listType);
					i.putStringArrayListExtra("marker_array", listMarker);
					mContext.startActivity(i);
				}
				
				// Se il marker non è un cluster, si visualizza il POI;
				// nel caso in cui il marker si riferisca ad il punto
				// di partenza o di arrivo, non viene eseguita alcuna azione
				// alla selezione del marker
				else{
					if(markerOptions.mRequestType==TouristExplorer.START_POINT_REQUEST || markerOptions.mRequestType==TouristExplorer.FINISH_POINT_REQUEST){
					}
					else {
						Intent intent = new Intent(mContext, ShowPoiActivity.class);
						
						intent.putExtra("enableShowPath", enableShowPathButton);
						intent.putExtra("request", markerOptions.mRequestType);
						
						intent.putExtra("id", markerOptions.mId);
						intent.putExtra("marker", marker.getId());
						mContext.startActivity(intent);
					}
				}
			}
		});
		
		mMyApp.getMainMap();

	}
	
}