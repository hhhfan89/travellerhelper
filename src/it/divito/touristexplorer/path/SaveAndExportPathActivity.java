package it.divito.touristexplorer.path;

import android.app.Activity;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.R;
import it.divito.touristexplorer.database.DatabaseAdapter;

/**
 * This activity allows the user to save and export a track 
 * immediately after stop tracking
 * @author Stefano Di Vito
 *
 */
public class SaveAndExportPathActivity extends Activity {
	
	private MyApplication myApp; 
	private DatabaseAdapter dbAdapter;
	
	private EditText mEditTrackname;
	private EditText mEditKMZname;
	private Button buttonSave;
	private CheckBox checkBox;
	
	private Intent mIntent;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_save_and_export_path);
    	
    	mIntent = getIntent();
    	
    	mEditTrackname = (EditText) findViewById(R.id.save_and_export_edittext_track_name);
    	mEditKMZname = (EditText) findViewById(R.id.save_and_export_edittext_kmz_name);
    	buttonSave = (Button) findViewById(R.id.save_and_export_button_save);
    	checkBox = (CheckBox) findViewById(R.id.checkBox);
    	mEditTrackname.setText(getDefaultTrackname());
    	
    	myApp = ((MyApplication) this.getApplication());
    	dbAdapter = myApp.getDbAdapter();
    	
    	enableExportLayout(checkBox.isChecked());
			
    	checkBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				enableExportLayout(checkBox.isChecked());
			}
			
		});
		
	}
    
    
    
    /**
     * Change the layout of this activity if ths user checked the "Save and Export" checkbox
     * @param isChecked if true, the user can modify the name of KMZ file, and save it;
     * 					otherwise, the user can only modify and save the track name
     */
    private void enableExportLayout(boolean isChecked){
    	
    	LinearLayout layout = (LinearLayout) findViewById(R.id.save_and_export_layout_export);
		for (int i = 0; i < layout.getChildCount(); i++) {
		    View child = layout.getChildAt(i);
		    child.setEnabled(isChecked);
		}
		// Se la checkbox è sleezionata, allora l'utente può salvare 
		// ed esportare la traccia in un file KMZ; 
		// altrimenti può solo salvare la traccia, senza esportarla
		if(isChecked){
			buttonSave.setText("Salva & Esporta");
			mEditKMZname.setText(mEditTrackname.getText().toString());
		}
		else{
			buttonSave.setText("Salva");
			mEditKMZname.setText("");
		}
    }
    
    
    /**
     * Save the track and, if checkbox il selected, also the file KMZ
     * @param view the "Save" button
     */
    public void saveTrack(View v){
    	
    	dbAdapter.open();
		String trackname = mEditTrackname.getText().toString();
		dbAdapter.updateTrack(myApp.getCurrentTrack().intValue(), trackname);
		dbAdapter.close();
		
		if(checkBox.isChecked()){
			String KMZFilename = mEditKMZname.getText().toString();
			new KMZCreator(getApplicationContext(), KMZFilename, myApp.getCurrentTrack().intValue());
			Toast toast = Toast.makeText(getApplicationContext(), 
					"Traccia " + KMZFilename + " esportata con successo", Toast.LENGTH_LONG);  
			toast.show();
			
		}
		
		setResult(RESULT_OK, mIntent);
		finish();
	
    }
    
    
    /**
     * Listener for "Cancel" button
     * @param view the "Cancel" button
     */
    public void cancel(View view){
    	
    	dbAdapter.open();
		dbAdapter.deleteTrack(myApp.getCurrentTrack().intValue());
		dbAdapter.close();
		
    	Toast toast = Toast.makeText(getApplicationContext(), 
				R.string.message_path_not_saved, Toast.LENGTH_LONG);  
		toast.show();
				
		setResult(RESULT_CANCELED, mIntent);
    	finish();
    	
    }
    
    
    /**
     * Get default trackname
     * @return default trackname
     */
    private String getDefaultTrackname(){
    	return mIntent.getStringExtra("trackname");
	}
    
}
