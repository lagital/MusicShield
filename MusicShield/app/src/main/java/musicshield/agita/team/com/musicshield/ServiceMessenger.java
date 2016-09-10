package musicshield.agita.team.com.musicshield;

import android.app.ActivityManager;
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

    private static final String TAG = "ServiceMessenger";

    private static ServiceMessenger mServiceMessenger;
    private Context mContext;
    private ActivityMain mActivity;
    private ServiceConnection mServiceConnection;
    private Boolean mIsBound;
    private Messenger toServiceMessenger;
    private Messenger mMessenger;

    private Integer currentServiceState = ControlService.STATE_UNBLOCK_CALLS;

    /* A private Constructor prevents any other
     * class from instantiating.
    */
    private ServiceMessenger(Context context){
        mContext = context;
        mActivity = (ActivityMain) mContext;
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
        if (m == ControlService.MSG_BLOCK_CALLS) {
            runService(mActivity, ControlService.class);
            return;
        }
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
                toServiceMessenger = new Messenger(service);
                Log.d(TAG, "onServiceConnected");
            }

            public void onServiceDisconnected(ComponentName className) {
                toServiceMessenger = null;
                Log.d(TAG, "onServiceDisconnected");
            }
        };
    }

    private void runService(Context context, Class<?> serviceClass) {
        context.startService(new Intent(context, serviceClass));
        initiateConnection();
        doBindService();
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
        }
    }

    public static boolean serviceExists(Context context, Class<?> serviceClass) {
        Log.d(TAG, "serviceExists");
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "checkAndRunService: " + "service found.");
                return true;
            }
        }
        return false;
    }

    private void setCurrentServiceState(Integer currentServiceState) {
        this.currentServiceState = currentServiceState;
    }

    public Integer getCurrentServiceState() {
        return currentServiceState;
    }
}