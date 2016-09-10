package musicshield.agita.team.com.musicshield;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.samsara.team.samsaralib.purchase.SamsaraPurchase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Settings Activity
 */

public class ActivitySettings extends AppCompatActivity implements PurchaseDialogFragment.PurchaseDialogListener {
    private static final String TAG = "ActivitySettings";

    private static final String ESPRESSO_SKU = "aespresso";
    private static final String CAPPUCCINO_SKU = "bcappuccino";
    private static final String LATTE_SKU = "clatte";
    private static final int PURCHASE_REQUEST_CODE = 1001;

    private Toolbar mToolbar;
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;
    private LinearLayout mSettingsList;

    private String mDeveloperPayloadString;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        View v;

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Settings");

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

        mSettingsList = (LinearLayout) findViewById(R.id.settings_list);

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        // support letter setting
        fillSetting(mSettingsList, R.id.contact_us, R.string.setting_support_letter,
                R.drawable.ic_email_black_36dp,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View t) {
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

        // share setting
        fillSetting(mSettingsList, R.id.share_app, R.string.setting_share,
                R.drawable.ic_share_black_36dp,
                new View.OnClickListener() {
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

        // rate setting
        fillSetting(mSettingsList, R.id.rate_app, R.string.setting_rate,
                R.drawable.ic_sentiment_satisfied_black_36dp,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        rateApp(ActivitySettings.this);
                    }
                });

        //coffee setting
        fillSetting(mSettingsList, R.id.treat_to_coffee, R.string.setting_coffee,
                R.drawable.ic_local_cafe_black_36dp,
                new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "onClick - Treat to coffee");
                ArrayList<String> skuList = new ArrayList<String>();
                skuList.add(ESPRESSO_SKU);
                skuList.add(CAPPUCCINO_SKU);
                skuList.add(LATTE_SKU);
                Bundle skuDetails = new Bundle();
                Bundle mart;
                ArrayList<String> responseList;

                skuDetails = SamsaraPurchase.getSkuDetails(ActivitySettings.this, skuDetails,
                        skuList, mService);
                int response = skuDetails.getInt("RESPONSE_CODE");

                if (response != 0) {
                    Toast.makeText(ActivitySettings.this, R.string.connection_problem,
                            Toast.LENGTH_SHORT);
                    return;
                }

                responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                if (responseList == null) {
                    Toast.makeText(ActivitySettings.this, R.string.no_products_found,
                            Toast.LENGTH_SHORT);
                    return;
                }

                Log.d(TAG, "Skus: " + responseList.toString());
                mart = SamsaraPurchase.getMart(responseList);

                if (mart != null) {
                    PurchaseDialogFragment p = new PurchaseDialogFragment();
                    p.setArguments(mart);
                    p.show(getSupportFragmentManager(), "PurchaseDialogFragment");
                }
            }
        });

        // about setting
        fillSetting(mSettingsList, R.id.about, null, null,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(ActivitySettings.this, ActivityAbout.class);
                        startActivity(i);
                    }
                });
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
        if (requestCode == PURCHASE_REQUEST_CODE) {
            Log.d(TAG, "RC " + Integer.toString(PURCHASE_REQUEST_CODE));

            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            try {
                JSONObject jo = new JSONObject(purchaseData);
                String sku = jo.getString("productId");
                String token = jo.getString("purchaseToken");
                Boolean check = SamsaraPurchase.validatePurchase(jo, resultCode,
                        mDeveloperPayloadString, dataSignature);

                if (check) {
                    Toast.makeText(this, getResources().getString(R.string.successful_purchase), Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "Purchase was cancelled.");
                    return;
                }

                if (sku.equals(ESPRESSO_SKU) || sku.equals(CAPPUCCINO_SKU) || sku.equals(LATTE_SKU)) {
                    SamsaraPurchase.consumePurchase(this, token, mService);
                }
            } catch (JSONException e) {
                Toast.makeText(this, getResources().getString(R.string.connection_problem),
                        Toast.LENGTH_LONG).show();
                    e.printStackTrace();
            }
        }
    }

    @Override
    public void onFinishPurchaseDialog(String sku) {
        Log.d(TAG, "onFinishPurchaseDialog");
        mDeveloperPayloadString = SamsaraPurchase.makePurchase(this, sku, mService,
                PURCHASE_REQUEST_CODE);
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

    private void fillSetting(View root, Integer elementId, Integer labelRes, Integer iconRes,
                             View.OnClickListener listener) {
        CardView c = (CardView) root.findViewById(elementId);
        ImageView i = (ImageView) c.findViewById(R.id.setting_icon);
        TextView t = (TextView) c.findViewById(R.id.setting_text);
        if (i != null) {
            i.setImageResource(iconRes);
        }
        if (t != null) {
            t.setText(labelRes);
        }
        c.setOnClickListener(listener);
    }
}