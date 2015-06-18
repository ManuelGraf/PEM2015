package pem.yara.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

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
