package it.divito.touristexplorer.poi;

import it.divito.touristexplorer.R;
import it.divito.touristexplorer.TouristExplorer;
import it.divito.touristexplorer.Utils;

import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;


public class VideoActivity extends Activity {

	private String mPath;
    private String mDatetime;
    
    private Button mRecordButton;
    private Button mStopButton;
    
    private boolean isRecording = false;
    
    private Intent mIntent;

    private Parameters params;
    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
       
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_custom);
        
        mIntent = getIntent();
        // Create an instance of Camera
        mCamera = getCameraInstance();
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        params = mCamera.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_AUTO);
        params.setFlashMode(Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(params);
        
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.camera_video_buttons,
                 (ViewGroup)findViewById(R.id.buttons_camera));
        
        mRecordButton = (Button) v.findViewById(R.id.button_record);
	    mStopButton = (Button) v.findViewById(R.id.button_stop);
	    
    }
	
    
    public void record(View v){
    	
    	// Se si sta registrando, allora si ferma la registrazione
    	if (isRecording) {
            
            mMediaRecorder.stop();  
            releaseMediaRecorder(); 
            mCamera.lock();         

            mIntent.putExtra("path", mPath);
            mIntent.putExtra("datetime", mDatetime);
            setResult(RESULT_OK, mIntent);
            finish();
        
    	} 
    	// Se non si sta registrando, allora si inizia la registrazione
    	else {
            // Inizializzazione della camera
            if (prepareVideoRecorder()) {
                // Inizia la registrazione
            	mMediaRecorder.start();

                mRecordButton.setEnabled(false);
                mStopButton.setEnabled(true);
                isRecording = true;
            } 
            // Se l'inizializzazione è fallita, la camera viene rilasciata
            else {
                releaseMediaRecorder();
            }
        }
    }
    
    
    /**
     * Stop the video registration
     * @param view
     */
    public void stop(View view){
    
    	 mMediaRecorder.stop();  
         releaseMediaRecorder(); 
         mCamera.lock();         

         mIntent.putExtra("path", mPath);
         mIntent.putExtra("datetime", mDatetime);
         setResult(RESULT_OK, mIntent);
         finish();
    
    }
    
    private boolean prepareVideoRecorder(){

         mMediaRecorder = new MediaRecorder();

         mCamera.unlock();
         mMediaRecorder.setCamera(mCamera);

         // Vengono impostate le opzioni del Media Recorder
         mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
         mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
         mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

         // Viene impostato l'output del video
         mDatetime = TouristExplorer.DATETIME_FORMAT.format(new Date());
         mPath = Utils.getOutputMediaFile(TouristExplorer.MEDIA_TYPE_VIDEO, mDatetime).toString();
         mMediaRecorder.setOutputFile(mPath);
         
         // Viene impostata la preview
         mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

         // Viene preparato il Media Recorder
         try {
             mMediaRecorder.prepare();
         } catch (IllegalStateException e) {
             releaseMediaRecorder();
             return false;
         } catch (IOException e) {
             releaseMediaRecorder();
             return false;
         }
         return true;
     }
  
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       
        releaseCamera();              
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

    
    /**
     * Release media recorder
     */
    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   
            mMediaRecorder.release(); 
            mMediaRecorder = null;
            mCamera.lock();          
        }
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