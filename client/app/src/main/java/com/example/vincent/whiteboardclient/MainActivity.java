package com.example.vincent.whiteboardclient;

import android.app.FragmentManager;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity implements FragmentCallback {
    private CanvasFragment canvasFragment;
    private Socket socketInstance;
    private String roomName;
    private String userName;
    private NetworkReceiver networkReceiver;
    private boolean receiverRegistered = false;
    private LocationHelper locationHelper;
    private AudioHelper audioHelper;

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
        register();
        locationHelper = new LocationHelper(this);
        audioHelper = new AudioHelper();
        if (savedInstanceState != null) {
            roomName = savedInstanceState.getString(Constants.ROOM_NAME_KEY);
            userName = savedInstanceState.getString(Constants.USER_NAME_KEY);
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
        if (canvasFragment != null)
            canvasFragment.saveBitmap();
        unregister();
        audioHelper.stopStream();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (canvasFragment != null)
            canvasFragment.saveBitmap();
        unregister();
        audioHelper.stopStream();
    }

    @Override
    protected void onResume() {
        super.onResume();
        register();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (roomName != null)
            outState.putString(Constants.ROOM_NAME_KEY, roomName);

        if (userName != null)
            outState.putString(Constants.USER_NAME_KEY, userName);
    }

    public void emitLocation(String location) {
        if (location == null)
            return;

        JSONObject json = new JSONObject();
        try {
            json.put(Constants.LOCATION_KEY, location);

        } catch (org.json.JSONException e) {
            Log.e("json", e.getLocalizedMessage());
        }
        getSocketInstance().emit(Constants.LOCATION_EVENT, json);
    }


    public void enterRoom(String user, String room) {
        roomName = room;
        userName = user;
        setupCanvasFragment();

        JSONObject json = new JSONObject();
        try {
            json.put(Constants.ROOM_NAME_KEY, roomName);
            json.put(Constants.USER_NAME_KEY, userName);

        } catch (org.json.JSONException e) {
            Log.e("json", e.getLocalizedMessage());
        }
        getSocketInstance().emit(Constants.JOIN_ROOM_EVENT, json);
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

    private void register() {
        if (networkReceiver == null)
            networkReceiver = new NetworkReceiver(this);

        if (!receiverRegistered) {
            registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            receiverRegistered = true;
        }
    }

    private void unregister() {
        if (networkReceiver != null && receiverRegistered) {
            unregisterReceiver(networkReceiver);
            receiverRegistered = false;
        }
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

        audioHelper.startStream();
    }
}
