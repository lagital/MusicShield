package musicshield.agita.team.com.musicshield;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Settings Activity
 */
public class ActivitySettings extends AppCompatActivity {
    private static final String TAG = "ActivitySettings";

    private ActionBar mToolbar;
    private LinearLayout mSettingsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = getSupportActionBar();

        mSettingsList = (LinearLayout) findViewById(R.id.settings_list);
        LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        // support letter setting
        View v = inflater.inflate(R.layout.item_setting, null);
        CardView c = (CardView) v.findViewById(R.id.setting_card);
        ImageView i = (ImageView) v.findViewById(R.id.setting_icon);
        i.setImageResource(R.drawable.ic_email_black_36dp);
        TextView t = (TextView) v.findViewById(R.id.setting_text);
        t.setText(R.string.setting_support_letter);
        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Letter to Support");
                String uriText =
                        "mailto:" + getResources().getString(R.string.support_email_address) +
                                "?subject=" +
                                Uri.encode(getResources().getString(R.string.support_email_subject)) +
                                "&body=" + Uri.encode(getSystemInfo());
                Uri uri = Uri.parse(uriText);
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                sendIntent.setData(uri);
                startActivity(Intent.createChooser(sendIntent, "Send email..."));
            }
        });
        mSettingsList.addView(v);

        // share setting
        v = inflater.inflate(R.layout.item_setting, null);
        c = (CardView) v.findViewById(R.id.setting_card);
        i = (ImageView) v.findViewById(R.id.setting_icon);
        i.setImageResource(R.drawable.ic_share_black_36dp);
        t = (TextView) v.findViewById(R.id.setting_text);
        t.setText(R.string.setting_share);
        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Sharing");
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = getResources().getString(R.string.share_body);
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        getResources().getString(R.string.share_subject));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody
                + "https://play.google.com/store/apps/details?id="
                        + getPackageName());
                startActivity(Intent.createChooser(sharingIntent,
                        getResources().getString(R.string.share_title)));
            }
        });
        mSettingsList.addView(v);

        // rate setting
        v = inflater.inflate(R.layout.item_setting, null);
        c = (CardView) v.findViewById(R.id.setting_card);
        i = (ImageView) v.findViewById(R.id.setting_icon);
        i.setImageResource(R.drawable.ic_sentiment_satisfied_black_36dp);
        t = (TextView) v.findViewById(R.id.setting_text);
        t.setText(R.string.setting_rate);
        c.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                        + getPackageName())));
            }
        });
        mSettingsList.addView(v);

    }

    public String getSystemInfo() {
        String s="\n" + getResources().getString(R.string.support_email_body_title) + ":";
        s += "\n";
        s += "\nOS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
        s += "\nOS API Level: " + android.os.Build.VERSION.SDK_INT;
        s += "\nDevice: " + android.os.Build.DEVICE;
        s += "\nModel (and Product): " + android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")";
        return s;
    }

    public String getPackageName() {
        if (BuildConfig.DEBUG) {
            return "com.google.android.apps.maps";
        } else {
            return getApplicationContext().getPackageName().trim();
        }
    }
}
