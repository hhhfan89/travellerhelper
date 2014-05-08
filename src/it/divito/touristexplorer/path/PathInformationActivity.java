package it.divito.touristexplorer.path;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.R;
import it.divito.touristexplorer.TouristExplorer;
import it.divito.touristexplorer.database.DatabaseAdapter;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * This activity allows the display of the selected path information,
 * like start and finish date/time, distance, time, etc.
 * 
 * @author Stefano Di Vito
 *
 */

public class PathInformationActivity extends Activity {
	
	private int mTrackID;			// id della traccia
	
	private String mTrackName; 		// nome della traccia
	private String mStartAddress; 
	private String mFinishAddress; 
	private String mStartDate; 		// data di partenza 
	private String mStartTime; 		// ora di partenza
	private String mFinishDate;  	// data di arrivo
	private String mFinishTime; 	// ora di arrivo
	private String mTrackTime;
	private String mTrackDistance;
	private String mAvgSpeed; 
			
	private TextView mTotalDistanceTextView; 
	private TextView mStartAddressTextView;		
	private TextView mFinishAddressTextView; 
	private TextView mStartDatetimeTextView; 
	private TextView mFinishDatetimeTextView;
	private TextView mTrackTimeTextView;
	private TextView mAverageSpeedTextView;
	
	private Intent mIntent;
	private MyApplication myApp;
	private DatabaseAdapter dbAdapter;
	
