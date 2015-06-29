package pem.yara.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by yummie on 17.06.2015.
 */
public class TrackDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "YARA.db";

    /* Inner class that defines the table contents */
    public static abstract class TrackDbItem implements BaseColumns {
        public static final String TABLE_NAME = "tracks";
        public static final String COLUMN_NAME_TRACK_NAME = "name";
        public static final String COLUMN_NAME_PATH = "path"; // GeoLoc Punkte der Strecke als TEXT
        public static final String COLUMN_NAME_LENGTH = "length";
        public static final String COLUMN_NAME_DATE_CREATED = "date_created";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + TrackDbItem.TABLE_NAME + " (" +
                    TrackDbItem._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TrackDbItem.COLUMN_NAME_TRACK_NAME + " TEXT, "+
                    TrackDbItem.COLUMN_NAME_LENGTH + " REAL, "+
                    TrackDbItem.COLUMN_NAME_PATH + " TEXT, "+
                    TrackDbItem.COLUMN_NAME_DATE_CREATED + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TrackDbItem.TABLE_NAME;

    public TrackDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void resetDB(){
        Log.d("TrackDbHelper", "!!! resetting database");
        getWritableDatabase().execSQL(SQL_DELETE_ENTRIES);
        onCreate(getWritableDatabase());
    }
    public void listEntries(){
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                TrackDbItem._ID,
                TrackDbItem.COLUMN_NAME_TRACK_NAME,
                TrackDbItem.COLUMN_NAME_PATH,
                TrackDbItem.COLUMN_NAME_LENGTH,
                TrackDbItem.COLUMN_NAME_DATE_CREATED

        };

        String sortOrder = TrackDbItem._ID + " DESC";
        Cursor c = db.query(
                TrackDbItem.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
        c.moveToFirst();

        int offset = 0;

        Log.d("TrackDBHelper", "Tracks in Database:");
        while(offset < c.getCount()){
            Log.d("TrackDbHelper",
                    "ID: " + c.getString(c.getColumnIndexOrThrow(TrackDbItem._ID))+" \n"+
                    "NAME " + c.getString(c.getColumnIndexOrThrow(TrackDbItem.COLUMN_NAME_TRACK_NAME))+" \n"+
                    "POINTS: " + c.getString(c.getColumnIndexOrThrow(TrackDbItem.COLUMN_NAME_PATH))+" \n"+
                    "LENGTH: " + c.getString(c.getColumnIndexOrThrow(TrackDbItem.COLUMN_NAME_LENGTH))+" \n"+
                    "CREATED: " + c.getString(c.getColumnIndexOrThrow(TrackDbItem.COLUMN_NAME_DATE_CREATED)) + " \n" +
                    "---------------------------------------------");

            c.moveToNext();
            offset++;
        }
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    /**
     * Deletes a certain Track from the Database
     * @param trackID ID of the track to delete
     */
    public void deleteTrack(int trackID){
        String[] w = {"" + trackID};
        Log.d("deleteTrack", "Deleting Track " + trackID + "...");
        getWritableDatabase().delete(TrackDbItem.TABLE_NAME, TrackDbItem._ID + "=?", w);
        Log.d("deleteTrack", "Track " + trackID + " deleted.");
    }

    // Insert one track into the table
    public int insertNewTrack(String name, ArrayList<Location> path, String createdAt){
        ContentValues cv = new ContentValues();
        Double myLength = 0.d;
        String myPath = "";

        if(path != null)
            myPath = path.get(0).getLatitude() + "," + path.get(0).getLongitude();

        // Get length and list of points:
        if(!(path==null) && path.size() == 1)
            myLength = 0.d;
        else
            for(int i=1; i < path.size(); i++){
                myLength += path.get(i-1).distanceTo(path.get(i));
                myPath += ";" + path.get(0).getLatitude() + "," + path.get(0).getLongitude();
            }

        Log.d("TrackDBHelper", "Inserting Track: " + name + ", length: " + myLength + ", #points: " + path.size());

        cv.put(TrackDbItem.COLUMN_NAME_TRACK_NAME, name);
        cv.put(TrackDbItem.COLUMN_NAME_LENGTH, myLength);
        cv.put(TrackDbItem.COLUMN_NAME_PATH, myPath);
        cv.put(TrackDbItem.COLUMN_NAME_DATE_CREATED, createdAt);

        onCreate(getWritableDatabase());
        getWritableDatabase().insert(TrackDbItem.TABLE_NAME, null, cv);

        String[] projection = {TrackDbItem._ID };
        Cursor c = getReadableDatabase().query(TrackDbItem.TABLE_NAME,
                projection,
                null, null, null, null, TrackDbItem._ID + " DESC");

        c.moveToFirst();
        int newID = c.getInt(c.getColumnIndexOrThrow(TrackDbItem._ID));
        c.close();

        Log.d("TrackDBHelper", "Track inserted. ID: " + newID);

        return newID;
    }

    // TODO: Work out the connection between the list of tracks the user sees, and this method call.
    // How will a track be identified? Hidden field (containing ID) in the list?
    public void getTrackInfos(int trackID){
        // TODO: Work out the return type of this method
    }

}
