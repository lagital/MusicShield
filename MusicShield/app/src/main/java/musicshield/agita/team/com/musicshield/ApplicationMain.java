package musicshield.agita.team.com.musicshield;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by pborisenko on 5/13/2016.
 */
public class ApplicationMain extends Application {

    public static SQLiteDatabase WRITE_DB;
    public static SQLiteDatabase READ_DB;

    @Override
    public void onCreate() {
        super.onCreate();

        AsyncTaskInitDB task = new AsyncTaskInitDB(this);
        task.execute();
    }

    public static void setWriteDb(SQLiteDatabase writeDb) {
        WRITE_DB = writeDb;
    }

    public static void setReadDb(SQLiteDatabase readDb) {
        READ_DB = readDb;
    }
}
