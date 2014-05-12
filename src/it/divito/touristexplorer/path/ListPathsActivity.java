package it.divito.touristexplorer.path;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.R;
import it.divito.touristexplorer.TouristExplorer;
import it.divito.touristexplorer.database.DatabaseAdapter;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
 
/**
 * This activity allows the display of the list of 
 * the tracks previously saved by the user
 * 
 * @author Stefano Di Vito
 *
 */

public class ListPathsActivity extends Activity {
	
	private ArrayAdapter<String> mAdapter;		// per la visualizzazione della traccia e dei pulsanti affianco a quest'ultima
	private Intent mIntent;						
	private ListView mListView;					// lista delle tracce
	private DatabaseAdapter dbAdapter;			
	
	private ProgressDialog mProgressDialog;
	private Handler mHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_path_list);
		
		EditText inputSearch = (EditText) findViewById(R.id.listPathsInputSearch);
		
		mIntent = new Intent(this, ShowPathActivity.class);
		dbAdapter = ((MyApplication)getApplication()).dbAdapter;
		dbAdapter.open();
		
		mListView = (ListView) findViewById(R.id.listPathsListView);
		
		mAdapter = new ArrayAdapter<String>(this, R.layout.item_track_list, R.id.path_item);
		populateListView();
		
		mListView.setAdapter(mAdapter);
		mListView.setTextFilterEnabled(true);
		
		// Permette la ricerca di un file commento nella lista
		inputSearch.addTextChangedListener(new TextWatcher() {
             
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                ListPathsActivity.this.mAdapter.getFilter().filter(cs);   
            }
             
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {                 
            }

			@Override
			public void afterTextChanged(Editable s) {
			}
			
        });
		
	}
 
	/**
	 * Populates the list with tracks in memory
	 */
	private void populateListView() {

		Cursor cursorTrack = dbAdapter.selectAllTracks();

		while (cursorTrack.moveToNext()) {
			String path = cursorTrack.getString(1);
			mAdapter.add(path);
		}
		
	}
	
	
	/*public void showPathInformation(View v){
		
		String trackName = getTrackName(v);
		Cursor cursorTrack = dbAdapter.selectTrack(trackName);
		
		if (cursorTrack.moveToNext()) {
			
			Intent i = new Intent(ListPathsActivity.this, PathInformationActivity.class);
			i.putExtra("trackID", cursorTrack.getInt(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_ID)));
			i.putExtra("trackName", cursorTrack.getString(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_NAME)));
			
			startActivity(i);
		
		}
		
	}*/
	
	
	/**
	 * Export the selected track in a KMZ file
	 * @param View view the selected track
	 */
	public void exportPath(View view){
		
		String trackName = getTrackName(view);
		Cursor cursorTrack = dbAdapter.selectTrack(trackName);
		
		if (cursorTrack.moveToNext()) {
			
			Intent i = new Intent(ListPathsActivity.this, ExportPathActivity.class);
			i.putExtra("name", trackName);
			i.putExtra("trackID", cursorTrack.getInt(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_ID)));
			
			startActivity(i);
		
		}
		
		
	}
	
	
	/**
	 * Show the selected track in the map
	 * @param View view the selected track
	 */
	@SuppressLint("HandlerLeak")
	public void showPath(final View view){
		
		//MA SERVE???
		
		
		
		mProgressDialog = ProgressDialog.show(this, "" , "Attendere prego");
		mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what==0) {
				mProgressDialog.dismiss();
				mIntent.putExtra("showingPath", true);
				startActivity(mIntent);
				}
			}
		};   
		
		Thread thread = new Thread() {
			@Override
			public void run() {
				String trackName = getTrackName(view);
				Cursor cursorTrack = dbAdapter.selectTrack(trackName);

				if (cursorTrack.moveToNext()) {
					mIntent.putExtra("trackID", cursorTrack.getInt(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_ID)));
					mIntent.putExtra("trackName", cursorTrack.getString(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_NAME)));
				}
				mHandler.sendEmptyMessage(0);

			}
		};
		thread.start();
	
	}
	
	

	/**
	 * Delete the selected track
	 * @param view the selected track
	 */
	public void deletePath(View view){
		
		String trackName = getTrackName(view);
		deleteConfirm(trackName);
	
	}
	
	
	
	/**
	 * Asks the user to confirm the deletion of the tracks
	 * 
	 * @param trackName track to delete
	 */
	private void deleteConfirm(final String trackName){

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
		
		alertDialogBuilder.setMessage("Vuoi eliminare " + trackName + "?");
		
		// Imposta il messaggio
		alertDialogBuilder
			.setCancelable(true)
			.setPositiveButton("Si",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						mAdapter.remove(trackName);
					    Toast.makeText(getApplicationContext(), 
					    		"Traccia " + trackName + " eliminata", 
					    		Toast.LENGTH_LONG).show(); 
						dialog.cancel();
						alertDeletePoi(trackName);
					}
			})
			.setNegativeButton("No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						//followMeLocationSource.deactivate();
						dialog.cancel();
					}
			});

		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();

	}
	
	
	/**
	 * Asks the user to confirm the deletion of the POI
	 * 
	 * @param trackName track to delete
	 */
	private void alertDeletePoi(final String trackName){

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		
		alertDialogBuilder.setMessage(R.string.alert_delete_poi);
		
		// Imposta il messaggio
		alertDialogBuilder
			.setCancelable(true)
			.setPositiveButton("Si",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						delete(trackName, true);
						Toast.makeText(getApplicationContext(), 
					    		"Traccia " + trackName + " eliminata", 
					    		Toast.LENGTH_LONG).show(); 
						dialog.cancel();
					}
			})
			.setNegativeButton("No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						delete(trackName, false);
						dialog.cancel();
					}
			});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();

	}
	
	
	/**
	 * Delete the track and, eventually, the POIs
	 * @param trackName name of the track to delete
	 * @param deletePoi if true, delete also the POIs related to track; otherwise do not delete POIs
	 */
	private void delete(String trackName, boolean deletePoi){
		
		int trackID = -1;
		Cursor cursorTrack = dbAdapter.selectTrack(trackName);
		if(cursorTrack.moveToNext())
			trackID = cursorTrack.getInt(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_ID));
		
		if(deletePoi){
			deletePhoto(trackID);
			deleteVideo(trackID);
			deleteAudio(trackID);
			dbAdapter.deleteTrack(trackID);
		}
		else {
			dbAdapter.deleteTrackWithoutPoi(trackID);
		}
		
	}
	
	
	/**
	 * Delete photos of a selected track
	 * @param trackID id of the track where are the photos
	 */
	private void deletePhoto(int trackID){
		
		ArrayList<String> list = dbAdapter.selectPoiPath(trackID, TouristExplorer.PHOTO_REQUEST);
		for(String path : list){
			File file = new File(path);
			file.delete();
		}
	
	}
	
	
	/**
	 * Delete videos of a selected track
	 * @param trackID id of the track where are the videos
	 */
	private void deleteVideo(int trackID){
		
		ArrayList<String> list = dbAdapter.selectPoiPath(trackID, TouristExplorer.VIDEO_REQUEST);
		for(String path : list){
			File file = new File(path);
			file.delete();
		}
	
	}
	
	
	/**
	 * Delete audio files of a selected track
	 * @param trackID id of the track where are the audio files
	 */
	private void deleteAudio(int trackID){
		
		ArrayList<String> list = dbAdapter.selectPoiPath(trackID, TouristExplorer.AUDIO_REQUEST);
		for(String path : list){
			File file = new File(path);
			file.delete();
		}
	
	}
	
	
    @Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED, mIntent);
		finish();
	}
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_actionbar_list_path, menu);
        return (super.onCreateOptionsMenu(menu));
    
    }
	
	
    /**
	 * Get track name
	 * @param view the selected track 
	 */
	private String getTrackName(View view){
		
		LinearLayout parent = (LinearLayout) view.getParent();
		LinearLayout vwParentRow = (LinearLayout) parent.getParent();
		TextView path = (TextView) vwParentRow.getChildAt(0);
		return path.getText().toString();
	
	}
    
}