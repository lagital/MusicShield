package musicshield.agita.team.com.musicshield;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

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
    public static final int MSG_GET_STATE = 100;
    public static final int STATE_BLOCK_CALLS = 1;
    public static final int STATE_UNBLOCK_CALLS = 0;
    public static final int STATE_PAUSE_BLOCK_CALLS = -1;
    private int NOTIFICATION = R.string.contorol_service_idt;

    private PhoneStateListener mPhoneStateListener;
    private ServiceHandler mServiceHandler;
    private int mCurrentState;
    private TelephonyManager mTelephonyManager;
    ITelephony mTelephonyService = null;
    private NotificationManager mNotificationManager;
    private Messenger mMessenger;
    private Messenger toActivityMessenger;
    private AudioManager mAudioManager;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
            Log.d(TAG, "ServiceHandler constructor");
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: " + Integer.toString(msg.what));
            toActivityMessenger = msg.replyTo;
            switch (msg.what) {
                case MSG_BLOCK_CALLS:
                    mCurrentState = STATE_BLOCK_CALLS;
                    break;
                case MSG_UNBLOCK_CALLS:
                    mCurrentState = STATE_UNBLOCK_CALLS;
                    break;
                case MSG_PAUSE_BLOCK_CALLS:
                    mCurrentState = STATE_PAUSE_BLOCK_CALLS;
                    break;
                case MSG_KILL_CONTROL_SERVICE:
                    Log.d(TAG, "stopSelf");
                    ControlService.this.stopSelf();
                    break;
                case MSG_GET_STATE:
                    Log.d(TAG, "returning current state");
                    if (toActivityMessenger != null) {
                        Message stateMsg = Message.obtain(mServiceHandler, MSG_GET_STATE);
                        stateMsg.arg1 = mCurrentState;
                        stateMsg.replyTo = mMessenger;

                        try {
                            if( toActivityMessenger != null )
                                toActivityMessenger.send(stateMsg);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceHandler = new ServiceHandler(thread.getLooper());
        mMessenger = new Messenger(mServiceHandler);
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        initiateTelephonyService();
        mPhoneStateListener = new ControlPhoneStateListener();
        mTelephonyManager.listen(new ControlPhoneStateListener(), mPhoneStateListener.LISTEN_CALL_STATE);
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        showNotification();
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
        Log.d(TAG, "onBind");
        return mMessenger.getBinder();
    }

    private void initiateTelephonyService() {
        Log.d(TAG, "initiateTelephonyService");
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

        private boolean wasRinging;
        private boolean wasAnswered = false;
        //private boolean muted;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            Log.d(TAG, "onCallStateChanged");
            switch(state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "onCallStateChanged: RINGING");
                    wasRinging = true;
                    if (mAudioManager.isMusicActive()) {
                        Log.d(TAG, "onCallStateChanged: " + "music is playing.");
                        if (mCurrentState == MSG_BLOCK_CALLS) {
                            Log.d(TAG, "onCallStateChanged: " + "state - blocking.");
                            try {
                                //audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                                // Change the stream to your stream of choice.
                                /*
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
                                } else {
                                    mAudioManager.setStreamMute(AudioManager.STREAM_RING, true);
                                }
                                muted = true;
                                */
                                mTelephonyService.endCall();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d(TAG, "onCallStateChanged: " + "state - non-blocking.");
                        }
                    } else {
                        Log.d(TAG, "onCallStateChanged: " + "music is not playing.");
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "onCallStateChanged: OFFHOOK");
                    wasAnswered = true;
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.i(TAG, "onCallStateChanged: IDLE");
                    /*
                    if (muted && wasRinging) {
                        Log.d(TAG, "onCallStateChanged: phone was muted - lets unmute.");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);
                        } else {
                            mAudioManager.setStreamMute(AudioManager.STREAM_RING, false);
                        }
                        muted = false;
                    }*/
                    if (!wasAnswered) {
                        //TODO: notification about missed call
                    }
                    wasAnswered = false;
                    wasRinging = false;
                    break;
                }
        }
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.notification);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.logo_disabled).setContent(
                remoteViews);
        // Creates an explicit intent for an Activity in your app
        Intent toActivityIntent     = new Intent(this, ActivityMain.class);
        Intent blockCallsIntent     = new Intent(this, ControlService.class);
        Intent unblockCallsIntent   = new Intent(this, ControlService.class);
        Intent pauseCallsIntent     = new Intent(this, ControlService.class);

        blockCallsIntent.putExtra("MSG", ControlService.MSG_BLOCK_CALLS);
        unblockCallsIntent.putExtra("MSG", ControlService.MSG_UNBLOCK_CALLS);
        pauseCallsIntent.putExtra("MSG", ControlService.MSG_PAUSE_BLOCK_CALLS);

        /*PendingIntent toActivityPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);*/
        PendingIntent blockCallsPendingIntent = PendingIntent.getService
                (this, 579, blockCallsIntent, PendingIntent.FLAG_NO_CREATE);
        PendingIntent unblockCallsPendingIntent = PendingIntent.getService
                (this, 579, unblockCallsIntent, PendingIntent.FLAG_NO_CREATE);
        PendingIntent pauseCallsPendingIntent = PendingIntent.getService
                (this, 579, pauseCallsIntent, PendingIntent.FLAG_NO_CREATE);

        remoteViews.setOnClickPendingIntent(R.id.notification_block_calls_btn,
                blockCallsPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.notification_unblock_calls_btn,
                unblockCallsPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.notification_pause_calls_blocking_btn,
                pauseCallsPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = mBuilder.build();
        n.flags = Notification.FLAG_ONGOING_EVENT;
        // mId allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION, n);
    }
}