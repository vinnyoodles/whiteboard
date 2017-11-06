package com.example.vincent.whiteboardclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by vincent on 11/5/17.
 */

public class CanvasFragment extends Fragment implements SocketEventListener {
    /* View Variables */
    private FragmentCallback cb;
    private CanvasView canvasView;
    private FloatingActionButton clearButton;
    private FloatingActionButton penButton;
    private FloatingActionButton eraserButton;

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
        clearButton = (FloatingActionButton) view.findViewById(R.id.clear_button);
        penButton = (FloatingActionButton) view.findViewById(R.id.pen_button);
        eraserButton = (FloatingActionButton) view.findViewById(R.id.eraser_button);

        canvasView.setSocketEventListener(this);

        Socket socket = getSocketInstance();
        socket.connect();
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.on(Constants.TOUCH_EVENT, onReceivedTouchEvent);
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
        socket.off(Constants.TOUCH_EVENT, onReceivedTouchEvent);
    }

    public void onTouchEvent(MotionEvent event) {

        String eventType;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                eventType = Constants.TOUCH_DOWN_EVENT;
                break;
            case MotionEvent.ACTION_MOVE:
                eventType = Constants.TOUCH_MOVE_EVENT;
                break;
            default:
                return; // Ignore irrelevant events.
        }

        JSONObject json = new JSONObject();
        try {
            json.put(Constants.X_COORDINATE, (double) event.getX());
            json.put(Constants.Y_COORDINATE, (double) event.getY());
            json.put(Constants.EVENT_TYPE, eventType);
        } catch (org.json.JSONException e) {
            Log.e("json", e.getLocalizedMessage());
        }
        getSocketInstance().emit(Constants.TOUCH_EVENT, json);
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

    private Emitter.Listener onReceivedTouchEvent = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = (JSONObject) args[0];
                    try {
                        float x = (float) json.getDouble(Constants.X_COORDINATE);
                        float y = (float) json.getDouble(Constants.Y_COORDINATE);
                        switch (json.getString(Constants.EVENT_TYPE)) {
                            case Constants.TOUCH_DOWN_EVENT:
                                canvasView.startPath(x, y, true /* socket path */);
                                break;
                            case Constants.TOUCH_MOVE_EVENT:
                                canvasView.movePath(x, y, true /* socket path */);
                                break;
                            default:
                                return;
                        }
                        canvasView.invalidate();
                    } catch (org.json.JSONException e) {
                        Log.e("json", e.getLocalizedMessage());
                    }
                }
            });
        }
    };

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