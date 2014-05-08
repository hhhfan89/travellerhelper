package it.divito.touristexplorer.path;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.R;
import it.divito.touristexplorer.database.DatabaseAdapter;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
 
/**
 * This activity allows the display of the list of 
 * the tracks previously saved by the user
 * 
 * @author Stefano Di Vito
 *
 */

@SuppressLint("NewApi")
public class PathListFragment extends Fragment {
	
	private static final String LOG_TAG = PathListFragment.class.getSimpleName();
	
	private ArrayAdapter<String> mAdapter;		// per la visualizzazione della traccia e dei pulsanti affianco a quest'ultima
	private Intent mIntent;						
	private ListView mListView;					// lista delle tracce
	private DatabaseAdapter dbAdapter;			
	
	private ProgressDialog mProgressDialog;
	private Handler mHandler;
	
	/*
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_paths);
		
		/*EditText inputSearch = (EditText) findViewById(R.id.listPathsInputSearch);
		
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
		View rootView = inflater.inflate(R.layout.fragment_path_list,
				container, false);
	
		EditText inputSearch = (EditText) rootView.findViewById(R.id.listPathsInputSearch);
		
		//mIntent = new Intent(this, ShowPathActivity.class);
		dbAdapter = ((MyApplication) getActivity().getApplication()).dbAdapter;
		dbAdapter.open();
		
		mListView = (ListView) rootView.findViewById(R.id.listPathsListView);
		
		mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_track_list, R.id.path_item);
		//populateListView();
		
		mListView.setAdapter(mAdapter);
		mListView.setTextFilterEnabled(true);
		
		// Permette la ricerca di un file commento nella lista
		/*inputSearch.addTextChangedListener(new TextWatcher() {
             
           @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                getActivity().get.mAdapter.getFilter().filter(cs);   
            }
             
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {                 
            }

			@Override
			public void afterTextChanged(Editable s) {
			}
			
        });
		*/
		
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
		menu.getItem(0).setVisible(false);
		menu.getItem(1).setVisible(false);
		menu.getItem(2).setVisible(false);
		getActivity().getActionBar().setTitle("Lista tracce salvate");
		inflater.inflate(R.menu.menu_actionbar_list_path, menu);
		
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchItem.getActionView();
		searchView.setQueryHint("Ricerca la traccia");
	}
	
	
	
	
}