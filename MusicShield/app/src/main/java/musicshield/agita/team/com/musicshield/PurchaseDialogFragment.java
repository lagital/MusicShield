package musicshield.agita.team.com.musicshield;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.samsara.team.samsaralib.purchase.SamsaraProduct;
import com.samsara.team.samsaralib.purchase.SamsaraPurchase;

import java.awt.font.TextAttribute;
import java.util.ArrayList;

/**
 * Created by pborisenko on 6/24/2016.
 */
public class PurchaseDialogFragment extends DialogFragment {
    private static final String TAG = "PurchaseDialogFragment";

    private ArrayList<SamsaraProduct> mProducts;
    private String[] mProductsToShow;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mProducts = args.getParcelableArrayList(SamsaraPurchase.MART_LIST);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (mProducts.size() != 0) {
            mProductsToShow = new String[mProducts.size()];
            for (int i = 0; i < mProductsToShow.length; i++) {
                mProductsToShow[i] = mProducts.get(i).getProductId();

                mProductsToShow[i] = ActivitySettings.mapSku(getContext(), mProducts.get(i).getProductId())
                        + " - " + mProducts.get(i).getPrice()
                        + "(" + mProducts.get(i).getPriceCurrencyCode() + ")";;
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
                        if (mProductsToShow.length != 0) {
                            activity.onFinishPurchaseDialog(mProducts.get(which).getProductId());
                        } else {
                            activity.onFinishPurchaseDialog(null);
                        }
                        PurchaseDialogFragment.this.dismiss();
                    }
                });
        return builder.create();
    }

    public interface PurchaseDialogListener {
        void onFinishPurchaseDialog(String sku);
    }
}