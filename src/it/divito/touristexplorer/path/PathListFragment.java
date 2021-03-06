package it.divito.touristexplorer.path;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.R;
import it.divito.touristexplorer.TouristExplorer;
import it.divito.touristexplorer.database.DatabaseAdapter;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
 
/**
 * This activity allows the display of the list of 
 * the tracks previously saved by the user
 * 
 * @author Stefano Di Vito
 *
 */

@SuppressLint("NewApi")
public class PathListFragment extends Fragment implements OnItemClickListener{
	
	private static final String LOG_TAG = PathListFragment.class.getSimpleName();
	
	private ImageButton btnPathExport;
	private ImageButton btnPathShow;
	private ImageButton btnPathDelete;
	
	private ArrayAdapter<String> mAdapter;		// per la visualizzazione della traccia e dei pulsanti affianco a quest'ultima
	private Intent mIntent;						
	private ListView mListView;					// lista delle tracce
	private DatabaseAdapter dbAdapter;			
	
	private ProgressDialog mProgressDialog;
	private Handler mHandler;
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_path_list,
				container, false);
	
		Button inputSearch = (Button) rootView.findViewById(R.id.listPathsInputSearch);
		
		//mIntent = new Intent(this, ShowPathActivity.class);
		dbAdapter = ((MyApplication) getActivity().getApplication()).dbAdapter;
		dbAdapter.open();
		
		mListView = (ListView) rootView.findViewById(R.id.path_list);		
		mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.fragment_path_item, R.id.path_item);
		populateListView();
		
		mListView.setAdapter(mAdapter);
		mListView.setTextFilterEnabled(true);
		registerForContextMenu(mListView);

		mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int pos, long row) {
            	String s = (String) ((TextView) view.findViewById(R.id.path_item)).getText();
            	getActivity().openContextMenu(view);
            }
        });
		
		/*mListView.setTextFilterEnabled(true);
		
		final LayoutInflater factory = getActivity().getLayoutInflater();
		final View textEntryView = factory.inflate(R.layout.fragment_path_item, null);

		btnPathExport = (ImageButton) textEntryView.findViewById(R.id.btn_path_export);
		btnPathShow = (ImageButton) textEntryView.findViewById(R.id.btn_path_show);
		btnPathDelete = (ImageButton) textEntryView.findViewById(R.id.btn_path_delete);
		
		btnPathExport.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(LOG_TAG, "btnPathExport");
				exportPath(v);
			}
		});
		
		
		btnPathDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(LOG_TAG, "btnPathDelete");
				deletePath(v);
			}
		});
		
		
		btnPathShow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(LOG_TAG, "btnPathShow");
				showPath(v);
			}
		});
		  */
		
		
		// Permette la ricerca di un file commento nella lista
		/*inputSearch.addTextChangedListener(new TextWatcher() {
             
           @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                mAdapter.getFilter().filter(cs);   
            }
             
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {                 
            }

			@Override
			public void afterTextChanged(Editable s) {
			}
			
        });*/
		
		setHasOptionsMenu(true);
		return rootView;
	
	}
	
	/*
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	};*/
	
	@Override
	public void onCreateOptionsMenu(
	      Menu menu, MenuInflater inflater) {
		Log.d(LOG_TAG, "onCreateOptionsMenu");
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setHomeButtonEnabled(true);
	    
		menu.getItem(0).setVisible(false);
		menu.getItem(1).setVisible(false);
		menu.getItem(2).setVisible(false);
		getActivity().getActionBar().setTitle("Lista tracce salvate");
		inflater.inflate(R.menu.menu_actionbar_list_path, menu);
		
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchItem.getActionView();
		searchView.setQueryHint("Ricerca la traccia");
		SearchManager searchManager = (SearchManager) getActivity().getSystemService( Context.SEARCH_SERVICE );
		searchView.setSearchableInfo( searchManager.getSearchableInfo( getActivity().getComponentName() ) );
		
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String arg0) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				if (TextUtils.isEmpty(newText)) {
		            mListView.clearTextFilter();
		        } else {
		            mListView.setFilterText(newText.toString());
		        }
		        return true;
			}
		});
		
		
	}
	
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.menu_list_path_options, menu);
	}
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		RelativeLayout r = (RelativeLayout) info.targetView;
		String trackName = ((TextView) r.findViewById(R.id.path_item)).getText().toString();

        switch(item.getItemId()){
        
        case R.id.path_list_show:
        	showPath(trackName);
        	break;
        case R.id.path_list_export:
        	exportPath(trackName);
       	 	break;
        case R.id.path_list_delete:
        	deletePath(trackName);
       	 	break;
        case R.id.path_list_info:
        	infoPath(trackName);
       	 	break;
        
        }
        
		return super.onContextItemSelected(item);
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
	
	
	
	public void infoPath(String trackName){
		
		Cursor cursorTrack = dbAdapter.selectTrack(trackName);
		
		if (cursorTrack.moveToNext()) {
			/*
			Intent i = new Intent(ListPathsActivity.this, PathInformationActivity.class);
			i.putExtra("trackID", cursorTrack.getInt(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_ID)));
			i.putExtra("trackName", cursorTrack.getString(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_NAME)));
			
			startActivity(i);
			 */
		}
		
	}
	
	
	/**
	 * Export the selected track in a KMZ file
	 * @param View view the selected track
	 */
	public void exportPath(String trackName){
		
		Log.d(LOG_TAG, "exportPath inside");
		Cursor cursorTrack = dbAdapter.selectTrack(trackName);
		
		if (cursorTrack.moveToNext()) {
			
			Toast.makeText(getActivity().getApplicationContext(), 
		    		"TODO: EXPORT PATH", 
		    		Toast.LENGTH_LONG).show(); 
			
			/*Intent i = new Intent(ListPathsActivity_old.this, ExportPathActivity.class);
			i.putExtra("name", trackName);
			i.putExtra("trackID", cursorTrack.getInt(cursorTrack.getColumnIndex(TouristExplorer.COLUMN_ID)));
			
			startActivity(i);
		*/
		}
		
		
	}
	
	
	/**
	 * Show the selected track in the map
	 * @param View view the selected track
	 */
	@SuppressLint("HandlerLeak")
	public void showPath(final String trackName){
		
		//MA SERVE???
		
		
		
		mProgressDialog = ProgressDialog.show(getActivity(), "" , "Attendere prego");
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
	public void deletePath(String trackName){
		
		deleteConfirm(trackName);
	
	}
	
	
	
	/**
	 * Asks the user to confirm the deletion of the tracks
	 * 
	 * @param trackName track to delete
	 */
	private void deleteConfirm(final String trackName){

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				getActivity());
		
		alertDialogBuilder.setMessage("Vuoi eliminare " + trackName + "?");
		
		// Imposta il messaggio
		alertDialogBuilder
			.setCancelable(true)
			.setPositiveButton("Si",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						mAdapter.remove(trackName);
					    Toast.makeText(getActivity().getApplicationContext(), 
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

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		
		alertDialogBuilder.setMessage(R.string.alert_delete_poi);
		
		// Imposta il messaggio
		alertDialogBuilder
			.setCancelable(true)
			.setPositiveButton("Si",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						delete(trackName, true);
						Toast.makeText(getActivity().getApplicationContext(), 
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Log.d("ci", "ci");
	}
	
	
}