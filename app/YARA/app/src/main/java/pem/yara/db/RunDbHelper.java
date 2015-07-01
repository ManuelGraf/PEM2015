package pem.yara.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

import pem.yara.entity.YaraRun;
import pem.yara.entity.YaraTrack;

public class RunDbHelper  extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "YARA.db";

    /* Inner class that defines the table contents */
    public static abstract class RunDbItem implements BaseColumns {
        public static final String TABLE_NAME = "runs";
        public static final String COLUMN_NAME_TRACK_ID = "track_id";
        public static final String COLUMN_NAME_AVG_BPM = "avg_bpm";
        public static final String COLUMN_NAME_AVG_SPEED = "avg_speed";
        public static final String COLUMN_NAME_DURATION = "completion_time";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_DISTANCE = "distance";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + RunDbItem.TABLE_NAME + " (" +
                    RunDbItem._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RunDbItem.COLUMN_NAME_TRACK_ID + " INTEGER, "+
                    RunDbItem.COLUMN_NAME_AVG_BPM + " REAL, "+
                    RunDbItem.COLUMN_NAME_AVG_SPEED + " INTEGER, "+
                    RunDbItem.COLUMN_NAME_DURATION + " REAL, " +
                    RunDbItem.COLUMN_NAME_DISTANCE + " REAL, "+
                    RunDbItem.COLUMN_NAME_DATE + " TEXT, "+
                    "FOREIGN KEY ("+RunDbItem.COLUMN_NAME_TRACK_ID +") REFERENCES "+ TrackDbHelper.TrackDbItem.TABLE_NAME+" ("+TrackDbHelper.TrackDbItem._ID+"))";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RunDbItem.TABLE_NAME;

    public RunDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void resetDB(){
        Log.d("RunDbHelper", "!!! resetting database: " + SQL_DELETE_ENTRIES);
        getWritableDatabase().execSQL(SQL_DELETE_ENTRIES);
        onCreate(getWritableDatabase());
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.d("RunDBHelper", SQL_CREATE_ENTRIES);
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
                RunDbItem.COLUMN_NAME_TRACK_ID,
                RunDbItem.COLUMN_NAME_AVG_BPM,
                RunDbItem.COLUMN_NAME_AVG_SPEED,
                RunDbItem.COLUMN_NAME_DATE,
                RunDbItem.COLUMN_NAME_DISTANCE,
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
                    "TRACK ID: " + c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_TRACK_ID))+" \n"+
                    "AVG_BPM: " + c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_AVG_BPM))+" \n"+
                    "AVG_SPEED: " + c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_AVG_SPEED))+" \n"+
                    "DATE: " + c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_DATE))+" \n"+
                    "DISTANCE: " + c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_DISTANCE))+" \n"+
                    "DURATION: " + c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_DURATION)) + " \n" +
                            "---------------------------------------------");
            c.moveToNext();
            offset++;
        }
    }

    /**
     * Writes one run into the Runs-Table, linking it to the corresponding track (if the user runs a known track) or inserting a new track into the database.
     */

    public YaraRun insertRun(YaraRun mYaraRun, Context c){

        // If this happens to be a Run on a new Track, we have to register the Track first.
        // This means assigning a new ID (and a name):
        if(mYaraRun.getTrackID() == -1) {
            Log.d("insertRun", "Inserting new track into Database");
            TrackDbHelper mTrackDBDbHelper = new TrackDbHelper(c);
            // TODO: Wie bzw. wann benennt der User seine Tracks?
            YaraTrack mYaraTrack = new YaraTrack(-1, "new Track name", mYaraRun.getMyTrack(), mYaraRun.getDate());

            mYaraRun.setTrackID(mTrackDBDbHelper.insertNewTrack(mYaraTrack));
        }

        ContentValues cv = new ContentValues();
        cv.put(RunDbItem.COLUMN_NAME_TRACK_ID, mYaraRun.getTrackID());
        cv.put(RunDbItem.COLUMN_NAME_AVG_BPM, mYaraRun.getAvgBpm());
        cv.put(RunDbItem.COLUMN_NAME_DATE, mYaraRun.getDate());
        cv.put(RunDbItem.COLUMN_NAME_DISTANCE, mYaraRun.getRunDistance());
        cv.put(RunDbItem.COLUMN_NAME_DURATION, mYaraRun.getCompletionTime());
        cv.put(RunDbItem.COLUMN_NAME_AVG_SPEED, mYaraRun.getAvgSpeed());

        Log.d("insertRun", "Inserting Run...: " + cv.toString());

        this.onCreate(this.getWritableDatabase());
        this.getWritableDatabase().insert(RunDbItem.TABLE_NAME, null, cv);
        Log.d("insertRun", "Run inserted!");

        return mYaraRun;
    }


    /**
     * Receives the Run with the given ID from the database. If no ID is given (ID < 0), all Runs are received.
     * @param runID ID of the Run to receive. If empty, all Runs are received.
     * @param trackID ID of a Track. If > 0, only Runs on this Track will be returned.
     * @return One or all Runs from the Database.
     */
    public ArrayList<YaraRun> getRuns(int runID, int trackID){

        ArrayList<YaraRun> myResult = new ArrayList<>();

        String[] projection = {
                RunDbItem._ID,
                RunDbItem.COLUMN_NAME_TRACK_ID,
                RunDbItem.COLUMN_NAME_DATE,
                RunDbItem.COLUMN_NAME_DURATION,
                RunDbItem.COLUMN_NAME_DISTANCE,
                RunDbItem.COLUMN_NAME_AVG_BPM,
                RunDbItem.COLUMN_NAME_AVG_SPEED
        };

        String sortOrder = RunDbItem.COLUMN_NAME_DATE + " DESC";

        String selection;
        String[] selectionArgs;

        // Build selection: Ask for Run_ID and Track_ID respectively.
        if(runID < 0){
            // No ID given: Get all Tracks ==> No selection parameters
            selection=null;
            selectionArgs = null;
        } else {
            selection= RunDbItem._ID + "=?";
            selectionArgs = new String[]{runID + ""};
        }

        if(trackID > 0){
            if(selection==null){
                selection = RunDbItem.COLUMN_NAME_TRACK_ID + "=?";
                selectionArgs = new String[]{trackID+""};
            } else {
                selection += " AND " + RunDbItem.COLUMN_NAME_TRACK_ID + "=?";
                selectionArgs = new String[]{selectionArgs[0], trackID + ""};
            }
        }

        Cursor c = getReadableDatabase().query(
                RunDbItem.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        c.moveToFirst();

        int offset = 0;

        while(offset < c.getCount()){
            myResult.add(new YaraRun(
                    c.getInt(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_TRACK_ID)),
                    c.getDouble(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_AVG_BPM)),
                    "",
                    c.getDouble(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_DURATION)),
                    c.getDouble(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_DISTANCE)),
                    c.getDouble(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_AVG_SPEED)),
                    c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_DATE))
            ));
            c.moveToNext();
            offset++;
        }
        c.close();

        return myResult;
    }

    /**
     * Receives the last run on a given Track
     * @param trackID Track of which the last run will be returned
     * @return Goddamnit, read!
     */
    public YaraRun getLastRunToTrack(int trackID){
        YaraRun myResult;

        String[] projection = {RunDbItem._ID};
        String selection = RunDbItem.COLUMN_NAME_TRACK_ID + "=?";
        String[] selectionArgs = {trackID + ""};
        String order = RunDbItem.COLUMN_NAME_DATE + " DESC";
        String group = RunDbItem.COLUMN_NAME_TRACK_ID;
        String having = RunDbItem.COLUMN_NAME_DATE + "=MAX(" + RunDbItem.COLUMN_NAME_DATE + ")";

        Cursor c = getReadableDatabase().query(
                RunDbItem.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                group,
                having,
                order
        );

        c.moveToFirst();
        Log.d("getLastRunToTrack", "Letzter Run (ID): " + c.getInt(c.getColumnIndexOrThrow(RunDbItem._ID)));

        myResult = getRuns(c.getInt(c.getColumnIndexOrThrow(RunDbItem._ID)), -1).get(0);

        c.close();

        return myResult;
    }
}
