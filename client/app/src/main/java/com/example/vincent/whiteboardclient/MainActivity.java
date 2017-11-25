package com.example.vincent.whiteboardclient;

import android.app.FragmentManager;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity implements FragmentCallback {
    private CanvasFragment canvasFragment;
    private Socket socketInstance;
    private String roomName;
    private NetworkReceiver networkReceiver;
    private boolean receiverRegistered = false;

    // Screen dimensions
    private double width;
    private double height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = (double) size.x;
        height = (double) size.y;
        networkReceiver = new NetworkReceiver(this);
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        receiverRegistered = true;
        if (savedInstanceState != null) {
            roomName = savedInstanceState.getString(Constants.ROOM_NAME_KEY);
        }

        // Show the room fragment to request a room name.
        if (roomName == null) {
            FragmentManager fm = getFragmentManager();
            RoomFragment roomFragment = new RoomFragment();
            roomFragment.setCallback(this);
            fm.beginTransaction().add(R.id.frame, roomFragment).commit();
            return;
        }

        setupCanvasFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkReceiver != null && receiverRegistered) {
            unregisterReceiver(networkReceiver);
            receiverRegistered = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (networkReceiver != null && receiverRegistered) {
            unregisterReceiver(networkReceiver);
            receiverRegistered = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (networkReceiver != null && !receiverRegistered) {
            registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            receiverRegistered = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (roomName != null)
            outState.putString(Constants.ROOM_NAME_KEY, roomName);
    }

    public void enterRoom(String name) {
        roomName = name;
        setupCanvasFragment();
    }

    public void connected() {
        getSocketInstance().connect();
        if (canvasFragment != null)
            canvasFragment.addListeners();
    }

    public void disconnected() {
        getSocketInstance().disconnect();
        if (canvasFragment != null)
            canvasFragment.removeListeners();
    }

    public Socket getSocketInstance() {
        if (socketInstance == null) {
            try {
                socketInstance = IO.socket(Constants.SERVER_URL);
            } catch (URISyntaxException e) {
                Log.e("socket", e.getLocalizedMessage());
            }
        }
        return socketInstance;
    }

    public void onFragmentViewCreated() {
        canvasFragment.setRotation(getResources().getConfiguration().orientation);
    }

    private void setupCanvasFragment() {
        FragmentManager fm = getFragmentManager();
        canvasFragment = (CanvasFragment) fm.findFragmentByTag(Constants.RETAINED_FRAGMENT);

        if (canvasFragment == null) {
            canvasFragment = new CanvasFragment();
            Bundle bundle = new Bundle();
            bundle.putDouble(Constants.WIDTH, width);
            bundle.putDouble(Constants.HEIGHT, height);
            canvasFragment.setArguments(bundle);
            canvasFragment.setCallback(this);
            fm.beginTransaction().add(R.id.frame, canvasFragment, Constants.RETAINED_FRAGMENT).commit();
        }
    }
}
