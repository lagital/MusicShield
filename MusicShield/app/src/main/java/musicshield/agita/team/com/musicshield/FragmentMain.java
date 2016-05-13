package musicshield.agita.team.com.musicshield;

import android.app.ActivityManager;
import android.support.v4.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by pborisenko on 5/8/2016.
 */

public class FragmentMain extends Fragment {

    private static final String TAG = "ActivityMain";
    private static final String ARG_SECTION_NUMBER = "section_number";

    private Button blockCallsBtn;
    private Button unblockCallsBtn;
    private ImageView logo;
    private LinearLayout mainLayout;
    private ServiceConnection mServiceConnection;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    /** Messenger for communicating with service. */
    Messenger toServiceMessenger;
    private Integer mCurrentServiceState = ControlService.STATE_UNBLOCK_CALLS;

    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;
    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Message handled: " + Integer.toString(msg.arg1));
            mCurrentServiceState = msg.arg1;
        }
    }

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

        if (!serviceExists(ControlService.class)) {
            runService(ControlService.class);
        }
        getControlServiceState();

        blockCallsBtn = (Button) rootView.findViewById(R.id.block_calls_btn);
        unblockCallsBtn = (Button) rootView.findViewById(R.id.unblock_calls_btn);
        logo = (ImageView) rootView.findViewById(R.id.logo);
        mainLayout = (LinearLayout) rootView.findViewById(R.id.main_layout);
        updateUI(mCurrentServiceState);

        blockCallsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Button - block calls");
                if (!serviceExists(ControlService.class)) {
                    runService(ControlService.class);
                }
                sendMessageToService(ControlService.MSG_BLOCK_CALLS);
                unblockCallsBtn.setVisibility(View.VISIBLE);
                blockCallsBtn.setVisibility(View.GONE);
                logo.setBackgroundResource(R.drawable.logo_enabled);
            }
        });

        unblockCallsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Button - unblock calls");
                if (!serviceExists(ControlService.class)) {
                    sendMessageToService(ControlService.MSG_KILL_CONTROL_SERVICE);
                    unblockCallsBtn.setVisibility(View.GONE);
                    blockCallsBtn.setVisibility(View.VISIBLE);
                    logo.setBackgroundResource(R.drawable.logo_disabled);
                }
            }
        });

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!serviceExists(ControlService.class)) {
                    runService(ControlService.class);
                }
                getControlServiceState();
                switch (mCurrentServiceState) {
                    case ControlService.STATE_BLOCK_CALLS:
                        Log.d(TAG, "Logo - unblock calls");
                        sendMessageToService(ControlService.MSG_KILL_CONTROL_SERVICE);
                        unblockCallsBtn.setVisibility(View.GONE);
                        blockCallsBtn.setVisibility(View.VISIBLE);
                        logo.setBackgroundResource(R.drawable.logo_disabled);
                        break;
                    case ControlService.STATE_UNBLOCK_CALLS:
                        Log.d(TAG, "Logo - block calls");
                        sendMessageToService(ControlService.MSG_BLOCK_CALLS);
                        unblockCallsBtn.setVisibility(View.VISIBLE);
                        blockCallsBtn.setVisibility(View.GONE);
                        logo.setBackgroundResource(R.drawable.logo_enabled);
                        break;
                    case ControlService.STATE_PAUSE_BLOCK_CALLS:
                        Log.d(TAG, "Logo - block calls from pause");
                        sendMessageToService(ControlService.MSG_BLOCK_CALLS);
                        unblockCallsBtn.setVisibility(View.VISIBLE);
                        blockCallsBtn.setVisibility(View.GONE);
                        logo.setBackgroundResource(R.drawable.logo_enabled);
                        break;
                }
            }
        });
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            ActivityMain a = (ActivityMain) getActivity();
            a.showClearMissedCallsBtn(false);
        }
    }

    void doBindService() {
        Log.d(TAG, "doBindService");
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        getActivity().bindService(new Intent(getActivity(),
                ControlService.class), mServiceConnection, Context.BIND_WAIVE_PRIORITY);
        mIsBound = true;
    }

    void doUnbindService() {
        Log.d(TAG, "doUnbindService");
        if (mIsBound) {
            // Detach our existing connection.
            getActivity().unbindService(mServiceConnection);
            mIsBound = false;
        }
    }

    void sendMessageToService (Integer m) {
        Log.d(TAG, "sendMessageToService: " + Integer.toString(m));
        if (toServiceMessenger != null) {
            try {
                Message msg = Message.obtain(null, m);
                msg.replyTo = mMessenger;
                toServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    void initiateConnection () {
        Log.d(TAG, "initiateConnection");
        mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // This is called when the connection with the service has been
                // established, giving us the service object we can use to
                // interact with the service.  We are communicating with our
                // service through an IDL interface, so get a client-side
                // representation of that from the raw service object.
                toServiceMessenger = new Messenger(service);
                // As part of the sample, tell the user what happened.
                Log.d(TAG, "onServiceConnected");
            }

            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                toServiceMessenger = null;
                // As part of the sample, tell the user what happened.
                Log.d(TAG, "onServiceDisconnected");
            }
        };
    }

    void getControlServiceState () {
        Log.d(TAG, "getControlServiceState");
        sendMessageToService(ControlService.MSG_GET_STATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        doUnbindService();
    }

    private boolean serviceExists(Class<?> serviceClass) {
        Log.d(TAG, "checkAndRunService");
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "checkAndRunService: " + "service found.");
                return true;
            }
        }
        return false;
    }

    private void runService(Class<?> serviceClass) {
        getActivity().startService(new Intent(getActivity(), serviceClass));
        initiateConnection();
        doBindService();
    }

    private void updateUI (Integer state) {
        Log.d(TAG, "updateUI to state " + Integer.toString(state));
        switch (state) {
            case ControlService.STATE_BLOCK_CALLS:
                unblockCallsBtn.setVisibility(View.VISIBLE);
                blockCallsBtn.setVisibility(View.GONE);
                logo.setBackgroundResource(R.drawable.logo_enabled);
            case ControlService.STATE_UNBLOCK_CALLS:
                unblockCallsBtn.setVisibility(View.GONE);
                blockCallsBtn.setVisibility(View.VISIBLE);
                logo.setBackgroundResource(R.drawable.logo_disabled);
            case ControlService.STATE_PAUSE_BLOCK_CALLS:
                unblockCallsBtn.setVisibility(View.GONE);
                blockCallsBtn.setVisibility(View.VISIBLE);
                logo.setBackgroundResource(R.drawable.logo_disabled);
        }
        mainLayout.invalidate();
    }
}