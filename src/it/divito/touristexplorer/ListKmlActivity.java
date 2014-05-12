package it.divito.touristexplorer;

import it.divito.touristexplorer.R;
import it.divito.touristexplorer.TouristExplorer;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
 * This activity allows the display in a list of all 
 * KMZ files exported by the user
 * 
 * @author Stefano Di Vito
 * 
 */

public class ListKmlActivity extends Activity {
	
	private Intent mIntent;
	private ListView mListView;
	private EditText mInputSearch;
	private ArrayAdapter<String> mAdapter;
	
	private ArrayList<String> kmlNameList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_path_list);
		
		mIntent = getIntent();
		
		mInputSearch = (EditText) findViewById(R.id.listPathsInputSearch);
		mListView = (ListView) findViewById(R.id.list);
		
		ArrayList<String> kmlList = Utils.getFiles(TouristExplorer.KML_PATH);
		kmlNameList = Utils.getFilename(kmlList);
		
		mAdapter = new ArrayAdapter<String>(this, R.layout.kml_list_item, R.id.kml_item);
		populateListView(kmlNameList);
		
		mListView.setAdapter(mAdapter);
		mListView.setTextFilterEnabled(true);
		
		// Permette la ricerca di un file KMZ nella lista
		mInputSearch.addTextChangedListener(new TextWatcher() {
             
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
            	ListKmlActivity.this.mAdapter.getFilter().filter(cs);   
            }
             
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                    int arg3) {
            }

			@Override
			public void afterTextChanged(Editable s) {
			}
             
          });
	}
 
	
	/**
	 * Populates the gallery with KMZ files in memory
	 */
	private void populateListView(ArrayList<String> list){
    
		for(String file : list) {
    		mAdapter.add(file);
    	} 
    	
    }
	
	
	/**
	 * Allows to share the KMZ file between applications
	 * @param v
	 */
	public void share(View v){
		
		// Si ricava il nome della file KMZ da condividere
		String trackName = getTrackName(v);
		
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("*/*");
		share.putExtra(Intent.EXTRA_STREAM,  Uri.parse("file://" + TouristExplorer.KML_PATH + "/" + trackName + ".kmz"));
		share.putExtra(Intent.EXTRA_TEXT, "Grazie per aver utilizzato Tourist Explorer!");
		startActivity(Intent.createChooser(share, "Condividi il file KMZ"));
	
	}
	
	
	/**
	 * Allows to delete the KMZ file
	 * @param v
	 */
	public void deleteKml(View v){
		
		// Si ricava il nome della file KMZ da eliminare
		String trackName = getTrackName(v);
		deleteConfirm(trackName);
	
	}
	
	
	/**
	 * Confirmation message, asking if the user confirms the deletion
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
						delete(trackName);
					    Toast.makeText(getApplicationContext(), 
					    		"File " + trackName + " eliminata", 
					    		Toast.LENGTH_LONG).show(); 
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
	 * Delete the KMZ file
	 * @param trackName the name of the track to delete
	 */
	private void delete(String trackName){
		
		File file = new File(TouristExplorer.KML_PATH + "/" + trackName + ".kmz");
		file.delete();
		
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
	 * Determines the name of the selected track
	 * @param view the selected view
	 * @return the name of the selected view
	 */
	private String getTrackName(View view){
		
		LinearLayout parent = (LinearLayout) view.getParent();
		LinearLayout vwParentRow = (LinearLayout) parent.getParent();
		TextView path = (TextView) vwParentRow.getChildAt(0);
		return path.getText().toString();
	
	}
    
}