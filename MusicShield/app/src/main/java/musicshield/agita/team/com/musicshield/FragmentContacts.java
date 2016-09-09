package musicshield.agita.team.com.musicshield;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by pborisenko on 5/14/2016.
 */
public class FragmentContacts extends Fragment {

    private static final String TAG = "FragmentContacts";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String PREF_NAME = "musicshield.agita.team.com.musicshield";
    private static final String CHECKED_NUMBERS = "CHECKED_NUMBERS";

    private ContactsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<Contact> mDataset = null;
    private SharedPreferences mSP;
    private ProgressBar mBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;

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

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContactList();
            }
        });

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (mDataset == null) {
            mDataset = new ArrayList<>();
            mBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
            new ProgressTask(true).execute();
        }

        // specify an adapter (see also next example)
        mAdapter = new ContactsAdapter(mDataset);
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity(), null));
        mRecyclerView.setAdapter(mAdapter);

        mSP = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return rootView;
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
            holder.mContactName.setText(mDataset.get(position).name);
            holder.mContactNumbers.removeAllViews();
            for (String n : mDataset.get(position).numbers) {
                TextView t = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.contact_text_view, null);
                t.setText(n);
                holder.mContactNumbers.addView(t);
            }
            holder.mContactCheckBox.setOnCheckedChangeListener(new OnContactCheckListener(position));
            holder.mContactCheckBox.setChecked(mDataset.get(position).checked);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    public void saveCheckedContacts () {
        SharedPreferences.Editor editor = mSP.edit();
        HashSet<String> hs = new HashSet<>();
        for (Contact c : mDataset) {
            if (c.checked) {
                for (String n : c.numbers) {
                    hs.add(n);
                    Log.d(TAG, n + " " + c.checked.toString());
                }
            }
        }
        editor.putStringSet(CHECKED_NUMBERS, hs);
        editor.apply();

        Toast.makeText(getActivity(), R.string.save_contacts_toast, Toast.LENGTH_SHORT).show();
    }

    private class OnContactCheckListener implements CompoundButton.OnCheckedChangeListener
    {
        private Integer datasetPosition;

        public OnContactCheckListener(Integer position) {
            datasetPosition = position;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                mDataset.get(datasetPosition).checked = true;
            } else {
                mDataset.get(datasetPosition).checked = false;
            }
        }
    }

    public void selectAllItems (Boolean selected) {
        for (int i = 0; i < mDataset.size(); i++) {
            mDataset.get(i).checked = selected;
        }
        mAdapter.notifyDataSetChanged();
    }

    private class ProgressTask extends AsyncTask<Void,Void,ArrayList<Contact>> {
        private Boolean mFirstLoad;

        ProgressTask (boolean firstLoad) {
            mFirstLoad = firstLoad;
        }

        @Override
        protected void onPreExecute(){
            if (mFirstLoad) {
                mRecyclerView.setVisibility(View.GONE);
                mBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected ArrayList<Contact> doInBackground(Void... arg0) {
            return Contact.retrieveContacts(getActivity());
        }

        @Override
        protected void onPostExecute(ArrayList<Contact> contacts) {
            ArrayList<Contact> tmpDataset = new ArrayList<>();
            tmpDataset.addAll(contacts);
            mDataset.clear();
            mDataset = tmpDataset;
            mAdapter.notifyDataSetChanged();

            if (mFirstLoad) {
                mBar.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            } else {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void refreshContactList () {
        Log.d(TAG, "refreshContactList");
        mDataset.clear();
        new ProgressTask(false).execute();
    }
}