	private ProgressDialog mProgressDialog;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.path_info);
	    
	    mStartAddressTextView = (TextView) findViewById(R.id.startAddressText);
	    mFinishAddressTextView = (TextView) findViewById(R.id.finishAddressText);
	    mStartDatetimeTextView = (TextView) findViewById(R.id.startDateTimeText);
	    mFinishDatetimeTextView = (TextView) findViewById(R.id.finishDateTimeText);
	    mTrackTimeTextView = (TextView) findViewById(R.id.trackTimeText);
	    mAverageSpeedTextView = (TextView) findViewById(R.id.averageSpeedText);
	    mTotalDistanceTextView = (TextView) findViewById(R.id.totalDistanceText);
	    
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
	    
	    mIntent = getIntent();    
		mTrackID = mIntent.getIntExtra("trackID", -1);
		boolean trackingJustStopped = mIntent.getBooleanExtra("saveTrack", false); 
		
		myApp = ((MyApplication) this.getApplication());
	    dbAdapter = myApp.getDbAdapter();
		
	    dbAdapter.open();
	    
	    // Se il tracking non è stato fermato, vuol dire che l'utente
	    // vuole visualizzare le informazioni di una traccia
	    // precedentemente salvata, e quindi sotto quest'ultime
	    // ci sarà un pulsante di "Ok"
	    if(!trackingJustStopped){
	    	LayoutInflater inflater = getLayoutInflater();
	    	inflater.inflate(
                 R.layout.activity_path_info_ok,
                 (ViewGroup)findViewById(R.id.layout_buttons));
        }
	    // Se il tracking è appena stato fermato, allora sotto le informazioni
	    // viene inserito un pulsante di salvataggio traccia
        else{
        	LayoutInflater inflater = getLayoutInflater();
             
        	inflater.inflate(
                    R.layout.activity_path_info_save,
                    (ViewGroup)findViewById(R.id.layout_buttons));
        }
	    
	    
	    // Vengono determinate tutte le informazioni 
	    Cursor cursorTrack = dbAdapter.selectTrack(mTrackID);
	    getPathInfoFromDb(cursorTrack);
	    
	    setInformationInLayout();
				
	}
	
	
	@Override
	public void onBackPressed(){
		
		setResult(RESULT_CANCELED, mIntent);
		finish();
		
	}

	
	/**
	 * Listener for "Cancel" button
	 * @param view the "Cancel" button
	 */
	public void cancel(View v){
    	
		setResult(RESULT_CANCELED, mIntent);
    	finish();
		
    }
	
	
	/**
	 * Listener for "Ok" button
	 * @param view the "Ok" button
	 */
	public void ok(View v){
    	
		setResult(RESULT_OK, mIntent);
    	finish();
		
    }
	
	
	/**
	 * Listener for "Save" button
	 * @param view the "Save" button
	 */
	public void save(View v){
    	
		Intent i = new Intent(PathInformationActivity.this, SaveAndExportPathActivity.class);
		i.putExtra("trackname", mTrackName);
		startActivityForResult(i, TouristExplorer.SAVE_PATH_REQUEST);
	
    }

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (data == null) {
			finish();
		}

		switch (requestCode) {
		
		case TouristExplorer.SAVE_PATH_REQUEST:
			setResult(resultCode, mIntent);
			finish();
			return;
		
		}
	
	}
	
	
	private void getPathInfoFromDb(Cursor cursorTrack){
	
		if(cursorTrack.moveToNext()){
			
			mTrackName = cursorTrack.getString(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_NAME));
			mStartAddress = cursorTrack.getString(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_START_ADDRESS));
			mFinishAddress = cursorTrack.getString(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_FINISH_ADDRESS));
			mStartDate = cursorTrack.getString(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_START_DATE));
			mStartTime = cursorTrack.getString(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_START_TIME));
			mFinishDate = cursorTrack.getString(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_FINISH_DATE));
			mFinishTime = cursorTrack.getString(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_FINISH_TIME));
			mTrackTime = getTrackTime();
			mTrackDistance = cursorTrack.getString(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_TOTAL_DISTANCE)) + " km";
			mAvgSpeed = cursorTrack.getDouble(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_AVG_SPEED)) + " km/h";
		
		}
		
		if(mStartAddress==null){
			getAddress(mTrackID, TouristExplorer.TYPE_ADDRESS_START);
		}else{
			mStartAddressTextView.setText(mStartAddress);
		}
		
		if(mFinishAddress==null){
			getAddress(mTrackID, TouristExplorer.TYPE_ADDRESS_FINISH);
		}else{
			mFinishAddressTextView.setText(mFinishAddress);
		}
		
	}
	
	
	private void setInformationInLayout(){
		
		setTitle(mTrackName);
		mStartAddressTextView.setText(mStartAddress);
		mFinishAddressTextView.setText(mFinishAddress);
		mStartDatetimeTextView.setText(mStartDate + " " + mStartTime.substring(0, 2) + ":" + mStartTime.substring(2, 4) + ":" + mStartTime.substring(4, 6));
		mFinishDatetimeTextView.setText(mFinishDate + " " + mFinishTime.substring(0, 2) + ":" + mFinishTime.substring(2, 4) + ":" + mFinishTime.substring(4, 6));
		mTrackTimeTextView.setText(mTrackTime);
		mTotalDistanceTextView.setText(mTrackDistance);
		mAverageSpeedTextView.setText(mAvgSpeed);
		
	}
	
	
	/**
	 * Get start address of the track
	 * @param trackID the id of the track
	 */
	private String getStartAddress(int trackID) {
		
		Location location = new Location("");
		dbAdapter.open();
		   
		Cursor cursorLocation = dbAdapter.selectFirstLocation(trackID);
		if (cursorLocation.moveToNext()) {
			location.setLatitude(cursorLocation.getDouble(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LATITUDE)));
			location.setLongitude(cursorLocation.getDouble(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LONGITUDE)));
		}
		
		// Determina le informazioni relative alla posizione di partenza della traccia,
		// grazie ad una classe messa a disposizione da Google (Geocoder)
		Geocoder geocoder = new Geocoder(this, Locale.ITALY);
		try {
	    	List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
		    
	    	// Alcune volte potrebbe non effettuare il download delle informazioni sul punto 
	    	// di partenza, quindi viene inserito "Indirizzo non disponibile"
	    	if(addresses.size()==0 || addresses==null){
		    	return "Indirizzo non disponibile";
	        }
	        else{
	        	Address obj = addresses.get(0);
		        String address = obj.getAddressLine(0);
		        String locality = obj.getLocality();
		        String country = obj.getCountryName();
		        String postalCode = obj.getPostalCode();
		        
		        if(postalCode==null){
		        	postalCode = "";
		        }
		        
		        dbAdapter.updateStartAddressTrack(trackID, address + "\n" + postalCode + " " + locality + "\n" + country);
		        		 	   
		        return address + "\n" + postalCode + " " + locality + "\n" + country;
		        
	        }
		}
	    catch (IOException e) {
	    	return "Indirizzo non disponibile";
	    }
	    	
	}
	
	
	/**
	 * Get finish address of the track
	 * @param trackID the id of the track
	 */
	private String getFinishAddress(int trackID) {
		
		Location location = new Location("");
				 
		Cursor cursorLocation = dbAdapter.selectLastLocation(trackID);
		if (cursorLocation.moveToNext()) {
			location.setLatitude(cursorLocation.getDouble(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LATITUDE)));
			location.setLongitude(cursorLocation.getDouble(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LONGITUDE)));
		}

		// Determina le informazioni relative alla posizione di arrivo della traccia,
		// grazie ad una classe messa a disposizione da Google (Geocoder)
		Geocoder geocoder = new Geocoder(this, Locale.ITALY);
		
		try {
			List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			
			// Alcune volte potrebbe non effettuare il download delle informazioni sul punto 
	    	// di arrivo, quindi viene inserito "Indirizzo non disponibile"
	    	if(addresses.size()==0){
	        	return "Indirizzo non disponibile";
	        }
	        else{
		        Address obj = addresses.get(0);
		        String address = obj.getAddressLine(0);
		        String locality = obj.getLocality();
		        String country = obj.getCountryName();
		        String postalCode = obj.getPostalCode();
		        
		        if(postalCode==null){
		        	postalCode = "";
		        }
		        
		        dbAdapter.updateFinishAddressTrack(trackID, address + "\n" + postalCode + " " + locality + "\n" + country);
		       
		        return address + "\n" + postalCode + " " + locality + "\n" + country;
		        
	        }
	       
	    } catch (IOException e) {
	    	return "Indirizzo non disponibile";
	    }	    
		
	}
	
	
	private String getTrackTime(){

		try {
			
			Date startDateTime = TouristExplorer.DATETIME_FORMAT.parse(mStartDate + "-" + mStartTime);
			Date finishDateTime = TouristExplorer.DATETIME_FORMAT.parse(mFinishDate + "-" + mFinishTime);
			long diff = finishDateTime.getTime() - startDateTime.getTime();
			long diffSeconds = diff / 1000 % 60;
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000);
			
			return diffHours + " ore " + diffMinutes + " min " + diffSeconds + " sec";
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
				
	}
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case TouristExplorer.TYPE_ADDRESS_START:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setTitle("Attendere");
			mProgressDialog.setMessage("Sto caricando l'indirizzo di partenza..");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			return mProgressDialog;
		case TouristExplorer.TYPE_ADDRESS_FINISH:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setTitle("Attendere");
			mProgressDialog.setMessage("Sto caricando l'indirizzo di arrivo..");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			return mProgressDialog;
		default:
			return null;
		}
	}	
	
	
	private void getAddress(int trackID, int addressType) {
		new GetAddressAsync(addressType).execute(trackID, addressType);
	}
	
	
	
	/**
	 * This inner class allows to download the information of start and finish address
	 * in a asynchronized way
	 * @author Stefano Di Vito
	 *
	 */
	class GetAddressAsync extends AsyncTask<Integer, String, String[]> {
		
		int count;
		int addressType;
		
		public GetAddressAsync(int addressType){
			this.addressType = addressType;
		}
		
		
		@SuppressWarnings("deprecation")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(addressType);
		}

		@Override
		protected String[] doInBackground(Integer... add) {

			String address[] = new String[2];
			
			switch (add[1]){
				
			case TouristExplorer.TYPE_ADDRESS_START:
				address[0] = Integer
						.toString(TouristExplorer.TYPE_ADDRESS_START);
				address[1] = getStartAddress(add[0]);
				break;

			case TouristExplorer.TYPE_ADDRESS_FINISH:
				address[0] = Integer
						.toString(TouristExplorer.TYPE_ADDRESS_FINISH);
				address[1] = getFinishAddress(add[0]);
				break;

			}
			
			return address;
		}

		
		protected void onProgressUpdate(String... progress) {
			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String[] address) {
			
			dismissDialog(Integer.parseInt(address[0]));
			switch (Integer.parseInt(address[0])){
			
			case TouristExplorer.TYPE_ADDRESS_START:
				mStartAddressTextView.setText(address[1]);
				break;
			
			case TouristExplorer.TYPE_ADDRESS_FINISH:
				mFinishAddressTextView.setText(address[1]);
				break;
			}
		}
	}
}
