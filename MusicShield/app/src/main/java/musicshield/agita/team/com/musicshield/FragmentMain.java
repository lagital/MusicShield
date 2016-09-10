package musicshield.agita.team.com.musicshield;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by pborisenko on 5/8/2016.
 */

public class FragmentMain extends Fragment {

    private static final String TAG = "FragmentMain";
    private static final String ARG_SECTION_NUMBER = "section_number";

    private ImageView logo;
    /** Messenger for communicating with service. */
    private ServiceMessenger mServiceMessenger;

    private Integer CurrentServiceState = ControlService.STATE_UNBLOCK_CALLS;

    public static FragmentMain newInstance(int sectionNumber) {
        FragmentMain fragment = new FragmentMain();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentMain() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Log.d(TAG, "onCreateView");

        logo = (ImageView) rootView.findViewById(R.id.logo);

        if (ServiceMessenger.serviceExists(getActivity(), ControlService.class)) {
            logo.setImageResource(R.drawable.logo_enabled);
        } else {
            logo.setImageResource(R.drawable.logo_disabled);
        }

        mServiceMessenger = ServiceMessenger.getInstance(getActivity());

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBlockingState();
            }
        });
        return rootView;
    }

    public void changeBlockingState () {
        switch (mServiceMessenger.getCurrentServiceState()) {
            case ControlService.STATE_BLOCK_CALLS:
                mServiceMessenger.sendMessageToService(ControlService.MSG_UNBLOCK_CALLS);
                // TODO: Animation
                logo.setImageResource(R.drawable.logo_disabled);
                break;
            case ControlService.STATE_UNBLOCK_CALLS:
                mServiceMessenger.sendMessageToService(ControlService.MSG_BLOCK_CALLS);
                // TODO: Animation
                logo.setImageResource(R.drawable.logo_enabled);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mServiceMessenger.doUnbindService();
    }

    public void setCurrentServiceState(Integer mCurrentServiceState) {
        this.CurrentServiceState = mCurrentServiceState;
    }
}