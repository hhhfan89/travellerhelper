package it.divito.touristexplorer.path;

import android.app.Activity;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.View;
import android.content.Intent;

import it.divito.touristexplorer.R;


/**
 * This activity allows you to export in KMZ file 
 * the track just ended, or a previously saved track.
 * The KMZ file allows to see the track (with POIs) on
 * Google Earth and/or Google Maps
 * 
 * @author Stefano Di Vito
 *
 */

public class ExportPathActivity extends Activity {
	   
	private EditText mEditFilename;		// nome del file KMZ da esportare
	private String mDefaultName;		// nome di default per il salvataggio del file KMZ
	private int mTrackID;				
	
	private Intent mIntent;
	
	
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_export_path);
    	
    	mEditFilename = (EditText) findViewById(R.id.edittext_filename);
    	
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
	    
		mIntent = getIntent();
		mDefaultName = mIntent.getStringExtra("name");
    	mTrackID = mIntent.getIntExtra("trackID", -1);
    	
    	mEditFilename.setText(mDefaultName);
		
    }
    
    
    /**
     * Export the selected track in a KMZ file
     * @param view
     */
    public void exportPath(View view){
    	
    	String KMZFilename = mEditFilename.getText().toString();
		new KMZCreator(ExportPathActivity.this, KMZFilename, mTrackID);
		Toast toast = Toast.makeText(getApplicationContext(), 
				"Traccia " + KMZFilename + " esportata con successo", Toast.LENGTH_LONG);  
		toast.show();
		
		finish();
				    	
    }
    
    
    /**
     * Return to previous activity without export track
     * @param view
     */
    public void cancel(View v){
    	
    	Toast toast = Toast.makeText(getApplicationContext(), 
				R.string.message_path_not_exported, Toast.LENGTH_LONG);  
		toast.show();
		finish();
		
    }

}