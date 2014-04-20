package it.divito.touristexplorer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;


/**
 * This class provides functionality used by different 
 * activities and classes of the application
 * 
 * @author Stefano Di Vito
 *
 */

public class Utils {
	
	
	/**
	 * Check if the directory is empty
	 * @param path the path of the directory
	 * @return true is the directory is empty, false otherwise 
	 */
	public static boolean isDirectoryEmpty(String path){ 
		
		File file = new File(path);
		if(file.list().length>0){
			return false;
		}
		return true;
		
	}
	
	
	
	/**
	 * Get files from the specified directory
	 * @param path the directory where to get the file
	 * @return the file list
	 */
	public static ArrayList<String> getFiles(String path) {
		
		ArrayList<String> fileList = new ArrayList<String>();
		
		try {
			File directory = new File(path);

			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				fileList.add(file.getPath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fileList;
	}
	
	
	
	/**
	 * Get the filenames of each file in the specified list
	 * @param list the list of files
	 * @return list of filenames
	 */
	public static ArrayList<String> getFilename(ArrayList<String> list) {
		ArrayList<String> filenameList = new ArrayList<String>();
		for(String path : list)
			filenameList.add(getFilename(path));
		return filenameList;
	}
	
	
	/**
	 * Get the filename of a file 
	 * @param path the path of the file
	 * @return the filename
	 */
	public static String getFilename(String path) {
		if(path.lastIndexOf('/') == -1) 
			return path;
		
		return path.substring(path.lastIndexOf('/')+1, path.lastIndexOf('.'));
	}
	
	
	/**
	 * Sets the orientation of a bitmap
	 * @param bitmap the image where get the orientation
	 * @param path the path of the image
	 * @return the rotated image
	 */
	public static Bitmap setBitmapOrientation(Bitmap bitmap, String path){
    	
    	ExifInterface exif;
		try {
			exif = new ExifInterface(path);
			Log.d("EXIF value", exif.getAttribute(ExifInterface.TAG_ORIENTATION));
	    	if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){
	    		bitmap = rotate(bitmap, 90);
		    } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
		    	bitmap = rotate(bitmap, 270);
		    } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
		    	bitmap = rotate(bitmap, 180);
		    } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")){
		    	bitmap = rotate(bitmap, 90);
		    
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return bitmap;
    
	}
    	
    
	/**
	 * Rotates the image
	 * @param bitmap the image to rotate
	 * @param degree how many degrees to rotate the image
	 * @return the rotated image
	 */
    public static Bitmap rotate(Bitmap bitmap, int degree) {
    	Log.d("bitmap", bitmap.toString());
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.setRotate(degree);
        Log.d("rotate", bitmap.toString());
        
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }
    
    
    
	/**
	 * Scales the bitmap
	 * @param bitmap the bitmap to scale
	 * @param scalingFactor the scaling factor
	 * @return the bitmap scaled
	 */
	public static Bitmap scaleBitmap(Bitmap bitmap, float scalingFactor) {
        int scaleHeight = (int) (bitmap.getHeight() * scalingFactor);
        int scaleWidth = (int) (bitmap.getWidth() * scalingFactor);

        return Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);
    }
	
	  
	/**
	 * Create a file Uri for saving an image or video
     * @param type type of media to save
     * @param date the current date
     * @return the uri of the new media file
     */
    public static Uri getOutputMediaFileUri(int type, String date){
          return Uri.fromFile(getOutputMediaFile(type, date));
    }

    
    /**
     * Create a File for saving an image or video
     * @param type  type of file to create
     * @param the current date
     * @return the file just created
     */
    public static File getOutputMediaFile(int type, String date){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

    	//String newDate = TouristExplorer.DATETIME_FORMAT.format(new Date());
		File mediaFile = null;
		if (type == TouristExplorer.MEDIA_TYPE_IMAGE){
	        mediaFile = new File(TouristExplorer.IMAGE_PATH + File.separator +
	        "IMG_"+ date + ".jpeg");
	    } 
		else if(type == TouristExplorer.MEDIA_TYPE_VIDEO) {
	    	mediaFile = new File(TouristExplorer.VIDEO_PATH + File.separator +
	    			"VID_" + date + ".mp4");
	    } 
		else if(type == TouristExplorer.MEDIA_TYPE_AUDIO) {
	    	mediaFile = new File(TouristExplorer.AUDIO_PATH + File.separator +
	    			"AUD_" + date + ".3gp");
		}
		
		return mediaFile;
    }
	
}
