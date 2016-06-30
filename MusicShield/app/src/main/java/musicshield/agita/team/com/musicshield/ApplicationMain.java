package musicshield.agita.team.com.musicshield;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import com.samsara.team.samsaralib.core.SamsaraCore;

/**
 * Created by pborisenko on 5/13/2016.
 */
public class ApplicationMain extends Application {
    private static final String TAG = "ApplicationMain";

    private static final String  PREF_NAME = "musicshield.agita.team.com.musicshield";
    private static final String  START_COUNTER = "START_COUNTER";
    private static final Integer START_LIMIT = 10;
    public static final String  IS_RATE_DONE_NAME = "IS_RATE_DONE";

    public static SQLiteDatabase WRITE_DB;
    public static SQLiteDatabase READ_DB;
    public static Boolean NOTIFY_RATE = false;

    private static SharedPreferences mSP;
    private static SharedPreferences.Editor mEditor;

    @Override
    public void onCreate() {
        super.onCreate();

        AsyncTaskInitDB task_a = new AsyncTaskInitDB(this);
        task_a.execute();

        if (BuildConfig.DEBUG) {
            SamsaraCore.setLocale(this, null, R.string.locale);
        }

        mSP = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mSP.edit();

        Boolean isRateDone = mSP.getBoolean(IS_RATE_DONE_NAME, false);
        if (!isRateDone) {
            Integer startCounter = mSP.getInt(START_COUNTER, 0);
            if (startCounter >= START_LIMIT) {
                NOTIFY_RATE = true;
            } else {
                Log.d(TAG, "Start counter raised to " + Integer.toString(startCounter + 1));
                mEditor.putInt(START_COUNTER, startCounter + 1);
                mEditor.apply();
            }
        }
    }

    public static void setWriteDb(SQLiteDatabase writeDb) {
        WRITE_DB = writeDb;
    }
    public static void setReadDb(SQLiteDatabase readDb) {
        READ_DB = readDb;
    }

    private class AsyncTaskInitDB extends AsyncTask<Void,Void,Void> {
        private static final String TAG = "AsyncTaskInitDB";

        private Context mContext;

        AsyncTaskInitDB (Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");
            DBHelper mDbHelper = new DBHelper(mContext);
            ApplicationMain.setWriteDb(mDbHelper.getWritableDatabase());
            ApplicationMain.setReadDb(mDbHelper.getReadableDatabase());
            return null;
        }

    }

    public static void stopRateNotification () {
        mEditor.putBoolean(IS_RATE_DONE_NAME, true);
        mEditor.apply();
    }

    public static String getPackageName(Context context) {
        if (BuildConfig.DEBUG) {
            return "com.google.android.apps.maps";
        } else {
            return context.getPackageName().trim();
        }
    }
}
