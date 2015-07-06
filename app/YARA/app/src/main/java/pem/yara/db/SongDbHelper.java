package pem.yara.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import pem.yara.entity.YaraSong;

/**
 * Created by yummie on 17.06.2015.
 */
public class SongDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "YARA.db";

    /* Inner class that defines the table contents */
    public static abstract class SongDbItem implements BaseColumns {
        public static final String TABLE_NAME = "songs";
        public static final String COLUMN_NAME_ARTIST = "artist";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_URI = "uri";
        public static final String COLUMN_NAME_BPM = "bpm";
        public static final String COLUMN_NAME_PLAYCOUNT = "count";
        public static final String COLUMN_NAME_SCORE = "score";
        public static final String COLUMN_NAME_BLOCKED = "blocked";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + SongDbItem.TABLE_NAME + " (" +
                    SongDbItem._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SongDbItem.COLUMN_NAME_ARTIST + " TEXT, "+
                    SongDbItem.COLUMN_NAME_TITLE + " TEXT, "+
                    SongDbItem.COLUMN_NAME_URI + " TEXT, "+
                    SongDbItem.COLUMN_NAME_BPM + " REAL, "+
                    SongDbItem.COLUMN_NAME_PLAYCOUNT + " INTEGER, "+
                    SongDbItem.COLUMN_NAME_BLOCKED + " INTEGER, "+
                    SongDbItem.COLUMN_NAME_SCORE + " REAL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SongDbItem.TABLE_NAME;
    
    public SongDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public List<YaraSong> findSongsWithinRange(double lowerBound, double upperBound) {
        String[] projection = {SongDbItem._ID,SongDbItem.COLUMN_NAME_TITLE, SongDbItem.COLUMN_NAME_ARTIST,SongDbItem.COLUMN_NAME_URI , SongDbItem.COLUMN_NAME_BPM,SongDbItem.COLUMN_NAME_PLAYCOUNT,SongDbItem.COLUMN_NAME_SCORE,SongDbItem.COLUMN_NAME_BLOCKED};

        Cursor cursor = getReadableDatabase().query(
                SongDbHelper.SongDbItem.TABLE_NAME,
                projection,                         // The columns to return
                "bpm > " + lowerBound + " AND bpm < " + upperBound,
                null,                               // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                null                                // The sort order
        );

        cursor.moveToFirst();
        List<YaraSong> songs = new ArrayList<>();
        while(!cursor.isAfterLast()) {
            YaraSong song = new YaraSong(cursor.getInt(0),cursor.getString(1), cursor.getString(2), cursor.getString(3), Double.parseDouble(cursor.getString(4)),cursor.getDouble(5),cursor.getInt(6),cursor.getInt(7));
            songs.add(song);
            Log.d("SongDbHelper","song "+song.getId()+" fits to the range ["+lowerBound+","+upperBound+"]" );
            cursor.moveToNext();
        }
        cursor.close();
        Log.d("SongDbHelper",songs.size()+" songs fit to the range ["+lowerBound+","+upperBound+"]" );
        return songs;
    }

    public List<YaraSong> getAllSongs(){
        String[] projection = {SongDbItem._ID,SongDbItem.COLUMN_NAME_TITLE, SongDbItem.COLUMN_NAME_ARTIST,SongDbItem.COLUMN_NAME_URI , SongDbItem.COLUMN_NAME_BPM,SongDbItem.COLUMN_NAME_PLAYCOUNT,SongDbItem.COLUMN_NAME_SCORE,SongDbItem.COLUMN_NAME_BLOCKED};

            Cursor cursor = getReadableDatabase().query(
                    SongDbHelper.SongDbItem.TABLE_NAME,
                    projection,                         // The columns to return
                    null,
                    null,                               // The values for the WHERE clause
                    null,                               // don't group the rows
                    null,                               // don't filter by row groups
                    null                                // The sort order
            );

            cursor.moveToFirst();
            List<YaraSong> songs = new ArrayList<>();
            while(!cursor.isAfterLast()) {
                YaraSong song = new YaraSong(cursor.getInt(0),cursor.getString(1), cursor.getString(2), cursor.getString(3), Double.parseDouble(cursor.getString(4)),cursor.getDouble(5),cursor.getInt(6),cursor.getInt(7));
                songs.add(song);
                cursor.moveToNext();
            }
            cursor.close();
            return songs;

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db ,int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
