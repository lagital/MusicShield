package musicshield.agita.team.com.musicshield;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.res.ResourcesCompat;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pborisenko on 5/13/2016.
 */
public class Contact {
    private static final String TAG = "Contact";
    private static final String CHECKED_NUMBERS = "CHECKED_NUMBERS";
    private static final String PREF_NAME = "musicshield.agita.team.com.musicshield";

    public ArrayList<String> numbers;
    public String name;
    public Drawable photo;
    public Boolean checked;
    public String id;

    Contact(ArrayList<String> i_numbers, String i_name, Drawable i_photo, Boolean i_checked){
        numbers = i_numbers;
        name = i_name;
        photo = i_photo;
        checked = i_checked;
    }

    public static ArrayList<Contact> retrieveContacts (Context context) {
        Log.d(TAG, "retrieveContacts");

        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        ArrayList<Contact> l = new ArrayList<>();
        String id;
        String name;
        Set<String> checkedNumbers;
        String u; // string uri
        checkedNumbers = sp.getStringSet(CHECKED_NUMBERS, new HashSet<String>());

        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
                + ("1") + "'";
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
                + " COLLATE LOCALIZED ASC";

        Drawable defaultPhoto = ResourcesCompat.getDrawable(context.getResources(),
                R.drawable.ic_person_black_36dp, null);

        Drawable photo = defaultPhoto;
        Uri contactPhotoUri;

        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(
                ContactsContract.Contacts.CONTENT_URI, null, selection
                        + " AND " + ContactsContract.Contacts.HAS_PHONE_NUMBER
                        + "=1", null, sortOrder);

        if (cur == null) {
            Log.d(TAG, "cursor is null");
            return l;
        } else if (cur.getCount() > 0) {
            Log.d(TAG, "cursor is not empty!");
            while (cur.moveToNext()) {
                Boolean checked = false;
                id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                u = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));

                if (u != null) {
                    contactPhotoUri = Uri.parse(u);
                    try {
                        InputStream inputStream = context.getContentResolver().openInputStream(contactPhotoUri);
                        photo = Drawable.createFromStream(inputStream, contactPhotoUri.toString());
                        inputStream.close();
                    } catch (Exception e) {
                        photo = defaultPhoto;
                        e.printStackTrace();
                    }
                }

                Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                try {
                    ArrayList<String> numbers = new ArrayList<>();
                    while (phones.moveToNext()) {
                        String n = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        numbers.add(n);
                        if (checkedNumbers.contains(n)) {
                            checked = true;
                        }
                    }
                    phones.close();
                    l.add(new Contact(numbers, name, photo, checked));
                } catch (Exception e) {
                    e.printStackTrace();
                    l.add(new Contact(new ArrayList<String>(), name, photo, checked));
                }

                photo = defaultPhoto;
            }
            cur.close();
        } else {
            Log.d(TAG, "cursor is empty");
        }
        return l;
    }

    public static boolean compareNumbers (String number1, String number2) {
        /* In case of mask the idea is that mask's length is equal to a number's length.
         * It is easy for users to fill empty characters of numbers as "*".
         */
        if (number1.length() != number2.length()) {
            return PhoneNumberUtils.compare(number1, number2);
        }
        // check number1 is a mask or not
        if (number1.contains("*")) {
            char[] numbers1 = number1.toCharArray();
            char[] numbers2 = number2.toCharArray();

            for (int i=0; i<numbers1.length; i++) {
                if (numbers1[i] != numbers2[i] && numbers1[i] != '*') {
                    Log.d(TAG, "compareNumbers: not matched");
                    return false;
                }
            }
        }
        return PhoneNumberUtils.compare(number1, number2);
    }
}