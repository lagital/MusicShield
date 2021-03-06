package musicshield.agita.team.com.musicshield;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by pborisenko on 5/14/2016.
 */
public class FragmentMissedCalls extends Fragment {

    private static final String TAG = "FragmentMissedCalls";
    private static final String ARG_SECTION_NUMBER = "section_number";

    private MissedCallsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DBHelper mDBHelper;
    private ArrayList<Call> mDataset;

    public static FragmentMissedCalls newInstance(int sectionNumber) {
        FragmentMissedCalls fragment = new FragmentMissedCalls();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentMissedCalls() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_missed_calls, container, false);
        Log.d(TAG, "onCreateView");

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mDBHelper = new DBHelper(getActivity());

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshCallList();
            }
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDBHelper.debugInsertMissedCall(ApplicationMain.WRITE_DB, "+78888888888",
                DateFormat.getDateTimeInstance().format(new Date()), DBHelper.CallType.BLOCKED);
        mDBHelper.debugInsertMissedCall(ApplicationMain.WRITE_DB, "+7 777 777-77-77",
                DateFormat.getDateTimeInstance().format(new Date()), DBHelper.CallType.BLOCKED);
        mDataset = mDBHelper.retrieveMissedCalls(ApplicationMain.READ_DB);

        // specify an adapter (see also next example)
        mAdapter = new MissedCallsAdapter(mDataset);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    private class MissedCallsAdapter extends RecyclerView.Adapter<MissedCallsAdapter.ViewHolder> {
        private ArrayList<Call> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public View mView;
            public ImageView mMissedCallIcon;
            public TextView mMissedCallNumname;
            public TextView mMissedCallDateTime;
            public TextView mMissedCallNumberSecret;

            public ViewHolder(View v) {
                super(v);
                mView = v;
                mMissedCallIcon = (ImageView) v.findViewById(R.id.missed_call_icon);
                mMissedCallNumname = (TextView) v.findViewById(R.id.missed_call_numname);
                mMissedCallDateTime = (TextView) v.findViewById(R.id.missed_call_date_time);
                mMissedCallNumberSecret = (TextView) v.findViewById(R.id.missed_call_number_secret);
                v.setOnClickListener(new OnNumberClickListener());
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MissedCallsAdapter(ArrayList<Call> i_dataset) {
            mDataset = i_dataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MissedCallsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_missed_call, parent, false);
            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.mMissedCallIcon.setBackground(Call.getPhoto(getActivity(), mDataset.get(position).number));
            holder.mMissedCallNumname.setText(Call.getNumName(getActivity(), mDataset.get(position).number));
            holder.mMissedCallDateTime.setText(Call.getFormattedCallDate(getActivity(), mDataset.get(position).date_time));
            holder.mMissedCallNumberSecret.setText(mDataset.get(position).number);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    public void refreshCallList () {
        Log.d(TAG, "refreshCallList");
        mDataset.clear();
        mDataset.addAll(mDBHelper.retrieveMissedCalls(ApplicationMain.READ_DB));
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void clearCallList () {
        Log.d(TAG, "clearCallList");
        mDBHelper.clearMissedCalls(ApplicationMain.WRITE_DB);
        mDataset.clear();
        mDataset.addAll(mDBHelper.retrieveMissedCalls(ApplicationMain.READ_DB));
        mAdapter.notifyDataSetChanged();
    }

    private class OnNumberClickListener implements View.OnClickListener
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