package com.example.vincent.whiteboardclient;

import android.view.MotionEvent;

/**
 * Created by vincent on 11/6/17.
 */

public interface SocketEventEmitter {
    void sendTouchEvent(MotionEvent event, int paintType);
}
