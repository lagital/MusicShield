package musicshield.agita.team.com.musicshield;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    private static final String PREF_NAME = "musicshield.agita.team.com.musicshield";
    private static final String CHECKED_NUMBERS = "CHECKED_NUMBERS";
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
    public Integer missedCallsCounter = 0;
    private DBHelper mDBHelper;
    private SharedPreferences mSP;

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
            processMessage(msg.what);
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

        mSP = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        mDBHelper = new DBHelper(this);

        mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        initiateTelephonyService();
        mPhoneStateListener = new ControlPhoneStateListener();
        mTelephonyManager.listen(new ControlPhoneStateListener(), mPhoneStateListener.LISTEN_CALL_STATE);
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        mCurrentState = STATE_BLOCK_CALLS;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if (intent != null) {
            if (intent.hasExtra("MSG")) {
                Log.d(TAG, "onStartCommand: msg intent recieved");
                processMessage(intent.getIntExtra("MSG", MSG_UNBLOCK_CALLS));
            }
        }

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
                        Boolean matched = false;
                        Set<String> set = mSP.getStringSet(CHECKED_NUMBERS,
                                new HashSet<String>());
                        for (String s : set) {
                            if (!matched) {
                                matched = PhoneNumberUtils.compare(s, incomingNumber);
                            }
                        }
                        if (mCurrentState == MSG_BLOCK_CALLS && !matched) {
                            Log.d(TAG, "onCallStateChanged: " + "state - blocking.");
                            try {
                                mTelephonyService.endCall();
                                missedCallsCounter += 1;
                                String date_time = DateFormat.getDateTimeInstance().format(new Date());

                                mDBHelper.insertMissedCall(ApplicationMain.WRITE_DB,
                                        incomingNumber,
                                        date_time,
                                        DBHelper.CallType.BLOCKED);
                                showNotification();
                                Log.d(TAG, "onCallStateChanged: " + "blocked " + incomingNumber
                                + " on " + date_time);
                            } catch (Exception e) {
                                e.printStackTrace();
                                break;
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
        Intent resultIntent = new Intent(this, ActivityMain.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity
                (this, NOTIFICATION, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_small)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                                R.drawable.ic_notification_large))
                        .setContentTitle(getResources().getString(R.string.notification_title))
                        .setContentIntent(pendingIntent)
                        .setContentText(getResources().getString(R.string.notification_missed_calls_title)
                                + ' ' + Integer.toString(missedCallsCounter));

        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = mBuilder.build();
        n.contentView.setImageViewResource(R.drawable.man_icon_hi, R.drawable.man_icon_hi);
        n.flags |= Notification.FLAG_ONGOING_EVENT;
        // mId allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION, n);
    }

    private void processMessage(Integer what) {
        Log.d(TAG, "processMessage: " + Integer.toString(what));
        switch (what) {
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
                mCurrentState = STATE_UNBLOCK_CALLS;
                sendCurrentState ();
                Log.d(TAG, "stopSelf");
                ControlService.this.stopSelf();
                break;
            case MSG_GET_STATE:
                sendCurrentState();
                break;
            default:
                break;
        }
    }

    private void sendCurrentState () {
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
    }
}