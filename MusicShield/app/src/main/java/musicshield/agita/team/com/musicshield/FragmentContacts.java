package musicshield.agita.team.com.musicshield;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by pborisenko on 5/14/2016.
 */
public class FragmentContacts extends Fragment {

    private static final String TAG = "FragmentContacts";
    private static final String ARG_SECTION_NUMBER = "section_number";

    private ContactsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<Contact> mDataset;
    private AdView mAdView;

    public static FragmentContacts newInstance(int sectionNumber) {
        FragmentContacts fragment = new FragmentContacts();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentContacts() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        Log.d(TAG, "onCreateView");

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDataset = Contact.retrieveContacts(getActivity());

        // specify an adapter (see also next example)
        mAdapter = new ContactsAdapter(mDataset);
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            ActivityMain a = (ActivityMain) getActivity();
            a.updateToolbar(1);
        }
    }

    private class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
        private ArrayList<Contact> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public View mView;
            public ImageView mContactIcon;
            public TextView mContactName;
            public LinearLayout mContactNumbers;
            public CheckBox mContactCheckBox;

            public ViewHolder(View v) {
                super(v);
                mView = v;
                mContactIcon   = (ImageView) v.findViewById(R.id.contact_icon);
                mContactName   = (TextView) v.findViewById(R.id.contact_name);
                mContactNumbers = (LinearLayout) v.findViewById(R.id.contact_numbers);
                mContactCheckBox = (CheckBox) v.findViewById(R.id.contact_checkbox);
                v.setOnClickListener(new OnContactClickListener());
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public ContactsAdapter(ArrayList<Contact> i_dataset) {
            mDataset = i_dataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_contact, parent, false);
            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.mContactIcon.setBackground(mDataset.get(position).photo);
            holder.mContactCheckBox.setChecked(mDataset.get(position).checked);
            holder.mContactName.setText(mDataset.get(position).name);
            TextView t;
            for (String n : mDataset.get(position).numbers) {
                t = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.contact_text_view, null);
                t.setText(n);
                holder.mContactNumbers.addView(t);
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    private class OnContactClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(final View v)
        {
            TextView t = (TextView) v.findViewById(R.id.missed_call_number_secret);
            if (t != null) {
                String number = t.getText().toString().trim();
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
                startActivity(callIntent);
            }
        }
    }
}