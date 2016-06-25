package musicshield.agita.team.com.musicshield;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.awt.font.TextAttribute;

/**
 * Created by pborisenko on 6/24/2016.
 */
public class PurchaseDialogFragment extends DialogFragment {
    private static final String TAG = "PurchaseDialogFragment";

    public static final String SKUS_BUNDLE_CODE = "skus";
    public static final String PRICES_BUNDLE_CODE = "prices";
    public static final String CURRENCIES_BUNDLE_CODE = "price_currency_code";

    private String[] mSkus;
    private String[] mPrices;
    private String[] mCurruncies;
    private String[] mProductsToShow;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mSkus = args.getStringArray(SKUS_BUNDLE_CODE);
        mPrices = args.getStringArray(PRICES_BUNDLE_CODE);
        mCurruncies = args.getStringArray(CURRENCIES_BUNDLE_CODE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (mSkus.length == 0) {
            String[] s = {getResources().getString(R.string.no_products_found)};
            mProductsToShow = s;
        } else {
            mProductsToShow = new String[mSkus.length];
            //mProductsToShow = getFormattedProductList(mSkus, mPrices, mCurruncies);
            for (int i = 0; i < mSkus.length; i++) {
                mProductsToShow[i] = ActivitySettings.mapSku(getContext(), mSkus[i])
                        + " - " + mPrices[i]
                        + "(" + mCurruncies[i] + ")";;
            }
        }

        builder.setIcon(R.drawable.ic_local_cafe_black_36dp)
                .setTitle(R.string.alert_buy_coffee)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setItems(mProductsToShow, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        PurchaseDialogListener activity = (PurchaseDialogListener) getActivity();
                        activity.onFinishPurchaseDialog(mSkus[which]);
                        PurchaseDialogFragment.this.dismiss();
                    }
                });
        return builder.create();
    }

    public interface PurchaseDialogListener {
        void onFinishPurchaseDialog(String sku);
    }

    private String[] getFormattedProductList (String[] skus, String[] prices, String[] currencies) {
        String[] productsToShow = new String[skus.length];
        int maxTabLength = 0;
        String tab = "\t";
        int tabLength = tab.length();
        String s;

        for (int i = 0; i < skus.length; i++) {
            // replace product IDs with product names
            skus[i] = ActivitySettings.mapSku(getContext(), skus[i]);
            // detect the biggest product name with tab
            s = skus[i] + tab;
            if (s.length() > maxTabLength) {
                maxTabLength = s.length();
            }
        }
        Log.d(TAG, "maxTabLength " + Integer.toString(maxTabLength));
        Log.d(TAG, "tabLength " + Integer.toString(tabLength));
        for (int i=0; i < skus.length; i++) {
            Log.d(TAG, "sku length " + Integer.toString(skus[i].length()));
            skus[i] = skus[i] + new String(new char[(maxTabLength - skus[i].length()) / tabLength - 1])
                    .replace("\0", tab);
            Log.d(TAG, "sku length " + Integer.toString(skus[i].length()));
            productsToShow[i] = skus[i] + prices[i] + "(" + currencies[i] + ")";
        }

        return productsToShow;
    }
}