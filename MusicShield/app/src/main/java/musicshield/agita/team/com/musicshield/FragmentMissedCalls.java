package musicshield.agita.team.com.musicshield;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by pborisenko on 5/14/2016.
 */
public class FragmentMissedCalls extends Fragment {

    private static final String TAG = "ActivityMain";
    private static final String ARG_SECTION_NUMBER = "section_number";

    private MissedCallsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
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

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        mDBHelper = new DBHelper(getActivity());

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDBHelper.debugInsertMissedCall(ApplicationMain.WRITE_DB, "test", "test", DBHelper.CallType.BLOCKED);
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
            public ViewHolder(View v) {
                super(v);
                mView = v;
                mMissedCallIcon = (ImageView) v.findViewById(R.id.missed_call_icon);
                mMissedCallNumname = (TextView) v.findViewById(R.id.missed_call_numname);
                mMissedCallDateTime = (TextView) v.findViewById(R.id.missed_call_date_time);
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
            holder.mMissedCallIcon.setBackground(
                    ContextCompat.getDrawable(getActivity(), R.drawable.ic_call_missed_black_36dp));
            holder.mMissedCallNumname.setText(Call.getNumName(getActivity(), mDataset.get(position).number));
            holder.mMissedCallDateTime.setText(mDataset.get(position).date_time);

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
}