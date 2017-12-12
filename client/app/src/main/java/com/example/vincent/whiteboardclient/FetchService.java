package com.example.vincent.whiteboardclient;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import io.socket.client.Socket;

/**
 * Created by vincent on 12/11/17.
 */

public class FetchService extends Service {
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        FetchService getService() {
            return FetchService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void fetchLocation(Socket socket) {
        socket.emit(Constants.REQUEST_LOCATION);
    }
}
