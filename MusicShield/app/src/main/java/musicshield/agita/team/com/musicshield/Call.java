package musicshield.agita.team.com.musicshield;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.format.DateUtils;

import java.text.DateFormat;

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

    public static String getFormattedCallDate(Context context, String date) {
        long t;
        try {
            t = DateFormat.getDateInstance().parse(date).getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return date;
        }

        if (DateUtils.isToday(t)) {
            return date.replaceAll("^(.*?)\\.", context.getResources()
                    .getString(R.string.date_today_title) + ".");
        }

        if (DateUtils.isToday(t + 86400000)) {
            return date.replaceAll("^(.*?)\\.", context.getResources()
                    .getString(R.string.date_yesterday_title) + ".");
        }

        return date;
    }
}
