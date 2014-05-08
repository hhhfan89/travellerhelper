package it.divito.touristexplorer.poi;

import it.divito.touristexplorer.R;
import it.divito.touristexplorer.TouristExplorer;
import it.divito.touristexplorer.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * This activity allows the user to save a photo, while is tracking his path
 * 
 * @author Stefano Di Vito
 *
 */
public class PhotoActivity extends Activity {

	private Intent intent;
	
    private Camera mCamera;
    private CameraPreview mPreview;
    
    private Button mButtonTakePicture;

    private File pictureFile;
    private String datetimeMarker;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.camera_photo_buttons,
                 (ViewGroup)findViewById(R.id.buttons_camera));
        
        mButtonTakePicture = (Button) v.findViewById(R.id.button_take);
        intent = getIntent();
        
        // Crea una nuova istanza della camera
        mCamera = getCameraInstance();
        // Crea la preview della camera, e la visualizza nell'activity
        mPreview = new CameraPreview(this, mCamera);
       
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        // Opzioni per la camera
        Parameters params = mCamera.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_AUTO);
        params.setFlashMode(Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(params);
        
        // Classe interna che gestisce il salvataggio della fotografia
        final PictureCallback mPicture = new PictureCallback() {

	        @Override
	        public void onPictureTaken(byte[] data, Camera camera) {
	        	
	        	Date date = new Date();
	        	String datetimeFile = TouristExplorer.DATETIME_FORMAT.format(date);
	    		datetimeMarker = TouristExplorer.DATETIME_MARKER_FORMAT.format(date);
	        	
	        	pictureFile = Utils.getOutputMediaFile(TouristExplorer.MEDIA_TYPE_IMAGE, datetimeFile);
	            if (pictureFile == null){
	                return;
	            }

	            try {
	                FileOutputStream fos = new FileOutputStream(pictureFile);
	                Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);

	                realImage = Utils.setBitmapOrientation(realImage, pictureFile.getAbsolutePath());

	                realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
	                fos.write(data);
	                fos.close();
	            } catch (Exception e) {
	            }
	        }
	   };
	
	
		// Listener del pulsante di scatto della fotografia
		mButtonTakePicture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get an image from the camera
				mCamera.takePicture(null, null, mPicture);

				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						intent.putExtra("path", pictureFile.getAbsolutePath());
						intent.putExtra("datetime", datetimeMarker);
						setResult(RESULT_OK, intent);
						finish();
					}
				}, 2000);
			}
		});

    }
	
    
    /**
     * Get an instance of the Camera
     * @return a instance of the Camera
     */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); 
        }
        catch (Exception e){
        }
        return c; 
    }
    
    
    @Override
    protected void onPause() {
        super.onPause();      
        releaseCamera();              
    }
    
    
    /**
     * Release the camera for other applications 
     */
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();       
            mCamera = null;
        }
    }
}