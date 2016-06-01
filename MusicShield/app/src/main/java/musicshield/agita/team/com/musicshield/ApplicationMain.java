package musicshield.agita.team.com.musicshield;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by pborisenko on 5/13/2016.
 */
public class ApplicationMain extends Application {
    private static final String TAG = "ApplicationMain";
    private static final String PREF_NAME = "musicshield.agita.team.com.musicshield";

    public static SQLiteDatabase WRITE_DB;
    public static SQLiteDatabase READ_DB;
    public static SharedPreferences PREFERENCES;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        AsyncTaskInitDB task_a = new AsyncTaskInitDB(this);
        task_a.execute();
        AsyncTaskInitPreferences task_b = new AsyncTaskInitPreferences(this);
        task_b.execute();

    }

    public static void setWriteDb(SQLiteDatabase writeDb) {
        WRITE_DB = writeDb;
    }
    public static void setReadDb(SQLiteDatabase readDb) {
        READ_DB = readDb;
    }

    private class AsyncTaskInitDB extends AsyncTask {
        private static final String TAG = "AsyncTaskInitDB";

        private Context mContext;

        AsyncTaskInitDB (Context context) {
            Log.d(TAG, "Constructor");
            mContext = context;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            Log.d(TAG, "doInBackground");
            DBHelper mDbHelper = new DBHelper(mContext);
            ApplicationMain.setWriteDb(mDbHelper.getWritableDatabase());
            ApplicationMain.setReadDb(mDbHelper.getReadableDatabase());
            return null;
        }

    }

    private class AsyncTaskInitPreferences extends AsyncTask {
        private static final String TAG = "AsyncTaskInitSettings";

        AsyncTaskInitPreferences    (Context context) {
            Log.d(TAG, "Constructor");
        }

        @Override
        protected Object doInBackground(Object[] params) {
            Log.d(TAG, "doInBackground");
            PREFERENCES = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            return null;
        }

    }
}
