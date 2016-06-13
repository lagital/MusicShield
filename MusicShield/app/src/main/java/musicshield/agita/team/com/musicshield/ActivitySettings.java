package musicshield.agita.team.com.musicshield;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

/**
 * Settings Activity
 */
public class ActivitySettings extends AppCompatActivity {
    private static final String TAG = "ActivitySettings";

    private static final String COFFEE_SKU = "coffee";

    private ActionBar mToolbar;
    private LinearLayout mSettingsList;
    IInAppBillingService mService;
    ServiceConnection mServiceConn;
    String mCoffeePrice;
    private String developerPayloadString;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = getSupportActionBar();

        mSettingsList = (LinearLayout) findViewById(R.id.settings_list);
        LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name,
                                           IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }
        };

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

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
                rateApp(ActivitySettings.this);
            }
        });
        mSettingsList.addView(v);

        //coffee setting
        v = inflater.inflate(R.layout.item_setting, null);
        c = (CardView) v.findViewById(R.id.setting_card);
        i = (ImageView) v.findViewById(R.id.setting_icon);
        i.setImageResource(R.drawable.ic_local_cafe_black_36dp);
        t = (TextView) v.findViewById(R.id.setting_text);
        t.setText(R.string.setting_coffee);
        c.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "onClick - Treat to coffee");
                ArrayList<String> skuList = new ArrayList<String>();
                skuList.add(COFFEE_SKU);
                Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

                Bundle skuDetails = new Bundle();
                try {
                    skuDetails = mService.getSkuDetails(3,
                            getPackageName(), "inapp", querySkus);
                } catch (RemoteException e) {
                    Toast.makeText(ActivitySettings.this,
                            getResources().getString(R.string.connection_problem),
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                String sku = "";
                int response = skuDetails.getInt("RESPONSE_CODE");
                if (response == 0) {
                    ArrayList<String> responseList
                            = skuDetails.getStringArrayList("DETAILS_LIST");

                    Log.d(TAG, "Skus: " + responseList.toString());

                    for (String thisResponse : responseList) {
                        try {
                            JSONObject object = new JSONObject(thisResponse);
                            sku = object.getString("productId");
                            String price = object.getString("price");
                            if (sku.equals(COFFEE_SKU)) mCoffeePrice = price;

                            new AlertDialog.Builder(ActivitySettings.this)
                                    .setTitle(price)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            makePurchase(COFFEE_SKU);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .setIcon(R.drawable.ic_local_cafe_black_36dp)
                                    .setMessage(getResources().getString(R.string.alert_buy_coffee_message))
                                    .show();
                        } catch (JSONException e) {
                            Toast.makeText(ActivitySettings.this,
                                    getResources().getString(R.string.connection_problem),
                                    Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }
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

    public static void rateApp (Context context) {
        ApplicationMain.stopRateNotification();
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                + ApplicationMain.getPackageName(context))));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (requestCode == 1001) {
            Log.d(TAG, "RC 1001");
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    String token = jo.getString("purchaseToken");
                    Toast.makeText(this, getResources().getString(R.string.successful_purchase),
                            Toast.LENGTH_LONG).show();

                    if (sku.equals(COFFEE_SKU)) {
                        ConsumeTask t = new ConsumeTask(token);
                        t.execute();
                    }
                }
                catch (Exception e) {
                    Toast.makeText(this, getResources().getString(R.string.connection_problem),
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void makePurchase (String sku) {
        Log.d(TAG, "makePurchase");
        if (!sku.equals("")) {
            try {
                developerPayloadString = generateDeveloperPayload(new Random(),
                        "abcdefghijklmnopqrstuvwxyz1234567890/+-()&#@%!",
                        20);
                Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                        sku, "inapp", developerPayloadString);
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                startIntentSenderForResult(pendingIntent.getIntentSender(),
                        1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                        Integer.valueOf(0));
            } catch (Exception e) {
                Toast.makeText(ActivitySettings.this,
                        getResources().getString(R.string.connection_problem),
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public static String generateDeveloperPayload(Random rng, String characters, int length)
    {
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

    private class ConsumeTask extends AsyncTask<Void,Void,Void> {

        private String mToken;

        ConsumeTask (String token) {
            mToken = token;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                int response = mService.consumePurchase(3, getPackageName(), mToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}