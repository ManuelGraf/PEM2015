package pem.yara.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

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
        public static final String COLUMN_NAME_TIME = "completion_time";
        public static final String COLUMN_NAME_DATE = "date";

    }

    
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + RunDbItem.TABLE_NAME + " (" +
                    RunDbItem._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    RunDbItem.COLUMN_NAME_TRACK + "TEXT ,"+
                    RunDbItem.COLUMN_NAME_AVG_BPM + "REAL ,"+
                    RunDbItem.COLUMN_NAME_AVG_SPEED + "INTEGER ,"+
                    RunDbItem.COLUMN_NAME_DATE + "TEXT,"+
                    "FOREIGN KEY("+RunDbItem.COLUMN_NAME_TRACK+") REFERENCES "+ SongDbHelper.SongDbItem.TABLE_NAME+"("+SongDbHelper.SongDbItem._ID+")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RunDbItem.TABLE_NAME;

    public RunDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void resetDB(SQLiteDatabase db){
        Log.d("RunDbHelper", "!!! resetting database");
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
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
                RunDbItem.COLUMN_NAME_TRACK,
                RunDbItem.COLUMN_NAME_AVG_BPM,
                RunDbItem.COLUMN_NAME_AVG_SPEED,
                RunDbItem.COLUMN_NAME_DATE,
                RunDbItem.COLUMN_NAME_TIME

        };

        String sortOrder = RunDbItem.COLUMN_NAME_TIME + " DESC";
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

        while(offset < c.getCount()){
            Log.d("RunDbHelper", c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_TRACK))+" "+
                    c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_AVG_BPM))+" "+
                    c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_AVG_SPEED))+" "+
                    c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_DATE))+" "+
                    c.getString(c.getColumnIndexOrThrow(RunDbItem.COLUMN_NAME_TIME)));
            c.moveToNext();
            offset++;

        }
    }
}
