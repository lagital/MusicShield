package musicshield.agita.team.com.musicshield;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import java.io.FileNotFoundException;
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

        ArrayList<Contact> l = new ArrayList<>();
        String id;
        String name;
        String n; // number
        Boolean checked = false;
        Set<String> checkedNumbers;
        String u; // string uri
        Uri m;
        checkedNumbers = ApplicationMain.PREFERENCES.getStringSet(CHECKED_NUMBERS, null);

        if (checkedNumbers == null) {
            checkedNumbers = new HashSet<>();
        }

        Drawable photo = ResourcesCompat.getDrawable(context.getResources(),
                R.drawable.ic_person_black_36dp, null);
        Uri contactPhotoUri;
        ArrayList<String> numbers = new ArrayList<>();

        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_GROUP_URI,
                null, null, null, null);

        if (cur == null) {
            Log.d(TAG, "cursor is null");
            return l;
        } else if (cur.getCount() > 0) {
            Log.d(TAG, "cursor is not empty!");
            while (cur.moveToNext()) {
                id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                u = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));

                if (u != null) {
                    Log.d(TAG, "contact has photo");
                    contactPhotoUri = Uri.parse(u);
                    try {
                        InputStream inputStream = context.getContentResolver().openInputStream(contactPhotoUri);
                        photo = Drawable.createFromStream(inputStream, contactPhotoUri.toString());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Log.d(TAG, "contact has phone number");
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    if (pCur != null) {
                        while (pCur.moveToNext()) {
                            n = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                            if (checkedNumbers.contains(n)) {
                                checked = true;
                            }
                            numbers.add(n);
                        }
                        pCur.close();
                    }
                }
                l.add(new Contact(numbers, name, photo, checked));
                checked = false;
                numbers.clear();
            }
            cur.close();
        } else {
            Log.d(TAG, "cursor is empty");
        }
        return l;
    }
}
