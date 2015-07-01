package pem.yara.entity;

import android.location.Location;

import java.util.ArrayList;

public class YaraRun {
    private int trackID;
    private double avgAccuracy;      // Meters
    private double avgBpm;
    private double avgSpeed;         // m/s
    private double runMinSpeed;      // m/s
    private double runMaxSpeed;      // m/s
    private double completionTime;   // Seconds
    private double runDistance;      // Meters
    private String date;
    private String trackString;
    private ArrayList<Location> myTrack;

    public YaraRun(int trackID, double avgBPM, ArrayList<Location> aTrack, String date) {
        this.trackID = trackID;
        this.avgBpm = avgBPM;
        this.myTrack = aTrack;
        this.date = date;

        evaluateTrack();
    }

    public YaraRun(int trackID, double avgBPM, String trackString, double duration, double distance, double avgSpeed, String date){
        this.trackID = trackID;
        this.avgBpm = avgBPM;
        this.trackString = trackString;
        this.date = date;
        this.completionTime=duration;
        this.runDistance=distance;
        this.avgSpeed=avgSpeed;
    }

    private void evaluateTrack(){
        if(myTrack.size() > 1){
            completionTime = (myTrack.get(myTrack.size()-1).getTime() - myTrack.get(0).getTime())/1000;
            runMinSpeed = 100.f;
            trackString="";
            // Iterate from first to *second* last element
            for(int i=0; i < myTrack.size()-1; i++){
                Location actLocation = myTrack.get(i);
                Location nextLocation = myTrack.get(i+1);

                // Distance statistics:
                float actDistance = actLocation.distanceTo(nextLocation);
                runDistance += actDistance;

                float actSpeed = actDistance/(nextLocation.getTime()-actLocation.getTime());
                trackString += actLocation.toString() + ";";

                // Speed statistics:
                if(actSpeed > runMaxSpeed)
                    runMaxSpeed = actSpeed;

                if(actSpeed < runMinSpeed)
                    runMinSpeed = actSpeed;

                avgAccuracy += actLocation.getAccuracy();
            }
            // Don't forget the last element:
            trackString += myTrack.get(myTrack.size()-1).toString();
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

    public double getAvgAccuracy() {
        return avgAccuracy;
    }

    public double getAvgBpm() {
        return avgBpm;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public double getRunMinSpeed() {
        return runMinSpeed;
    }

    public double getRunMaxSpeed() {
        return runMaxSpeed;
    }

    public double getCompletionTime() {
        return completionTime;
    }

    public double getRunDistance() {
        return runDistance;
    }

    public ArrayList<Location> getMyTrack() {
        return myTrack;
    }

    public String getDate() {
        return date;
    }

}
