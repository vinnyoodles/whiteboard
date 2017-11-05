package com.example.vincent.whiteboardclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by vincent on 11/5/17.
 */

public class CanvasFragment extends Fragment {
    /* View Variables */
    private FragmentCallback cb;
    private CanvasView canvasView;

    /* Socket Variables */
    private Socket socketInstance;
    private Boolean isConnected = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_canvas, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        canvasView = (CanvasView) view.findViewById(R.id.canvas);
        Socket socket = getSocketInstance();
        socket.connect();
        socket.off(Socket.EVENT_CONNECT, onConnect);
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Socket socket = getSocketInstance();
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnect);
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
    }

    public void setCallback(FragmentCallback cb) {
        this.cb = cb;
    }


    private Socket getSocketInstance() {
        if (socketInstance == null) {
            try {
                socketInstance = IO.socket(Constants.SERVER_URL);
            } catch (URISyntaxException e) {
                Log.e("socket", e.getLocalizedMessage());
            }
        }
        return socketInstance;
    }

    /* Helper Functions */

    public void showToast(int message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /* Socket Listeners */

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Client is already connected.
                    if (isConnected) {
                        return;
                    }
                    getSocketInstance().emit(Constants.ADD_USER_EVENT, "vincent");
                    showToast(R.string.on_connect);
                    isConnected = true;
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected = false;
                    showToast(R.string.on_disconnect);
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast(R.string.on_connect_error);
                }
            });
        }
    };

}