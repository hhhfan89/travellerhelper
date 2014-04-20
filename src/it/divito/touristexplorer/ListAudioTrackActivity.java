package it.divito.touristexplorer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
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
 * This activity allows the display in a list of all audio tracks 
 * taken by the user in its paths.
 * 
 * @author Stefano Di Vito
 * 
 */

public class ListAudioTrackActivity extends Activity {
   
	private Intent mIntent;
	private ListView mListView;
	private EditText mInputSearch;
	private ArrayList<String> mAudioNameList;	// lista dei nomi dei file audio presenti in memoria
	private ArrayList<String> mAudioFileList;	// lista dei file audio presenti in memoria
	private ArrayAdapter<String> mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_audio_comment);
		
		mIntent = getIntent();
		
		mInputSearch = (EditText) findViewById(R.id.listPathsInputSearch);
		mInputSearch.setHint("Ricerca audio..");
		mListView = (ListView) findViewById(R.id.list);
		
		mAudioFileList = Utils.getFiles(TouristExplorer.AUDIO_PATH);		
		mAudioNameList = Utils.getFilename(mAudioFileList);
		
		mAdapter = new ArrayAdapter<String>(this, R.layout.list_audio_comment_item, R.id.item, mAudioNameList);
		
		mListView.setAdapter(mAdapter);
		mListView.setTextFilterEnabled(true);
		
		// Permette la ricerca di un file audio nella lista
		mInputSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				ListAudioTrackActivity.this.mAdapter.getFilter().filter(cs);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}

		});
		
		
		// Determina l'apertura del POI con quel file audio a seguito della
		// selezione della traccia stessa dalla lista
		mListView.setOnItemClickListener(new OnItemClickListener() {
		    
			@Override 
			public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3){ 
				Intent previewIntent = new Intent(ListAudioTrackActivity.this, ShowPoiActivity.class);
				previewIntent.putExtra("path", mAudioFileList.get(position));
           	 	previewIntent.putExtra("request", TouristExplorer.AUDIO_REQUEST);
           	 	previewIntent.putExtra("enableShowPathButton", true);
        	 	startActivity(previewIntent);
				
		    }
			
		});
	}

	
	@Override
	public void onBackPressed() {
		setResult(RESULT_OK, mIntent);
		finish();
	}

	
}