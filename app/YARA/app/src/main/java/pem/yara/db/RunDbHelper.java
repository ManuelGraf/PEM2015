package pem.yara.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.provider.BaseColumns;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by yummie on 17.06.2015.
 */
public class RunDbHelper  extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "YARA.db";

    /* Inner class that defines the table contents */
    public static abstract class RunDbItem implements BaseColumns {
        public static final String TABLE_NAME = "runs";
        public static final String COLUMN_NAME_TRACK = "track_id";
        public static final String COLUMN_NAME_AVG_BPM = "avg_bpm";
        public static final String COLUMN_NAME_AVG_SPEED = "avg_speed";
        public static final String COLUMN_NAME_DURATION = "completion_time";
        public static final String COLUMN_NAME_DATE = "date";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + RunDbItem.TABLE_NAME + " (" +
                    RunDbItem._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RunDbItem.COLUMN_NAME_TRACK + " INTEGER, "+
                    RunDbItem.COLUMN_NAME_AVG_BPM + " REAL, "+
                    RunDbItem.COLUMN_NAME_AVG_SPEED + " INTEGER, "+
                    RunDbItem.COLUMN_NAME_DURATION + " REAL, " +
                    RunDbItem.COLUMN_NAME_DATE + " TEXT, "+
                    "FOREIGN KEY ("+RunDbItem.COLUMN_NAME_TRACK+") REFERENCES "+ TrackDbHelper.TrackDbItem.TABLE_NAME+" ("+TrackDbHelper.TrackDbItem._ID+"))";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RunDbItem.TABLE_NAME;

    public RunDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void resetDB(){
        Log.d("RunDbHelper", "!!! resetting database");
        getWritableDatabase().execSQL(SQL_DELETE_ENTRIES);
        onCreate(getWritableDatabase());
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    public void listEntries(){
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                RunDbItem._ID,
                RunDbItem.COLUMN_NAME_TRACK,
                RunDbItem.COLUMN_NAME_AVG_BPM,
                RunDbItem.COLUMN_NAME_AVG_SPEED,
                RunDbItem.COLUMN_NAME_DATE,
                RunDbItem.COLUMN_NAME_DURATION

        };

        String sortOrder = RunDbItem.COLUMN_NAME_DURATION + " DESC";
        Cursor c = db.query(
                RunDbItem.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
        c.moveToFirst();

        int offset = 0;

        Log.d("RunDBHelper", "Runs in Database:");
        while(offset < c.getCount()){
            Log.d("RunDbHelper",
                    "ID: " + c.getString(c.getColumnIndexOrThrow(RunDbItem._ID))+" \n"+
                    "TRACK NAME: " + c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_TRACK))+" \n"+
                    "AVG_BPM: " + c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_AVG_BPM))+" \n"+
                    "AVG_SPEED: " + c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_AVG_SPEED))+" \n"+
                    "DATE: " + c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_DATE))+" \n"+
                    "DURATION: " + c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_DURATION)) + " \n" +
                            "---------------------------------------------");
            c.moveToNext();
            offset++;
        }
    }

    /**
     * Writes one run into the Runs-Table, linking it to the corresponding track (if the user runs a known track) or inserting a new track into the database.
     * @param trackID If the user is entering a run on a known track, this ID identifies the track in the Database. In this case, must be > 0.
     *                If the user is entering a new track, this must be -1
     * @param aTrack A List of Location objects, implicitly containing all information about the run
     * @param name The name of the track. Used only if the track is to be newly inserted into the database
     * @param avgBPM Average Beats per Minute = Average steps per minute
     */
    public void insertRun(long trackID, ArrayList<Location> aTrack, String name, float avgBPM, Context c){

        String myDateTime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(Calendar.getInstance().getTime());

        if(trackID == -1) {
            Log.d("insertRun", "Inserting new track into Database");
            TrackDbHelper mTrackDBDbHelper = new TrackDbHelper(c);
            trackID = mTrackDBDbHelper.insertNewTrack(name, aTrack, myDateTime);
        }

        Log.d("insertRun", "Inserting run into Database");

        double myDuration = 0.d;
        double myAvgSpeed = 0.d;

        if(aTrack.size() > 0)
            for(int i=1; i<aTrack.size(); i++){
                Location lastLocation = aTrack.get(i-1);
                Location thisLocation = aTrack.get(i);

                // time between two points
                Long tmpTime = thisLocation.getTime() - lastLocation.getTime();
                myDuration +=tmpTime;

                // Speed between two points
                myAvgSpeed += lastLocation.distanceTo(thisLocation)/tmpTime;
            }
        else {
            Log.d("insertRun", "Track size was Zero!");
            return;
        }

        myAvgSpeed /= aTrack.size();

        Log.d("insertRun", "Run information: " + trackID + ", " + avgBPM + ", " + myDateTime + ", " + myDuration + ", " + myAvgSpeed);

        ContentValues cv = new ContentValues();
        cv.put(RunDbItem.COLUMN_NAME_TRACK, trackID);
        cv.put(RunDbItem.COLUMN_NAME_AVG_BPM, avgBPM);
        cv.put(RunDbItem.COLUMN_NAME_DATE, myDateTime);
        cv.put(RunDbItem.COLUMN_NAME_DURATION, myDuration);
        cv.put(RunDbItem.COLUMN_NAME_AVG_SPEED, myAvgSpeed);

        Log.d("insertRun", cv.toString());

        Log.d("insertRun", "Trying to insert run...");
        this.onCreate(this.getWritableDatabase());
        this.getWritableDatabase().insert(RunDbItem.TABLE_NAME, null, cv);
        Log.d("insertRun", "Run inserted!");

    }
}
