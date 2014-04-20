package it.divito.touristexplorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.media.ThumbnailUtils;

import it.divito.touristexplorer.database.DatabaseAdapter;
import it.divito.touristexplorer.path.ShowPathActivity;

import java.io.File;

import pl.mg6.android.maps.extensions.Marker;


/**
 * This activity allows the display of the POI, from where you can choose to
 * view the information, view its location on the map, edit, delete or
 * share it through other app (WhatsApp, Facebook, Twitter, etc.)
 * 
 * @author Stefano Di Vito
 * 
 */
public class ShowPoiActivity extends Activity {

	private int mId; 				// id del POI
	private int mRequest;			// tipo di POI
	private int mLocation;			// location del POI

	private String mPath;			// percorso del file del POI
	private String mTitle;			// titolo del POI
	private String mDescription;	// descrizione del POI
	private String mLatitude;		// latitudine del POI
	private String mLongitude;		// longitudine del POI
	private String mDatetime;		// data ed ora del POI
	private String mMarker;			// marker relativo al POI
	private String mShareType; 		// tipo di sharing
	private String mShareTitle;		// titolo della finestra di sharing
	
	private ImageView mImageView;
	private Button mButtonDetails;
	private Button mButtonShow;
	
	private Intent mIntent;
	
	private MyApplication myApp;
	private DatabaseAdapter dbAdapter;
	
	private LayoutInflater mLayoutInflater;
	private View mView;
	
