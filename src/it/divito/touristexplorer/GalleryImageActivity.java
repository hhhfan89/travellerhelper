package it.divito.touristexplorer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;


/**
 * This activity allows the display in a gallery of all the pictures 
 * taken by the user in its tracks.
 * 
 * @author Stefano Di Vito
 * 
 */

@SuppressWarnings("deprecation")
public class GalleryImageActivity extends Activity {
   
	private String mPath;					// percorso dell'immagine visualizzata
	private ImageView mImageView;
	private Intent mIntent;	
	private ArrayList<String> mImageList; 	// lista delle immagini presenti in memoria
	private Gallery gallery;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		
		mIntent = getIntent();
		
		mImageView = (ImageView) findViewById(R.id.imageView);
		gallery = (Gallery) findViewById(R.id.gallery);
		
		populateGallery();
		
		Bitmap bitmap = BitmapFactory.decodeFile(mPath);
		mImageView.setImageBitmap(bitmap);

		// Quando viene selezionata un'immagine dalla galleria,
		// viene visualizzata nello spazio sottostante a quest'ultima
		gallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				mPath = ((List<String>) mImageList).get(position).toString();
				Bitmap bitmap = BitmapFactory.decodeFile(mPath);
				mImageView.setImageBitmap(bitmap);
			}
		});

		
		// Quando si seleziona l'immagine sottostante alla galleria,
		// viene visualizzato il POI relativo a quell'immagine
		mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { 
            	 Intent previewIntent = new Intent(GalleryImageActivity.this, ShowPoiActivity.class);
            	 previewIntent.putExtra("path", mPath);
            	 previewIntent.putExtra("request", TouristExplorer.PHOTO_REQUEST);
            	 previewIntent.putExtra("enableShowPathButton", true);
            	 startActivityForResult(previewIntent, TouristExplorer.PREVIEW_REQUEST);
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
			break;
		
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
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.menu_gallery_image, menu);
	
	}
	

	/**
	 * Populates the gallery with images in memory
	 */
	private void populateGallery(){
		
		// Determina la lista delle immagini
		mImageList = Utils.getFiles(TouristExplorer.IMAGE_PATH);
		// Se ce n'è almeno una, allora la prima di queste verra inserita
		// nello spazio sottostante alla galleria
		if(mImageList.size()>0){
			gallery.setAdapter(new ImageAdapter(this, mImageList));
			mPath = ((List<String>) mImageList).get(0).toString();
		}
		else{
			setResult(RESULT_OK, mIntent);
			finish();
		}
	}

	

	/**
	 * This class determines the correct display of images in the gallery
	 * 
	 * @author Stefano Di Vito
	 *
	 */
	
	public class ImageAdapter extends BaseAdapter {
		
		int mGalleryItemBackground;
		private Context mContext;
		private List<String> FileList;

		public ImageAdapter(Context c, List<String> fList) {
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

			Bitmap bm = BitmapFactory.decodeFile(FileList.get(position)
					.toString());
			i.setImageBitmap(bm);

			i.setLayoutParams(new Gallery.LayoutParams(150,120));
			i.setScaleType(ImageView.ScaleType.FIT_XY);
			i.setBackgroundResource(mGalleryItemBackground);
			return i;
		}

	}
}