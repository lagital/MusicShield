package musicshield.agita.team.com.musicshield;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by pborisenko on 5/13/2016.
 */
public class AsyncTaskInitDB extends AsyncTask{

    private Context mContext;

    AsyncTaskInitDB (Context context) {
        mContext = context;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        DBHelper mDbHelper = new DBHelper(mContext);
        ApplicationMain.setWriteDb(mDbHelper.getWritableDatabase());
        ApplicationMain.setReadDb(mDbHelper.getReadableDatabase());
        return null;
    }

}
