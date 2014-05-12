package it.divito.touristexplorer.poi;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.R;
import it.divito.touristexplorer.TouristExplorer;
import it.divito.touristexplorer.Utils;
import it.divito.touristexplorer.database.DatabaseAdapter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * This activity allows the user to save a photo or a video, while is tracking his path
 * 
 * @author Stefano Di Vito
 *
 */

public class CameraActivity extends Activity {

	private Intent mIntent;
	private EditText mEditTitle;
	private EditText mEditDescription;
	private Button mButtonSave;
	private Button mButtonCancel;
	private ImageView mImageView;
	
	private String mPath;
	private String mRequest; 
	private Location location = null;
    private String datetime;
    
    private DatabaseAdapter dbAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        MyApplication myApp = ((MyApplication)this.getApplication());
		dbAdapter = myApp.getDbAdapter();
		
		mIntent = getIntent(); 
	    mRequest = mIntent.getStringExtra("request_type");
	    location = (Location) mIntent.getParcelableExtra("location");
		
	    mImageView = (ImageView) findViewById(R.id.img_video_preview);
		mEditTitle = (EditText) findViewById(R.id.edt_camera_title);
		mEditDescription = (EditText) findViewById(R.id.edt_camera_description);
		mButtonSave = (Button) findViewById(R.id.btn_camera_save);
		mButtonCancel = (Button) findViewById(R.id.btn_camera_cancel); 
		
		// A seconda del tipo di POI (foto o video) che l'utente
		// vuole registrare, verrà avviata l'activity richiesta. 
        if(mRequest.equals("video")){
	    	Log.d("request_type", "video");
	    	Intent videoIntent = new Intent(this, VideoActivity.class);
	    	startActivityForResult(videoIntent, TouristExplorer.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
	    }
	    if(mRequest.equals("photo")){
	    	Intent photoIntent = new Intent(this, PhotoActivity.class);
	    	startActivityForResult(photoIntent, TouristExplorer.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	    }
	    
	    // Listener per il pulsante di salvataggio
	    mButtonSave.setOnClickListener(new View.OnClickListener() {

	    	@Override
	    	public void onClick(View arg0) { 

				String title = mEditTitle.getText().toString();
				String description = mEditDescription.getText().toString();
				
				int id = insertIntoDB(mRequest, title, description, mPath, datetime);
				
				mIntent.putExtra("id", id);
				mIntent.putExtra("location", location);
				mIntent.putExtra("title", title);
				mIntent.putExtra("description", description);
				mIntent.putExtra("path", mPath);
				mIntent.putExtra("datetime", datetime);
				
				setResult(RESULT_OK, mIntent);
	    		finish();
	    	}

	    });
	   
	    // Listener per il pulsante di cancellazione
	    mButtonCancel.setOnClickListener(new View.OnClickListener() {

	    	@Override
	    	public void onClick(View arg0) { 

				setResult(RESULT_CANCELED, mIntent);
	    		finish();
	    	
	    	}

	    });
    
    }
   
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (data == null) {
			setResult(RESULT_CANCELED, mIntent);
			finish();
		}
		
		if (requestCode == TouristExplorer.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				mPath = data.getStringExtra("path");
				datetime = data.getStringExtra("datetime");
				Bitmap bitmap = BitmapFactory.decodeFile(mPath);
				mImageView.setImageBitmap(bitmap);
				setResult(RESULT_OK, mIntent);
			}
		}
		
		if (requestCode == TouristExplorer.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				mPath = data.getStringExtra("path");
				datetime = data.getStringExtra("datetime");
				Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mPath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
				mImageView.setImageBitmap(Utils.setBitmapOrientation(bitmap, mPath));
				setResult(RESULT_OK, mIntent);

			}
		}
	}
    
    
    /**
     * Insert the photo/video informations into db, after saving the photo/video
     * @param request type of POI (photo or video)
     * @param title title of the audio track
     * @param description description of the audio track
     * @param datetime datetime of the audio track
     * @param path filepath of the audio track
     * @return the row number of the new insert
     */
    private int insertIntoDB(String request, String title, String description, String path, String datetime){
		
    	Cursor cursorLocation = dbAdapter.selectLastLocation(location.getLongitude(), location.getLatitude());
    	
		int i = cursorLocation.getCount();
		
		if(i >= 1) {
			cursorLocation.moveToFirst();
			Long row = dbAdapter.insertMedia(request, title, description, path, datetime, cursorLocation.getInt(0));
			return row.intValue();
		}
		
		return -1;
	
    }
    
    
    @Override
	public void onBackPressed() {
    	
    	setResult(RESULT_CANCELED, mIntent);
		finish();
		
    }
    
}