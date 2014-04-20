package it.divito.touristexplorer;

import it.divito.touristexplorer.database.DatabaseAdapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * This activity allows the display in a list of all markers 
 * which are included in a cluster marker
 * 
 * @author Stefano Di Vito
 * 
 */

public class ListMarkerActivity extends Activity {
   
	private ArrayAdapter<String> mAdapter;
	private Intent mIntent;
	private DatabaseAdapter dbAdapter;
	private ArrayList<String> mListKMZPath, listMarker;
	private ArrayList<String> mListKMZFilename;
	private ArrayList<Integer> listRequest;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_marker);
		
		MyApplication myApp = ((MyApplication) this.getApplication());
		dbAdapter = myApp.getDbAdapter();
		
		mIntent = getIntent();
		
		listMarker = mIntent.getStringArrayListExtra("marker_array");
		listRequest = mIntent.getIntegerArrayListExtra("request_array");
		mListKMZPath = mIntent.getStringArrayListExtra("path_array");
		
		mListKMZFilename = Utils.getFilename(mListKMZPath);
		
		ListView listView = (ListView) findViewById(R.id.list);
		dbAdapter = ((MyApplication)getApplication()).dbAdapter;
		dbAdapter.open();
		
		mAdapter = new ArrayAdapter<String>(this, R.layout.list_audio_comment_item, R.id.item, mListKMZFilename);
		
		listView.setAdapter(mAdapter);
		listView.setTextFilterEnabled(true);
		
		// Determina l'apertura del POI del marker selezionato dalla lista 
		listView.setOnItemClickListener(new OnItemClickListener() {
		    
			@Override 
			public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3){ 
				Intent previewIntent = new Intent(ListMarkerActivity.this, ShowPoiActivity.class);
				previewIntent.putExtra("marker", listMarker.get(position));
				previewIntent.putExtra("path", mListKMZPath.get(position));
           	 	previewIntent.putExtra("request", listRequest.get(position));
           	 	startActivityForResult(previewIntent, 1);
				
		    }
			
		});
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (data == null) {
			finish();
		}

		switch (requestCode) {
		
		case 1:
			if (resultCode == RESULT_OK) {
				String path = data.getStringExtra("marker");
				mListKMZPath.remove(path);
			}
		}
	}
	
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_OK, mIntent);
		finish();
	}

	
}