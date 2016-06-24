package musicshield.agita.team.com.musicshield;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
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
public class ActivitySettings extends AppCompatActivity implements PurchaseDialogFragment.PurchaseDialogListener {
    private static final String TAG = "ActivitySettings";

    private static final String ESPRESSO_SKU = "espresso";
    private static final String CAPPUCCINO_SKU = "cappuccino";
    private static final String LATTE_SKU = "latte";
    private static final int DIALOG_REQUEST_CODE = 500;

    private ActionBar mToolbar;
    private LinearLayout mSettingsList;
    IInAppBillingService mService;
    ServiceConnection mServiceConn;
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
                skuList.add(ESPRESSO_SKU);
                skuList.add(CAPPUCCINO_SKU);
                skuList.add(LATTE_SKU);

                Bundle skuDetails = new Bundle();
                skuDetails = getSkuDetails(ActivitySettings.this, skuDetails, skuList);

                int response = skuDetails.getInt("RESPONSE_CODE");
                ArrayList<String> responseList;
                if (response == 0) {
                    responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                } else {
                    Toast.makeText(ActivitySettings.this, R.string.connection_problem,
                            Toast.LENGTH_SHORT);
                    return;
                }

                Bundle mart = new Bundle();

                if (responseList == null) {
                    Toast.makeText(ActivitySettings.this, R.string.no_products_found,
                            Toast.LENGTH_SHORT);
                    return;
                } else {
                    Log.d(TAG, "Skus: " + responseList.toString());

                    mart = getMart(ActivitySettings.this, responseList, mart);
                }

                if (mart.containsKey(PurchaseDialogFragment.SKUS_BUNDLE_CODE)) {
                    PurchaseDialogFragment p = new PurchaseDialogFragment();
                    p.setArguments(mart);
                    p.show(getSupportFragmentManager(), "PurchaseDialogFragment");
                } else {
                    return;
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

    public static void rateApp(Context context) {
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

                    if (sku.equals(ESPRESSO_SKU) || sku.equals(CAPPUCCINO_SKU)
                            || sku.equals(LATTE_SKU)) {
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

    public static String makePurchase (Activity activity, String sku, IInAppBillingService service) {
        Log.d(TAG, "makePurchase");

        if (!sku.equals("")) {
            try {
                String developerPayloadString = generateDeveloperPayload(new Random(),
                        "abcdefghijklmnopqrstuvwxyz1234567890/+-()&#@%!",
                        20);
                Bundle buyIntentBundle = service.getBuyIntent(3, activity.getPackageName(),
                        sku, "inapp", developerPayloadString);
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                activity.startIntentSenderForResult(pendingIntent.getIntentSender(),
                        1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                        Integer.valueOf(0));
                return developerPayloadString;
            } catch (Exception e) {
                Toast.makeText(activity,
                        activity.getResources().getString(R.string.connection_problem),
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return "";
            }
        }

        return "";
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

    private Bundle getSkuDetails (Context context, Bundle skuDetails, ArrayList<String> skuList) {
        Log.d(TAG, "getSkuDetails");

        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

        try {
            skuDetails = mService.getSkuDetails(3,
                    getPackageName(), "inapp", querySkus);
        } catch (RemoteException e) {
            Toast.makeText(context,
                    getResources().getString(R.string.connection_problem),
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return skuDetails;
    }

    private Bundle getMart (Context context, ArrayList<String> responseList, Bundle mart) {
        Log.d(TAG, "getMart");

        ArrayList<String> skus = new ArrayList<>();
        ArrayList<String> prices = new ArrayList<>();

        for (String thisResponse : responseList) {
            try {
                JSONObject object = new JSONObject(thisResponse);
                skus.add(object.getString("productId"));
                prices.add(object.getString("price"));
            } catch (JSONException e) {
                Toast.makeText(context, getResources().getString(R.string.connection_problem),
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return new Bundle();
            }
        }

        int s = skus.size();
        int p = prices.size();

        if (p == s) {
            String[] skusArray = new String[s];
            String[] pricesArray = new String[p];
            skusArray = skus.toArray(skusArray);
            pricesArray = prices.toArray(pricesArray);

            mart.putStringArray(PurchaseDialogFragment.SKUS_BUNDLE_CODE, skusArray);
            mart.putStringArray(PurchaseDialogFragment.PRICES_BUNDLE_CODE, pricesArray);
        }

        return mart;
    }

    @Override
    public void onFinishPurchaseDialog(String sku) {
        Log.d(TAG, "onFinishPurchaseDialog");
        ActivitySettings.makePurchase(this, sku, mService);
    }

    public static String mapSku(Context context, String sku) {
        switch(sku) {
            case ESPRESSO_SKU:
                return context.getResources().getString(R.string.espresso);
            case CAPPUCCINO_SKU:
                return context.getResources().getString(R.string.cappuccino);
            case LATTE_SKU:
                return context.getResources().getString(R.string.latte);
            default:
                return sku;
        }
    }
}