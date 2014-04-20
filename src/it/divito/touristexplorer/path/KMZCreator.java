package it.divito.touristexplorer.path;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.TouristExplorer;
import it.divito.touristexplorer.database.DatabaseAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * This class allows you to create a KMZ file; this will contain the KML file
 * and all the files (photo, video, audio) references to the KML file
 * 
 * @author Stefano Di Vito
 * 
 */

public class KMZCreator {
	
	private String mKMZFilename;   		//da sistemare
	private Context mContext;
	private File mKMZFile;
	private int mTrackID;	
	
	private MyApplication myApp;
	private DatabaseAdapter dbAdapter;
	
	public KMZCreator(){
		
	}
	
	public KMZCreator(Context context, String KMZFilename, int trackID){
		
		this.mContext = context;
		this.mTrackID = trackID;
		this.mKMZFilename = KMZFilename;
		
		myApp = (MyApplication) context.getApplicationContext();
		dbAdapter = myApp.getDbAdapter();
		dbAdapter.open();
		
		mKMZFile = new File(TouristExplorer.KML_PATH);
		
		createKMLFile();
		createKMZFile();
		
	}
	
	
	/**
	 * Creates the KML file
	 */
	private void createKMLFile(){
		
		new KMLCreator(mContext, mKMZFilename, mTrackID);
	
	}
	
	
	/**
	 * Creates the KMZ file
	 */
	private void createKMZFile() {

		String KMLPath = getKMLFile(mTrackID);
		ArrayList<String> photoPath = getPhotoFile(mTrackID);
		ArrayList<String> videoPath = getVideoFile(mTrackID);
		ArrayList<String> audioPath = getAudioFile(mTrackID);

		ArrayList<String> pathList = new ArrayList<String>();

		pathList.add(KMLPath);
		pathList.addAll(photoPath);
		pathList.addAll(videoPath);
		pathList.addAll(audioPath);

		writeKMZFile(mKMZFilename, mKMZFile, pathList);
		
	}
	
	
	/**
	 * Get the KML file 
	 * @param trackID the id of the track
	 * @return the KML file
	 */
	public String getKMLFile(int trackID){
		
		String path = "";
		
		Cursor c = dbAdapter.selectKMLFile(trackID);
		if(c.moveToNext())
			path = c.getString(1);
		
		return path;
		
	}
	
	
	
	/**
	 * Gets photo files
	 * @param trackID the id of the track
	 * @return the list of photo of the track
	 */
	public ArrayList<String> getPhotoFile(int trackID){
		
		ArrayList<String> pathList = new ArrayList<String>();
		
		Cursor c = dbAdapter.selectLocation(trackID);
		while(c.moveToNext()){
			Cursor c1 = dbAdapter.selectPhoto(c.getInt(0));
			
			while(c1.moveToNext()){
				pathList.add(c1.getString(c1.getColumnIndex(TouristExplorer.COLUMN_PATH)));
			}
			
		}
		
		return pathList;
		
	}
	
	
	/**
	 * Gets video files
	 * @param trackID the id of the track
	 * @return the list of video of the track
	 */
	public ArrayList<String> getVideoFile(int trackID){
		
		ArrayList<String> pathList = new ArrayList<String>();
		
		Cursor c = dbAdapter.selectLocation(trackID);
		while(c.moveToNext()){
			Cursor c1 = dbAdapter.selectVideo(c.getInt(0));
			
			while(c1.moveToNext()){
				pathList.add(c1.getString(c1.getColumnIndex(TouristExplorer.COLUMN_PATH)));
			}
			
		}
		
		return pathList;
		
	}
	
	
	/**
	 * Gets audio files
	 * @param trackID the id of the track
	 * @return the list of video of the track
	 */
	public ArrayList<String> getAudioFile(int trackID){
		
		ArrayList<String> pathList = new ArrayList<String>();
		
		Cursor c = dbAdapter.selectLocation(trackID);
		while(c.moveToNext()){
			Cursor c1 = dbAdapter.selectAudio(c.getInt(0));
			
			while(c1.moveToNext()){
				pathList.add(c1.getString(c1.getColumnIndex(TouristExplorer.COLUMN_PATH)));
			}
			
		}
		
		return pathList;
		
	}
	
	
	/**
	 * Write and save KMZ file on memory
	 * @param name name of KMZ file
	 * @param KMZFile KMZ file
	 * @param fileList list of files related to KMZ file
	 */
	public static void writeKMZFile(String name, File KMZFile, List<String> fileList) {

		try {
			
			FileOutputStream fos = new FileOutputStream(KMZFile.getCanonicalPath() + File.separator + name + ".kmz");
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (String path : fileList) {
				addToKMZ(KMZFile, path, zos);
			}

			zos.close();
			fos.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	
	/**
	 * Add files to KMZ
	 * @param KMZFile the KMZ file
	 * @param path the path of the file to add
	 * @param zos
	 */
	public static void addToKMZ(File KMZFile, String path, ZipOutputStream zos) throws FileNotFoundException,
			IOException {

		String zipFilePath;
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);

		String ext = "";
		int i = path.lastIndexOf('.');
		if (i > 0) {
			ext = path.substring(i+1);
		}
		
		zipFilePath = file.getCanonicalPath().substring(TouristExplorer.TOURIST_EXPLORER_PATH.length() + 1,
				file.getCanonicalPath().length());
		
		ZipEntry zipEntry = new ZipEntry(zipFilePath);
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}
		
		zos.closeEntry();
		fis.close();

		if(ext.equals("kml")){
			file.delete();
		}
		
	}

	
	public static void compress(File f, String path, ZipOutputStream zos)
			throws IOException {
		
		boolean isDirectory = f.isDirectory();

		String nextPath = path + f.getName() + (isDirectory ? "/" : "");
		ZipEntry zipEntry = new ZipEntry(nextPath);
		zos.putNextEntry(zipEntry);

		if (isDirectory) {
			File[] child = f.listFiles();
			
			// ricorsione per ogni figlio
			for (int i = 0; i < child.length; i++) {
				compress(child[i], nextPath, zos);
			}
		}
		
		else if (f.isFile()) {
			Log.d("file", f.toString());
			FileInputStream fis = new FileInputStream(f);
			byte[] readBuffer = new byte[4096];
			int bytesIn = 0;
			
			while ((bytesIn = fis.read(readBuffer)) != -1) {
				zos.write(readBuffer, 0, bytesIn);
			}

			fis.close();

		}
		zos.closeEntry();
	}

}
