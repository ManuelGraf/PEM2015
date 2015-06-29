package pem.yara.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by yummie on 17.06.2015.
 */
public class TrackDbHelper  extends SQLiteOpenHelper {

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
                    TrackDbItem._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TrackDbItem.COLUMN_NAME_TRACK_NAME + "TEXT ,"+
                    TrackDbItem.COLUMN_NAME_LENGTH + "REAL ,"+
                    TrackDbItem.COLUMN_NAME_PATH + "TEXT ,"+
                    TrackDbItem.COLUMN_NAME_DATE_CREATED + "TEXT";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TrackDbItem.TABLE_NAME;

    public TrackDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }
}
