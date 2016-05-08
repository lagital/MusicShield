package musicshield.agita.team.com.musicshield;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

/**
 * Created by pborisenko on 5/8/2016.
 */
public class ControlService extends Service {

    private final String TAG = "ControlService";

    public static final int MSG_BLOCK_CALLS = 1;
    public static final int MSG_UNBLOCK_CALLS = 0;
    public static final int MSG_PAUSE_BLOCK_CALLS = -1;
    public static final int MSG_KILL_CONTROL_SERVICE = -2;
    private int NOTIFICATION = R.string.contorol_service_idt;

    private PhoneStateListener mPhoneStateListener;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private int mBlockingState;
    private TelephonyManager mTelephonyManager;
    ITelephony mTelephonyService = null;
    private NotificationManager mNotificationManager;

    public class LocalBinder extends Binder {
        ControlService getService() {
            return ControlService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();


    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.

            switch (msg.arg1) {
                case MSG_BLOCK_CALLS:
                    break;
                case MSG_UNBLOCK_CALLS:
                    break;
                case MSG_PAUSE_BLOCK_CALLS:
                    break;
                case MSG_KILL_CONTROL_SERVICE:
                    break;
                default:
                    break;
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        mPhoneStateListener = new ControlPhoneStateListener();
        mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        initiateTelephonyService();

        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        // Cancel the persistent notification.
        mNotificationManager.cancel(NOTIFICATION);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void initiateTelephonyService () {
        Class c = null;
        try {
            c = Class.forName(mTelephonyManager.getClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Method m = null;
        if (c != null) {
            try {
                m = c.getDeclaredMethod("getITelephony");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (m != null) {
            m.setAccessible(true);
            try {
                mTelephonyService = (ITelephony) m.invoke(mTelephonyManager);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private class ControlPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            if (state == TelephonyManager.CALL_STATE_RINGING) {
                if (mBlockingState == MSG_BLOCK_CALLS) {

                } else {
                    //nothing
                }
            }
        }
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ActivityMain.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_icon)  // the status icon
                .setTicker("TEST")  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("TEST")  // the label of the entry
                .setContentText("TEST")  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .setPriority(Notification.PRIORITY_MAX)
                .build();

        // Send the notification.
        mNotificationManager.notify(NOTIFICATION, notification);
    }
}