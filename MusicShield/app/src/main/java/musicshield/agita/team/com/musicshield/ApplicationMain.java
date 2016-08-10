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

    public static SQLiteDatabase WRITE_DB;
    public static SQLiteDatabase READ_DB;

    @Override
    public void onCreate() {
        super.onCreate();

        AsyncTaskInitDB task_a = new AsyncTaskInitDB(this);
        task_a.execute();

        if (BuildConfig.DEBUG) {
            SamsaraCore.setLocale(this, null, R.string.locale);
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

    public static String getPackageName(Context context) {
        if (BuildConfig.DEBUG) {
            return "com.google.android.apps.maps";
        } else {
            return context.getPackageName().trim();
        }
    }
}
