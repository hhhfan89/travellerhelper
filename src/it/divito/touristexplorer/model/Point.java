package it.divito.touristexplorer.model;

import java.io.Serializable;
import java.util.ArrayList;

import android.location.Location;

/**
 * This class describe the point on a map
 * 
 * @author Stefano Di Vito
 *
 */

public class Point implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Location location;
	private int color;
	private ArrayList<Poi> listPoi = new ArrayList<Poi>();
	
	public Point(){
		
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public ArrayList<Poi> getPoiList(){
		return listPoi;
	}
	
	public void addPoi(Poi poi){
		listPoi.add(poi);
	}
	
	
/*
	public int getPoiType(){
		return type;
	}
	
	public void setPoiType(int type) {
		this.type = type;
	}*/
	
}
