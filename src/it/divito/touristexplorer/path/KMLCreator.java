package it.divito.touristexplorer.path;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.TouristExplorer;
import it.divito.touristexplorer.database.DatabaseAdapter;
import it.divito.touristexplorer.model.Audio;
import it.divito.touristexplorer.model.Comment;
import it.divito.touristexplorer.model.Photo;
import it.divito.touristexplorer.model.Poi;
import it.divito.touristexplorer.model.Point;
import it.divito.touristexplorer.model.Video;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.util.Xml;


/**
 * This class allows you to create a KML file; this will contain the trace
 * performed, along with references to the POI for the track
 * 
 * @author Stefano Di Vito
 * 
 */

public class KMLCreator {
	
	private int mTrackID;
	private String mFilename;
	
	private XmlSerializer serializer;
	private StringWriter writer;
	
	private MyApplication myApp;
	private DatabaseAdapter dbAdapter;
	
	
	public KMLCreator(){
		
	}

	
	public KMLCreator(Context context, String filename, int trackID) {

		this.mTrackID = trackID;
		this.mFilename = filename;
		
		myApp = (MyApplication) context.getApplicationContext();

		dbAdapter = myApp.getDbAdapter();
		dbAdapter.open();
		
		// Scrive il file KML
		writeKML();
		
	}
	
	
	/**
	 * Write the KML file
	 */
	private void writeKML(){

		serializer = Xml.newSerializer();
		writer = new StringWriter();

		try {
			
			// Vengono scritte gli elementi iniziali del file KMZ
			serializer.setOutput(writer);
			serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output",
                    true);
			serializer.startDocument("UTF-8", true);
			serializer.setPrefix(" ", "http://www.opengis.net/kml/2.2");
			serializer.startTag("", "kml");
			serializer.startTag("", "Document");
			
			// Scrive il percorso
			writePath();
			
			// Scrive il punto iniziale
			writeStartPoint();

			// Scrive il punto finale
			writeFinishPoint();
			
			// Scrive i commenti
			writeComment();

			// Scrive i riferimenti alle immagini
			writeImage();

			// Scrive i riferimenti ai video
			writeVideo();

			// Scrive i riferimenti ai file audio
			writeAudio();

			
			// Scrive gli elementi di chiusura del file KML
			serializer.endTag("", "Document");
			serializer.endTag("", "kml");
			serializer.endDocument();

			write(writer.toString(), mFilename);
		

		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
	

	/**
	 * Write the path into KML file
	 */
	private void writePath(){

		boolean connectionLine = false;
		
		// Si ricavano le location della traccia
		Cursor cursorLocation = dbAdapter.selectLocation(mTrackID);
		ArrayList<Point> listPoint = new ArrayList<Point>();
		
		while(cursorLocation.moveToNext()) {
			
			Point point = new Point();
			
			Location location = new Location("aProvider");
			location.setLongitude(cursorLocation.getFloat(1));
			location.setLatitude(cursorLocation.getFloat(2));
			point.setLocation(location);
			
			point.setColor(cursorLocation.getInt(4));
			listPoint.add(point);
		}
		
		// Si inseriscono le singole posizioni nel file KML
		try {

			for (int i=0; i < listPoint.size()-1; i++){
				
				connectionLine = false;
				serializer.startTag("", "Placemark");
				serializer.startTag("", "LineString");
				serializer.startTag("", "coordinates");
				serializer.text(listPoint.get(i).getLocation().getLongitude() + "," + listPoint.get(i).getLocation().getLatitude() + " ");
				
				while( listPoint.get(i).getColor() == listPoint.get(i+1).getColor() && i+1<listPoint.size()-1){
					serializer.text(listPoint.get(i+1).getLocation().getLongitude() + "," + listPoint.get(i+1).getLocation().getLatitude() + " ");
					i++;
					connectionLine = true;
				}
				
				if(connectionLine)
					serializer.text(listPoint.get(i+1).getLocation().getLongitude() + "," + listPoint.get(i+1).getLocation().getLatitude() + " ");
				
				serializer.endTag("", "coordinates");
				serializer.endTag("", "LineString");
	
				// Stile linea (colore e spessore)
				serializer.startTag("", "Style");
				serializer.startTag("", "LineStyle");
				serializer.startTag("", "color");
				serializer.text(colorConvert(listPoint.get(i).getColor()));
				serializer.endTag("", "color");
				serializer.startTag("", "width");
				serializer.text("4");
				serializer.endTag("", "width");
				serializer.endTag("", "LineStyle");
				serializer.endTag("", "Style");
				serializer.endTag("", "Placemark");
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 

	}
	
	
	/**
	 * Write start point into KML file
	 */
	private void writeStartPoint(){
		
		// Si ricavano le location della traccia
		Cursor cursorLocation = dbAdapter.selectFirstLocation(mTrackID);
		
		try{
			
			if (cursorLocation.moveToNext()) {
				setIcon(TouristExplorer.START_PLACEMARK_CODE);
				
				serializer.startTag("", "Placemark");
				
				// Inserimento titolo
				serializer.startTag("", "name");
				serializer.text("Punto di partenza");
				serializer.endTag("", "name");
				
				// Inserimento marker commento
				serializer.startTag("", "styleUrl");
				serializer.text(TouristExplorer.START_MARK);
				serializer.endTag("", "styleUrl");
	
				serializer.startTag("", "Point");
				serializer.startTag("", "coordinates");
				serializer.
					text(cursorLocation.getString(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LONGITUDE))
						+ "," + (cursorLocation.getString(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LATITUDE))));
								
				serializer.endTag("", "coordinates");
				serializer.endTag("", "Point");
				serializer.endTag("", "Placemark");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		
	}
	
	
	/**
	 * Write finish point into KML file
	 */
	private void writeFinishPoint() {

		// Si ricavano le location della traccia
		Cursor cursorLocation = dbAdapter.selectLastLocation(mTrackID);

		try {

			if (cursorLocation.moveToNext()) {
				setIcon(TouristExplorer.FINISH_PLACEMARK_CODE);

				serializer.startTag("", "Placemark");

				// Inserimento titolo
				serializer.startTag("", "name");
				serializer.text("Punto di arrivo");
				serializer.endTag("", "name");

				// Inserimento marker commento
				serializer.startTag("", "styleUrl");
				serializer.text(TouristExplorer.FINISH_MARK);
				serializer.endTag("", "styleUrl");

				serializer.startTag("", "Point");
				serializer.startTag("", "coordinates");
				serializer
						.text(cursorLocation.getString(cursorLocation
								.getColumnIndex(TouristExplorer.COLUMN_LONGITUDE))
								+ ","
								+ (cursorLocation.getString(cursorLocation
										.getColumnIndex(TouristExplorer.COLUMN_LATITUDE))));

				serializer.endTag("", "coordinates");
				serializer.endTag("", "Point");
				serializer.endTag("", "Placemark");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	/**
	 * Write comments into KML file
	 */
	private void writeComment(){

		// Si ricavano le location della traccia
		Cursor cursorLocation = dbAdapter.selectLocation(mTrackID);
		ArrayList<Point> listComment = new ArrayList<Point>();
		
		// Per ogni posizione, si controlla se c'è un commento; 
		// se si, viene inserito nel file KML
		while(cursorLocation.moveToNext()) {
			
			Cursor cursorComment = dbAdapter.selectComment(cursorLocation.getInt(0));
			if(cursorComment.moveToNext()){
				
				Poi comment = new Comment();
				Point point = new Point();
				
				Location location = new Location("aProvider");
				location.setLongitude(cursorLocation.getFloat(1));
				location.setLatitude(cursorLocation.getFloat(2));
				point.setLocation(location);
				
				comment.setTitle(cursorComment.getString(1));
				comment.setDescription(cursorComment.getString(2));
								
				point.addPoi(comment);
				
				listComment.add(point);
			
			}
		}
		
		try{
			for (Point commentPoint : listComment) {
				
				ArrayList<Poi> commentPoi = commentPoint.getPoiList();
				
				for(Poi poi : commentPoi) {
					
					if(poi instanceof Comment){
						// Inserisce l'icona del commento
						setIcon(TouristExplorer.COMMENT_PLACEMARK_CODE);
		
						serializer.startTag("", "Placemark");
						
						// Inserimento titolo
						serializer.startTag("", "name");
						serializer.text(poi.getTitle());
						serializer.endTag("", "name");
						
						// Inserimento descrizione
						serializer.startTag("", "description");
						serializer.text(poi.getDescription());
						serializer.endTag("", "description");
						
						// Inserimento marker commento
						serializer.startTag("", "styleUrl");
						serializer.text(TouristExplorer.COMMENT_MARK);
						serializer.endTag("", "styleUrl");
		
						serializer.startTag("", "Point");
						serializer.startTag("", "coordinates");
						serializer.text(commentPoint.getLocation().getLongitude() + "," + commentPoint.getLocation().getLatitude());  
						serializer.endTag("", "coordinates");
						serializer.endTag("", "Point");
						serializer.endTag("", "Placemark");
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}


	/**
	 * Write the references to the images
	 */
	private void writeImage(){

		// Si ricavano le location della traccia
		Cursor cursorLocation = dbAdapter.selectLocation(mTrackID);
		ArrayList<Point> listPhoto = new ArrayList<Point>();
		
		// Per ogni posizione, si controlla se c'è una foto; 
		// se si, viene inserita nel file KML
		while(cursorLocation.moveToNext()) {
			
			Cursor cursorComment = dbAdapter.selectPhoto(cursorLocation.getInt(0));
			if(cursorComment.moveToNext()){
				
				Poi photo = new Photo();
				Point point = new Point();
				
				Location location = new Location("aProvider");
				location.setLongitude(cursorLocation.getFloat(1));
				location.setLatitude(cursorLocation.getFloat(2));
				point.setLocation(location);
				
				photo.setTitle(cursorComment.getString(1));
				photo.setDescription(cursorComment.getString(2));
				photo.setPath(cursorComment.getString(3));
				
				point.addPoi(photo);
				
				listPhoto.add(point);
			
			}
		}
		
		try{
			for (Point photoPoint : listPhoto) {
				
				ArrayList<Poi> listPhotoPoi = photoPoint.getPoiList();
				
				for(Poi poi : listPhotoPoi) {
					if(poi instanceof Photo){
						
						
						setIcon(TouristExplorer.IMAGE_PLACEMARK_CODE);
		
						serializer.startTag("", "Placemark");
						serializer.startTag("", "name");
						serializer.text(poi.getTitle());
						serializer.endTag("", "name");
						
						serializer.startTag("", "description");
						if(!poi.getDescription().equals("")){
							serializer.cdsect(poi.getDescription() + "<img " +
								"src=\"" + getRelativePath(poi.getPath()) + "\"/>");
						}
						else{
							serializer.cdsect("<img " +
									"src=\"" + getRelativePath(poi.getPath()) + "\"/>");
						}
						
						serializer.endTag("", "description");
						
						serializer.startTag("", "styleUrl");
						serializer.text(TouristExplorer.PHOTO_MARK);
						serializer.endTag("", "styleUrl");
						
						serializer.startTag("", "Point");
						serializer.startTag("", "coordinates");
						serializer.text(photoPoint.getLocation().getLongitude() + "," + photoPoint.getLocation().getLatitude());        
						
						serializer.endTag("", "coordinates");
						serializer.endTag("", "Point");
						serializer.endTag("", "Placemark");
						
						dbAdapter.insertPhotoFile(poi.getPath(), mTrackID);
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Write the references to the videos
	 */
	private void writeVideo(){

		Cursor cursorLocation = dbAdapter.selectLocation(mTrackID);
		ArrayList<Point> listVideo = new ArrayList<Point>();
		
		// Per ogni posizione, si controlla se c'è un video; 
		// se si, viene inserito nel file KML
		while(cursorLocation.moveToNext()) {
			
			Cursor cursorVideo = dbAdapter.selectVideo(cursorLocation.getInt(0));
			if(cursorVideo.moveToNext()){
				
				Poi video = new Video();
				Point point = new Point();
				
				Location location = new Location("aProvider");
				location.setLongitude(cursorLocation.getFloat(1));
				location.setLatitude(cursorLocation.getFloat(2));
				point.setLocation(location);
				
				video.setTitle(cursorVideo.getString(1));
				video.setDescription(cursorVideo.getString(2));
				video.setPath(cursorVideo.getString(3));
				
				point.addPoi(video);
				
				listVideo.add(point);
			
			}
		}
		
		try{
			for (Point videoPoint : listVideo) {
				ArrayList<Poi> listVideoPoi = videoPoint.getPoiList();
				
				for(Poi poi : listVideoPoi) {
					if(poi instanceof Video){
						//Inserisce l'icona dell'immagine
						setIcon(TouristExplorer.VIDEO_PLACEMARK_CODE);

						serializer.startTag("", "Placemark");
						serializer.startTag("", "name");
						serializer.text(poi.getTitle());
						serializer.endTag("", "name");
						
						serializer.startTag("", "description");
						
						if(!poi.getDescription().equals("")){
							serializer.cdsect(poi.getDescription() + " " +
								insertMediaFile(getRelativePath(poi.getPath())));
						}
						else{
							serializer.cdsect(insertMediaFile(getRelativePath(poi.getPath())));
						}
						
						serializer.endTag("", "description");
						
						serializer.startTag("", "styleUrl");
						serializer.text(TouristExplorer.VIDEO_MARK);
						serializer.endTag("", "styleUrl");
						
						serializer.startTag("", "Point");
						serializer.startTag("", "coordinates");
						serializer.text(videoPoint.getLocation().getLongitude() + "," + videoPoint.getLocation().getLatitude());        
						
						serializer.endTag("", "coordinates");
						serializer.endTag("", "Point");
						serializer.endTag("", "Placemark");
						
						dbAdapter.insertVideoFile(poi.getPath(), mTrackID);
					
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Write the references to the audio files
	 */
	private void writeAudio(){

		Cursor cursorLocation = dbAdapter.selectLocation(mTrackID);
		ArrayList<Point> listAudio = new ArrayList<Point>();
		
		// Per ogni posizione, si controlla se c'è un file audio; 
		// se si, viene inserito nel file KML
		while(cursorLocation.moveToNext()) {
			
			Cursor cursorAudio = dbAdapter.selectAudio(cursorLocation.getInt(0));
			if(cursorAudio.moveToNext()){
				
				Poi audio = new Audio();
				Point point = new Point();
				
				Location location = new Location("aProvider");
				location.setLongitude(cursorLocation.getFloat(1));
				location.setLatitude(cursorLocation.getFloat(2));
				point.setLocation(location);
				
				audio.setTitle(cursorAudio.getString(1));
				audio.setDescription(cursorAudio.getString(2));
				audio.setPath(cursorAudio.getString(3));
				
				point.addPoi(audio);
				
				listAudio.add(point);
			
			}
		}
		
		try{
			for (Point audioPoint : listAudio) {
				ArrayList<Poi> listAudioPoi = audioPoint.getPoiList();
				
				for(Poi poi : listAudioPoi) {
					if(poi instanceof Audio){
						//Inserisce l'icona dell'immagine
						setIcon(TouristExplorer.AUDIO_PLACEMARK_CODE);
		
						serializer.startTag("", "Placemark");
						serializer.startTag("", "name");
						serializer.text(poi.getTitle());
						serializer.endTag("", "name");
						
						serializer.startTag("", "description");
						
						if(!poi.getDescription().equals("")){
							serializer.cdsect(poi.getDescription() + " " +
								insertMediaFile(getRelativePath(poi.getPath())));
						}
						else{
							serializer.cdsect(insertMediaFile(getRelativePath(poi.getPath())));
						}
						
						serializer.endTag("", "description");
						
						serializer.startTag("", "styleUrl");
						serializer.text(TouristExplorer.AUDIO_MARK);
						serializer.endTag("", "styleUrl");
						
						serializer.startTag("", "Point");
						serializer.startTag("", "coordinates");
						serializer.text(audioPoint.getLocation().getLongitude() + "," + audioPoint.getLocation().getLatitude());        
						
						serializer.endTag("", "coordinates");
						serializer.endTag("", "Point");
						serializer.endTag("", "Placemark");
		
						//serve per poi ricavare il tutto per il file kmz
						dbAdapter.insertAudioFile(poi.getPath(), mTrackID);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Insert the video or audio tags into KML file 
	 * @param file the audio o video file
	 * @return code to insert into KML file
	 */
	private String insertMediaFile(String file){

		XmlSerializer s = Xml.newSerializer();
		StringWriter w = new StringWriter();
		try {
			s.setOutput(w);
			s.startTag("", "object");

			s.startTag("", "embed");
			s.attribute("", "wmode", "transparent");
			s.attribute("", "type", "application/x-mplayer2");
			s.attribute("", "src", file);
			s.attribute("", "height", TouristExplorer.KML_VIDEO_HEIGHT);  //metti come variabile globale
			s.attribute("", "width", TouristExplorer.KML_VIDEO_WIDTH);	//metti come variabile globale
			s.endTag("", "embed");
			s.endTag("", "object");
			s.endDocument();

			return w.toString();
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}



		return "";
	}

	
	/**
	 * Sets icon accorting to type of placemark
	 * @param value type of POI
	 */
	private void setIcon(int type){

		String icon = "";
		String mark = "";
		
		switch(type){
		case TouristExplorer.COMMENT_PLACEMARK_CODE:
			icon = TouristExplorer.COMMENT_PLACEMARK;
			mark = "comment_mark";
			break;
		case TouristExplorer.IMAGE_PLACEMARK_CODE:
			icon = TouristExplorer.IMAGE_PLACEMARK;
			mark = "photo_mark";
			break;
		case TouristExplorer.VIDEO_PLACEMARK_CODE:
			icon = TouristExplorer.VIDEO_PLACEMARK;
			mark = "video_mark";
			break;
		case TouristExplorer.AUDIO_PLACEMARK_CODE:
			icon = TouristExplorer.AUDIO_PLACEMARK;
			mark = "audio_mark";
			break;
		case TouristExplorer.START_PLACEMARK_CODE:
			icon = TouristExplorer.START_PLACEMARK;
			mark = "start_mark";
			break;
		case TouristExplorer.FINISH_PLACEMARK_CODE:
			icon = TouristExplorer.FINISH_PLACEMARK;
			mark = "finish_mark";
			break;			
		}

		try {
			serializer.startTag("", "Style");
			serializer.attribute("", "id", mark); 
			serializer.startTag("", "IconStyle");
			serializer.startTag("", "scale");
			serializer.text(TouristExplorer.PLACEMARK_SCALE);
			serializer.endTag("", "scale");
			serializer.startTag("", "Icon");
			serializer.startTag("", "href");
			serializer.text(icon);
			serializer.endTag("", "href");
			serializer.endTag("", "Icon");
			serializer.endTag("", "IconStyle");
			serializer.endTag("", "Style");
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
	

	/** 
	 * Write KML in a file, and save it in memory
	 * @param data the KML code 
	 * @param filename the KML name
	 */
	public void write(String data, String filename){ 

		FileWriter fw = null; 
		BufferedWriter bw = null; 
		
		File file = new File(TouristExplorer.TOURIST_EXPLORER_PATH, filename + ".kml");
		
		dbAdapter.insertKMLFile(file.getAbsolutePath(), mTrackID);
		
		try{ 
			fw = new FileWriter(file, false);
			bw = new BufferedWriter(fw); 
			bw.write(data); 
			bw.close(); 
			fw.close();   
		} 
		catch (IOException e) {       
			e.printStackTrace();  
		} 
	}

	
	private String colorConvert(int n){
		
		String.format("#%X", n); 
		String htmlColor = Integer.toHexString(n);
		String kmlColor = htmlColor.substring(2, htmlColor.length());
		return "50" + kmlColor.substring(4,6) + kmlColor.substring(2,4) + kmlColor.substring(0,2);
	
	}
	
	
	/**
	 * Get the relative path
	 * @param path the absolute path of a file
	 * @return the relative path of a file
	 */
	private String getRelativePath(String path){
		return path.substring(29, path.length());
	}
	
}
