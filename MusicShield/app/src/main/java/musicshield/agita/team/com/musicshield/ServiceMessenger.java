package musicshield.agita.team.com.musicshield;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by pborisenko on 7/2/2016.
 */
public class ServiceMessenger {

    private static final String TAG = "FragmentMain";

    private static ServiceMessenger mServiceMessenger;
    private Context mContext;
    private ServiceConnection mServiceConnection;
    private Boolean mIsBound;
    private Messenger toServiceMessenger;
    private Messenger mMessenger;
    public Integer currentServiceState = ControlService.STATE_UNBLOCK_CALLS;

    /* A private Constructor prevents any other
     * class from instantiating.
    */
    private ServiceMessenger(Context context){
        mContext = context;
        mMessenger = new Messenger(new ServiceIncomingHandler(context));
        initiateConnection();
        doBindService();
    }

    /* Static 'instance' method */
    public static ServiceMessenger getInstance(Context context) {
        if (mServiceMessenger == null) {
            mServiceMessenger = new ServiceMessenger(context);
        }
        return mServiceMessenger;
    }

    void doBindService() {
        Log.d(TAG, "doBindService");
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        mContext.bindService(new Intent(mContext,
                ControlService.class), mServiceConnection, Context.BIND_WAIVE_PRIORITY);
        mIsBound = true;
    }

    void doUnbindService() {
        Log.d(TAG, "doUnbindService");
        if (mIsBound) {
            // Detach our existing connection.
            mContext.unbindService(mServiceConnection);
            mIsBound = false;
        }
    }

    public void sendMessageToService (Integer m) {
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

    private void runService(Context context, Class<?> serviceClass) {
        context.startService(new Intent(context, serviceClass));
        initiateConnection();
        doBindService();
    }

    public void setCurrentServiceState(Integer currentServiceState) {
        this.currentServiceState = currentServiceState;
    }

    private static class ServiceIncomingHandler extends Handler {

        private Context mContext;

        ServiceIncomingHandler (Context context) {
            mContext = context;
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Message handled: " + Integer.toString(msg.arg1));
            ServiceMessenger.getInstance(mContext).setCurrentServiceState(msg.arg1);
            // TODO: UPDATE UI
        }
    }
}