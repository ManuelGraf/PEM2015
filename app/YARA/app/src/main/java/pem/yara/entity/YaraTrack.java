package pem.yara.entity;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by yummie on 29.06.2015.
 */
public class YaraTrack {
    private int id;
    private String title;
    private ArrayList<Location> path;
    private String pathString;
    private String date_created;
    private float length;

    public YaraTrack(int id, String title, ArrayList<Location> path, String date_created) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.date_created = date_created;
        this.pathString="";
        evaluateTrack();
    }

    /**
     * This constructor is only to be used for displaying a List of Tracks! It is not suited to store a track into the database.
     * @param id ID of the Track (must be known in Database)
     * @param title User-given Title of the track
     * @param path List of Points (Latitude,Longitude) separated by ";"
     * @param date_created Creation date
     * @param length Length in meters
     */
    public YaraTrack(int id, String title, String path, String date_created, float length){
        this.id=id;
        this.title=title;
        this.pathString=path;
        this.date_created=date_created;
        this.length=length;
    }

    private void evaluateTrack(){
        for(int i=0; i < path.size()-1; i++){
            length += path.get(i).distanceTo(path.get(i+1));
            pathString += path.get(i).getLatitude() + "," + path.get(i).getLongitude() + ";";
        }

        if(path.size()>0)
            pathString += path.get(path.size()-1).getLatitude() + "," + path.get(path.size()-1).getLongitude();
    }

    public int getId() { return id; }

    public String getTitle() {
        return title;
    }

    public ArrayList<Location> getPath() {
        return path;
    }

    public String getDate_created() {
        return date_created;
    }

    public float getLength() {
        return length;
    }

    public String getPathString() {
        return pathString;
    }
}
