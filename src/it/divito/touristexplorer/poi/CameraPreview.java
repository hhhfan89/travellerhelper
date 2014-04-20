package it.divito.touristexplorer.poi;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * This class provides the camera preview, allowing the user 
 * to see what is going to photograph
 * 
 * @author Stefano Di Vito
 *
 */
@SuppressLint("ViewConstructor")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    
	private SurfaceHolder mHolder;
    private Camera mCamera;

    @SuppressWarnings("deprecation")
	public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
   
    }

    
    // The Surface has been created; now tell the camera where to draw the preview.
    public void surfaceCreated(SurfaceHolder holder) {
        try {
        	mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } 
        catch (IOException e) {
        }
    }

   
    // Manages the changes or rotations of the preview 
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        
    	// Se la preview non esiste
    	if (mHolder.getSurface() == null){
    		return;
        }

    	// Si ferma la preview
    	try {
            mCamera.stopPreview();
        } catch (Exception e){
        }

    	// SI avvia la preview con i nuovi settings
    	try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } 
        catch (Exception e){
        }
    }


	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
	}
}