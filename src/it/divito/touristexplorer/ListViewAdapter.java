package it.divito.touristexplorer;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import android.widget.ImageView;


/**
 * This class allows the display of the elements that make up the sidebar
 * 
 * @author Stefano Di Vito
 *
 */
public class ListViewAdapter extends BaseAdapter {
	
	Context context;
	String[] mTitle;
	String[] mSubTitle;
	ArrayList<Integer> mIcon;
	LayoutInflater inflater;

	public ListViewAdapter(Context context, String[] title, String[] subtitle, ArrayList<Integer> icon) {
	    this.context = context;
	    this.mTitle = title;
	    this.mSubTitle = subtitle;
	    this.mIcon = icon;
	    
	    inflater = (LayoutInflater) context
	            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getViewTypeCount() {
	    return super.getViewTypeCount();
	}

	@Override
	public int getItemViewType(int position) {
	    return super.getItemViewType(position);
	}

	@Override
	public int getCount() {
	    return mTitle.length;
	}

	@Override
	public Object getItem(int position) {
	    return mTitle[position];
	}

	@Override
	public long getItemId(int position) {
	    return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
	    
		TextView txtTitle;
	    ImageView imgIcon;
	    
	    View itemView = inflater.inflate(R.layout.drawer_list_item, parent,
	            false);

	    txtTitle = (TextView) itemView.findViewById(R.id.menurow_title);
	    
	    imgIcon = (ImageView) itemView.findViewById(R.id.menurow_icon);

	    txtTitle.setText(mTitle[position]);
	   
	    imgIcon.setImageResource(mIcon.get(position));
	    return itemView;
	}

}