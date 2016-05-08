package musicshield.agita.team.com.musicshield;

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
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by pborisenko on 5/8/2016.
 */

public class ActivityMain extends AppCompatActivity {

    private final String TAG = "ActivityMain";

    private Button blockCallsBtn;
    private Button unblockCallsBtn;
    private ImageView logo;
    private ControlService mBoundService;
    private ServiceConnection mServiceConnection;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /** Messenger for communicating with service. */
    Messenger mService = null;
    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;
    /** Some text view we are using to show state information. */
    TextView mCallbackText;

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            /*
            switch (msg.what) {
                case ControlService.MSG_SET_VALUE:
                    mCallbackText.setText("Received from service: " + msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
            */
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, ControlService.class));

        blockCallsBtn = (Button) findViewById(R.id.block_calls_btn);
        unblockCallsBtn = (Button) findViewById(R.id.unblock_calls_btn);
        logo = (ImageView) findViewById(R.id.logo);

        blockCallsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        unblockCallsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // This is called when the connection with the service has been
                // established, giving us the service object we can use to
                // interact with the service.  We are communicating with our
                // service through an IDL interface, so get a client-side
                // representation of that from the raw service object.
                mService = new Messenger(service);
                mCallbackText.setText("Attached.");

                // We want to monitor the service for as long as we are
                // connected to it.
                try {
                    Message msg = Message.obtain(null,
                            ControlService.MSG_BLOCK_CALLS);
                    msg.replyTo = mMessenger;
                    mService.send(msg);

                    msg = Message.obtain(null,
                            ControlService.MSG_KILL_CONTROL_SERVICE, this.hashCode(), 0);
                    mService.send(msg);
                } catch (RemoteException e) {
                    // In this case the service has crashed before we could even
                    // do anything with it; we can count on soon being
                    // disconnected (and then reconnected if it can be restarted)
                    // so there is no need to do anything here.
                }

                // As part of the sample, tell the user what happened.
                Log.d(TAG, "onServiceConnected");
            }

            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                mService = null;
                mCallbackText.setText("Disconnected.");

                // As part of the sample, tell the user what happened.
                Log.d(TAG, "onServiceDisconnected");
            }
        };
    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(this,
                ControlService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        mCallbackText.setText("Binding.");
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            ControlService.MSG_PAUSE_BLOCK_CALLS);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mServiceConnection);
            mIsBound = false;
            mCallbackText.setText("Unbinding.");
        }
    }
}