package it.divito.touristexplorer.model;

import java.io.Serializable;

/**
 * This class describe the photo POI 
 * 
 * @author Stefano Di Vito
 *
 */

public class Photo implements Serializable, Poi {

	private static final long serialVersionUID = 1L;
	
	private int id;
	private String title;
	private String description;
	private String datetime;
	private String path;
	
	public Photo(){
		
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDatetime() {
		return datetime;
	}

	@Override
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}
	
}	