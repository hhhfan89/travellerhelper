package it.divito.touristexplorer.poi;

import java.util.Date;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.R;
import it.divito.touristexplorer.TouristExplorer;
import it.divito.touristexplorer.database.DatabaseAdapter;

import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;


/**
 * This activity allows the user to save a comment, while is tracking his path
 * 
 * @author Stefano Di Vito
 *
 */
public class CommentActivity extends Activity {

	private EditText mEditTitle;
	private EditText mEditDescription;
	private Button mButtonSave;
	private Button mButtonCancel;

	private DatabaseAdapter dbAdapter;
	
	private Location mLocation = null;
	private Intent mIntent;
	
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_comment);
	    
	    mEditTitle = (EditText) findViewById(R.id.edt_comment_title);
		mEditDescription = (EditText) findViewById(R.id.edt_comment_description);
	    
	    mButtonSave = (Button) findViewById(R.id.button_save);
	    mButtonCancel = (Button) findViewById(R.id.button_cancel); 
	    
	    mIntent = getIntent();    
		mLocation = (Location) mIntent.getParcelableExtra("location");
		
		MyApplication myApp = ((MyApplication)this.getApplication());
		dbAdapter = myApp.getDbAdapter();
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		
		// Listener del pulsante di salvataggio del commento
		mButtonSave.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) { 
				
				Date date = new Date();
				
				String title = mEditTitle.getText().toString();
				String description = mEditDescription.getText().toString();
				String datetime = TouristExplorer.DATETIME_MARKER_FORMAT.format(date);
				String path = TouristExplorer.DATETIME_FORMAT.format(date);
				int id = insertIntoDB(title, description, "COMMENT_" + path, datetime);
				
				mIntent.putExtra("id", id);
				mIntent.putExtra("location", mLocation);
				mIntent.putExtra("title", title);
				mIntent.putExtra("description", description);
				mIntent.putExtra("datetime", datetime);
				mIntent.putExtra("path", "COMMENT_" + path);
				
				setResult(RESULT_OK, mIntent);

				finish();
				
			}
		
		});
	   
		// Listener del pulsante di eliminazione del commento
	    mButtonCancel.setOnClickListener(new View.OnClickListener() {

	    	@Override
	    	public void onClick(View arg0) { 

				setResult(RESULT_CANCELED, mIntent);
	    		finish();
	    	}

	    });
	    
	}
	
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED, mIntent);
		finish();
	}
	
	
	/**
     * Insert the comment informations into db, after saving the comment
     * @param title title of the audio track
     * @param description description of the audio track
     * @param datetime datetime of the audio track
     * @return the row number of the new insert
     */
	private int insertIntoDB(String title, String description, String path, String datetime){
		
		Cursor cursorLocation = dbAdapter.selectLastLocation(mLocation.getLongitude(), mLocation.getLatitude());
		int i = cursorLocation.getCount();
		
		if(i >= 1) {
			cursorLocation.moveToFirst();
			Long d = dbAdapter.insertComment(title, description, path, datetime, cursorLocation.getInt(0));
			return d.intValue();
		}
		return -1;
		
	} 

}
