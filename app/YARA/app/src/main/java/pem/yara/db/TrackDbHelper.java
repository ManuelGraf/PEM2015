package pem.yara.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

import pem.yara.entity.YaraTrack;

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
                    "NAME: " + c.getString(c.getColumnIndexOrThrow(TrackDbItem.COLUMN_NAME_TRACK_NAME))+" \n"+
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

    /**
     * Deletes a certain Track from the Database
     * @param trackID ID of the track to delete
     */
    public void saveTrackName(int trackID,String name){
        String[] w = {"" + trackID};
        ContentValues cv = new ContentValues();
        cv.put(TrackDbItem.COLUMN_NAME_TRACK_NAME, name);
        getWritableDatabase().update(TrackDbItem.TABLE_NAME,cv, TrackDbItem._ID + "=?", w);
        Log.d("saveTrackName", "named track #" + trackID + ": "+name);
    }

    // Insert one track into the table, return the new ID
    public int insertNewTrack(YaraTrack mYaraTrack){
        ContentValues cv = new ContentValues();

        cv.put(TrackDbItem.COLUMN_NAME_TRACK_NAME, mYaraTrack.getTitle());
        cv.put(TrackDbItem.COLUMN_NAME_LENGTH, mYaraTrack.getLength());
        cv.put(TrackDbItem.COLUMN_NAME_PATH, mYaraTrack.getPathString());
        cv.put(TrackDbItem.COLUMN_NAME_DATE_CREATED, mYaraTrack.getDate_created());

        Log.d("TrackDBHelper", "Registering new Track: " + cv.toString());

        onCreate(getWritableDatabase());
        getWritableDatabase().insert(TrackDbItem.TABLE_NAME, null, cv);

        // Read current newest ID
        String[] projection = {TrackDbItem._ID };
        Cursor c = getReadableDatabase().query(TrackDbItem.TABLE_NAME,
                projection,
                null, null, null, null, TrackDbItem._ID + " DESC");

        c.moveToFirst();
        int newID = c.getInt(c.getColumnIndexOrThrow(TrackDbItem._ID));
        c.close();

        Log.d("TrackDBHelper", "Track newly registered. New ID: " + newID);

        return newID;
    }

    public void setTrackName(int ID, String newName){

        ContentValues cv = new ContentValues();
        cv.put(TrackDbItem.TABLE_NAME, newName);

        String whereArgs[] = {"" + ID};
        getWritableDatabase().update(TrackDbItem.TABLE_NAME,
                cv,
                TrackDbItem._ID + "=?",
                whereArgs);
    }

    /**
     * Receives the Track with the given ID from the database. If no ID is given (ID < 0), all Tracks are received.
     * @param trackID ID of the Track to receive. If empty, all Tracks are received.
     * @return One or all Tracks from the Database.
     */
    public ArrayList<YaraTrack> getTracks(int trackID){

        ArrayList<YaraTrack> myResult = new ArrayList<>();

        String[] projection = {
                TrackDbHelper.TrackDbItem._ID,
                TrackDbHelper.TrackDbItem.COLUMN_NAME_TRACK_NAME,
                TrackDbHelper.TrackDbItem.COLUMN_NAME_PATH,
                TrackDbHelper.TrackDbItem.COLUMN_NAME_LENGTH,
                TrackDbHelper.TrackDbItem.COLUMN_NAME_DATE_CREATED
        };

        String sortOrder = TrackDbHelper.TrackDbItem.COLUMN_NAME_DATE_CREATED + " DESC";

        String selection;
        String[] selectionArgs;

        if(trackID < 0){
            // No ID given: Get all Tracks ==> No selection parameters
            selection=null;
            selectionArgs = null;
        } else {
            selection= TrackDbItem._ID + "=?";
            selectionArgs = new String[]{trackID + ""};
        }

        Cursor c = getReadableDatabase().query(
                TrackDbHelper.TrackDbItem.TABLE_NAME,
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
            myResult.add(new YaraTrack(
                    c.getInt(c.getColumnIndexOrThrow(TrackDbHelper.TrackDbItem._ID)),
                    c.getString(c.getColumnIndexOrThrow(TrackDbHelper.TrackDbItem.COLUMN_NAME_TRACK_NAME)),
                    c.getString(c.getColumnIndexOrThrow(TrackDbHelper.TrackDbItem.COLUMN_NAME_PATH)),
                    c.getString(c.getColumnIndexOrThrow(TrackDbHelper.TrackDbItem.COLUMN_NAME_DATE_CREATED)),
                    c.getInt(c.getColumnIndexOrThrow(TrackDbHelper.TrackDbItem.COLUMN_NAME_LENGTH))
            ));
            c.moveToNext();
            offset++;
        }
        c.close();

        return myResult;
    }

}
