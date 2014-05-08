package it.divito.touristexplorer.poi;

import it.divito.touristexplorer.R;
import android.app.Activity;
import android.widget.EditText;
import android.os.Bundle;
import android.widget.Button;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.media.MediaRecorder;

import it.divito.touristexplorer.MyApplication;
import it.divito.touristexplorer.TouristExplorer;
import it.divito.touristexplorer.Utils;
import it.divito.touristexplorer.database.DatabaseAdapter;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * This activity allows the user to save an audio track, while is tracking his path
 * 
 * @author Stefano Di Vito
 *
 */
public class AudioActivity extends Activity {
		
	private String mFilePath = null;	
	private String mDatetimeFile;		// data ed ora da inserire nel database
	private String mDatetimeMarker;		// data ed ora da inserire nella preview del marker
	   
	private Location mLocation = null;

    private Button mBtnRecord;
    private Button mBtnStop;
    private Button mBtnSave;
    private Button mBtnCancel;
    
    private EditText mEditTitle;
    private EditText mEditComment;
    
    private Intent mIntent;
    private MediaRecorder mRecorder = null;
    
    private DatabaseAdapter dbAdapter;    
    
    
    @Override
    public void onCreate(Bundle icicle) {

    	super.onCreate(icicle);
		
    	setContentView(R.layout.activity_audio);
    	
    	MyApplication myApp = ((MyApplication)this.getApplication());
		dbAdapter = myApp.getDbAdapter();
		
		mIntent = getIntent();    
		mLocation = (Location) mIntent.getParcelableExtra("location");
		
		setNames();
		
		mBtnRecord = (Button) findViewById(R.id.btn_audio_record);
		mBtnStop = (Button) findViewById(R.id.btn_audio_stop);
		mEditTitle = (EditText) findViewById(R.id.edt_audio_title);
		mEditComment = (EditText) findViewById(R.id.edt_audio_description);
        mBtnSave = (Button) findViewById(R.id.btn_audio_save);
        mBtnCancel = (Button) findViewById(R.id.btn_audio_cancel); 
	   
    }
    
    
    @Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED, mIntent);
		finish();
	}
    
    
    /**
     * Listener for the "Save" button
     * 
     * @param view the "Save" button
     */
    public void save(View view){
    	
    	String title = mEditTitle.getText().toString();
		String description = mEditComment.getText().toString();
		
		int id = insertIntoDB(title, description, mDatetimeFile, mFilePath);
		
		mIntent.putExtra("id", id);
		mIntent.putExtra("title", title);
		mIntent.putExtra("description", description);
		mIntent.putExtra("location", mLocation);
		mIntent.putExtra("datetime", mDatetimeMarker);
		mIntent.putExtra("path", mFilePath);
		
		setResult(RESULT_OK, mIntent);
		finish();
	
    }
    
    
    /**
     * Listener for the "Cancel" button
     * 
     * @param view the "Cancel" button
     */
    public void cancel(View v){
    	setResult(RESULT_CANCELED, mIntent);
		finish();
    }
    
    
    /**
     * Listener for the "Record" button
     * 
     * @param view the "Record" button
     */
    public void record(View v) {
    	
    	startRecording();
    
    }
    
    
    /**
     * Listener for the "Stop" button
     * 
     * @param view the "Stop" button
     */
    public void stop(View v) {

    	stopRecording();
    	
    }
    
    
    /**
     * Start the recording of audio track
     */
    private void startRecording() {
    	
    	setButtonsEnabled(false);
    	mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
        	e.printStackTrace();
        }

        mRecorder.start();
    
    }

    
    /**
     * Stop the recording of audio track
     */
    private void stopRecording() {
       
    	setButtonsEnabled(true);
    	mRecorder.stop();
        mRecorder.release();
		mRecorder = null;
		setButtonsEnabled(true);
		setResult(RESULT_OK, mIntent);

    }
    
    
    
    /**
     * Enable or disable the layout buttons and edittext
     * @param enable
     */
    private void setButtonsEnabled(boolean enable){
	    
    	mEditTitle.setEnabled(enable);
		mEditComment.setEnabled(enable);
		mBtnSave.setEnabled(enable);
		mBtnCancel.setEnabled(enable);
		mBtnStop.setEnabled(!enable);
		mBtnRecord.setEnabled(enable);
		
    }
    
    
    /**
     * Set audio filename
     */
    private void setNames(){
    	
    	Date date = new Date();
    	mDatetimeFile = TouristExplorer.DATETIME_FORMAT.format(date);
    	mDatetimeMarker = TouristExplorer.DATETIME_MARKER_FORMAT.format(date);
		
		File audioFile = Utils.getOutputMediaFile(TouristExplorer.MEDIA_TYPE_AUDIO, mDatetimeFile);
		mFilePath = audioFile.getAbsolutePath();
       
    }
    
    
    /**
     * Insert the audio informations into db, after stopping and saving the audio track
     * @param title title of the audio track
     * @param description description of the audio track
     * @param datetime datetime of the audio track
     * @param path filepath of the audio track
     * @return the row number of the new insert
     */
    private int insertIntoDB(String title, String description, String datetime, String path){
		
    	Cursor cursorLocation = dbAdapter.selectLastLocation(mLocation.getLongitude(), mLocation.getLatitude());
    	int i = cursorLocation.getCount();
		
    	if(i >= 1) {	
    		Log.d("insert audio", "insert audio");
    		cursorLocation.moveToFirst();
			Long row = dbAdapter.insertAudio(title, description, path, datetime, cursorLocation.getInt(0));
			return row.intValue();
		}
		
    	Log.d("not insert audio", "not insert audio");
		
		return -1;
	
    }
    
}