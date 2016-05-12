package musicshield.agita.team.com.musicshield;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.sql.Time;
import java.util.Date;

/**
 * Created by pborisenko on 5/13/2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MusicShield.db";

    private static final String TEXT_TYPE = "TEXT";
    private static final String INT_TYPE = "INTEGER";
    private static final String SEP = ",";
    private static final String SQL_CREATE_TABLES =
            "CREATE TABLE " + CallColumns.TABLE_NAME + " (" +
                    CallColumns._ID + " INTEGER PRIMARY KEY," +
                    CallColumns.COL_CALL_NUMBER + INT_TYPE + SEP +
                    CallColumns.COL_CALL_DATE + TEXT_TYPE + SEP +
                    CallColumns.COL_CALL_TIME + INT_TYPE + SEP +
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + CallColumns.TABLE_NAME;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLES);
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

    private static abstract class CallColumns implements BaseColumns {
        public static final String TABLE_NAME = "missed_call";
        public static final String COL_CALL_NUMBER = "number";
        public static final String COL_CALL_DATE = "date";
        public static final String COL_CALL_TIME = "time";
    }

    // API
    public long insertCall (SQLiteDatabase db, Integer number, Date date, Time time) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(CallColumns.COL_CALL_NUMBER, number);
        values.put(CallColumns.COL_CALL_DATE, date.toString());
        values.put(CallColumns.COL_CALL_TIME, time.getTime());

        // Insert the new row, returning the primary key value of the new row
        return db.insert(CallColumns.TABLE_NAME, null, values);
    }
}
