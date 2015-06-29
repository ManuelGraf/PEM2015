package pem.yara.entity;

/**
 * Created by yummie on 29.06.2015.
 */
public class YaraRun {

    private int trackID;
    private float avgBpm;
    private float avgSpeed;
    private int completionTime;
    private String date;

    public YaraRun(int trackID, float avgBpm, float avgSpeed, int completionTime, String date) {
        this.trackID = trackID;
        this.avgBpm = avgBpm;
        this.avgSpeed = avgSpeed;
        this.completionTime = completionTime;
        this.date = date;
    }

    public int getTrackID() {
        return trackID;
    }

    public void setTrackID(int trackID) {
        this.trackID = trackID;
    }

    public float getAvgBpm() {
        return avgBpm;
    }

    public void setAvgBpm(float avgBpm) {
        this.avgBpm = avgBpm;
    }

    public float getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(float avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
