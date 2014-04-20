package it.divito.touristexplorer;

import android.app.Activity;
import android.widget.TextView;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.database.Cursor;

import it.divito.touristexplorer.database.DatabaseAdapter;


/**
 * This activity allows you to view details about a selected POI, that is 
 * its position, title, description, date and time of its registration
 * 
 * @author Stefano Di Vito
 * 
 */
public class PoiDetailsActivity extends Activity {

	private int mRequest;			// tipo di POI
	private int mLocationId;		// id della location, presnte nel database
	
	private String mPath;			// percorso del file del POI
	private String mTitle;			// titolo del POI
	private String mDescription;	// descrizione del POI
	private String mLatitude;		// latitudine del POI
	private String mLongitude;		// longitudine del POI
	private String mDatetime;		// data e ora del POI
	
	private TextView titleTextView;
	private TextView descriptionTextView;
	private TextView locationTextView;
	private TextView datetimeTextView;
	
	private Intent mIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.poi_details);
		
		mIntent = getIntent();
		mPath = mIntent.getStringExtra("path");
		mRequest = mIntent.getIntExtra("request", -1); 
		
		titleTextView = (TextView) findViewById(R.id.marker_info_title_text);
		descriptionTextView = (TextView) findViewById(R.id.marker_info_description_text);
		locationTextView = (TextView) findViewById(R.id.marker_info_location_text);
		datetimeTextView = (TextView) findViewById(R.id.marker_info_datetime_text);

		MyApplication myApp = (MyApplication) this.getApplicationContext();
		DatabaseAdapter dbAdapter = myApp.getDbAdapter();

		dbAdapter.open();
		
		// Query per ricavare dal db tutte le informazioni relative al POI selezionato
		Cursor poiCursor = dbAdapter.selectPoi(mPath, mRequest);
		if(poiCursor.moveToNext()){
			mTitle = poiCursor.getString(poiCursor.getColumnIndex(TouristExplorer.COLUMN_TITLE));
			mDescription = poiCursor.getString(poiCursor.getColumnIndex(TouristExplorer.COLUMN_DESCRIPTION));
			mDatetime = poiCursor.getString(poiCursor.getColumnIndex(TouristExplorer.COLUMN_DATETIME));
			mLocationId = poiCursor.getInt(poiCursor.getColumnIndex(TouristExplorer.COLUMN_LOCATION));
		}
		
		// Query per ricavare dal db la location relativa al POI selezionato
		Cursor cursorLocation = dbAdapter.selectLocationFromID(mLocationId);
		if(cursorLocation.moveToNext()){
			mLatitude = cursorLocation.getString(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LATITUDE));
			mLongitude = cursorLocation.getString(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LONGITUDE));
		}
		
		getDetails();
		
	}
	
	
	/**
	 * Insert the information of the POI in TextViews, for their display to the user
	 */
	private void getDetails(){
		
		// Se non c'è titolo, viene inserito nella textview "Nessun titolo"
		if(mTitle.length()==0){
			mTitle = TouristExplorer.NO_TITLE;
		}
		
		// Se non c'è descrizione, viene inserito nella textview "Nessuna descrizione"
		if(mDescription.length()==0){
			mDescription = TouristExplorer.NO_DESCRIPTION;
		}
		
		titleTextView.setText(mTitle);
		descriptionTextView.setText(mDescription);
	    locationTextView.setText(mLatitude + "," + mLongitude);
	    datetimeTextView.setText(mDatetime);
	   
	}
	
	
	/**
	 * Listener of "Ok" button
	 * @param view the button
	 */
	public void ok(View view) {
		finish();
	}
	
}