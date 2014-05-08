package it.divito.touristexplorer.path;

import it.divito.touristexplorer.R;
import it.divito.touristexplorer.database.DatabaseAdapter;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
 
/**
 * This activity allows the display of the list of 
 * the tracks previously saved by the user
 * 
 * @author Stefano Di Vito
 *
 */

@SuppressLint("NewApi")
public class ListPathsFragment1 extends android.support.v4.app.Fragment {
	
	private static final String LOG_TAG = ListPathsFragment1.class.getName();
	
	private ArrayAdapter<String> mAdapter;		// per la visualizzazione della traccia e dei pulsanti affianco a quest'ultima
	private Intent mIntent;						
	private ListView mListView;					// lista delle tracce
	private DatabaseAdapter dbAdapter;			
	
	private ProgressDialog mProgressDialog;
	private Handler mHandler;
	/*
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_paths);
		
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
                ListPathsFragment.this.mAdapter.getFilter().filter(cs);   
            }
             
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {                 
            }

			@Override
			public void afterTextChanged(Editable s) {
			}
			
        });
		
	}
 */
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.list_paths_fragment_layout1,
				container, false);
		Log.d(LOG_TAG, "onCreateView");
		return rootView;
	}
	
	
	public void onBackPressed() {
		Log.d(LOG_TAG, "onBackPressed");
	    FragmentManager fm = getActivity().getSupportFragmentManager();
	    fm.popBackStack();
	}
}