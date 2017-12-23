package com.vinnyoodles.vincent.whiteboardclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

/**
 * Created by vincent on 12/11/17.
 */

public class BackgroundServices {
    MainActivity mActivity;
    FetchService mService;
    boolean mBound = false;
    public BackgroundServices(MainActivity activity) {
        mActivity = activity;
    }

    public void onStop() {
        if (mBound)
            mActivity.unbindService(mConnection);
        mBound = false;
    }

    public void startLocationFetch() {
        Intent intent = new Intent(mActivity, FetchService.class);
        mActivity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void startFetchCycle() {
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!mBound)
                    return;
                mService.fetchLocation(mActivity.getSocketInstance());
                handler.postDelayed(this, 1500 /*10 seconds*/);
            }
        });

        thread.start();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            FetchService.LocalBinder binder = (FetchService.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
            startFetchCycle();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };
}
