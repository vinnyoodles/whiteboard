package com.vinnyoodles.vincent.whiteboardclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;

/**
 * Created by vincent on 11/23/17.
 */

public class NetworkReceiver extends BroadcastReceiver {
    private MainActivity mActivity;
    public NetworkReceiver(MainActivity activity) {
        super();
        mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                NetworkInfo info = (NetworkInfo) extras.get("networkInfo");
                NetworkInfo.State networkState = info.getState();
                if (networkState == NetworkInfo.State.CONNECTED)
                    mActivity.connected();
                else
                    mActivity.disconnected();
            }
        }
    }

}
