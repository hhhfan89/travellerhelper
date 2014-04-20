package it.divito.touristexplorer;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import pl.mg6.android.maps.extensions.ClusteringSettings.IconDataProvider;

@SuppressWarnings("deprecation")
public class TouristExplorerIconProvider implements IconDataProvider {

	private static final int[] res = { R.drawable.m1, R.drawable.m2,
			R.drawable.m3 };

	private static final int[] forCounts = { 10, 100, 1000 };

	private Bitmap[] baseBitmaps;

	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Rect bounds = new Rect();

	private MarkerOptions markerOptions = new MarkerOptions()
			.anchor(0.5f, 0.5f);

	public TouristExplorerIconProvider(Resources resources) {
		
		baseBitmaps = new Bitmap[res.length];
		for (int i = 0; i < res.length; i++) {
			baseBitmaps[i] = BitmapFactory.decodeResource(resources, res[i]);
		}
		paint.setColor(Color.WHITE);
		paint.setTextAlign(Align.CENTER);
		
	}

	@Override
	public MarkerOptions getIconData(int markersCount) {
		
		Bitmap base;
		int i = 0;
		do {
			base = baseBitmaps[i];
		} while (markersCount >= forCounts[i++]);

		Bitmap bitmap = base.copy(Config.ARGB_8888, true);

		String text = String.valueOf(markersCount);
		paint.getTextBounds(text, 0, text.length(), bounds);
		float x = bitmap.getWidth() / 2.0f;
		float y = (bitmap.getHeight() - bounds.height()) / 2.0f - bounds.top;

		Canvas canvas = new Canvas(bitmap);
		canvas.drawText(text, x, y, paint);

		BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
		return markerOptions.icon(icon);
	
	}
	
}