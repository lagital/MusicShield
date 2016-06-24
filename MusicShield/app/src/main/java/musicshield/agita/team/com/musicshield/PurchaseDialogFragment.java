package musicshield.agita.team.com.musicshield;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.android.vending.billing.IInAppBillingService;

/**
 * Created by pborisenko on 6/24/2016.
 */
public class PurchaseDialogFragment extends DialogFragment {

    public static final String SKUS_BUNDLE_CODE = "skus";
    public static final String PRICES_BUNDLE_CODE = "prices";

    private String[] mSkus;
    private String[] mPrices;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mSkus = args.getStringArray(SKUS_BUNDLE_CODE);
        mPrices = args.getStringArray(PRICES_BUNDLE_CODE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (mSkus.length == 0) {
            String[] s = {getResources().getString(R.string.no_products_found)};
            mSkus = s;
        } else {
            for (int i = 0; i < mSkus.length; i++) {
                // concatenate names and prices
                mSkus[i] = ActivitySettings.mapSku(getContext(), mSkus[i]) + " " + mPrices[i];
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
                .setItems(mSkus, new DialogInterface.OnClickListener() {
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
}