	// variabili per visualizzare l'immagine o il player del file audio/video
	private CustomVideoView mVideoView;
	private Bitmap mPreview;
	private BitmapDrawable mPreviewDrawable;
	
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_poi);
		
		mButtonDetails = (Button) findViewById(R.id.button_details);
		mButtonShow = (Button) findViewById(R.id.button_show);
		
		mIntent = getIntent();
		mRequest = mIntent.getIntExtra("request", -1);
		mPath = mIntent.getStringExtra("path");
		mId = mIntent.getIntExtra("id", -1);
		mMarker = mIntent.getStringExtra("marker");
		
		myApp = ((MyApplication) this.getApplication());
		dbAdapter = myApp.getDbAdapter();
		dbAdapter.open();
		
		// Si seleziona il POI dall'id o dal path, a seconda dei parametri
		// derivanti dalle activity
		Cursor cursor; 
		if(mId!=-1){
			cursor = dbAdapter.selectPoi(mId, mRequest);
		}
		else{
			cursor = dbAdapter.selectPoi(mPath, mRequest);
		}
		
		// Se sono stati trovati dei POI, si prosegue; nel caso contrario, 
		// ovvero non c'è quel POI nel database (la traccia relativa è 
		// tata cancellata, ma i POI no), allora viene chiesto all'utente 
		// se vuole cancellare il file dalla memoria o meno
		if(cursor.getCount()>0){
			cursor.moveToFirst();
		}
		else{
			deleteConfirm(mPath);
		}
		
		// Si determinano le informazioni del POI
		getPoiInformation(cursor);
		
		mButtonShow.setEnabled((mIntent.getBooleanExtra("enableShowPathButton", false)));
			    
		// A seconda del tipo di POI, si avranno diversi layout 
		// per la presentazione di quest'ultimo
		switch(mRequest){
		
		// Se il POI è un video, viene inserito un riproduttore multimediale
		case TouristExplorer.VIDEO_REQUEST:
	
			mLayoutInflater = getLayoutInflater();
	    	mView = mLayoutInflater.inflate(R.layout.marker_info_video,
                 (ViewGroup)findViewById(R.id.preview_layout));
	    	
	    	// Viene inserito il riproduttore multimediale nel layout
	    	mVideoView = (CustomVideoView) mView.findViewById(R.id.videoView);
	    	mVideoView.setMediaController(new MediaController(this){
				
	    		@Override
				public void setMediaPlayer(MediaPlayerControl player) {
					super.setMediaPlayer(player);
				}
	    		
	    		
				@Override
				public void setAnchorView(View view) {
					super.setAnchorView(mVideoView);
				}

	            public boolean dispatchKeyEvent(KeyEvent event){
	                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
	                    ((Activity) getContext()).finish();

	                return super.dispatchKeyEvent(event);
	            }
	        });
	        
	    	// Listener dei pulsanti Play e Stop
	    	mVideoView.setPlayPauseListener(new CustomVideoView.PlayPauseListener() {

	    	    @Override
	    	    public void onPlay() {
	    	    	mVideoView.setBackgroundDrawable(null);
	    	    }

	    	    @Override
	    	    public void onPause() {
	    	    }
	    	    
	    	});

	    	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    	mVideoView.setLayoutParams(getLayoutParams(true));
            
	    	// Per la visualizzazione della preview del video
            mPreview = ThumbnailUtils.createVideoThumbnail(mPath,
                    MediaStore.Images.Thumbnails.MINI_KIND);
            mPreviewDrawable = new BitmapDrawable(mPreview);
            mVideoView.setBackgroundDrawable(mPreviewDrawable);
            
            mVideoView.setVideoPath(mPath);
			
			break;
			
		// Se il POI è un immagine, viene visualizzata nel layout
		case TouristExplorer.PHOTO_REQUEST:
			mLayoutInflater = getLayoutInflater();
	    	mView = mLayoutInflater.inflate(R.layout.marker_info_photo,
                 (ViewGroup)findViewById(R.id.preview_layout));
	    	
	    	mImageView = (ImageView) mView.findViewById(R.id.imageView);
	    	
            Bitmap photoPreview = BitmapFactory.decodeFile(mPath);
            setBitmap(photoPreview);
			break;
		
		// Se il POI è un file audio, viene inserito un riproduttore multimediale
		case TouristExplorer.AUDIO_REQUEST:
			
			mLayoutInflater = getLayoutInflater();
	    	mView = mLayoutInflater.inflate(R.layout.marker_info_video,
                 (ViewGroup)findViewById(R.id.preview_layout));
	    	
	    	// Viene inserito il riproduttore multimediale nel layout
			mVideoView = (CustomVideoView) mView.findViewById(R.id.videoView);
			mVideoView.setMediaController(new MediaController(this){
				
				@Override
				public void setMediaPlayer(MediaPlayerControl player) {
					super.setMediaPlayer(player);
				}
	    		
	    		@Override
				public void setAnchorView(View view) {
					super.setAnchorView(mVideoView);
				} 
	    		
				public boolean dispatchKeyEvent(KeyEvent event) {
					if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
						((Activity) getContext()).finish();

					return super.dispatchKeyEvent(event);
				}
				
	        });
	        
			// Listener dei pulsanti Play e Stop
			mVideoView.setPlayPauseListener(new CustomVideoView.PlayPauseListener() {

	    	    @Override
	    	    public void onPlay() {
	    	    	System.out.println("Play!");
	    	    }

	    	    @Override
	    	    public void onPause() {
	    	        System.out.println("Pause!");
	    	    }
	    	});

			// Poichè non c'è alcun video, viene inserita un immagine
			// autoesplicativa, ad indicare che il file aperto è una traccia audio
			mVideoView.setBackgroundResource(R.drawable.audio_icon);
            mVideoView.setLayoutParams(getLayoutParams(false));
			
            mVideoView.setVideoPath(mPath);
            
            break;
        
        // Se il POI è un commento, vengono visualizzate direttamente le informazioni
        // relative al POI
    	case TouristExplorer.COMMENT_REQUEST:
			mLayoutInflater = getLayoutInflater();
	    	mView = mLayoutInflater.inflate(R.layout.poi_details,
                 (ViewGroup)findViewById(R.id.preview_layout));
	    	showComment(mView);
	    	break;			
		}
		
	}
	
	
	/**
	 * Get the POI's informations
	 * @param cursor cursor that contains the information of the POI taken from the database 
	 */
	private void getPoiInformation(Cursor cursor) {
		
		int i = cursor.getCount();
		if(i == 1){
			mTitle = cursor.getString(cursor.getColumnIndex(TouristExplorer.COLUMN_TITLE));
			mDescription = cursor.getString(cursor.getColumnIndex(TouristExplorer.COLUMN_DESCRIPTION));
			mPath = cursor.getString(cursor.getColumnIndex(TouristExplorer.COLUMN_PATH));
			mDatetime = cursor.getString(cursor.getColumnIndex(TouristExplorer.COLUMN_DATETIME));
			mLocation = cursor.getInt(cursor.getColumnIndex(TouristExplorer.COLUMN_LOCATION));
		}
		
		Cursor cursorLocation = dbAdapter.selectLocationFromID(mLocation);
		if(cursorLocation.moveToNext()){
			mLatitude = cursorLocation.getString(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LATITUDE));
			mLongitude = cursorLocation.getString(cursorLocation.getColumnIndex(TouristExplorer.COLUMN_LONGITUDE));
		}
	}


	
	/**
	 * If the POI is a comment, displays its informations
	 * @param view layout for the comment
	 */
	private void showComment(View view){
		
		mButtonDetails.setEnabled(false);
		
		TextView titleTextView = (TextView) view.findViewById(R.id.marker_info_title_text);
		TextView descriptionTextView = (TextView) view.findViewById(R.id.marker_info_description_text);
		TextView locationTextView = (TextView) view.findViewById(R.id.marker_info_location_text);
		TextView datetimeTextView = (TextView) view.findViewById(R.id.marker_info_datetime_text);
		RelativeLayout lay = (RelativeLayout) view.findViewById(R.id.btnLayout);
		
		lay.setVisibility(View.INVISIBLE);
		
		if(mTitle.length()==0){
			mTitle = TouristExplorer.NO_TITLE;
		}
		
		if(mDescription.length()==0){
			mDescription = TouristExplorer.NO_DESCRIPTION;
		}
		
		titleTextView.setText(mTitle);
		descriptionTextView.setText(mDescription);
	    locationTextView.setText(mLatitude + "," + mLongitude);
	    datetimeTextView.setText(mDatetime);
	   
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.show_poi_menu, menu);
		return true;
	}
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
		
        case R.id.delete:
			delete();
			return true;
			
        case R.id.edit:
			edit();
			return true;
			
        }
        return false;
    }
	
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_OK, mIntent);
		finish();
	}
	
	
	
	/**
	 * Get the layout parameters to determine the measures of the multimedia player
	 * @param isVideo 
	 * @return
	 */
	private android.widget.LinearLayout.LayoutParams getLayoutParams(boolean isVideo){
		
		DisplayMetrics metrics = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
        android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) mVideoView.getLayoutParams();
        params.leftMargin = 0;
        if(isVideo){
        	params.height = metrics.heightPixels;
        	params.width =  metrics.widthPixels;
        }else{
        	params.height = metrics.heightPixels/2;
        }
        	
        return params;
        
	}
	
	
	/**
	 * Delete both the POI marker on the map, and the POI file
	 */
	private void delete(){
		
		Marker mark = myApp.getMarker(mMarker);
		if(mark!=null)
			mark.remove();
		
		dbAdapter.open();
		dbAdapter.deleteMedia(mPath, mRequest);
		
		mIntent.putExtra("marker", mPath);
		
		setResult(RESULT_OK, mIntent);
		
		deleteAFile(mPath);
		finish();
		
	}
	
	
	/**
	 * Delete the POI
	 * @param path POI location in device memory
	 */
	private void deleteAFile(String path){
		File file = new File(path);
		file.delete();
	}
	
	
	/**
	 * Allows the user to edit POI's title, description and filename (if POI is not a comment)
	 */
	private void edit(){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		
		View dialogView = inflater.inflate(R.layout.dialog_modify_poi, null);

		builder.setView(dialogView);
		
		final EditText editFilename = (EditText) dialogView.findViewById(R.id.edit_filename);
		final EditText editTitle = (EditText) dialogView.findViewById(R.id.edit_title);
		final EditText editDescription = (EditText) dialogView.findViewById(R.id.edit_description);
		
		if(mRequest!=TouristExplorer.COMMENT_REQUEST)
			editFilename.setText(Utils.getFilename(mPath)); 
		else
			editFilename.setEnabled(false);
		
		editTitle.setText(mTitle);
		editDescription.setText(mDescription);
		
		builder
			.setPositiveButton("Salva",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								
								if(isFilenameValid(Utils.getFilename(mPath), editFilename.getText().toString())){
									update(mPath, editFilename.getText().toString(),
										editTitle.getText().toString(),
										editDescription.getText().toString(), mRequest);
								}
								else{
									dialog.cancel();
									Toast toast = Toast.makeText(getApplicationContext(), 
											"Nome già usato", Toast.LENGTH_LONG);  
									toast.show();
								}
							}
						})
				.setNegativeButton("Esci",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		builder.create();
		
		builder.show();

	}
	
	
	/**
	 * Asks the user to confirm the deletion of the file
	 * 
	 * @param path the file path to delete
	 */
	private void deleteConfirm(final String path){

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
		
		alertDialogBuilder.setMessage("Non trovata nel db, la vuoi eliminare?");
		
		// Imposta il messaggio
		alertDialogBuilder
			.setCancelable(true)
			.setPositiveButton("Si",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						Toast.makeText(getApplicationContext(), 
					    		R.string.poi_deleted, 
					    		Toast.LENGTH_LONG).show(); 
						setResult(RESULT_OK, mIntent);
						deleteAFile(path);
						finish();
					}
			})
			.setNegativeButton("No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
						setResult(RESULT_OK, mIntent);
						finish();
					}
			});

		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();

	}
	
	
	
	/**
	 * Check if new filename entered by the user as a result of
	 * the modification of the POI, not shown to be already used
	 * 
	 * @param oldFilename the filename before modification
	 * @param newFilename the filename after modification
	 * @return true if the modification is successful, false otherwise
	 */
	private boolean isFilenameValid(String oldFilename, String newFilename){
		
		if(oldFilename.equals(newFilename))
			return true;
		
		dbAdapter.open();
		
		String newPath = getNewPath(newFilename, mRequest);
		
		Cursor poiCursor = dbAdapter.selectPoi(newPath, mRequest);
		if(poiCursor.getCount()>0)
			return false;
		
		return true;
		
	}
	
	
	
	/**
	 * Update the POI after a modification by the user
	 * @param oldPath the old path of the POI
	 * @param filename the new filename of the POI
	 * @param title the new title of the POI
	 * @param description the new description of the POI
	 * @param request the type of the POI
	 */
	private void update(String oldPath, String filename, String title, String description, int request){
		
		String newPath = getNewPath(filename, request);
		
		dbAdapter.open();
		dbAdapter.updatePoi(oldPath, newPath, title, description, request);
		dbAdapter.close();
		File file = new File(oldPath);
		File file2 = new File(newPath);
	    file.renameTo(file2);
	    
	    mPath = newPath;
	    	
	}
	
	
	/**
	 * Get the new filepath
	 * @param filename the new filename
	 * @param request the type of the POI
	 * @return the new filepath
	 */
	private String getNewPath(String filename, int request){
		
		switch(request){
		case TouristExplorer.AUDIO_REQUEST:
			return TouristExplorer.AUDIO_PATH + "/" + filename + ".3gp";
		case TouristExplorer.PHOTO_REQUEST:
			return TouristExplorer.IMAGE_PATH + "/" + filename + ".jpeg";
		case TouristExplorer.VIDEO_REQUEST:
			return TouristExplorer.VIDEO_PATH + "/" + filename + ".mp4";
		case TouristExplorer.COMMENT_REQUEST:
			return filename;
		}
		
		return null;
		
	}
	
	
	
	private void setBitmap(Bitmap image){
		
		float scalingFactor = this.getBitmapScalingFactor(image);
		Bitmap newBitmap = scaleBitmap(image, scalingFactor);
		mImageView.setImageBitmap(newBitmap);
		
	}
	
	
	public static Bitmap scaleBitmap(Bitmap bm, float scalingFactor) {
        int scaleHeight = (int) (bm.getHeight() * scalingFactor);
        int scaleWidth = (int) (bm.getWidth() * scalingFactor);

        return Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
    }
	
	
	@SuppressWarnings("deprecation")
	private float getBitmapScalingFactor(Bitmap bm) {
        // Get display width from device
        int displayWidth = getWindowManager().getDefaultDisplay().getWidth();

        // Get margin to use it for calculating to max width of the ImageView
        LinearLayout.LayoutParams layoutParams = 
            (LinearLayout.LayoutParams) this.mImageView.getLayoutParams();
        int leftMargin = layoutParams.leftMargin;
        int rightMargin = layoutParams.rightMargin;

        // Calculate the max width of the imageView
        int imageViewWidth = displayWidth - (leftMargin + rightMargin);

        // Calculate scaling factor and return it
        return ( (float) imageViewWidth / (float) bm.getWidth() );
    }
	
	
	
	/**
	 * Get the details of the POI
	 * @param view
	 */
	public void details(View v){
		
		Intent i = new Intent(this, PoiDetailsActivity.class);
		
		i.putExtra("path", mPath);
		i.putExtra("request", mRequest);
		
		startActivity(i);
	
	}
	
	
	/**
	 * Share the POI through other app
	 * @param view
	 */
	public void share(View v){
		
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND); 
		getShareType();
		shareIntent.setType(mShareType);
		shareIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {""}); 
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "sub"); 
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "body");
		shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + mPath));
		startActivity(Intent.createChooser(shareIntent, "Condividi " + mShareTitle));
	
	}
	
	
	/**
	 * Get the sharing type for that POI
	 */
	public void getShareType(){
		
		switch(mRequest){
		case TouristExplorer.PHOTO_REQUEST:
			mShareType = "image/jpeg";
			mShareTitle = "la foto";
			break;
		case TouristExplorer.VIDEO_REQUEST:
			mShareType = "video/mp4";
			mShareTitle = "il video";
			break;
		case TouristExplorer.AUDIO_REQUEST:
			mShareType = "audio/3gp";
			mShareTitle = "la traccia audio";
			break;
		case TouristExplorer.COMMENT_REQUEST:
			mShareType = "text/plain";
			mShareTitle = "il commento";
			break;
		}
		
	}
	
	
	
	/**
	 * Show the POI on the map
	 * @param view
	 */
	public void show(View view){
		
		dbAdapter.open();
		Cursor cursor = dbAdapter.selectPoi(mPath, mRequest);
		
		if(cursor.moveToNext()){
			mLocation = cursor.getInt(cursor.getColumnIndex(TouristExplorer.COLUMN_LOCATION));
			Intent showPathIntent = new Intent(this, ShowPathActivity.class);
			showPathIntent.putExtra("location", mLocation);
			showPathIntent.putExtra("showingPath", false);
			showPathIntent.putExtra("request", mRequest);
			startActivity(showPathIntent);
		}
	
	}
	
}


/*
class CustomController1 extends MediaController {

	private Context context;
	
	public CustomController1(Context context, View anchor)     {
		super(context);   
        super.setAnchorView(anchor);  
		this.context = context;
	}    
      @Override  
       public void setAnchorView(View view)     {  
           // Do nothing   
      } 
      
     
      @Override
      public void hide() {
          this.show(0);
      }

      @Override
      public void setMediaPlayer(MediaPlayerControl player) {
          super.setMediaPlayer(player);
          this.show();
      }
      
   
      @Override
      public boolean dispatchKeyEvent(KeyEvent event) {
          if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
              ((Activity) context).finish();
          }
          return true;
      }
    } 
*/
