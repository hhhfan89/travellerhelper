package it.divito.touristexplorer;

import it.divito.touristexplorer.database.DatabaseAdapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

/**
 * This activity allows the display in a list of all comments 
 * taken by the user in its tracks.
 * 
 * @author Stefano Di Vito
 * 
 */

public class ListCommentTrackActivity extends Activity {

	private Intent mIntent;
	private ListView mListView;
	private EditText mInputSearch;
	private DatabaseAdapter dbAdapter;
	private ArrayAdapter<String> mAdapter;
	private ArrayList<String> mCommentList;		// lista dei commenti in memoria
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_audio_comment);
		
		mIntent = getIntent();
		
		MyApplication myApp = ((MyApplication) this.getApplication());
		dbAdapter = myApp.getDbAdapter();
		
		mInputSearch = (EditText) findViewById(R.id.listPathsInputSearch);
		mInputSearch.setHint("Ricerca commento..");
		mListView = (ListView) findViewById(R.id.list);
		
		mCommentList = new ArrayList<String>();
		
		dbAdapter = ((MyApplication)getApplication()).getDbAdapter();
		
		populateList();
		
		mAdapter = new ArrayAdapter<String>(this, R.layout.list_audio_comment_item, R.id.item, mCommentList);
		
		mListView.setAdapter(mAdapter);
		mListView.setTextFilterEnabled(true);
		
		// Permette la ricerca di un file commento nella lista
		mInputSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				// When user changed the Text
				ListCommentTrackActivity.this.mAdapter.getFilter().filter(cs);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}

		});
		
		// Determina l'apertura del POI con quel commento a seguito della
		// selezione del commento stesso dalla lista
		mListView.setOnItemClickListener(new OnItemClickListener() {
		    
			@Override public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3){ 
				Intent previewIntent = new Intent(ListCommentTrackActivity.this, ShowPoiActivity.class);
				previewIntent.putExtra("path", mCommentList.get(position));
           	 	previewIntent.putExtra("request", TouristExplorer.COMMENT_REQUEST);
           	 	previewIntent.putExtra("enableShowPathButton", true);
           	 	startActivityForResult(previewIntent, 1);
				
		    }
			
		});
	}

	
	@Override
	public void onBackPressed() {
		setResult(RESULT_OK, mIntent);
		finish();
	}

	
	/**
	 * Populates the gallery with video in memory
	 */
	private void populateList(){
		
		dbAdapter.open();
		Cursor cursor = dbAdapter.selectAllComment();
		while(cursor.moveToNext()){
			mCommentList.add(cursor.getString(cursor.getColumnIndex(TouristExplorer.COLUMN_PATH)));
		}
		dbAdapter.close();
		
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (data == null) {
			finish();
		}

		switch (requestCode) {
		
		case TouristExplorer.SETTING_REQUEST:
			if (resultCode == RESULT_CANCELED) {
				populateList();
			}
		}
	}
}