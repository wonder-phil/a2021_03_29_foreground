package me.pgb.a2021_03_29_foreground.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public class RadioService extends Service {

    private final String TAG = "_SERVICE";
    private final IBinder binder = new LocalBinder();
    private int counter = 0;
    private MyHandler myHandler;
    private HandlerThread handlerThread;
    private Thread backgroundThread;
    private boolean runningInBackground = false;
    private boolean keepRunning = true;

    public class LocalBinder extends Binder {
        public RadioService getService() {
            return RadioService.this;
        }
    }

    public class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int startId = msg.arg1;
            Object someObject = msg.obj;

            Log.i(TAG,"msg from Handler: " + someObject.toString());
            Integer integer = Integer.getInteger(someObject.toString(),0);
            if (integer.intValue() >= 200) {
                keepRunning = false;
                try {
                    backgroundThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (!runningInBackground) {
                    runningInBackground = true;
                    backgroundThread = new Thread("Background Thread") {
                        @Override
                        public void run() {
                            while (keepRunning) {
                                try {
                                    Thread.sleep(500);
                                    counter++;
                                } catch (InterruptedException e) {
                                    // Restore interrupt status.
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }
                    };
                    if (!keepRunning) {
                        try {
                            backgroundThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        backgroundThread.start();
                    }
                }
            }

            //counter++;
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate()");

        handlerThread = new HandlerThread("My Thread", Process.THREAD_PRIORITY_FOREGROUND);

        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        myHandler = new MyHandler(looper);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        handlerThread.quitSafely();
    }

    /*
     *
     * if this service is run as an Intent, and we call startService on the intent
     * then the next code is useful
     *
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG,"onStartCommand");

        if(intent != null) {
            // May not have an Intent is the service was killed and restarted
            // (See STICKY_SERVICE).
            Log.i(TAG,"do stuff in onStartCommand");
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"onBind");

        if(intent != null) { // May not have an Intent is the service was killed and restarted (See STICKY_SERVICE).
            Log.i(TAG,"do stuff in onBind");
        }
        return binder;
    }

    public int getCounter() { /** method for clients */
        Message msg = myHandler.obtainMessage();
        msg.arg1 = 99;
        msg.obj = String.valueOf(counter).toString();
        myHandler.sendMessage(msg);
        return counter;
    }

    public void stopForegroundCounter() { /** method for clients */
        keepRunning = false;
    }
}