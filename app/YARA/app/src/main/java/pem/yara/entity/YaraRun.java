package pem.yara.entity;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by yummie on 29.06.2015.
 */
public class YaraRun {
    private int trackID;
    private float avgAccuracy;      // Meters
    private float avgBpm;
    private float avgSpeed;         // m/s
    private float runMinSpeed;      // m/s
    private float runMaxSpeed;      // m/s
    private float completionTime;   // Seconds
    private float runDistance;      // Meters
    private String date;
//    private String trackString;
    private ArrayList<Location> myTrack;

    public YaraRun(int trackID, float avgBpm, ArrayList<Location> aTrack, String date) {
        this.trackID = trackID;
        this.avgBpm = avgBpm;
        this.myTrack = aTrack;
        this.date = date;

        evaluateTrack();
    }

    private void evaluateTrack(){
        if(myTrack.size() > 1){
            completionTime = (myTrack.get(myTrack.size()-1).getTime() - myTrack.get(0).getTime())/1000;
            runMinSpeed = 100.f;
            // Iterate from first to *second* last element
            for(int i=0; i < myTrack.size()-1; i++){
                Location actLocation = myTrack.get(i);
                Location nextLocation = myTrack.get(i+1);

                // Distance statistics:
                float actDistance = actLocation.distanceTo(nextLocation);
                runDistance += actDistance;

                float actSpeed = actDistance/(nextLocation.getTime()-actLocation.getTime());

                // Speed statistics:
                if(actSpeed > runMaxSpeed)
                    runMaxSpeed = actSpeed;

                if(actSpeed < runMinSpeed)
                    runMinSpeed = actSpeed;

                avgAccuracy += actLocation.getAccuracy();
            }
            // Don't forget the last element:
            avgAccuracy += myTrack.get(myTrack.size()-1).getAccuracy();
            avgAccuracy /= myTrack.size();
            avgSpeed = runDistance/completionTime;
        }
    }

    // Allow to change an unknown Track ID once the Track hits the DB
    public void setTrackID(int trackID) {
        this.trackID = trackID;
    }

    public int getTrackID() {
        return trackID;
    }

    public float getAvgAccuracy() {
        return avgAccuracy;
    }

    public float getAvgBpm() {
        return avgBpm;
    }

    public float getAvgSpeed() {
        return avgSpeed;
    }

    public float getRunMinSpeed() {
        return runMinSpeed;
    }

    public float getRunMaxSpeed() {
        return runMaxSpeed;
    }

    public float getCompletionTime() {
        return completionTime;
    }

    public float getRunDistance() {
        return runDistance;
    }

    public ArrayList<Location> getMyTrack() {
        return myTrack;
    }

    public String getDate() {
        return date;
    }

}
