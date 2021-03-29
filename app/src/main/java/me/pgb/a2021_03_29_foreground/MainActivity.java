package me.pgb.a2021_03_29_foreground;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import me.pgb.a2021_03_29_foreground.service.RadioService;

import android.os.IBinder;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity<ONGOING_NOTIFICATION_ID> extends AppCompatActivity {

    private static final String CHANNEL_DEFAULT_IMPORTANCE = "notification action high";
    private static final int ONGOING_NOTIFICATION_ID = 1;
    private RadioService mService;
    private Intent notificationIntent;
    private boolean mBound = false;
    private Button binderButton;
    private Button stopBackgroundThread;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        binderButton = findViewById(R.id.binder_button);
        stopBackgroundThread = findViewById(R.id.stop_background_thread_button);

        binderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num = mService.getCounter();
                Toast.makeText(getApplicationContext(), "number: " + String.valueOf(num).toString(), Toast.LENGTH_SHORT).show();
            }
        });

        stopBackgroundThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.stopForegroundCounter();
            }
        });

        notificationIntent = new Intent(this, RadioService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notificationIntent.putExtra("data", "Hello!");

        Notification notification =
                new Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText(getText(R.string.notification_message))
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.ticker_text))
                        .build();

        bindService(notificationIntent, connection, Context.BIND_AUTO_CREATE);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            RadioService.LocalBinder binder = (RadioService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

}
