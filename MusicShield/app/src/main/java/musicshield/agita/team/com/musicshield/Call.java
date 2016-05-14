package musicshield.agita.team.com.musicshield;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Created by pborisenko on 5/13/2016.
 */
public class Call {
    private static final String TAG = "Call";

    public String number;
    public String date_time;
    public DBHelper.CallType status;

    Call(String i_number, String i_date_time, DBHelper.CallType i_type){
        number = i_number;
        date_time = i_date_time;
        status = i_type;
    }

    public static String getNumName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(!cursor.isClosed()) {
            cursor.close();
        }

        return (contactName == null || contactName == "")?phoneNumber:contactName;
    }
}
