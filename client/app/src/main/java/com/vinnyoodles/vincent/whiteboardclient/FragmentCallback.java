package com.vinnyoodles.vincent.whiteboardclient;

import io.socket.client.Socket;

/**
 * Created by vincent on 11/5/17.
 */

public interface FragmentCallback {
    Socket getSocketInstance();
    void enterRoom(String user);
    void startRecording();
    void stopRecording();
    void onFragmentLoaded();
}
