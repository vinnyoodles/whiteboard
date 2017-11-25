package com.example.vincent.whiteboardclient;

import io.socket.client.Socket;

/**
 * Created by vincent on 11/5/17.
 */

public interface FragmentCallback {
    Socket getSocketInstance();
    void onFragmentViewCreated();
    void enterRoom(String name);
}
