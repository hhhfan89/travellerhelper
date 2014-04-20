package it.divito.touristexplorer.model;

/**
 * This interface describe POI's field
 * 
 * @author Stefano Di Vito
 *
 */

public interface Poi {

	public int getId();
	public void setId(int id);
	public String getTitle();
	public void setTitle(String title);
	public String getDescription();
	public void setDescription(String description);
	public String getDatetime();
	public void setDatetime(String datetime);
	public String getPath();
	public void setPath(String path);
	
}
