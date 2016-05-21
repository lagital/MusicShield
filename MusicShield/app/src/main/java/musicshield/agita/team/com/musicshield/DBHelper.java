package musicshield.agita.team.com.musicshield;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by pborisenko on 5/13/2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "MusicShield.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String SEP = ",";
    private static final String SQL_CREATE_TABLES =
            "CREATE TABLE " + CallColumns.TABLE_NAME + " (" +
                    CallColumns._ID + " INTEGER PRIMARY KEY," +
                    CallColumns.COL_CALL_NUMBER + TEXT_TYPE + SEP +
                    CallColumns.COL_CALL_TIME + TEXT_TYPE + SEP +
                    CallColumns.COL_CALL_STATUS + INT_TYPE + SEP +
                    CallColumns.COL_IS_ACTIVE + INT_TYPE +
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + CallColumns.TABLE_NAME;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "Constructor");
    }
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        db.execSQL(SQL_CREATE_TABLES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        Log.d(TAG, "onUpgrade");
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onDowngrade");
        onUpgrade(db, oldVersion, newVersion);
    }

    private static abstract class CallColumns implements BaseColumns {
        public static final String TABLE_NAME = "missed_call";
        public static final String COL_CALL_NUMBER = "number";
        public static final String COL_CALL_TIME = "date_time";
        public static final String COL_CALL_STATUS = "status";
        public static final String COL_IS_ACTIVE = "isactive";
    }

    private Integer callTypeToInt (CallType type) {
        Log.d(TAG, "callTypeToInt");
        switch (type) {
            case ANSWERED: return 1;
            case MISSED:   return 0;
            case BLOCKED:  return -1;
            default: return 0;
        }
    }

    private CallType intToCallType (Integer intg) {
        Log.d(TAG, "intToCallType");
        switch (intg) {
            case 1: return CallType.ANSWERED;
            case 0: return CallType.MISSED;
            case -1:return CallType.BLOCKED;
            default: return CallType.MISSED;
        }
    }

    /*-----------API-----------*/

    public void insertMissedCall (SQLiteDatabase db, String number, String dateTime, CallType type) {
        Log.d(TAG, "insertMissedCall: " + number);
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(CallColumns.COL_CALL_NUMBER, number);
        values.put(CallColumns.COL_CALL_TIME, dateTime);
        values.put(CallColumns.COL_CALL_STATUS, callTypeToInt(type));
        values.put(CallColumns.COL_IS_ACTIVE, 1);
        // Insert the new row, returning the primary key value of the new row
        try {
            db.insert(CallColumns.TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearMissedCalls (SQLiteDatabase db) {
        Log.d(TAG, "clearMissedCalls");
        // Define 'where' part of query.
        String condition = "1 = 1";
        // Issue SQL statement.
        ContentValues cv = new ContentValues();
        cv.put(CallColumns.COL_IS_ACTIVE, "0");
        try {
            db.update(CallColumns.TABLE_NAME, cv, condition, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Call> retrieveMissedCalls (SQLiteDatabase db) {
        Log.d(TAG, "retrieveMissedCalls");
        ArrayList<Call> calls = new ArrayList<Call>();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " +
                    CallColumns.TABLE_NAME + " WHERE " + CallColumns.COL_IS_ACTIVE
                    + " = 1 " +"ORDER BY _ID DESC", null);

            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    calls.add(new Call(
                            cursor.getString(cursor.getColumnIndex(CallColumns.COL_CALL_NUMBER)),
                            cursor.getString(cursor.getColumnIndex(CallColumns.COL_CALL_TIME)),
                            intToCallType(cursor.getInt(cursor.getColumnIndex(CallColumns.COL_CALL_STATUS)))
                    ));
                    cursor.moveToNext();
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calls;
    }

    public enum CallType {
        ANSWERED,
        MISSED,
        BLOCKED
    }

    /*-----------API-----------*/
    /*-------Debug API --------*/

    public void debugInsertMissedCall (SQLiteDatabase db, String number, String dateTime, CallType type) {
        Log.d(TAG, "debugInsertMissedCall: " + number);
        try {
            if (BuildConfig.DEBUG) {
                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(CallColumns.COL_CALL_NUMBER, number);
                values.put(CallColumns.COL_CALL_TIME, dateTime);
                values.put(CallColumns.COL_CALL_STATUS, callTypeToInt(type));
                values.put(CallColumns.COL_IS_ACTIVE, 1);

                // Insert the new row, returning the primary key value of the new row
                db.insert(CallColumns.TABLE_NAME, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*-------Debug API --------*/
}
