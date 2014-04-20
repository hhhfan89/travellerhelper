package it.divito.touristexplorer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * This activity allows the display in a gallery of all the videos 
 * taken by the user in its tracks.
 * 
 * @author Stefano Di Vito
 * 
 */

@SuppressWarnings("deprecation")
public class GalleryVideoActivity extends Activity {
   
	private String mPath;					// percorso dell'immagine visualizzata
	private ImageView mImageView;
	private Intent mIntent;	
	private ArrayList<String> mVideoList;	// lista dei video presenti in memoria
	private Gallery gallery;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.gallery);
		
		mIntent = getIntent();
		
		mImageView = (ImageView) findViewById(R.id.imageView);
		gallery = (Gallery) findViewById(R.id.gallery);
		
		populateGallery();
		
		Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mPath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
		mImageView.setImageBitmap(Utils.setBitmapOrientation(bitmap, mPath));
		
		// Quando viene selezionata un'immagine dalla galleria,
		// viene visualizzata nello spazio sottostante a quest'ultima
		gallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				mPath = ((List<String>) mVideoList).get(position).toString();
				Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mPath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
				mImageView.setImageBitmap(Utils.setBitmapOrientation(bitmap, mPath));
			}
		});
		
		// Quando si seleziona l'immagine sottostante alla galleria,
		// viene visualizzato il POI relativo a quell'immagine
		mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { 
            	 Intent previewIntent = new Intent(GalleryVideoActivity.this, ShowPoiActivity.class);
            	 previewIntent.putExtra("path", mPath);
            	 previewIntent.putExtra("request", TouristExplorer.VIDEO_REQUEST);
            	 previewIntent.putExtra("enableShowPathButton", true);
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
		
		case TouristExplorer.SETTING_REQUEST:
			if (resultCode == RESULT_CANCELED) {
				populateGallery();
			}
		
		case TouristExplorer.PREVIEW_REQUEST:
			if (resultCode == RESULT_OK) {
			populateGallery();
		}
		break;
	
		}
	}
	
	
	@Override
	public void onBackPressed() {
		
		setResult(RESULT_OK, mIntent);
		finish();
	
	}
	
	
	/**
	 * Populates the gallery with video in memory
	 */
	private void populateGallery(){
		
		mVideoList = Utils.getFiles(TouristExplorer.VIDEO_PATH);
		if(mVideoList.size()>0){
			gallery.setAdapter(new VideoAdapter(this, mVideoList));
			mPath = ((List<String>) mVideoList).get(0).toString();
		}
		else{
			setResult(RESULT_OK, mIntent);
			finish();
		}
		
	}
	

	/**
	 * This class determines the correct display of video in the gallery
	 * 
	 * @author Stefano Di Vito
	 *
	 */
	
	public class VideoAdapter extends BaseAdapter {
		int mGalleryItemBackground;
		private Context mContext;
		private List<String> FileList;

		public VideoAdapter(Context c, List<String> fList) {
			mContext = c;
			FileList = fList;
			TypedArray a = obtainStyledAttributes(R.styleable.gallery);
			mGalleryItemBackground = a.getResourceId(
					R.styleable.gallery_android_galleryItemBackground, 0);
			a.recycle();
		}

		public int getCount() {
			return FileList.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return (position);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(mContext);

			Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(FileList.get(position), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
			i.setImageBitmap(Utils.setBitmapOrientation(bitmap, FileList.get(position)));

			i.setLayoutParams(new Gallery.LayoutParams(150,120));
			i.setScaleType(ImageView.ScaleType.FIT_XY);
			i.setBackgroundResource(mGalleryItemBackground);
			return i;
		}

	}
}